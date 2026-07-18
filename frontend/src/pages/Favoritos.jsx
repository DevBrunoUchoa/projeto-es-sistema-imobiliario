import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import PropertyCard from '../components/PropertyCard';
import { useAuth } from '../contexts/AuthContext';
import { favoritoApi } from '../api/favoritoApi';
import { anuncioApi } from '../api/anuncioApi';
import { useFavoritos } from '../hooks/useFavoritos';

export default function Favoritos() {
  const { user } = useAuth();
  const { favoritos, toggle } = useFavoritos();
  const [anuncios, setAnuncios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    favoritoApi.listar(user.id)
      .then((lista) => Promise.all(lista.map((f) => anuncioApi.detalhes(f.adId).catch(() => null))))
      .then((resultados) => setAnuncios(resultados.filter(Boolean)))
      .catch(() => setError('Não foi possível carregar seus favoritos.'))
      .finally(() => setLoading(false));
  }, [user.id]);

  const visiveis = useMemo(() => anuncios.filter((a) => favoritos.has(a.id)), [anuncios, favoritos]);

  return (
    <>
      <Header />
      <main>
        <section className="props-section" style={{ paddingTop: 'calc(var(--nav-h) + 32px)' }}>
          <div className="container">
            <div className="props-header">
              <div>
                <h2 className="section-title">Meus Favoritos</h2>
                <p className="result-count">Imóveis que você salvou para ver depois</p>
              </div>
              <Link to="/" className="btn-primary-sm" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <i className="fa-solid fa-magnifying-glass" /> Buscar mais
              </Link>
            </div>

            {loading && <p style={{ color: 'var(--text-2)' }}>Carregando...</p>}
            {error && <div className="no-results"><div className="no-results-emoji">⚠️</div><h3>Não foi possível carregar</h3><p>{error}</p></div>}

            {!loading && !error && !visiveis.length && (
              <div className="no-results">
                <div className="no-results-emoji">🤍</div>
                <h3>Nenhum favorito ainda</h3>
                <p>Clique no coração de um imóvel pra salvá-lo aqui.</p>
              </div>
            )}

            {!loading && !error && visiveis.length > 0 && (
              <div className="props-grid">
                {visiveis.map((anuncio) => (
                  <PropertyCard key={anuncio.id} anuncio={anuncio} favorito={favoritos.has(anuncio.id)} onToggleFavorito={toggle} />
                ))}
              </div>
            )}
          </div>
        </section>
      </main>
    </>
  );
}