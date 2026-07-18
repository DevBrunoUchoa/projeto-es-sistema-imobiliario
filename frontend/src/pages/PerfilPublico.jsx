import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Header from '../components/Header';
import { userApi } from '../api/userApi';

const TIPO_LABELS = {
  ESTUDANTE: 'Estudante',
  LOCADOR: 'Locador',
  MISTO: 'Estudante e locador',
  ADMIN: 'Administrador',
};

export default function PerfilPublico() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [perfil, setPerfil] = useState(null);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setErro(null);
    userApi.publico(id, { signal: controller.signal })
      .then(setPerfil)
      .catch((err) => { if (err.name !== 'AbortError') setErro('Não foi possível carregar este perfil.'); })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [id]);

  const initials = useMemo(() => (
    perfil?.nome?.split(/\s+/).filter(Boolean).slice(0, 2).map((parte) => parte[0]).join('').toUpperCase() || 'U'
  ), [perfil?.nome]);

  const ehLocador = perfil?.tipoConta === 'LOCADOR' || perfil?.tipoConta === 'MISTO';

  return (
    <div className="app-shell">
      <Header />

      <main className="page-content profile-page">
        <button type="button" className="detail-back" onClick={() => navigate(-1)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
          <i className="fa-solid fa-arrow-left" /> Voltar
        </button>

        {loading && <p style={{ marginTop: 20 }}>Carregando perfil...</p>}

        {!loading && erro && (
          <div className="no-results"><div className="no-results-emoji">⚠️</div><h3>{erro}</h3></div>
        )}

        {!loading && !erro && perfil && (
          <>
            <div className="page-header">
              <div>
                <h1>Perfil</h1>
                <p>Informações públicas deste usuário.</p>
              </div>
            </div>

            <div className="profile-avatar-section">
              <div className="avatar-wrap">
                {perfil.fotoUrl
                  ? <img className="avatar-img" src={perfil.fotoUrl} alt={perfil.nome} />
                  : <div className="avatar-img">{initials}</div>}
              </div>

              <div className="avatar-info">
                <h3>{perfil.nome}</h3>
                <p>
                  {(perfil.curso || 'Curso não informado')} · {(perfil.instituicao || 'Instituição não informada')}
                </p>
                {perfil.verificado && (
                  <div className="verified-badge">
                    <i className="fa-solid fa-circle-check" /> Verificado
                  </div>
                )}
              </div>
            </div>

            <div className="card-section">
              <div className="card-section-title">Sobre</div>

              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Tipo de conta</label>
                  <input className="form-input" disabled value={TIPO_LABELS[perfil.tipoConta] ?? perfil.tipoConta ?? '—'} />
                </div>

                {ehLocador && (
                  <div className="form-group">
                    <label className="form-label">Reputação</label>
                    <input
                      className="form-input"
                      disabled
                      value={perfil.totalAvaliacoes
                        ? `${Number(perfil.notaMedia ?? 0).toFixed(1)} ★ (${perfil.totalAvaliacoes} ${perfil.totalAvaliacoes === 1 ? 'avaliação' : 'avaliações'})`
                        : 'Sem avaliações ainda'}
                    />
                  </div>
                )}
              </div>

              {perfil.bio && (
                <div className="form-group" style={{ marginTop: 14 }}>
                  <label className="form-label">Bio</label>
                  <textarea className="form-textarea" disabled value={perfil.bio} />
                </div>
              )}
            </div>

            <div className="card-section">
              <div className="card-section-title">Contato</div>

              {perfil.contatoLiberado ? (
                <div className="form-grid">
                  <div className="form-group">
                    <label className="form-label">E-mail</label>
                    <input className="form-input" disabled value={perfil.email || '—'} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Telefone</label>
                    <input className="form-input" disabled value={perfil.telefone || '—'} />
                  </div>
                </div>
              ) : (
                <p style={{ fontSize: 14, color: 'var(--text-2)' }}>
                  <i className="fa-solid fa-lock" style={{ marginRight: 6 }} />
                  Os dados de contato ficam disponíveis conforme as regras de privacidade da plataforma.
                </p>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
