import { apiRequest } from './api';

function toQueryString(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    query.set(key, value);
  });
  const str = query.toString();
  return str ? `?${str}` : '';
}

export const anuncioApi = {
  listar: (params = {}) => apiRequest(`/anuncios${toQueryString(params)}`),
  detalhes: (id) => apiRequest(`/anuncios/${id}`),
  criar: (payload) => apiRequest('/anuncios', { method: 'POST', body: payload }),
  atualizar: (id, payload) => apiRequest(`/anuncios/${id}`, { method: 'PUT', body: payload }),
  atualizarStatus: (id, status) => apiRequest(`/anuncios/${id}/status`, { method: 'PATCH', body: { status } }),
  estatisticas: (id) => apiRequest(`/anuncios/${id}/estatisticas`),
  mapa: () => apiRequest('/anuncios/mapa'),
  imagens: {
    listar: (adId) => apiRequest(`/anuncios/${adId}/imagens`),
    upload: (adId, files) => {
      const formData = new FormData();
      files.forEach((file) => formData.append('imagens', file));
      return apiRequest(`/anuncios/${adId}/imagens`, { method: 'POST', body: formData });
    },
    remover: (adId, imageId) => apiRequest(`/anuncios/${adId}/imagens/${imageId}`, { method: 'DELETE' }),
    definirPrincipal: (adId, imageId) => apiRequest(`/anuncios/${adId}/imagens/${imageId}/principal`, { method: 'PATCH' }),
  },
};
