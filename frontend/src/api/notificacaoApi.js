import { apiRequest } from './api';
import { toQueryString } from '../utils/queryString';

export const notificacaoApi = {
  listar: (params = {}) => apiRequest(`/notificacoes${toQueryString(params)}`),
  contarNaoLidas: () => apiRequest('/notificacoes/nao-lidas'),
  marcarComoLida: (id) => apiRequest(`/notificacoes/${id}/lida`, { method: 'PATCH' }),
  marcarTodasComoLidas: () => apiRequest('/notificacoes/lidas', { method: 'PATCH' }),
};
