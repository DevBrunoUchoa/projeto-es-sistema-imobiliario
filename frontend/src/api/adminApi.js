import { apiRequest } from './api';

export const adminApi = {
  listarUsuarios: () => apiRequest('/admin/usuarios'),
  listarDenuncias: () => apiRequest('/admin/denuncias'),
  verificarLocador: (id, verificado) => apiRequest(`/admin/locadores/${id}/verificar`, { method: 'PATCH', body: { verificado } }),
  relatorio: (dias) => apiRequest(`/admin/relatorios?dias=${dias}`),
  moderarDenuncia: (id, acao) => apiRequest(`/admin/denuncias/${id}/moderar`, { method: 'PATCH', body: { acao } }),
  relatorioCsvUrl: (dias) => `/admin/relatorios/csv?dias=${dias}`,
};
