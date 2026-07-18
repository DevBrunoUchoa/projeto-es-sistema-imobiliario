import { useCallback, useEffect, useRef, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { favoritoApi } from '../api/favoritoApi';

export function useFavoritos() {
  const { user } = useAuth();
  const [favoritos, setFavoritos] = useState(new Set());
  const favoritosRef = useRef(new Set());
  const operacoesPendentes = useRef(new Set());

  const atualizarFavoritos = useCallback((proximo) => {
    favoritosRef.current = proximo;
    setFavoritos(proximo);
  }, []);

  useEffect(() => {
    let ativo = true;

    if (!user) {
      atualizarFavoritos(new Set());
      return () => { ativo = false; };
    }

    favoritoApi.listar(user.id)
      .then((lista) => {
        if (ativo) atualizarFavoritos(new Set(lista.map((favorito) => favorito.adId)));
      })
      .catch((err) => {
        console.error('Falha ao carregar favoritos existentes:', err);
      });

    return () => { ativo = false; };
  }, [user, atualizarFavoritos]);

  const toggle = useCallback(async (adId) => {
    if (!user || operacoesPendentes.current.has(adId)) return;

    const jaFavoritado = favoritosRef.current.has(adId);
    const otimista = new Set(favoritosRef.current);
    if (jaFavoritado) otimista.delete(adId); else otimista.add(adId);

    operacoesPendentes.current.add(adId);
    atualizarFavoritos(otimista);

    try {
      if (jaFavoritado) {
        await favoritoApi.remover(user.id, adId);
      } else {
        await favoritoApi.adicionar(user.id, adId);
      }
    } catch (err) {
      // Um POST repetido significa que o estado desejado já existe no servidor.
      if (!jaFavoritado && err.status === 409) return;

      const revertido = new Set(favoritosRef.current);
      if (jaFavoritado) revertido.add(adId); else revertido.delete(adId);
      atualizarFavoritos(revertido);
      console.error('Falha ao atualizar favorito:', err);
    } finally {
      operacoesPendentes.current.delete(adId);
    }
  }, [user, atualizarFavoritos]);

  return { favoritos, toggle, habilitado: Boolean(user) };
}
