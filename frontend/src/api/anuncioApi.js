import { apiRequest, buildMultipartBody } from './api';
import { toQueryString } from '../utils/queryString';

export const anuncioApi = {
  listar: (params = {}) => apiRequest(`/anuncios${toQueryString(params)}`),
  meus: () => apiRequest('/anuncios/meus'),
  detalhes: (id) => apiRequest(`/anuncios/${id}`),
  criar: (payload) => apiRequest('/anuncios', { method: 'POST', body: payload }),
  atualizar: (id, payload) => apiRequest(`/anuncios/${id}`, { method: 'PUT', body: payload }),
  atualizarStatus: (id, status) => apiRequest(`/anuncios/${id}/status`, { method: 'PATCH', body: { status } }),
  estatisticas: (id) => apiRequest(`/anuncios/${id}/estatisticas`),
  mapa: () => apiRequest('/anuncios/mapa'),
  imagens: {
    listar: (adId) => apiRequest(`/anuncios/${adId}/imagens`),
    upload: (adId, files) => apiRequest(`/anuncios/${adId}/imagens`, { method: 'POST', body: buildMultipartBody('imagens', files) }),
    remover: (adId, imageId) => apiRequest(`/anuncios/${adId}/imagens/${imageId}`, { method: 'DELETE' }),
    definirPrincipal: (adId, imageId) => apiRequest(`/anuncios/${adId}/imagens/${imageId}/principal`, { method: 'PATCH' }),
  },
};
