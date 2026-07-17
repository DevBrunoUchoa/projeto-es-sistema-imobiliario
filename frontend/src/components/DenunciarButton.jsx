import { useState } from 'react';
import { denunciaApi } from '../api/denunciaApi';
import { MOTIVO_LABELS } from '../utils/denuncia';

const MOTIVOS = Object.keys(MOTIVO_LABELS);

export default function DenunciarButton({ tipoAlvo, alvoId, label }) {
  const [aberto, setAberto] = useState(false);
  const [motivo, setMotivo] = useState('');
  const [descricao, setDescricao] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState(null);
  const [enviado, setEnviado] = useState(false);

  function abrir() {
    setAberto(true);
    setErro(null);
    setEnviado(false);
  }

  async function enviar(event) {
    event.preventDefault();
    if (!motivo) return;
    setEnviando(true);
    setErro(null);
    try {
      await denunciaApi.criar({ tipoAlvo, alvoId, motivo, descricao });
      setEnviado(true);
    } catch (err) {
      setErro(err.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <button type="button" className="report-trigger" onClick={abrir}>
        <i className="fa-solid fa-flag" /> {label}
      </button>

      {aberto && (
        <div className="report-modal-overlay" onClick={(event) => { if (event.target === event.currentTarget) setAberto(false); }}>
          <div className="report-modal">
            <div className="report-modal-header">
              <span>{label}</span>
              <button type="button" className="report-modal-close" onClick={() => setAberto(false)} aria-label="Fechar"><i className="fa-solid fa-xmark" /></button>
            </div>

            {enviado ? (
              <div className="report-modal-body">
                <p className="contact-success"><i className="fa-solid fa-circle-check" /> Denúncia registrada. Nossa equipe vai analisar.</p>
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 12 }}>
                  <button type="button" className="btn-sm btn-outline" onClick={() => setAberto(false)}>Fechar</button>
                </div>
              </div>
            ) : (
              <form className="report-modal-body" onSubmit={enviar}>
                <div className="form-group">
                  <label className="form-label">Motivo</label>
                  <select className="form-select" value={motivo} onChange={(e) => setMotivo(e.target.value)} required>
                    <option value="">Selecione...</option>
                    {MOTIVOS.map((m) => <option key={m} value={m}>{MOTIVO_LABELS[m]}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Descrição (opcional)</label>
                  <textarea className="form-textarea" value={descricao} onChange={(e) => setDescricao(e.target.value)} placeholder="Conte o que aconteceu..." />
                </div>
                {erro && <p className="sidebar-note" style={{ color: '#991b1b' }}>{erro}</p>}
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
                  <button type="button" className="btn-sm btn-outline" onClick={() => setAberto(false)}>Cancelar</button>
                  <button type="submit" className="btn-sm btn-danger" disabled={enviando || !motivo}>{enviando ? 'Enviando...' : 'Denunciar'}</button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </>
  );
}
