import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Header from '../components/Header';
import { useAuth } from '../contexts/AuthContext';
import { anuncioApi } from '../api/anuncioApi';
import { userApi } from '../api/userApi';
import { contatoApi } from '../api/contatoApi';
import { avaliacaoApi } from '../api/avaliacaoApi';
import ReviewCard from '../components/ReviewCard';
import { useFavoritos } from '../hooks/useFavoritos';
import { TIPO_OFERTA_LABELS, TIPO_IMOVEL_LABELS, formatMoeda } from '../utils/anuncio';

export default function DetalheImovel() {
  const { id } = useParams();
  const { user } = useAuth();
  const { favoritos, toggle: toggleFavorito, habilitado: favoritosHabilitado } = useFavoritos();
  const [anuncio, setAnuncio] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lightboxIndex, setLightboxIndex] = useState(null);

  const [locador, setLocador] = useState(null);
  const [mensagem, setMensagem] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [interesseEnviado, setInteresseEnviado] = useState(false);
  const [contatoErro, setContatoErro] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    anuncioApi.detalhes(id)
      .then(setAnuncio)
      .catch(() => setError('Não encontramos esse imóvel. Ele pode ter sido removido ou o link está incorreto.'))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!anuncio?.locadorId) return;
    userApi.publico(anuncio.locadorId).then(setLocador).catch(() => {});
  }, [anuncio?.locadorId]);

  const [avaliacoes, setAvaliacoes] = useState([]);
  const [avaliacoesCarregando, setAvaliacoesCarregando] = useState(true);
  const [minhaNota, setMinhaNota] = useState(0);
  const [notaHover, setNotaHover] = useState(0);
  const [meuComentario, setMeuComentario] = useState('');
  const [enviandoAvaliacao, setEnviandoAvaliacao] = useState(false);
  const [avaliacaoErro, setAvaliacaoErro] = useState(null);
  const [avaliacaoEnviada, setAvaliacaoEnviada] = useState(false);

  useEffect(() => {
    if (!anuncio?.id) return;
    setAvaliacoesCarregando(true);
    avaliacaoApi.listarPorAnuncio(anuncio.id)
      .then((data) => setAvaliacoes(data.content ?? []))
      .catch(() => {})
      .finally(() => setAvaliacoesCarregando(false));
  }, [anuncio?.id]);

  async function enviarAvaliacao(event) {
    event.preventDefault();
    if (!minhaNota || !meuComentario.trim()) return;
    setEnviandoAvaliacao(true);
    setAvaliacaoErro(null);
    try {
      const nova = await avaliacaoApi.publicar({ adId: anuncio.id, nota: minhaNota, comentario: meuComentario });
      setAvaliacoes((current) => [nova, ...current]);
      setAvaliacaoEnviada(true);
      setMeuComentario('');
    } catch (err) {
      setAvaliacaoErro(err.message);
    } finally {
      setEnviandoAvaliacao(false);
    }
  }

  async function enviarInteresse(event) {
    event.preventDefault();
    if (!mensagem.trim()) return;
    setEnviando(true);
    setContatoErro(null);
    try {
      await contatoApi.registrarInteresse({ adId: anuncio.id, mensagem });
      setInteresseEnviado(true);
      setMensagem('');
      userApi.publico(anuncio.locadorId).then(setLocador).catch(() => {});
    } catch (err) {
      setContatoErro(err.message);
    } finally {
      setEnviando(false);
    }
  }

  useEffect(() => {
    if (lightboxIndex === null || !anuncio?.imagens?.length) return;
    function onKeyDown(event) {
      if (event.key === 'Escape') setLightboxIndex(null);
      if (event.key === 'ArrowLeft') setLightboxIndex((current) => (current - 1 + anuncio.imagens.length) % anuncio.imagens.length);
      if (event.key === 'ArrowRight') setLightboxIndex((current) => (current + 1) % anuncio.imagens.length);
    }
    document.addEventListener('keydown', onKeyDown);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKeyDown);
      document.body.style.overflow = '';
    };
  }, [lightboxIndex, anuncio]);

  if (loading) {
    return (
      <>
        <Header />
        <main style={{ paddingTop: 'var(--nav-h)', paddingBottom: 80 }}>
          <div className="container" style={{ paddingTop: 40 }}>Carregando imóvel...</div>
        </main>
      </>
    );
  }

  if (error || !anuncio) {
    return (
      <>
        <Header />
        <main style={{ paddingTop: 'var(--nav-h)', paddingBottom: 80 }}>
          <div className="container" style={{ paddingTop: 40 }}>
            <div className="no-results">
              <div className="no-results-emoji">🔍</div>
              <h3>Imóvel não encontrado</h3>
              <p>{error}</p>
              <Link to="/" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex' }}>Voltar para listagem</Link>
            </div>
          </div>
        </main>
      </>
    );
  }

  const imagens = anuncio.imagens ?? [];
  const precoTotal = Number(anuncio.precoAluguel ?? 0) + Number(anuncio.precoCondominio ?? 0) + Number(anuncio.precoIptu ?? 0);
  const availClass = anuncio.status === 'ATIVO' ? 'avail-yes' : 'avail-no';
  const availLabel = anuncio.status === 'ATIVO' ? 'Disponível' : anuncio.status === 'ALUGADO' ? 'Alugado' : 'Indisponível';
  const comodidades = [
    anuncio.mobiliado && ['fa-couch', 'Mobiliado'],
    anuncio.permitePets && ['fa-paw', 'Pet friendly'],
    anuncio.permiteFumantes && ['fa-smoking', 'Aceita fumantes'],
    anuncio.incluiAlimentacao && ['fa-utensils', 'Alimentação inclusa'],
  ].filter(Boolean);

  const endereco = [
    [anuncio.rua, anuncio.numero].filter(Boolean).join(', '),
    anuncio.complemento,
    [anuncio.bairro, anuncio.cidade, anuncio.estado].filter(Boolean).join(' — '),
  ].filter(Boolean).join(' · ');

  return (
    <>
      <Header />
      <main style={{ paddingTop: 'var(--nav-h)', paddingBottom: 80 }}>
        <div className="container" style={{ paddingTop: 40 }}>
          <Link to="/" className="detail-back"><i className="fa-solid fa-arrow-left" /> Voltar para listagem</Link>

          <div className="gallery">
            {imagens.length === 0 && <div className="gallery-empty"><i className="fa-solid fa-house" /></div>}
            {imagens.length > 0 && (
              <div className="gallery-main" onClick={() => setLightboxIndex(0)}>
                <img src={imagens[0]} alt={anuncio.titulo} />
              </div>
            )}
            {imagens.length > 1 && (
              <div className="gallery-thumb" onClick={() => setLightboxIndex(1)}>
                <img src={imagens[1]} alt={`${anuncio.titulo} — foto 2`} />
              </div>
            )}
            {imagens.length > 2 && (
              <div className="gallery-thumb gallery-more" data-more={`+${imagens.length - 2} fotos`} onClick={() => setLightboxIndex(2)}>
                <img src={imagens[2]} alt={`${anuncio.titulo} — foto 3`} />
              </div>
            )}
          </div>

          <div className="detail-layout">
            <div className="detail-main">
              <div className="detail-header">
                <div className="detail-type-row">
                  <span className="detail-type-tag">{TIPO_IMOVEL_LABELS[anuncio.tipoImovel] ?? anuncio.tipoImovel}</span>
                  <span className="detail-type-tag">{TIPO_OFERTA_LABELS[anuncio.tipoOferta] ?? anuncio.tipoOferta}</span>
                  <span className={`detail-avail ${availClass}`}><span className="avail-dot" /> {availLabel}</span>
                  {favoritosHabilitado && (
                    <button
                      type="button"
                      className="btn-ghost-sm"
                      style={{ marginLeft: 'auto' }}
                      onClick={() => toggleFavorito(anuncio.id)}
                    >
                      <i className={`fa-${favoritos.has(anuncio.id) ? 'solid' : 'regular'} fa-heart`} style={{ marginRight: 6, color: favoritos.has(anuncio.id) ? '#f87171' : undefined }} />
                      {favoritos.has(anuncio.id) ? 'Favoritado' : 'Favoritar'}
                    </button>
                  )}
                </div>
                <h1 className="detail-title">{anuncio.titulo}</h1>
                {endereco && <p className="detail-address"><i className="fa-solid fa-map-pin" /> {endereco}</p>}
              </div>

              <div className="specs-row">
                <div className="spec-item">
                  <i className="fa-solid fa-house spec-icon" />
                  <span className="spec-val">{TIPO_IMOVEL_LABELS[anuncio.tipoImovel] ?? anuncio.tipoImovel}</span>
                  <span className="spec-lbl">Tipo</span>
                </div>
                {anuncio.tipoOferta === 'VAGA_COMPARTILHADA' && (
                  <div className="spec-item">
                    <i className="fa-solid fa-users spec-icon" />
                    <span className="spec-val">{anuncio.vagasDisponiveis}/{anuncio.vagasTotal}</span>
                    <span className="spec-lbl">Vagas disponíveis</span>
                  </div>
                )}
                <div className="spec-item">
                  <i className="fa-solid fa-eye spec-icon" />
                  <span className="spec-val">{anuncio.visualizacoes ?? 0}</span>
                  <span className="spec-lbl">Visualizações</span>
                </div>
                <div className="spec-item">
                  <i className="fa-solid fa-star spec-icon" />
                  <span className="spec-val">{anuncio.notaMedia ? anuncio.notaMedia.toFixed(1) : '—'}</span>
                  <span className="spec-lbl">{anuncio.totalAvaliacoes ? `${anuncio.totalAvaliacoes} avaliações` : 'Sem avaliações'}</span>
                </div>
              </div>

              {anuncio.descricao && (
                <div className="detail-block">
                  <h2 className="detail-block-title">Sobre o imóvel</h2>
                  <p className="description-text">{anuncio.descricao}</p>
                </div>
              )}

              {comodidades.length > 0 && (
                <div className="detail-block">
                  <h2 className="detail-block-title">Comodidades</h2>
                  <div className="features-grid">
                    {comodidades.map(([icon, label]) => (
                      <div key={label} className="feature-item"><i className={`fa-solid ${icon}`} /> {label}</div>
                    ))}
                  </div>
                </div>
              )}

              {(anuncio.tempoPeMin || anuncio.tempoOnibusMin) && (
                <div className="detail-block">
                  <h2 className="detail-block-title">Deslocamento até a UFCG</h2>
                  <div className="travel-grid">
                    {anuncio.tempoPeMin && (
                      <div className="travel-item">
                        <div className="travel-icon"><i className="fa-solid fa-person-walking" /></div>
                        <div className="travel-info"><span className="travel-time">{anuncio.tempoPeMin} min</span><span className="travel-mode">A pé</span></div>
                      </div>
                    )}
                    {anuncio.tempoOnibusMin && (
                      <div className="travel-item">
                        <div className="travel-icon"><i className="fa-solid fa-bus" /></div>
                        <div className="travel-info"><span className="travel-time">{anuncio.tempoOnibusMin} min</span><span className="travel-mode">Ônibus</span></div>
                      </div>
                    )}
                  </div>
                </div>
              )}

              <div className="detail-block">
                <h2 className="detail-block-title">Avaliações {anuncio.totalAvaliacoes ? `(${anuncio.totalAvaliacoes})` : ''}</h2>

                {user && anuncio.locadorId !== user.id && (
                  avaliacaoEnviada ? (
                    <p className="contact-success"><i className="fa-solid fa-circle-check" /> Avaliação publicada!</p>
                  ) : (
                    <form className="contact-form" onSubmit={enviarAvaliacao} style={{ marginBottom: 20 }}>
                      <div style={{ display: 'flex', gap: 6, fontSize: 22, marginBottom: 10, cursor: 'pointer' }}>
                        {[1, 2, 3, 4, 5].map((n) => (
                          <i
                            key={n}
                            className={`fa-star ${n <= (notaHover || minhaNota) ? 'fa-solid' : 'fa-regular'}`}
                            style={{ color: '#fbbf24' }}
                            onMouseEnter={() => setNotaHover(n)}
                            onMouseLeave={() => setNotaHover(0)}
                            onClick={() => setMinhaNota(n)}
                          />
                        ))}
                      </div>
                      <textarea value={meuComentario} onChange={(e) => setMeuComentario(e.target.value)} placeholder="Conte sua experiência com esse imóvel e o locador..." required />
                      {avaliacaoErro && <p className="sidebar-note" style={{ color: '#991b1b' }}>{avaliacaoErro}</p>}
                      <button className="btn-sm btn-primary" type="submit" disabled={enviandoAvaliacao || !minhaNota}>{enviandoAvaliacao ? 'Enviando...' : 'Publicar avaliação'}</button>
                    </form>
                  )
                )}

                {avaliacoesCarregando && <p style={{ color: 'var(--text-3)', fontSize: 14 }}>Carregando avaliações...</p>}
                {!avaliacoesCarregando && !avaliacoes.length && <p style={{ color: 'var(--text-3)', fontSize: 14 }}>Ainda não há avaliações para este imóvel.</p>}
                {!avaliacoesCarregando && avaliacoes.length > 0 && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                    {avaliacoes.map((avaliacao) => (
                      <ReviewCard key={avaliacao.id} avaliacao={avaliacao} podeResponder={anuncio.locadorId === user?.id} />
                    ))}
                  </div>
                )}
              </div>
            </div>

            <aside className="detail-sidebar">
              <div className="sidebar-card">
                <div className="sidebar-price-block">
                  <div className="sidebar-price">
                    <span className="price-val">R$ {formatMoeda(precoTotal)}</span>
                    <span className="price-per">/mês</span>
                  </div>
                  <p className="sidebar-tagline">Aluguel R$ {formatMoeda(anuncio.precoAluguel)} + condomínio R$ {formatMoeda(anuncio.precoCondominio)} + IPTU R$ {formatMoeda(anuncio.precoIptu)}</p>
                </div>
                <div className="sidebar-body">
                  {locador && (
                    <div className="sidebar-landlord">
                      {locador.fotoUrl
                        ? <img className="landlord-avatar" src={locador.fotoUrl} alt={locador.nome} />
                        : <div className="landlord-avatar-fallback">{locador.nome?.charAt(0)?.toUpperCase() ?? '?'}</div>}
                      <div>
                        <p className="landlord-name">{locador.nome}{locador.verificado && <i className="fa-solid fa-circle-check" style={{ color: 'var(--forest)', marginLeft: 6, fontSize: 12 }} title="Verificado" />}</p>
                        <p className="landlord-type">{locador.curso ? `${locador.curso}${locador.instituicao ? ` · ${locador.instituicao}` : ''}` : 'Locador'}</p>
                        {locador.notaMedia != null && (
                          <div className="landlord-rating">
                            <i className="fa-solid fa-star" /> {Number(locador.notaMedia).toFixed(1)}
                            <span style={{ color: 'var(--text-3)', fontWeight: 400 }}>({locador.totalAvaliacoes ?? 0} avaliações)</span>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  {anuncio.locadorId === user?.id ? (
                    <p className="sidebar-note"><i className="fa-solid fa-circle-info" /> Este é o seu anúncio.</p>
                  ) : !user ? (
                    <Link to="/login" className="btn-contact" style={{ textDecoration: 'none', display: 'flex' }}>
                      <i className="fa-brands fa-whatsapp" /> Entrar para contatar
                    </Link>
                  ) : locador?.contatoLiberado ? (
                    <>
                      {locador.telefone && (
                        <div className="contact-info-row">
                          <div className="contact-info-icon"><i className="fa-solid fa-phone" /></div>
                          <div><p className="contact-info-label">Telefone / WhatsApp</p><p className="contact-info-val">{locador.telefone}</p></div>
                        </div>
                      )}
                      {locador.email && (
                        <div className="contact-info-row">
                          <div className="contact-info-icon"><i className="fa-solid fa-envelope" /></div>
                          <div><p className="contact-info-label">E-mail</p><p className="contact-info-val">{locador.email}</p></div>
                        </div>
                      )}
                      <p className="sidebar-note"><i className="fa-solid fa-circle-check" /> Contato liberado — você já demonstrou interesse neste anúncio.</p>
                    </>
                  ) : interesseEnviado ? (
                    <p className="contact-success"><i className="fa-solid fa-circle-check" /> Interesse enviado! O locador foi notificado por e-mail.</p>
                  ) : (
                    <form className="contact-form" onSubmit={enviarInteresse}>
                      <textarea value={mensagem} onChange={(e) => setMensagem(e.target.value)} placeholder="Escreva uma mensagem pro locador (ex: horários pra visitar, dúvidas sobre o imóvel...)" required />
                      {contatoErro && <p className="sidebar-note" style={{ color: '#991b1b' }}>{contatoErro}</p>}
                      <button className="btn-contact" type="submit" disabled={enviando}>
                        <i className="fa-brands fa-whatsapp" /> {enviando ? 'Enviando...' : 'Enviar interesse'}
                      </button>
                    </form>
                  )}
                </div>
              </div>
            </aside>
          </div>
        </div>
      </main>

      {lightboxIndex !== null && imagens.length > 0 && (
        <div className="lightbox open" onClick={(event) => { if (event.target === event.currentTarget) setLightboxIndex(null); }}>
          <div className="lightbox-img-wrap">
            <button className="lb-close" onClick={() => setLightboxIndex(null)} aria-label="Fechar"><i className="fa-solid fa-xmark" /></button>
            <img src={imagens[lightboxIndex]} alt={anuncio.titulo} />
            {imagens.length > 1 && (
              <>
                <button className="lb-btn lb-prev" onClick={() => setLightboxIndex((current) => (current - 1 + imagens.length) % imagens.length)} aria-label="Anterior"><i className="fa-solid fa-chevron-left" /></button>
                <button className="lb-btn lb-next" onClick={() => setLightboxIndex((current) => (current + 1) % imagens.length)} aria-label="Próxima"><i className="fa-solid fa-chevron-right" /></button>
              </>
            )}
            <span className="lb-counter">{lightboxIndex + 1} / {imagens.length}</span>
          </div>
        </div>
      )}
    </>
  );
}
