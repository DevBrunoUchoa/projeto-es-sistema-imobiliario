import { apiRequest, buildMultipartBody } from './api';

export const userApi = {
  buscar: (id, options = {}) => apiRequest(`/usuarios/${id}`, options),
  publico: (id, options = {}) => apiRequest(`/usuarios/${id}/publico`, options),
  atualizar: (id, payload) => apiRequest(`/usuarios/${id}`, { method: 'PUT', body: payload }),
  excluir: (id) => apiRequest(`/usuarios/${id}`, { method: 'DELETE' }),
  atualizarFoto: (id, file) => apiRequest(`/usuarios/${id}/foto`, { method: 'POST', body: buildMultipartBody('foto', file) }),
  promoverContaMista: (id) => apiRequest(`/usuarios/${id}/conta-mista`, { method: 'PATCH' }),
};
