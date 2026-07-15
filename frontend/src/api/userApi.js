import { apiRequest } from './api';

export const userApi = {
  buscar: (id, options = {}) => apiRequest(`/usuarios/${id}`, options),
  atualizar: (id, payload) => apiRequest(`/usuarios/${id}`, { method: 'PUT', body: payload }),
  excluir: (id) => apiRequest(`/usuarios/${id}`, { method: 'DELETE' }),
};
