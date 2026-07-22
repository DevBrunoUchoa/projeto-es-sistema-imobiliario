import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Header from '../components/Header';
import { anuncioApi } from '../api/anuncioApi';
import { TIPO_OFERTA_LABELS } from '../utils/anuncio';

const TIPOS_OFERTA = Object.keys(TIPO_OFERTA_LABELS);

export default function EditarAnuncio() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);
  const [form, setForm] = useState(null);

  useEffect(() => {
    anuncioApi.detalhes(id)
      .then((anuncio) => setForm({
        titulo: anuncio.titulo,
        descricao: anuncio.descricao ?? '',
        tipoOferta: anuncio.tipoOferta,
        precoAluguel: String(anuncio.precoAluguel ?? ''),
        precoCondominio: String(anuncio.precoCondominio ?? '0'),
        precoIptu: String(anuncio.precoIptu ?? '0'),
        vagasTotal: String(anuncio.vagasTotal ?? '1'),
        vagasDisponiveis: String(anuncio.vagasDisponiveis ?? '0'),
        dataDisponivelDe: anuncio.dataDisponivelDe ?? new Date().toISOString().slice(0, 10),
        dataDisponivelAte: anuncio.dataDisponivelAte ?? '',
        periodoMinMeses: anuncio.periodoMinMeses != null ? String(anuncio.periodoMinMeses) : '',
        periodoMaxMeses: anuncio.periodoMaxMeses != null ? String(anuncio.periodoMaxMeses) : '',
      }))
      .catch(() => setLoadError('Não foi possível carregar este anúncio.'))
      .finally(() => setLoading(false));
  }, [id]);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function salvar(event) {
    event.preventDefault();
    if (!form.titulo.trim()) { setSaveError('Informe um título para o anúncio.'); return; }
    if (!form.precoAluguel || Number(form.precoAluguel) <= 0) { setSaveError('Informe um valor de aluguel maior que zero.'); return; }
    if (Number(form.vagasDisponiveis) > Number(form.vagasTotal)) { setSaveError('Vagas disponíveis não pode ser maior que o total de vagas.'); return; }
    if (!form.dataDisponivelDe) { setSaveError('Informe a partir de quando o imóvel está disponível.'); return; }
    if (form.dataDisponivelAte && form.dataDisponivelAte < form.dataDisponivelDe) {
      setSaveError('A data final de disponibilidade não pode ser anterior à data inicial.');
      return;
    }
    if (form.periodoMinMeses && form.periodoMaxMeses && Number(form.periodoMaxMeses) < Number(form.periodoMinMeses)) {
      setSaveError('O máximo de meses não pode ser menor que o mínimo.');
      return;
    }

    setSaveError(null);
    setSaving(true);
    try {
      await anuncioApi.atualizar(id, {
        titulo: form.titulo,
        descricao: form.descricao,
        tipoOferta: form.tipoOferta,
        precoAluguel: form.precoAluguel,
        precoCondominio: form.precoCondominio || 0,
        precoIptu: form.precoIptu || 0,
        vagasTotal: Number(form.vagasTotal),
        vagasDisponiveis: Number(form.vagasDisponiveis),
        dataDisponivelDe: form.dataDisponivelDe,
        dataDisponivelAte: form.dataDisponivelAte || undefined,
        periodoMinMeses: form.periodoMinMeses !== '' ? Number(form.periodoMinMeses) : undefined,
        periodoMaxMeses: form.periodoMaxMeses !== '' ? Number(form.periodoMaxMeses) : undefined,
      });
      navigate(`/imoveis/${id}`);
    } catch (err) {
      setSaveError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <Header />
      <div className="page-wrap" style={{ background: 'var(--bg-2)' }}>
        <div className="container" style={{ maxWidth: 760, paddingTop: 40, paddingBottom: 60 }}>
          <div style={{ marginBottom: 32 }}>
            <h1 style={{ fontFamily: 'var(--font-display)', fontSize: 28, fontWeight: 500, color: 'var(--text-1)' }}>Editar anúncio</h1>
            <p style={{ fontSize: 15, color: 'var(--text-2)', marginTop: 6 }}>Atualize os dados do anúncio. Endereço e fotos não são editáveis aqui.</p>
          </div>

          {loading && <p style={{ color: 'var(--text-2)' }}>Carregando...</p>}

          {loadError && (
            <div className="no-results">
              <div className="no-results-emoji">🔍</div>
              <h3>Não foi possível carregar</h3>
              <p>{loadError}</p>
              <Link to="/meus-anuncios" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex' }}>Voltar pra meus anúncios</Link>
            </div>
          )}

          {form && (
            <form className="card-section" onSubmit={salvar}>
              <div className="card-section-title">Dados do anúncio</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
                {saveError && (
                  <div className="alert alert-info" style={{ borderColor: '#991b1b', color: '#991b1b' }}>
                    <i className="fa-solid fa-triangle-exclamation" /><span>{saveError}</span>
                  </div>
                )}

                <div className="form-group">
                  <label className="form-label">Tipo de oferta</label>
                  <div className="toggle-group">
                    {TIPOS_OFERTA.map((tipo) => (
                      <button key={tipo} type="button" className={`tg-btn ${form.tipoOferta === tipo ? 'active' : ''}`} onClick={() => update('tipoOferta', tipo)}>{TIPO_OFERTA_LABELS[tipo]}</button>
                    ))}
                  </div>
                </div>
                <div className="form-group">
                  <label className="form-label">Título do anúncio</label>
                  <input type="text" className="form-input" maxLength={80} value={form.titulo} onChange={(e) => update('titulo', e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">Descrição</label>
                  <textarea className="form-textarea" style={{ minHeight: 120 }} value={form.descricao} onChange={(e) => update('descricao', e.target.value)} />
                </div>
                <div className="form-grid">
                  <div className="form-group">
                    <label className="form-label">Aluguel mensal (R$)</label>
                    <input type="number" min="0" step="0.01" className="form-input" value={form.precoAluguel} onChange={(e) => update('precoAluguel', e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Condomínio (R$)</label>
                    <input type="number" min="0" step="0.01" className="form-input" value={form.precoCondominio} onChange={(e) => update('precoCondominio', e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">IPTU (R$)</label>
                    <input type="number" min="0" step="0.01" className="form-input" value={form.precoIptu} onChange={(e) => update('precoIptu', e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Total de vagas</label>
                    <input type="number" min="1" className="form-input" value={form.vagasTotal} onChange={(e) => update('vagasTotal', e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Vagas disponíveis</label>
                    <input type="number" min="0" className="form-input" value={form.vagasDisponiveis} onChange={(e) => update('vagasDisponiveis', e.target.value)} />
                  </div>
                </div>
                <div className="form-group">
                  <label className="form-label">Período de locação</label>
                  <span className="form-hint">Duração variável, sob demanda do aluno — janela de disponibilidade e, se quiser, mínimo/máximo de meses.</span>
                  <div className="form-grid" style={{ marginTop: 8 }}>
                    <div className="form-group">
                      <label className="form-label">Disponível a partir de</label>
                      <input type="date" className="form-input" value={form.dataDisponivelDe} onChange={(e) => update('dataDisponivelDe', e.target.value)} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Disponível até (opcional)</label>
                      <input type="date" className="form-input" value={form.dataDisponivelAte} onChange={(e) => update('dataDisponivelAte', e.target.value)} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Mínimo de meses (opcional)</label>
                      <input type="number" min="1" className="form-input" value={form.periodoMinMeses} onChange={(e) => update('periodoMinMeses', e.target.value)} placeholder="Ex: 3" />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Máximo de meses (opcional)</label>
                      <input type="number" min="1" className="form-input" value={form.periodoMaxMeses} onChange={(e) => update('periodoMaxMeses', e.target.value)} placeholder="Sem limite se vazio" />
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Link to="/meus-anuncios" className="btn-ghost-sm">Cancelar</Link>
                  <button className="btn-primary" type="submit" disabled={saving}>{saving ? 'Salvando...' : 'Salvar alterações'}</button>
                </div>
              </div>
            </form>
          )}
        </div>
      </div>
    </>
  );
}
