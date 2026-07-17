import { useEffect, useMemo, useRef, useState } from 'react';
import Header from '../components/Header';
import Alert from '../components/Alert';
import ConfirmDialog from '../components/ConfirmDialog';
import Toast from '../components/Toast';
import { userApi } from '../api/userApi';
import { useAuth } from '../contexts/AuthContext';

const emptyProfile = { nome:'', email:'', telefone:'', curso:'', instituicao:'', bio:'', verificado:false, fotoUrl:null, role:'ESTUDANTE' };
const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_SIZE = 5 * 1024 * 1024;
const ROLE_OPTIONS = [
  { value: 'ESTUDANTE', label: 'Estudante' },
  { value: 'LOCADOR', label: 'Locador' },
  { value: 'MISTO', label: 'Estudante e locador' },
];

export default function Perfil() {
  const { user, updateLocalUser } = useAuth();
  const [profile, setProfile] = useState(emptyProfile);
  const [draft, setDraft] = useState(emptyProfile);
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploadingPhoto, setUploadingPhoto] = useState(false);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [pendingPhoto, setPendingPhoto] = useState(null);
  const [message, setMessage] = useState({type:'',text:''});
  const [toast, setToast] = useState({type:'success', message:''});
  const [confirm, setConfirm] = useState(null);
  const photoInputRef = useRef(null);

  useEffect(() => {
    const controller = new AbortController();
    userApi.buscar(user.id, { signal: controller.signal })
      .then((data) => {
        const normalized = {...emptyProfile, ...data, role: data.role || user.role || 'ESTUDANTE'};
        setProfile(normalized);
        setDraft(normalized);
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setMessage({type:'error', text:err.status===401||err.status===403 ? 'Sua sessão expirou. Entre novamente.' : err.message});
      })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [user.id, user.role]);

  useEffect(() => () => {
    if (photoPreview) URL.revokeObjectURL(photoPreview);
  }, [photoPreview]);

  useEffect(() => {
    if (!toast.message) return undefined;
    const timeout = window.setTimeout(() => setToast({type:'success', message:''}), 3500);
    return () => window.clearTimeout(timeout);
  }, [toast]);

  const initials = useMemo(() => profile.nome?.split(/\s+/).filter(Boolean).slice(0,2).map((part) => part[0]).join('').toUpperCase() || 'U', [profile.nome]);
  const displayedPhoto = photoPreview || profile.fotoUrl;

  function requestSave() {
    setConfirm({
      kind: 'save',
      title: 'Confirmar alterações',
      message: draft.role !== profile.role
        ? 'Deseja salvar os dados do perfil e alterar também o tipo da sua conta?'
        : 'Deseja salvar as alterações realizadas no seu perfil?',
    });
  }

  async function save() {
    setSaving(true);
    setMessage({type:'', text:''});
    try {
      const updated = await userApi.atualizar(user.id, {
        nome: draft.nome.trim(),
        bio: draft.bio,
        telefone: draft.telefone,
        curso: draft.curso,
        instituicao: draft.instituicao,
        role: draft.role,
      });
      const next = {...profile, ...updated, role: updated.role || draft.role};
      setProfile(next);
      setDraft(next);
      setEditing(false);
      updateLocalUser({nome: next.nome, role: next.role, fotoUrl: next.fotoUrl});
      setToast({type:'success', message:'Alterações salvas com sucesso.'});
    } catch (err) {
      setToast({type:'error', message:err.message});
    } finally {
      setSaving(false);
      setConfirm(null);
    }
  }

  function handlePhotoSelected(event) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    if (!ACCEPTED_TYPES.includes(file.type)) {
      setToast({type:'error', message:'Escolha uma imagem JPEG, PNG ou WEBP.'});
      return;
    }
    if (file.size > MAX_SIZE) {
      setToast({type:'error', message:'A foto deve ter no máximo 5 MB.'});
      return;
    }
    if (photoPreview) URL.revokeObjectURL(photoPreview);
    setPhotoPreview(URL.createObjectURL(file));
    setPendingPhoto(file);
    setConfirm({kind:'photo', title:'Alterar foto de perfil', message:'Deseja usar esta imagem como sua nova foto de perfil?'});
  }

  function cancelPhotoChange() {
    if (photoPreview) URL.revokeObjectURL(photoPreview);
    setPhotoPreview(null);
    setPendingPhoto(null);
    setConfirm(null);
  }

  async function uploadPhoto() {
    if (!pendingPhoto) return;
    setUploadingPhoto(true);
    try {
      const updated = await userApi.foto(user.id, pendingPhoto);
      const next = {...profile, ...updated};
      setProfile(next);
      setDraft((current) => ({...current, fotoUrl: next.fotoUrl}));
      updateLocalUser({fotoUrl: next.fotoUrl});
      setToast({type:'success', message:'Foto de perfil atualizada com sucesso.'});
    } catch (err) {
      setToast({type:'error', message:err.message});
    } finally {
      if (photoPreview) URL.revokeObjectURL(photoPreview);
      setPhotoPreview(null);
      setPendingPhoto(null);
      setUploadingPhoto(false);
      setConfirm(null);
    }
  }

  if (loading) return <div className="app-shell"><Header/><main className="page-content"><p>Carregando perfil...</p></main></div>;

  return (
    <div className="app-shell">
      <Header />
      <main className="page-content profile-page profile-with-navbar">
        <div className="page-header"><div><h1>Meu perfil</h1><p>Gerencie os dados vinculados à sua conta.</p></div></div>
        <Alert type={message.type === 'success' ? 'success' : 'error'}>{message.text}</Alert>

        <div className="profile-avatar-section">
          <div className="avatar-wrap">
            {displayedPhoto
              ? <img className="avatar-img" src={displayedPhoto} alt={`Foto de perfil de ${profile.nome}`} />
              : <div className="avatar-img">{initials}</div>}
            <button type="button" className="avatar-edit" aria-label="Alterar foto de perfil" title="Alterar foto" disabled={uploadingPhoto} onClick={() => photoInputRef.current?.click()}>
              <i className={`fa-solid ${uploadingPhoto ? 'fa-spinner fa-spin' : 'fa-camera'}`} />
            </button>
            <input ref={photoInputRef} className="visually-hidden" type="file" accept="image/jpeg,image/png,image/webp" onChange={handlePhotoSelected} />
          </div>
          <div className="avatar-info">
            <h3>{profile.nome}</h3>
            <p>{profile.curso || 'Curso não informado'} · {profile.instituicao || 'Instituição não informada'}</p>
            <small className="photo-help">JPEG, PNG ou WEBP · máximo de 5 MB</small>
            {profile.verificado && <div className="verified-badge"><i className="fa-solid fa-circle-check"/> Verificado</div>}
          </div>
        </div>

        <div className="card-section">
          <div className="card-section-title">
            Dados pessoais
            <div className="title-actions">
              <button className="btn-sm btn-outline" onClick={() => { setEditing(!editing); setDraft(profile); }}>
                <i className={`fa-solid ${editing ? 'fa-xmark' : 'fa-pen'}`} /> {editing ? 'Cancelar' : 'Editar'}
              </button>
            </div>
          </div>

          <div className="form-grid">
            <div className="form-group"><label className="form-label">Nome completo</label><input className="form-input" disabled={!editing} value={draft.nome || ''} onChange={(event) => setDraft({...draft, nome:event.target.value})}/></div>
            <div className="form-group"><label className="form-label">E-mail</label><input className="form-input" disabled value={profile.email || user.email || ''}/><small className="field-help">O e-mail não pode ser alterado nesta tela.</small></div>
            <div className="form-group"><label className="form-label">Telefone</label><input className="form-input" disabled={!editing} value={draft.telefone || ''} placeholder="Não informado" onChange={(event) => setDraft({...draft, telefone:event.target.value})}/></div>
            <div className="form-group"><label className="form-label">Curso</label><input className="form-input" disabled={!editing} value={draft.curso || ''} onChange={(event) => setDraft({...draft, curso:event.target.value})}/></div>
            <div className="form-group"><label className="form-label">Instituição</label><input className="form-input" disabled={!editing} value={draft.instituicao || ''} onChange={(event) => setDraft({...draft, instituicao:event.target.value})}/></div>
            <div className="form-group">
              <label className="form-label" htmlFor="profile-role">Tipo da conta</label>
              <select id="profile-role" className="form-input" disabled={!editing} value={draft.role || 'ESTUDANTE'} onChange={(event) => setDraft({...draft, role:event.target.value})}>
                {ROLE_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
              </select>
              <small className="field-help">O perfil de administrador não pode ser selecionado pelo usuário.</small>
            </div>
          </div>

          <div className="form-group" style={{marginTop:14}}><label className="form-label">Bio</label><textarea className="form-textarea" disabled={!editing} value={draft.bio || ''} onChange={(event) => setDraft({...draft, bio:event.target.value})}/></div>
          {editing && <div className="profile-form-actions"><button className="btn-sm btn-outline" onClick={() => {setEditing(false); setDraft(profile);}}>Cancelar</button><button className="btn-sm btn-primary" disabled={saving || !draft.nome.trim()} onClick={requestSave}>{saving ? 'Salvando...' : 'Salvar alterações'}</button></div>}
        </div>
      </main>

      <ConfirmDialog
        open={Boolean(confirm)}
        title={confirm?.title}
        message={confirm?.message}
        loading={saving || uploadingPhoto}
        confirmLabel={confirm?.kind === 'photo' ? 'Usar esta foto' : 'Salvar alterações'}
        onCancel={confirm?.kind === 'photo' ? cancelPhotoChange : () => setConfirm(null)}
        onConfirm={confirm?.kind === 'photo' ? uploadPhoto : save}
      />
      <Toast type={toast.type} message={toast.message} onClose={() => setToast({type:'success', message:''})} />
    </div>
  );
}
