import { apiRequest } from './api';

export const userApi = {
  buscar: (id, options = {}) => apiRequest(`/usuarios/${id}`, options),
  publico: (id, options = {}) => apiRequest(`/usuarios/${id}/publico`, options),
  atualizar: (id, payload) => apiRequest(`/usuarios/${id}`, { method: 'PUT', body: payload }),
  foto: (id, arquivo) => {
    const form = new FormData();
    form.append('foto', arquivo);
    return apiRequest(`/usuarios/${id}/foto`, { method: 'POST', body: form });
  },
  excluir: (id) => apiRequest(`/usuarios/${id}`, { method: 'DELETE' }),
};
