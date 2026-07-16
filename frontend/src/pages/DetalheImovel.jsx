import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Header from '../components/Header';
import { anuncioApi } from '../api/anuncioApi';
import { TIPO_OFERTA_LABELS, TIPO_IMOVEL_LABELS, formatMoeda } from '../utils/anuncio';

export default function DetalheImovel() {
  const { id } = useParams();
  const [anuncio, setAnuncio] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lightboxIndex, setLightboxIndex] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    anuncioApi.detalhes(id)
      .then(setAnuncio)
      .catch(() => setError('Não encontramos esse imóvel. Ele pode ter sido removido ou o link está incorreto.'))
      .finally(() => setLoading(false));
  }, [id]);

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
                  <button className="btn-contact" type="button" disabled title="Mensagens ainda não integradas nesta versão">
                    <i className="fa-brands fa-whatsapp" /> Entrar em contato
                  </button>
                  <p className="sidebar-note">
                    <i className="fa-solid fa-circle-info" /> O contato direto com o anunciante ainda não está disponível nesta versão.
                  </p>
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
