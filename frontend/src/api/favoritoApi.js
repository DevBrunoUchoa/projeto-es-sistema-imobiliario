import { apiRequest } from './api';

export const favoritoApi = {
  listar: (userId) => apiRequest(`/usuarios/${userId}/favoritos`),
  adicionar: (userId, adId) => apiRequest(`/usuarios/${userId}/favoritos?adId=${adId}`, { method: 'POST' }),
  remover: (userId, adId) => apiRequest(`/usuarios/${userId}/favoritos/${adId}`, { method: 'DELETE' }),
};
