import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { favoritoApi } from '../api/favoritoApi';

export function useFavoritos() {
  const { user } = useAuth();
  const [favoritos, setFavoritos] = useState(new Set());

  useEffect(() => {
    if (!user) { setFavoritos(new Set()); return; }
    favoritoApi.listar(user.id)
      .then((lista) => setFavoritos(new Set(lista.map((f) => f.adId))))
      .catch(() => {});
  }, [user]);

  const toggle = useCallback((adId) => {
    if (!user) return;
    setFavoritos((current) => {
      const jaFavoritado = current.has(adId);
      const next = new Set(current);
      if (jaFavoritado) next.delete(adId); else next.add(adId);

      const chamada = jaFavoritado ? favoritoApi.remover(user.id, adId) : favoritoApi.adicionar(user.id, adId);
      chamada.catch(() => {
        setFavoritos((atual) => {
          const revert = new Set(atual);
          if (jaFavoritado) revert.add(adId); else revert.delete(adId);
          return revert;
        });
      });

      return next;
    });
  }, [user]);

  return { favoritos, toggle, habilitado: Boolean(user) };
}
