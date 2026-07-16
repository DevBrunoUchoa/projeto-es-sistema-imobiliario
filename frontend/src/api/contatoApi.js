import { apiRequest } from './api';

export const contatoApi = {
  registrarInteresse: (payload) => apiRequest('/interesses', { method: 'POST', body: payload }),
};
