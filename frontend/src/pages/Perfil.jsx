import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Alert from '../components/Alert';
import { userApi } from '../api/userApi';
import { useAuth } from '../contexts/AuthContext';
import { useFilePicker } from '../hooks/useFilePicker';

const emptyProfile = { nome:'', email:'', telefone:'', curso:'', instituicao:'', bio:'', verificado:false, fotoUrl:'', tipoConta:'' };

export default function Perfil() {
  const { user, updateLocalUser, clearLocalSession } = useAuth();
  const [profile, setProfile] = useState(emptyProfile);
  const [draft, setDraft] = useState(emptyProfile);
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({type:'',text:''});
  const [enviandoFoto, setEnviandoFoto] = useState(false);
  const [promovendo, setPromovendo] = useState(false);
  const [enviandoVerificacao, setEnviandoVerificacao] = useState(false);
  const [verificacaoStatus, setVerificacaoStatus] = useState(null);
  const { inputRef: fotoInputRef, openPicker: abrirSeletorFoto, resetPicker: limparSeletorFoto } = useFilePicker();
  const { inputRef: docInputRef, openPicker: abrirSeletorDoc, resetPicker: limparSeletorDoc } = useFilePicker();
  const navigate = useNavigate();

  useEffect(() => {
    const controller = new AbortController();
    userApi.buscar(user.id, { signal: controller.signal })
      .then((data)=>{ const normalized={...emptyProfile,...data}; setProfile(normalized); setDraft(normalized); })
      .catch((err)=>{ if(err.name!=='AbortError') setMessage({type:'error',text:err.status===401||err.status===403?'Sua sessão expirou. Entre novamente.':err.message}); })
      .finally(()=>setLoading(false));
    return ()=>controller.abort();
  }, [user.id]);

  const initials = useMemo(()=>profile.nome?.split(/\s+/).filter(Boolean).slice(0,2).map(p=>p[0]).join('').toUpperCase() || 'U',[profile.nome]);

  async function save() {
    setSaving(true); setMessage({type:'',text:''});
    try {
      const updated = await userApi.atualizar(user.id, { nome:draft.nome.trim(), bio:draft.bio, telefone:draft.telefone, curso:draft.curso, instituicao:draft.instituicao });
      const next={...profile,...updated}; setProfile(next); setDraft(next); setEditing(false); updateLocalUser({nome:next.nome}); setMessage({type:'success',text:'Perfil atualizado com sucesso.'});
    } catch(err){ setMessage({type:'error',text:err.message}); }
    finally{ setSaving(false); }
  }

  function logoutLocal() { clearLocalSession(); navigate('/login', {replace:true}); }

  async function onFotoSelecionada(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    setEnviandoFoto(true); setMessage({type:'',text:''});
    try {
      const updated = await userApi.atualizarFoto(user.id, file);
      setProfile((current)=>({...current, ...updated}));
      setDraft((current)=>({...current, fotoUrl:updated.fotoUrl}));
      setMessage({type:'success',text:'Foto de perfil atualizada.'});
    } catch (err) { setMessage({type:'error',text:err.message}); }
    finally { setEnviandoFoto(false); limparSeletorFoto(); }
  }

  async function promoverParaMista() {
    setPromovendo(true); setMessage({type:'',text:''});
    try {
      await userApi.promoverContaMista(user.id);
      setProfile((current)=>({...current, tipoConta:'MISTO'}));
      updateLocalUser({role:'MISTO'});
      setMessage({type:'success',text:'Conta liberada como Misto — agora você também pode anunciar imóveis.'});
    } catch (err) { setMessage({type:'error',text:err.message}); }
    finally { setPromovendo(false); }
  }

  async function onDocumentoSelecionado(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    setEnviandoVerificacao(true);
    setMessage({type:'',text:''});
    try {
      await userApi.solicitarVerificacao(user.id, file);
      setVerificacaoStatus('pendente');
      setMessage({type:'success',text:'Documento enviado! Vamos avisar quando a análise sair.'});
    } catch (err) {
      if (err.status === 409) {
        setVerificacaoStatus('pendente');
      } else {
        setMessage({type:'error',text:err.message});
      }
    } finally {
      setEnviandoVerificacao(false);
      limparSeletorDoc();
    }
  }

  if (loading) return <main className="page-content"><p>Carregando perfil...</p></main>;

  return <div className="app-shell"><header className="topbar"><div className="brand"><div className="logo-mark">E</div><span className="logo-text">Estudante<strong>Lar</strong></span></div><button className="btn-sm btn-outline" onClick={logoutLocal}><i className="fa-solid fa-arrow-right-from-bracket"/> Sair da interface</button></header><main className="page-content profile-page">
    <div className="page-header"><div><h1>Meu perfil</h1><p>Gerencie os dados vinculados à sua conta.</p></div></div>
    <Alert type={message.type==='success'?'success':'error'}>{message.text}</Alert>
    <div className="profile-avatar-section"><div className="avatar-wrap">{profile.fotoUrl ? <img className="avatar-img" src={profile.fotoUrl} alt={profile.nome} /> : <div className="avatar-img">{initials}</div>}<button type="button" className="avatar-edit" title="Alterar foto" disabled={enviandoFoto} onClick={abrirSeletorFoto}><i className={`fa-solid ${enviandoFoto?'fa-spinner fa-spin':'fa-camera'}`}/></button><input ref={fotoInputRef} type="file" accept="image/*" hidden onChange={onFotoSelecionada}/></div><div className="avatar-info"><h3>{profile.nome}</h3><p>{profile.curso || 'Curso não informado'} · {profile.instituicao || 'Instituição não informada'}</p>{profile.verificado && <div className="verified-badge"><i className="fa-solid fa-circle-check"/> Verificado</div>}</div></div>
    <div className="card-section"><div className="card-section-title">Dados pessoais <div className="title-actions"><button className="btn-sm btn-outline" onClick={()=>{setEditing(!editing);setDraft(profile);}}><i className={`fa-solid ${editing?'fa-xmark':'fa-pen'}`}/> {editing?'Cancelar':'Editar'}</button></div></div><div className="form-grid"><div className="form-group"><label className="form-label">Nome completo</label><input className="form-input" disabled={!editing} value={draft.nome || ''} onChange={(e)=>setDraft({...draft,nome:e.target.value})}/></div><div className="form-group"><label className="form-label">E-mail</label><input className="form-input" disabled value={profile.email || user.email || ''}/><small className="field-help">O backend não oferece alteração de e-mail nesta rota.</small></div><div className="form-group"><label className="form-label">Telefone</label><input className="form-input" disabled={!editing} value={draft.telefone || ''} placeholder="Não informado" onChange={(e)=>setDraft({...draft,telefone:e.target.value})}/></div><div className="form-group"><label className="form-label">Curso</label><input className="form-input" disabled={!editing} value={draft.curso || ''} onChange={(e)=>setDraft({...draft,curso:e.target.value})}/></div><div className="form-group"><label className="form-label">Instituição</label><input className="form-input" disabled={!editing} value={draft.instituicao || ''} onChange={(e)=>setDraft({...draft,instituicao:e.target.value})}/></div><div className="form-group"><label className="form-label">Tipo da conta</label><input className="form-input" disabled value={user.role || ''}/></div></div><div className="form-group" style={{marginTop:14}}><label className="form-label">Bio</label><textarea className="form-textarea" disabled={!editing} value={draft.bio || ''} onChange={(e)=>setDraft({...draft,bio:e.target.value})}/></div>{editing && <div style={{display:'flex',gap:10,justifyContent:'flex-end',marginTop:16}}><button className="btn-sm btn-outline" onClick={()=>{setEditing(false);setDraft(profile);}}>Cancelar</button><button className="btn-sm btn-primary" disabled={saving||!draft.nome.trim()} onClick={save}>{saving?'Salvando...':'Salvar alterações'}</button></div>}</div>
    {profile.tipoConta === 'ESTUDANTE' && <div className="card-section"><div className="card-section-title">Sublocar minha vaga</div><p style={{fontSize:14,color:'var(--text-2)',marginBottom:14}}>Também quer alugar um quarto na sua república? Vire uma conta Misto e ganhe acesso pra anunciar imóveis sem perder seu perfil de estudante.</p><button className="btn-sm btn-primary" disabled={promovendo} onClick={promoverParaMista}>{promovendo?'Ativando...':'Oferecer vaga na minha república'}</button></div>}
    {profile.tipoConta === 'MISTO' && <div className="card-section"><div className="card-section-title">Conta Misto ativa</div><p style={{fontSize:14,color:'var(--text-2)'}}>Você já pode <Link to="/criar-anuncio">anunciar imóveis</Link> além do seu perfil de estudante.</p></div>}
    {(profile.tipoConta === 'LOCADOR' || profile.tipoConta === 'MISTO') && !profile.verificado && (
      <div className="card-section">
        <div className="card-section-title">Verificação de identidade</div>
        {verificacaoStatus === 'pendente' ? (
          <p style={{fontSize:14,color:'var(--text-2)'}}><i className="fa-solid fa-hourglass-half" style={{marginRight:6}}/> Documento em análise. Avisamos quando sair o resultado.</p>
        ) : (
          <>
            <p style={{fontSize:14,color:'var(--text-2)',marginBottom:14}}>Envie um documento (RG, CNH ou CRECI) pra ganhar o selo de verificado e passar mais confiança aos estudantes.</p>
            <button className="btn-sm btn-primary" type="button" disabled={enviandoVerificacao} onClick={abrirSeletorDoc}>
              <i className="fa-solid fa-upload" style={{marginRight:6}}/> {enviandoVerificacao ? 'Enviando...' : 'Enviar documento'}
            </button>
            <input ref={docInputRef} type="file" accept="image/*,.pdf" hidden onChange={onDocumentoSelecionado}/>
          </>
        )}
      </div>
    )}
  </main></div>;
}
