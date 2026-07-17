import { useEffect, useRef, useState } from 'react';
import { notificacaoApi } from '../api/notificacaoApi';

const POLL_MS = 30000;

const TIPO_ICONS = {
  MATCH: 'fa-user-group',
  MENSAGEM: 'fa-envelope',
  AVALIACAO: 'fa-star',
  DENUNCIA_RESOLVIDA: 'fa-flag',
  VERIFICACAO_APROVADA: 'fa-circle-check',
};

function formatarData(iso) {
  if (!iso) return '';
  const data = new Date(iso);
  const agora = new Date();
  const diffMin = Math.round((agora - data) / 60000);
  if (diffMin < 1) return 'agora';
  if (diffMin < 60) return `${diffMin} min`;
  if (diffMin < 24 * 60) return `${Math.round(diffMin / 60)} h`;
  return data.toLocaleDateString('pt-BR');
}

export default function NotificationBell() {
  const [naoLidas, setNaoLidas] = useState(0);
  const [aberto, setAberto] = useState(false);
  const [notificacoes, setNotificacoes] = useState(null);
  const [carregando, setCarregando] = useState(false);
  const wrapRef = useRef(null);

  useEffect(() => {
    function atualizarContagem() {
      notificacaoApi.contarNaoLidas().then((data) => setNaoLidas(data.naoLidas ?? 0)).catch(() => {});
    }
    atualizarContagem();
    const intervalId = setInterval(atualizarContagem, POLL_MS);
    return () => clearInterval(intervalId);
  }, []);

  useEffect(() => {
    if (!aberto) return;
    function onClickFora(event) {
      if (wrapRef.current && !wrapRef.current.contains(event.target)) setAberto(false);
    }
    document.addEventListener('mousedown', onClickFora);
    return () => document.removeEventListener('mousedown', onClickFora);
  }, [aberto]);

  function alternarDropdown() {
    const abrindo = !aberto;
    setAberto(abrindo);
    if (abrindo && !notificacoes) {
      setCarregando(true);
      notificacaoApi.listar({ size: 10 })
        .then((data) => setNotificacoes(data.content ?? []))
        .catch(() => setNotificacoes([]))
        .finally(() => setCarregando(false));
    }
  }

  async function marcarUmaLida(notificacao) {
    if (notificacao.lida) return;
    setNotificacoes((current) => current.map((n) => n.id === notificacao.id ? { ...n, lida: true } : n));
    setNaoLidas((current) => Math.max(0, current - 1));
    try { await notificacaoApi.marcarComoLida(notificacao.id); } catch { /* mantém como lida localmente mesmo se a chamada falhar */ }
  }

  async function marcarTodasLidas() {
    setNotificacoes((current) => current?.map((n) => ({ ...n, lida: true })) ?? current);
    setNaoLidas(0);
    try { await notificacaoApi.marcarTodasComoLidas(); } catch { /* idem */ }
  }

  return (
    <div className="notif-bell-wrap" ref={wrapRef}>
      <button type="button" className="notif-bell-btn" aria-label="Notificações" onClick={alternarDropdown}>
        <i className="fa-solid fa-bell" />
        {naoLidas > 0 && <span className="notif-badge">{naoLidas > 9 ? '9+' : naoLidas}</span>}
      </button>

      {aberto && (
        <div className="notif-dropdown">
          <div className="notif-dropdown-header">
            <span>Notificações</span>
            {naoLidas > 0 && <button type="button" className="notif-mark-all" onClick={marcarTodasLidas}>Marcar todas como lidas</button>}
          </div>
          <div className="notif-list">
            {carregando && <p className="notif-empty">Carregando...</p>}
            {!carregando && notificacoes?.length === 0 && <p className="notif-empty">Nenhuma notificação por aqui.</p>}
            {!carregando && notificacoes?.map((n) => (
              <button key={n.id} type="button" className={`notif-item ${n.lida ? '' : 'unread'}`} onClick={() => marcarUmaLida(n)}>
                <i className={`fa-solid ${TIPO_ICONS[n.tipo] ?? 'fa-bell'} notif-item-icon`} />
                <div className="notif-item-body">
                  <span className="notif-item-title">{n.titulo}</span>
                  {n.mensagem && <span className="notif-item-msg">{n.mensagem}</span>}
                  <span className="notif-item-time">{formatarData(n.dataCriacao)}</span>
                </div>
                {!n.lida && <span className="notif-item-dot" />}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
