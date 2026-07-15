import { apiRequest } from './api';

export const authApi = {
  login: (payload) => apiRequest('/auth/login', { method: 'POST', body: payload }),
  cadastrar: (payload) => apiRequest('/auth/cadastro', { method: 'POST', body: payload }),
};
