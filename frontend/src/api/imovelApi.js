import { apiRequest } from './api';

export const imovelApi = {
  criar: (payload) => apiRequest('/imoveis', { method: 'POST', body: payload }),
};
