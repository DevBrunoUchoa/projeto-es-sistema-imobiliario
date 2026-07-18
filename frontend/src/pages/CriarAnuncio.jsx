import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import { imovelApi } from '../api/imovelApi';
import { anuncioApi } from '../api/anuncioApi';
import { useFilePicker } from '../hooks/useFilePicker';
import { TIPO_IMOVEL_LABELS, TIPO_OFERTA_LABELS } from '../utils/anuncio';

const TIPOS_IMOVEL = Object.keys(TIPO_IMOVEL_LABELS);
const TIPOS_OFERTA = Object.keys(TIPO_OFERTA_LABELS);

const STEPS = [
  { n: 1, label: 'Imóvel & anúncio' },
  { n: 2, label: 'Endereço' },
  { n: 3, label: 'Fotos' },
  { n: 4, label: 'Publicado' },
];

export default function CriarAnuncio() {
  const navigate = useNavigate();
  const { inputRef: fileInputRef, openPicker: abrirSeletorFotos, resetPicker: limparSeletorFotos } = useFilePicker();

  const [step, setStep] = useState(1);
  const [saving, setSaving] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);
  const [anuncioId, setAnuncioId] = useState(null);
  const [imagens, setImagens] = useState([]);
  const [filesToUpload, setFilesToUpload] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [cepLoading, setCepLoading] = useState(false);
  const [cepErro, setCepErro] = useState(null);
  // Só aparece se a geocodificação automática do endereço falhar no backend.
  const [mostrarCoords, setMostrarCoords] = useState(false);
  // Prévias locais das fotos escolhidas, antes de enviar ao servidor.
  const [previews, setPreviews] = useState([]);

  const [form, setForm] = useState({
    tipoImovel: 'APARTAMENTO',
    tipoOferta: 'IMOVEL_COMPLETO',
    titulo: '',
    descricao: '',
    precoAluguel: '',
    precoCondominio: '0',
    precoIptu: '0',
    vagasTotal: '1',
    vagasDisponiveis: '1',
    cep: '',
    rua: '',
    numero: '',
    complemento: '',
    bairro: '',
    cidade: 'Campina Grande',
    estado: 'PB',
    latitude: '',
    longitude: '',
    mobiliado: false,
    permitePets: false,
    permiteFumantes: false,
    incluiAlimentacao: false,
  });

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  // Autocompleta rua/bairro/cidade/UF a partir do CEP (ViaCEP, gratuito e sem
  // chave). O usuário ainda edita/completa o número e o complemento.
  async function buscarEnderecoPorCep(cepDigitado) {
    const cepLimpo = (cepDigitado || '').replace(/\D/g, '');
    if (cepLimpo.length !== 8) return;

    setCepLoading(true);
    setCepErro(null);
    try {
      const resposta = await fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`);
      const dados = await resposta.json();
      if (dados.erro) {
        setCepErro('CEP não encontrado. Preencha o endereço manualmente.');
        return;
      }
      setForm((current) => ({
        ...current,
        rua: dados.logradouro || current.rua,
        bairro: dados.bairro || current.bairro,
        cidade: dados.localidade || current.cidade,
        estado: (dados.uf || current.estado || '').toUpperCase(),
      }));
    } catch {
      setCepErro('Não foi possível consultar o CEP agora. Preencha o endereço manualmente.');
    } finally {
      setCepLoading(false);
    }
  }

  function onCepChange(valor) {
    update('cep', valor);
    setCepErro(null);
    if (valor.replace(/\D/g, '').length === 8) {
      buscarEnderecoPorCep(valor);
    }
  }

  function validarStep1() {
    if (!form.titulo.trim()) return 'Informe um título para o anúncio.';
    if (!form.precoAluguel || Number(form.precoAluguel) <= 0) return 'Informe um valor de aluguel maior que zero.';
    if (Number(form.vagasDisponiveis) > Number(form.vagasTotal)) return 'Vagas disponíveis não pode ser maior que o total de vagas.';
    return null;
  }

  function validarStep2() {
    if (!/^\d{5}-?\d{3}$/.test(form.cep)) return 'CEP inválido. Use o formato 00000-000.';
    if (!form.rua.trim() || !form.numero.trim() || !form.bairro.trim() || !form.cidade.trim()) return 'Preencha rua, número, bairro e cidade.';
    if (!/^[A-Z]{2}$/.test(form.estado)) return 'Estado deve ter 2 letras maiúsculas, ex: PB.';
    return null;
  }

  function irParaStep2() {
    const erro = validarStep1();
    if (erro) { setErrorMsg(erro); return; }
    setErrorMsg(null);
    setStep(2);
  }

  async function publicarAnuncio() {
    const erro = validarStep2();
    if (erro) { setErrorMsg(erro); return; }

    setErrorMsg(null);
    setSaving(true);
    try {
      const imovel = await imovelApi.criar({
        tipo: form.tipoImovel,
        cep: form.cep,
        rua: form.rua,
        numero: form.numero,
        complemento: form.complemento || undefined,
        bairro: form.bairro,
        cidade: form.cidade,
        estado: form.estado,
        // Opcionais: se o backend não geocodificar o endereço, o usuário pode
        // ter informado as coordenadas manualmente no bloco de fallback.
        latitude: form.latitude !== '' ? Number(form.latitude) : undefined,
        longitude: form.longitude !== '' ? Number(form.longitude) : undefined,
        mobiliado: form.mobiliado,
        permitePets: form.permitePets,
        permiteFumantes: form.permiteFumantes,
        incluiAlimentacao: form.incluiAlimentacao,
      });

      const anuncio = await anuncioApi.criar({
        imovelId: imovel.id,
        titulo: form.titulo,
        tipoOferta: form.tipoOferta,
        precoAluguel: form.precoAluguel,
        precoCondominio: form.precoCondominio || 0,
        precoIptu: form.precoIptu || 0,
        descricao: form.descricao,
        vagasTotal: Number(form.vagasTotal),
        vagasDisponiveis: Number(form.vagasDisponiveis),
      });

      setAnuncioId(anuncio.id);
      setStep(3);
    } catch (err) {
      const msg = err.message || 'Não foi possível publicar o anúncio.';
      // Backend não conseguiu localizar o endereço no mapa (Nominatim).
      // Em vez do erro cru pedindo lat/long, mostramos o bloco opcional de
      // coordenadas para o usuário conseguir concluir mesmo assim.
      if (/latitude|localizar o endere/i.test(msg)) {
        setMostrarCoords(true);
        setErrorMsg('Não localizamos esse endereço no mapa automaticamente. Confira o CEP e a rua acima — ou, se preferir, informe a localização manualmente no bloco abaixo (opcional) e publique novamente.');
      } else {
        setErrorMsg(msg);
      }
    } finally {
      setSaving(false);
    }
  }

  function onFilesSelected(event) {
    setFilesToUpload(Array.from(event.target.files ?? []));
  }

  function removerPendente(index) {
    setFilesToUpload((current) => current.filter((_, i) => i !== index));
  }

  // Gera (e libera) as URLs de prévia sempre que a seleção de fotos muda.
  useEffect(() => {
    const urls = filesToUpload.map((file) => URL.createObjectURL(file));
    setPreviews(urls);
    return () => urls.forEach((url) => URL.revokeObjectURL(url));
  }, [filesToUpload]);

  async function enviarFotos() {
    if (!filesToUpload.length) return;
    setUploading(true);
    setErrorMsg(null);
    try {
      const novasImagens = await anuncioApi.imagens.upload(anuncioId, filesToUpload);
      setImagens((current) => [...current, ...novasImagens]);
      setFilesToUpload([]);
      limparSeletorFotos();
    } catch (err) {
      setErrorMsg(err.message);
    } finally {
      setUploading(false);
    }
  }

  return (
    <>
      <Header />
      <div className="page-wrap" style={{ background: 'var(--bg-2)' }}>
        <div className="container" style={{ maxWidth: 760, paddingTop: 40, paddingBottom: 60 }}>
          <div style={{ marginBottom: 32 }}>
            <h1 style={{ fontFamily: 'var(--font-display)', fontSize: 28, fontWeight: 500, color: 'var(--text-1)' }}>Criar novo anúncio</h1>
            <p style={{ fontSize: 15, color: 'var(--text-2)', marginTop: 6 }}>Preencha as informações do seu imóvel</p>
          </div>

          <div className="step-progress">
            {STEPS.map((s) => (
              <div key={s.n} className={`step-item ${step === s.n ? 'active' : step > s.n ? 'done' : ''}`}>
                <div className="step-dot">{step > s.n ? <i className="fa-solid fa-check" style={{ fontSize: 11 }} /> : s.n}</div>
                <span className="step-label">{s.label}</span>
              </div>
            ))}
          </div>

          {errorMsg && (
            <div className="alert alert-info" style={{ marginBottom: 18, borderColor: '#991b1b', color: '#991b1b' }}>
              <i className="fa-solid fa-triangle-exclamation" /><span>{errorMsg}</span>
            </div>
          )}

          {step === 1 && (
            <div className="card-section">
              <div className="card-section-title">Tipo e informações básicas</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
                <div className="form-group">
                  <label className="form-label">Tipo de imóvel</label>
                  <div className="toggle-group">
                    {TIPOS_IMOVEL.map((tipo) => (
                      <button key={tipo} type="button" className={`tg-btn ${form.tipoImovel === tipo ? 'active' : ''}`} onClick={() => update('tipoImovel', tipo)}>{TIPO_IMOVEL_LABELS[tipo]}</button>
                    ))}
                  </div>
                </div>
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
                  <input type="text" className="form-input" maxLength={80} value={form.titulo} onChange={(e) => update('titulo', e.target.value)} placeholder="Ex: Quarto confortável próximo à universidade" />
                  <span className="form-hint">Seja descritivo e atrativo. Máximo 80 caracteres.</span>
                </div>
                <div className="form-group">
                  <label className="form-label">Descrição</label>
                  <textarea className="form-textarea" style={{ minHeight: 120 }} value={form.descricao} onChange={(e) => update('descricao', e.target.value)} placeholder="Descreva o imóvel, diferenciais, ambiente, vizinhança..." />
                </div>
                <div className="form-grid">
                  <div className="form-group">
                    <label className="form-label">Aluguel mensal (R$)</label>
                    <input type="number" min="0" step="0.01" className="form-input" value={form.precoAluguel} onChange={(e) => update('precoAluguel', e.target.value)} placeholder="0,00" />
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
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button className="btn-primary" type="button" onClick={irParaStep2}>Próximo <i className="fa-solid fa-arrow-right" style={{ marginLeft: 6, fontSize: 13 }} /></button>
                </div>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="card-section">
              <div className="card-section-title">Endereço do imóvel</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
                <div className="alert alert-info">
                  <i className="fa-solid fa-location-dot" />
                  <span>A distância até a UFCG é calculada automaticamente a partir do endereço.</span>
                </div>
                <div className="form-grid">
                  <div className="form-group">
                    <label className="form-label">CEP</label>
                    <input type="text" className="form-input" value={form.cep} onChange={(e) => onCepChange(e.target.value)} onBlur={(e) => buscarEnderecoPorCep(e.target.value)} placeholder="58400-000" />
                    {cepLoading && <span className="form-hint">Buscando endereço...</span>}
                    {cepErro && <span className="form-hint" style={{ color: '#991b1b' }}>{cepErro}</span>}
                    {!cepLoading && !cepErro && <span className="form-hint">Digite o CEP para preencher rua, bairro e cidade automaticamente.</span>}
                  </div>
                  <div className="form-group">
                    <label className="form-label">Rua / Avenida</label>
                    <input type="text" className="form-input" value={form.rua} onChange={(e) => update('rua', e.target.value)} placeholder="Rua das Flores" />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Número</label>
                    <input type="text" className="form-input" value={form.numero} onChange={(e) => update('numero', e.target.value)} placeholder="123" />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Complemento</label>
                    <input type="text" className="form-input" value={form.complemento} onChange={(e) => update('complemento', e.target.value)} placeholder="Apto, bloco..." />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Bairro</label>
                    <input type="text" className="form-input" value={form.bairro} onChange={(e) => update('bairro', e.target.value)} placeholder="Universitário" />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Cidade</label>
                    <input type="text" className="form-input" value={form.cidade} onChange={(e) => update('cidade', e.target.value)} />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Estado (UF)</label>
                    <input type="text" className="form-input" maxLength={2} value={form.estado} onChange={(e) => update('estado', e.target.value.toUpperCase())} placeholder="PB" />
                  </div>
                </div>
                {mostrarCoords && (
                  <div className="form-group">
                    <label className="form-label">Localização manual (opcional)</label>
                    <span className="form-hint">Só é necessário se não localizarmos o endereço no mapa. No Google Maps, clique com o botão direito sobre o local e copie os dois números que aparecem.</span>
                    <div className="form-grid" style={{ marginTop: 8 }}>
                      <div className="form-group">
                        <label className="form-label">Latitude</label>
                        <input type="text" className="form-input" value={form.latitude} onChange={(e) => update('latitude', e.target.value)} placeholder="-7.2296" />
                      </div>
                      <div className="form-group">
                        <label className="form-label">Longitude</label>
                        <input type="text" className="form-input" value={form.longitude} onChange={(e) => update('longitude', e.target.value)} placeholder="-35.8810" />
                      </div>
                    </div>
                  </div>
                )}
                <div className="form-group">
                  <label className="form-label">Comodidades</label>
                  <div className="fp-checks">
                    <label className="fp-check-item"><input type="checkbox" checked={form.mobiliado} onChange={(e) => update('mobiliado', e.target.checked)} /><span className="check-box" /><i className="fa-solid fa-couch" /> Mobiliado</label>
                    <label className="fp-check-item"><input type="checkbox" checked={form.permitePets} onChange={(e) => update('permitePets', e.target.checked)} /><span className="check-box" /><i className="fa-solid fa-paw" /> Pet friendly</label>
                    <label className="fp-check-item"><input type="checkbox" checked={form.permiteFumantes} onChange={(e) => update('permiteFumantes', e.target.checked)} /><span className="check-box" /><i className="fa-solid fa-smoking" /> Aceita fumantes</label>
                    <label className="fp-check-item"><input type="checkbox" checked={form.incluiAlimentacao} onChange={(e) => update('incluiAlimentacao', e.target.checked)} /><span className="check-box" /><i className="fa-solid fa-utensils" /> Alimentação inclusa</label>
                  </div>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <button className="btn-ghost-sm" type="button" onClick={() => setStep(1)}><i className="fa-solid fa-arrow-left" style={{ marginRight: 6 }} /> Voltar</button>
                  <button className="btn-primary" type="button" disabled={saving} onClick={publicarAnuncio}>
                    {saving ? 'Publicando...' : <>Publicar anúncio <i className="fa-solid fa-check" style={{ marginLeft: 6, fontSize: 13 }} /></>}
                  </button>
                </div>
              </div>
            </div>
          )}

          {step === 3 && (
            <div className="card-section">
              <div className="card-section-title">Fotos do imóvel</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
                <div className="alert alert-info">
                  <i className="fa-solid fa-camera" />
                  <span>O anúncio já está publicado. As fotos são opcionais, mas anúncios com fotos recebem mais contatos.</span>
                </div>
                <div className="photo-upload-grid">
                  {imagens.map((imagem, index) => (
                    <div key={imagem.id} className={`photo-slot filled ${index === 0 && !previews.length ? 'main-photo' : ''}`}>
                      <img src={imagem.url} alt="" />
                    </div>
                  ))}
                  {previews.map((url, index) => (
                    <div key={`preview-${index}`} className="photo-slot filled" style={{ position: 'relative' }}>
                      <img src={url} alt={`Prévia da foto ${index + 1}`} />
                      <span style={{ position: 'absolute', bottom: 4, left: 4, fontSize: 10, fontWeight: 700, background: 'rgba(0,0,0,.6)', color: '#fff', padding: '2px 6px', borderRadius: 4 }}>Não enviada</span>
                      <button
                        type="button"
                        aria-label="Remover foto"
                        onClick={() => removerPendente(index)}
                        style={{ position: 'absolute', top: 4, right: 4, width: 22, height: 22, borderRadius: '50%', border: 'none', background: 'rgba(0,0,0,.6)', color: '#fff', cursor: 'pointer', display: 'grid', placeItems: 'center', lineHeight: 1 }}
                      >
                        <i className="fa-solid fa-xmark" style={{ fontSize: 11 }} />
                      </button>
                    </div>
                  ))}
                  <div className="photo-slot" onClick={abrirSeletorFotos}>
                    <i className="fa-solid fa-plus" />
                    <span>Adicionar</span>
                  </div>
                </div>
                <input ref={fileInputRef} type="file" accept="image/*" multiple hidden onChange={onFilesSelected} />
                {filesToUpload.length > 0 && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <span className="form-hint">{filesToUpload.length} foto(s) selecionada(s)</span>
                    <button className="btn-ghost-sm" type="button" disabled={uploading} onClick={enviarFotos}>{uploading ? 'Enviando...' : 'Enviar fotos'}</button>
                  </div>
                )}
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button className="btn-primary" type="button" onClick={() => setStep(4)}>Concluir <i className="fa-solid fa-arrow-right" style={{ marginLeft: 6, fontSize: 13 }} /></button>
                </div>
              </div>
            </div>
          )}

          {step === 4 && (
            <div className="card-section" style={{ textAlign: 'center' }}>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20, padding: '20px 0' }}>
                <div style={{ width: 80, height: 80, borderRadius: '50%', background: 'rgba(34,197,94,.12)', border: '2px solid rgba(34,197,94,.3)', display: 'grid', placeItems: 'center', fontSize: 32, color: '#22c55e' }}>
                  <i className="fa-solid fa-check" />
                </div>
                <div>
                  <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 26, fontWeight: 500 }}>Anúncio publicado!</h2>
                  <p style={{ color: 'var(--text-2)', fontSize: 15, marginTop: 8, lineHeight: 1.6 }}>Seu imóvel já está visível na plataforma.</p>
                </div>
                <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', justifyContent: 'center' }}>
                  <Link to={`/imoveis/${anuncioId}`} className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: 8 }}><i className="fa-solid fa-eye" /> Ver anúncio</Link>
                  <button className="btn-ghost-sm" type="button" onClick={() => navigate('/meus-anuncios')}>Meus anúncios</button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
}