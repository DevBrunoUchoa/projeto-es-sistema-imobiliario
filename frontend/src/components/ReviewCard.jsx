import { useEffect, useState } from 'react';
import { userApi } from '../api/userApi';
import { avaliacaoApi } from '../api/avaliacaoApi';
import { anuncioApi } from '../api/anuncioApi';

function formatarData(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' });
}

function Estrelas({ nota }) {
  return (
    <div className="stars">
      {[1, 2, 3, 4, 5].map((n) => (
        <i key={n} className={`fa-solid fa-star ${n > nota ? 'star-empty' : ''}`} />
      ))}
    </div>
  );
}

export default function ReviewCard({ avaliacao, podeResponder, mostrarImovel }) {
  const [autor, setAutor] = useState(null);
  const [imovel, setImovel] = useState(null);
  const [respondendo, setRespondendo] = useState(false);
  const [resposta, setResposta] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState(null);
  const [respostaAtual, setRespostaAtual] = useState(avaliacao.respostaLocador);

  useEffect(() => {
    if (!avaliacao.avaliadorId) return;
    userApi.publico(avaliacao.avaliadorId).then(setAutor).catch(() => {});
  }, [avaliacao.avaliadorId]);

  useEffect(() => {
    if (!mostrarImovel || !avaliacao.adId) return;
    anuncioApi.detalhes(avaliacao.adId).then((a) => setImovel(a.titulo)).catch(() => {});
  }, [mostrarImovel, avaliacao.adId]);

  async function enviarResposta(event) {
    event.preventDefault();
    if (!resposta.trim()) return;
    setEnviando(true);
    setErro(null);
    try {
      const atualizada = await avaliacaoApi.responder(avaliacao.id, resposta);
      setRespostaAtual(atualizada.respostaLocador);
      setRespondendo(false);
    } catch (err) {
      setErro(err.message);
    } finally {
      setEnviando(false);
    }
  }

  const nome = autor?.nome ?? 'Estudante';

  return (
    <div className="review-card">
      <div className="review-header">
        <div className="review-avatar">{nome.charAt(0).toUpperCase()}</div>
        <div className="review-author-info">
          <h4>{nome}</h4>
          <span>{formatarData(avaliacao.dataCriacao)}</span>
        </div>
        <div style={{ marginLeft: 'auto' }}><Estrelas nota={avaliacao.nota} /></div>
      </div>
      {mostrarImovel && imovel && <span className="review-property"><i className="fa-solid fa-house" /> {imovel}</span>}
      <p className="review-text">{avaliacao.comentario}</p>

      {respostaAtual && (
        <div className="review-reply">
          <div className="review-reply-label"><i className="fa-solid fa-reply" /> Resposta do locador</div>
          <p>{respostaAtual}</p>
        </div>
      )}

      {podeResponder && !respostaAtual && (
        respondendo ? (
          <form className="contact-form" onSubmit={enviarResposta} style={{ marginTop: 10 }}>
            <textarea value={resposta} onChange={(e) => setResposta(e.target.value)} placeholder="Responda essa avaliação..." required />
            {erro && <p className="sidebar-note" style={{ color: '#991b1b' }}>{erro}</p>}
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn-sm btn-primary" type="submit" disabled={enviando}>{enviando ? 'Enviando...' : 'Responder'}</button>
              <button className="btn-sm btn-outline" type="button" onClick={() => setRespondendo(false)}>Cancelar</button>
            </div>
          </form>
        ) : (
          <button className="btn-ghost-sm" type="button" style={{ marginTop: 10 }} onClick={() => setRespondendo(true)}>
            <i className="fa-solid fa-reply" style={{ marginRight: 6 }} /> Responder
          </button>
        )
      )}
    </div>
  );
}
