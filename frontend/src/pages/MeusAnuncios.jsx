import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import { anuncioApi } from '../api/anuncioApi';
import { STATUS_LABELS, TIPO_OFERTA_LABELS, formatMoeda } from '../utils/anuncio';

const TABS = [
  { key: 'todos', label: 'Todos' },
  { key: 'ATIVO', label: 'Ativos' },
  { key: 'INATIVO', label: 'Inativos' },
  { key: 'ALUGADO', label: 'Alugados' },
];

export default function MeusAnuncios() {
  const [anuncios, setAnuncios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tab, setTab] = useState('todos');
  const [updatingId, setUpdatingId] = useState(null);

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function carregar() {
    setLoading(true);
    setError(null);
    anuncioApi.meus()
      .then(setAnuncios)
      .catch(() => setError('Não foi possível carregar seus anúncios.'))
      .finally(() => setLoading(false));
  }

  const filtrados = useMemo(() => tab === 'todos' ? anuncios : anuncios.filter((anuncio) => anuncio.status === tab), [anuncios, tab]);
  const contagens = useMemo(() => ({
    todos: anuncios.length,
    ATIVO: anuncios.filter((anuncio) => anuncio.status === 'ATIVO').length,
    INATIVO: anuncios.filter((anuncio) => anuncio.status === 'INATIVO').length,
    ALUGADO: anuncios.filter((anuncio) => anuncio.status === 'ALUGADO').length,
  }), [anuncios]);
  const totalVisualizacoes = useMemo(() => anuncios.reduce((sum, anuncio) => sum + (anuncio.visualizacoes ?? 0), 0), [anuncios]);

  async function alternarStatus(anuncio) {
    const novoStatus = anuncio.status === 'ATIVO' ? 'INATIVO' : 'ATIVO';
    setUpdatingId(anuncio.id);
    setError(null);
    try {
      await anuncioApi.atualizarStatus(anuncio.id, novoStatus);
      setAnuncios((current) => current.map((item) => item.id === anuncio.id ? { ...item, status: novoStatus } : item));
    } catch (err) {
      setError(err.message);
    } finally {
      setUpdatingId(null);
    }
  }

  return (
    <>
      <Header />
      <div className="page-wrap">
        <div className="dash-layout">
          <aside className="dash-sidebar">
            <span className="dash-sidebar-title">Conta</span>
            <Link to="/perfil" className="sidebar-link"><i className="fa-solid fa-user" /> Meu perfil</Link>
            <Link to="/meus-anuncios" className="sidebar-link active"><i className="fa-solid fa-house-chimney" /> Meus anúncios</Link>
            <Link to="/avaliacoes" className="sidebar-link"><i className="fa-solid fa-star" /> Avaliações</Link>
          </aside>

          <main className="dash-main">
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24, flexWrap: 'wrap', gap: 12 }}>
              <div>
                <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 24, fontWeight: 500, color: 'var(--text-1)' }}>Meus Anúncios</h2>
                <p style={{ fontSize: 14, color: 'var(--text-2)', marginTop: 4 }}>Gerencie seus imóveis anunciados</p>
              </div>
              <Link to="/criar-anuncio" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                <i className="fa-solid fa-plus" /> Novo anúncio
              </Link>
            </div>

            <div className="stats-strip" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 24 }}>
              <div className="stat-card">
                <span className="stat-card-label">Anúncios ativos</span>
                <span className="stat-card-val clr-green">{contagens.ATIVO}</span>
              </div>
              <div className="stat-card">
                <span className="stat-card-label">Total de anúncios</span>
                <span className="stat-card-val clr-primary">{contagens.todos}</span>
              </div>
              <div className="stat-card">
                <span className="stat-card-label">Visualizações totais</span>
                <span className="stat-card-val clr-accent">{totalVisualizacoes}</span>
              </div>
            </div>

            <div className="tabs">
              {TABS.map((t) => (
                <button key={t.key} type="button" className={`tab-btn ${tab === t.key ? 'active' : ''}`} onClick={() => setTab(t.key)}>{t.label} ({contagens[t.key]})</button>
              ))}
            </div>

            {loading && <p style={{ color: 'var(--text-2)', marginTop: 16 }}>Carregando...</p>}
            {error && (
              <div className="alert alert-info" style={{ marginTop: 16 }}>
                <i className="fa-solid fa-triangle-exclamation" /><span>{error}</span>
              </div>
            )}

            {!loading && !error && !filtrados.length && (
              <div className="no-results"><div className="no-results-emoji">🏠</div><h3>Nenhum anúncio aqui</h3><p>Crie um novo anúncio para começar.</p></div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginTop: filtrados.length ? 16 : 0 }}>
              {filtrados.map((anuncio) => (
                <div key={anuncio.id} className="listing-card" style={{ opacity: anuncio.status === 'INATIVO' ? 0.6 : 1 }}>
                  <div className="listing-info">
                    <h4>{anuncio.titulo}</h4>
                    <div className="listing-info-meta">
                      <span><i className="fa-solid fa-tag" style={{ marginRight: 4 }} />{TIPO_OFERTA_LABELS[anuncio.tipoOferta] ?? anuncio.tipoOferta}</span>
                      <span><i className="fa-solid fa-eye" style={{ marginRight: 4 }} />{anuncio.visualizacoes ?? 0} visualizações</span>
                      <span className={`listing-status ${anuncio.status === 'ATIVO' ? 'active' : 'inactive'}`}><i className="fa-solid fa-circle" style={{ fontSize: 7 }} /> {STATUS_LABELS[anuncio.status] ?? anuncio.status}</span>
                    </div>
                  </div>
                  <div className="listing-actions">
                    <span className="listing-price">R$ {formatMoeda(anuncio.precoAluguel)}/mês</span>
                    <div style={{ display: 'flex', gap: 6 }}>
                      <Link to={`/editar-anuncio/${anuncio.id}`} className="btn-sm btn-outline" title="Editar"><i className="fa-solid fa-pen" /></Link>
                      {anuncio.status !== 'ALUGADO' && (
                        <button
                          className="btn-sm btn-outline"
                          type="button"
                          title={anuncio.status === 'ATIVO' ? 'Desativar' : 'Reativar'}
                          disabled={updatingId === anuncio.id}
                          onClick={() => alternarStatus(anuncio)}
                        >
                          <i className={`fa-solid ${anuncio.status === 'ATIVO' ? 'fa-pause' : 'fa-play'}`} />
                        </button>
                      )}
                      <Link to={`/imoveis/${anuncio.id}`} className="btn-sm btn-primary">Ver</Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </main>
        </div>
      </div>
    </>
  );
}
