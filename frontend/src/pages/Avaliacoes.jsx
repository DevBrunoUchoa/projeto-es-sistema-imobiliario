import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import ReviewCard from '../components/ReviewCard';
import { useAuth } from '../contexts/AuthContext';
import { avaliacaoApi } from '../api/avaliacaoApi';
import { userApi } from '../api/userApi';

const PODE_RECEBER = ['LOCADOR', 'MISTO', 'ADMIN'];

export default function Avaliacoes() {
  const { user } = useAuth();
  const podeReceber = PODE_RECEBER.includes(user.role);
  const [tab, setTab] = useState(podeReceber ? 'recebidas' : 'minhas');

  const [recebidas, setRecebidas] = useState([]);
  const [recebidasLoading, setRecebidasLoading] = useState(podeReceber);
  const [reputacao, setReputacao] = useState(null);

  const [minhas, setMinhas] = useState([]);
  const [minhasLoading, setMinhasLoading] = useState(true);

  useEffect(() => {
    if (!podeReceber) return;
    avaliacaoApi.listarPorLocador(user.id).then((data) => setRecebidas(data.content ?? [])).catch(() => {}).finally(() => setRecebidasLoading(false));
    userApi.publico(user.id).then(setReputacao).catch(() => {});
  }, [podeReceber, user.id]);

  useEffect(() => {
    avaliacaoApi.minhas().then((data) => setMinhas(data.content ?? [])).catch(() => {}).finally(() => setMinhasLoading(false));
  }, []);

  return (
    <>
      <Header />
      <div className="page-wrap">
        <div className="dash-layout">
          <aside className="dash-sidebar">
            <span className="dash-sidebar-title">Conta</span>
            <Link to="/perfil" className="sidebar-link"><i className="fa-solid fa-user" /> Meu perfil</Link>
            {podeReceber && <Link to="/meus-anuncios" className="sidebar-link"><i className="fa-solid fa-house-chimney" /> Meus anúncios</Link>}
            <Link to="/minhas-avaliacoes" className="sidebar-link active"><i className="fa-solid fa-star" /> Avaliações</Link>
          </aside>

          <main className="dash-main">
            <div style={{ marginBottom: 24 }}>
              <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 24, fontWeight: 500, color: 'var(--text-1)' }}>Avaliações</h2>
              <p style={{ fontSize: 14, color: 'var(--text-2)', marginTop: 4 }}>Avaliações dos imóveis e locadores</p>
            </div>

            {podeReceber && reputacao?.notaMedia != null && (
              <div className="card-section" style={{ maxWidth: 260, marginBottom: 24 }}>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontFamily: 'var(--font-display)', fontSize: 48, fontWeight: 700, color: 'var(--primary)', lineHeight: 1 }}>{Number(reputacao.notaMedia).toFixed(1)}</div>
                  <div className="stars" style={{ justifyContent: 'center', fontSize: 16, margin: '8px 0' }}>
                    {[1, 2, 3, 4, 5].map((n) => <i key={n} className={`fa-solid fa-star ${n > Math.round(reputacao.notaMedia) ? 'star-empty' : ''}`} />)}
                  </div>
                  <div style={{ fontSize: 13, color: 'var(--text-3)' }}>Nota média · {reputacao.totalAvaliacoes ?? 0} avaliações</div>
                </div>
              </div>
            )}

            <div className="tabs">
              {podeReceber && <button className={`tab-btn ${tab === 'recebidas' ? 'active' : ''}`} type="button" onClick={() => setTab('recebidas')}>Recebidas ({recebidas.length})</button>}
              <button className={`tab-btn ${tab === 'minhas' ? 'active' : ''}`} type="button" onClick={() => setTab('minhas')}>Minhas avaliações ({minhas.length})</button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 14, marginTop: 16 }}>
              {tab === 'recebidas' && (
                recebidasLoading ? <p style={{ color: 'var(--text-3)' }}>Carregando...</p>
                : !recebidas.length ? <div className="no-results"><div className="no-results-emoji">⭐</div><h3>Nenhuma avaliação recebida ainda</h3></div>
                : recebidas.map((avaliacao) => <ReviewCard key={avaliacao.id} avaliacao={avaliacao} podeResponder mostrarImovel />)
              )}
              {tab === 'minhas' && (
                minhasLoading ? <p style={{ color: 'var(--text-3)' }}>Carregando...</p>
                : !minhas.length ? (
                  <div className="no-results"><div className="no-results-emoji">⭐</div><h3>Você ainda não avaliou nenhum imóvel</h3><p>Avalie um imóvel na página de detalhe dele, depois de ter demonstrado interesse.</p><Link to="/" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex' }}>Ver imóveis</Link></div>
                ) : minhas.map((avaliacao) => <ReviewCard key={avaliacao.id} avaliacao={avaliacao} mostrarImovel />)
              )}
            </div>
          </main>
        </div>
      </div>
    </>
  );
}
