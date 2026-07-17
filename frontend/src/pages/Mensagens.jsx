import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import MessageCard from '../components/MessageCard';
import { useAuth } from '../contexts/AuthContext';
import { contatoApi } from '../api/contatoApi';

const PODE_RECEBER = ['LOCADOR', 'MISTO', 'ADMIN'];

export default function Mensagens() {
  const { user } = useAuth();
  const podeReceber = PODE_RECEBER.includes(user.role);
  const [tab, setTab] = useState(podeReceber ? 'recebidas' : 'enviadas');

  const [recebidas, setRecebidas] = useState([]);
  const [recebidasLoading, setRecebidasLoading] = useState(podeReceber);
  const [recebidasErro, setRecebidasErro] = useState(null);

  const [enviadas, setEnviadas] = useState([]);
  const [enviadasLoading, setEnviadasLoading] = useState(true);
  const [enviadasErro, setEnviadasErro] = useState(null);

  useEffect(() => {
    if (!podeReceber) return;
    contatoApi.listarRecebidos()
      .then(setRecebidas)
      .catch(() => setRecebidasErro('Não foi possível carregar as mensagens recebidas.'))
      .finally(() => setRecebidasLoading(false));
  }, [podeReceber]);

  useEffect(() => {
    contatoApi.listarEnviados()
      .then(setEnviadas)
      .catch(() => setEnviadasErro('Não foi possível carregar suas mensagens.'))
      .finally(() => setEnviadasLoading(false));
  }, []);

  return (
    <>
      <Header />
      <div className="page-wrap">
        <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
          <div style={{ marginBottom: 24 }}>
            <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 24, fontWeight: 500, color: 'var(--text-1)' }}>Mensagens</h2>
            <p style={{ fontSize: 14, color: 'var(--text-2)', marginTop: 4 }}>Interesses trocados com locadores e estudantes</p>
          </div>

          <div className="tabs">
            {podeReceber && <button className={`tab-btn ${tab === 'recebidas' ? 'active' : ''}`} type="button" onClick={() => setTab('recebidas')}>Recebidas ({recebidas.length})</button>}
            <button className={`tab-btn ${tab === 'enviadas' ? 'active' : ''}`} type="button" onClick={() => setTab('enviadas')}>Enviadas ({enviadas.length})</button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginTop: 16 }}>
            {tab === 'recebidas' && (
              recebidasLoading ? <p style={{ color: 'var(--text-3)' }}>Carregando...</p>
              : recebidasErro ? <div className="alert alert-info"><i className="fa-solid fa-triangle-exclamation" /><span>{recebidasErro}</span></div>
              : !recebidas.length ? <div className="no-results"><div className="no-results-emoji">📭</div><h3>Nenhuma mensagem recebida ainda</h3></div>
              : recebidas.map((c) => <MessageCard key={c.id} contato={c} mostrarAutor />)
            )}
            {tab === 'enviadas' && (
              enviadasLoading ? <p style={{ color: 'var(--text-3)' }}>Carregando...</p>
              : enviadasErro ? <div className="alert alert-info"><i className="fa-solid fa-triangle-exclamation" /><span>{enviadasErro}</span></div>
              : !enviadas.length ? (
                <div className="no-results"><div className="no-results-emoji">📭</div><h3>Você ainda não enviou nenhuma mensagem</h3><p>Entre em contato com um locador na página de detalhe de um imóvel.</p><Link to="/" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex' }}>Ver imóveis</Link></div>
              ) : enviadas.map((c) => <MessageCard key={c.id} contato={c} />)
            )}
          </div>
        </div>
      </div>
    </>
  );
}
