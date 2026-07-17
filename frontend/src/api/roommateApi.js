import { apiRequest } from './api';

export const roommateApi = {
  meuPerfil: () => apiRequest('/roommates/perfil'),
  ativarPerfil: (payload) => apiRequest('/roommates/perfil', { method: 'POST', body: payload }),
  salvarPreferencias: (userId, payload) => apiRequest(`/usuarios/${userId}/preferencias-roommate`, { method: 'PUT', body: payload }),
  listarCompativeis: () => apiRequest('/roommates/compativeis'),
  solicitarMatch: (payload) => apiRequest('/roommates/match', { method: 'POST', body: payload }),
  listarPendentes: () => apiRequest('/roommates/match/pendentes'),
  responderMatch: (id, status) => apiRequest(`/roommates/match/${id}`, { method: 'PATCH', body: { status } }),
};
