import { useEffect, useState } from 'react';
import Header from '../components/Header';
import PropertyCard from '../components/PropertyCard';
import { useAuth } from '../contexts/AuthContext';
import { anuncioApi } from '../api/anuncioApi';
import { useFavoritos } from '../hooks/useFavoritos';

const QUICK_FILTERS = [
  ['mobiliado', 'fa-couch', 'Mobiliado'],
  ['permitePets', 'fa-paw', 'Pet friendly'],
  ['permiteFumantes', 'fa-smoking', 'Aceita fumantes'],
  ['incluiAlimentacao', 'fa-utensils', 'Alimentação inclusa'],
];

export default function Home() {
  const { user } = useAuth();
  const [search, setSearch] = useState('');
  const [tipoOferta, setTipoOferta] = useState('');
  const [maxPrice, setMaxPrice] = useState('9999');
  const [activeFilters, setActiveFilters] = useState([]);
  const [anuncios, setAnuncios] = useState([]);
  const [totalItems, setTotalItems] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { favoritos, toggle: toggleFavorito, habilitado: favoritosHabilitado } = useFavoritos();

  useEffect(() => {
    const timeout = setTimeout(() => {
      setLoading(true);
      setError(null);

      const params = {
        limit: 24,
        q: search || undefined,
        tipoOferta: tipoOferta || undefined,
        precoMax: maxPrice !== '9999' ? maxPrice : undefined,
      };
      activeFilters.forEach((filter) => { params[filter] = true; });

      anuncioApi.listar(params)
        .then((data) => {
          setAnuncios(data.items ?? []);
          setTotalItems(data.totalItems ?? 0);
        })
        .catch(() => setError('Não foi possível carregar os imóveis agora. Tente novamente em instantes.'))
        .finally(() => setLoading(false));
    }, 300);

    return () => clearTimeout(timeout);
  }, [search, tipoOferta, maxPrice, activeFilters]);

  function toggleFilter(filter) {
    setActiveFilters((current) => current.includes(filter) ? current.filter((item) => item !== filter) : [...current, filter]);
  }

  return (
    <>
      <Header />

      <main>
        <section className="hero">
          <div className="hero-blobs"><div className="blob b1" /><div className="blob b2" /><div className="blob b3" /></div>

          <div className="hero-content">
            <h1 className="hero-title">{user ? <>Olá, {user.nome?.split(' ')[0]}. Encontre seu <em>novo lar</em></> : <>Encontre sua <em>moradia</em><br />próxima à universidade</>}</h1>
            <p className="hero-sub">Explore os imóveis anunciados perto da UFCG e encontre o lugar certo para o seu próximo semestre.</p>

            <div className="search-bar">
              <div className="sb-field">
                <label className="sb-label" htmlFor="home-search">Buscar</label>
                <input id="home-search" className="sb-input" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Título ou descrição do anúncio..." />
              </div>
              <div className="sb-divider" />
              <div className="sb-field">
                <label className="sb-label" htmlFor="home-type">Tipo</label>
                <select id="home-type" className="sb-select" value={tipoOferta} onChange={(event) => setTipoOferta(event.target.value)}>
                  <option value="">Todos</option>
                  <option value="IMOVEL_COMPLETO">Imóvel completo</option>
                  <option value="VAGA_COMPARTILHADA">Vaga compartilhada</option>
                </select>
              </div>
              <div className="sb-divider" />
              <div className="sb-field">
                <label className="sb-label" htmlFor="home-price">Preço máximo</label>
                <select id="home-price" className="sb-select" value={maxPrice} onChange={(event) => setMaxPrice(event.target.value)}>
                  <option value="9999">Qualquer valor</option><option value="500">Até R$ 500</option><option value="800">Até R$ 800</option><option value="1200">Até R$ 1.200</option>
                </select>
              </div>
              <button className="sb-btn" type="button" aria-label="Buscar"><i className="fa-solid fa-magnifying-glass" /></button>
            </div>

            <div className="quick-filters">
              {QUICK_FILTERS.map(([key, icon, label]) => (
                <button key={key} type="button" className={`qf-chip ${activeFilters.includes(key) ? 'active' : ''}`} onClick={() => toggleFilter(key)}><i className={`fa-solid ${icon}`} /> {label}</button>
              ))}
            </div>
          </div>

          <div className="hero-stats">
            <div className="stat-item"><span className="stat-n">12k+</span><span className="stat-l">Estudantes</span></div><div className="stat-sep" />
            <div className="stat-item"><span className="stat-n">2.4k</span><span className="stat-l">Imóveis</span></div><div className="stat-sep" />
            <div className="stat-item"><span className="stat-n">98%</span><span className="stat-l">Satisfação</span></div>
          </div>
        </section>

        <section className="props-section" id="imoveis">
          <div className="container">
            <div className="props-header">
              <div><h2 className="section-title">Imóveis em destaque</h2><p className="result-count">{loading ? 'Carregando...' : error ? ' ' : `${totalItems} ${totalItems === 1 ? 'opção encontrada' : 'opções encontradas'}`}</p></div>
            </div>

            {error && (
              <div className="no-results"><div className="no-results-emoji">⚠️</div><h3>Não foi possível carregar</h3><p>{error}</p></div>
            )}

            {!error && (
              <div className="props-grid">
                {anuncios.map((anuncio) => (
                  <PropertyCard
                    key={anuncio.id}
                    anuncio={anuncio}
                    favorito={favoritos.has(anuncio.id)}
                    onToggleFavorito={favoritosHabilitado ? toggleFavorito : undefined}
                  />
                ))}
              </div>
            )}

            {!loading && !error && !anuncios.length && <div className="no-results"><div className="no-results-emoji">🔍</div><h3>Nenhum imóvel encontrado</h3><p>Altere os filtros para visualizar outras opções.</p></div>}
          </div>
        </section>

        <section className="hiw-section" id="como-funciona">
          <div className="container">
            <div className="hiw-header"><p className="section-eyebrow">Simples assim</p><h2 className="section-title">Como funciona</h2></div>
            <div className="hiw-grid">
              <div className="hiw-card"><div className="hiw-icon-wrap"><i className="fa-solid fa-magnifying-glass" /></div><span className="hiw-step-num">01</span><h3>Busque seu imóvel</h3><p>Use os filtros para encontrar opções alinhadas à sua rotina universitária.</p></div>
              <div className="hiw-connector" />
              <div className="hiw-card"><div className="hiw-icon-wrap"><i className="fa-solid fa-user-check" /></div><span className="hiw-step-num">02</span><h3>Complete seu perfil</h3><p>Mantenha curso, instituição e apresentação pessoal atualizados.</p></div>
              <div className="hiw-connector" />
              <div className="hiw-card"><div className="hiw-icon-wrap"><i className="fa-solid fa-comments" /></div><span className="hiw-step-num">03</span><h3>Entre em contato</h3><p>Veja os detalhes do anúncio e fale com o anunciante.</p></div>
            </div>
          </div>
        </section>
      </main>

      <footer className="footer"><div className="container"><div className="footer-bottom"><span>© 2026 EstudanteLar</span></div></div></footer>
    </>
  );
}
