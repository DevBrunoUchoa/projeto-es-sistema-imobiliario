import { apiRequest } from './api';

export const contatoApi = {
  registrarInteresse: (payload) => apiRequest('/interesses', { method: 'POST', body: payload }),
  listarEnviados: () => apiRequest('/interesses/enviados'),
  listarRecebidos: () => apiRequest('/interesses/recebidos'),
};
