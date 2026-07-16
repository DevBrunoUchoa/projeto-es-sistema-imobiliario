import { apiRequest } from './api';
import { toQueryString } from '../utils/queryString';

export const avaliacaoApi = {
  listarPorAnuncio: (adId, params = {}) => apiRequest(`/avaliacoes/anuncio/${adId}${toQueryString(params)}`),
  listarPorLocador: (locadorId, params = {}) => apiRequest(`/avaliacoes/locador/${locadorId}${toQueryString(params)}`),
  minhas: (params = {}) => apiRequest(`/avaliacoes/minhas${toQueryString(params)}`),
  publicar: (payload) => apiRequest('/avaliacoes', { method: 'POST', body: payload }),
  responder: (id, resposta) => apiRequest(`/avaliacoes/${id}/resposta`, { method: 'PUT', body: { resposta } }),
};
