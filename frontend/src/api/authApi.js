import { apiRequest } from './api';

export const authApi = {
  login: (payload) => apiRequest('/auth/login', { method: 'POST', body: payload }),
  cadastrar: (payload) => apiRequest('/auth/cadastro', { method: 'POST', body: payload }),
  esqueciSenha: (payload) => apiRequest('/auth/forgot-password', { method: 'POST', body: payload }),
  redefinirSenha: (payload) => apiRequest('/auth/reset-password', { method: 'POST', body: payload }),
};
