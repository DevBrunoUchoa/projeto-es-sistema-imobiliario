import { useMemo, useState } from 'react';
import Header from '../components/Header';
import PropertyCard from '../components/PropertyCard';
import { useAuth } from '../contexts/AuthContext';

const MOCK_PROPERTIES = [
  {
    id: 1,
    title: 'Studio moderno — perto da UFCG',
    type: 'Studio',
    price: 750,
    image: 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=900&q=80',
    address: 'Bodocongó, Campina Grande — PB',
    distance: 0.7,
    walking: 8,
    gender: 'any',
    petFriendly: true,
    furnished: true,
    wifi: true,
    parking: false,
  },
  {
    id: 2,
    title: 'Quarto feminino compartilhado — UEPB',
    type: 'Compartilhado',
    price: 370,
    image: 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=900&q=80',
    address: 'Bodocongó, Campina Grande — PB',
    distance: 0.9,
    walking: 11,
    gender: 'female',
    petFriendly: false,
    furnished: true,
    wifi: true,
    parking: false,
  },
  {
    id: 3,
    title: 'Kitnet completa — próxima à Facisa',
    type: 'Kitnet',
    price: 580,
    image: 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=900&q=80',
    address: 'Bairro Universitário, Campina Grande — PB',
    distance: 1.8,
    walking: 22,
    gender: 'any',
    petFriendly: false,
    furnished: true,
    wifi: true,
    parking: false,
  },
  {
    id: 4,
    title: 'Apartamento compacto com varanda',
    type: 'Apartamento',
    price: 1100,
    image: 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=900&q=80',
    address: 'Catolé, Campina Grande — PB',
    distance: 2.4,
    walking: 29,
    gender: 'any',
    petFriendly: true,
    furnished: false,
    wifi: false,
    parking: true,
  },
  {
    id: 5,
    title: 'Quarto individual em república estudantil',
    type: 'Quarto',
    price: 490,
    image: 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=900&q=80',
    address: 'Prata, Campina Grande — PB',
    distance: 1.2,
    walking: 16,
    gender: 'male',
    petFriendly: false,
    furnished: true,
    wifi: true,
    parking: false,
  },
  {
    id: 6,
    title: 'Studio iluminado e mobiliado',
    type: 'Studio',
    price: 890,
    image: 'https://images.unsplash.com/photo-1554995207-c18c203602cb?w=900&q=80',
    address: 'Centro, Campina Grande — PB',
    distance: 2.1,
    walking: 27,
    gender: 'any',
    petFriendly: true,
    furnished: true,
    wifi: true,
    parking: true,
  },
];

export default function Home() {
  const { user } = useAuth();
  const [search, setSearch] = useState('');
  const [type, setType] = useState('');
  const [maxPrice, setMaxPrice] = useState('9999');
  const [activeFilters, setActiveFilters] = useState([]);

  const filteredProperties = useMemo(() => MOCK_PROPERTIES.filter((property) => {
    const searchText = `${property.title} ${property.address}`.toLowerCase();
    const matchesSearch = searchText.includes(search.toLowerCase());
    const matchesType = !type || property.type.toLowerCase().includes(type);
    const matchesPrice = property.price <= Number(maxPrice);
    const matchesChips = activeFilters.every((filter) => {
      if (filter === 'pet') return property.petFriendly;
      if (filter === 'furnished') return property.furnished;
      if (filter === 'wifi') return property.wifi;
      if (filter === 'parking') return property.parking;
      return true;
    });
    return matchesSearch && matchesType && matchesPrice && matchesChips;
  }), [search, type, maxPrice, activeFilters]);

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
            <div className="hero-badge"><span className="badge-pulse" /><span>Ambiente demonstrativo com dados mockados</span></div>
            <h1 className="hero-title">{user ? <>Olá, {user.nome?.split(' ')[0]}. Encontre seu <em>novo lar</em></> : <>Encontre sua <em>moradia</em><br />próxima à universidade</>}</h1>
            <p className="hero-sub">Explore a página inicial, acesse seu perfil e visualize o fluxo completo antes de conectarmos os anúncios reais.</p>

            <div className="search-bar">
              <div className="sb-field">
                <label className="sb-label" htmlFor="home-search">Local</label>
                <input id="home-search" className="sb-input" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Cidade, bairro ou universidade..." />
              </div>
              <div className="sb-divider" />
              <div className="sb-field">
                <label className="sb-label" htmlFor="home-type">Tipo</label>
                <select id="home-type" className="sb-select" value={type} onChange={(event) => setType(event.target.value)}>
                  <option value="">Todos</option><option value="studio">Studio</option><option value="kitnet">Kitnet</option><option value="compartilhado">Compartilhado</option><option value="apartamento">Apartamento</option><option value="quarto">Quarto</option>
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
              {[['pet', 'fa-paw', 'Pet friendly'], ['furnished', 'fa-couch', 'Mobiliado'], ['wifi', 'fa-wifi', 'Wi-Fi incluso'], ['parking', 'fa-square-parking', 'Estacionamento']].map(([key, icon, label]) => (
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
              <div><h2 className="section-title">Imóveis em destaque</h2><p className="result-count">{filteredProperties.length} opções mockadas encontradas</p></div>
              <span className="mock-pill"><i className="fa-solid fa-flask" /> Dados de demonstração</span>
            </div>
            <div className="props-grid">{filteredProperties.map((property) => <PropertyCard key={property.id} property={property} />)}</div>
            {!filteredProperties.length && <div className="no-results"><div className="no-results-emoji">🔍</div><h3>Nenhum imóvel encontrado</h3><p>Altere os filtros para visualizar outras opções.</p></div>}
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
              <div className="hiw-card"><div className="hiw-icon-wrap"><i className="fa-solid fa-comments" /></div><span className="hiw-step-num">03</span><h3>Entre em contato</h3><p>O contato com os anunciantes será conectado em uma próxima etapa.</p></div>
            </div>
          </div>
        </section>
      </main>

      <footer className="footer"><div className="container"><div className="footer-bottom"><span>© 2026 EstudanteLar — versão React demonstrativa.</span><span>Home mockada • Login e perfil integrados</span></div></div></footer>
    </>
  );
}
