import { apiRequest } from './api';

export const denunciaApi = {
  criar: (payload) => apiRequest('/denuncias', { method: 'POST', body: payload }),
  contar: (alvoId) => apiRequest(`/denuncias/contar/${alvoId}`),
};
