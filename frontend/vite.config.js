import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const backend = 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/auth': { target: backend, changeOrigin: true },
      '/usuarios': { target: backend, changeOrigin: true },
      // Só os subcaminhos da API (ex.: /roommates/perfil), nunca a rota
      // /roommates da SPA — senão o proxy engole a navegação da página.
      '^/roommates/.+': { target: backend, changeOrigin: true },
      // /anuncios não tem rota de SPA própria (a página fica em /imoveis/:id),
      // então pode ser prefixo aberto.
      '/anuncios': { target: backend, changeOrigin: true },
      // Só o POST exato de criação de imóvel — não há rota de SPA em /imoveis
      // (a página de detalhe fica em /imoveis/:id), mas um GET direto em
      // /imoveis (sem id) ainda deve cair no catch-all da SPA, não no 404 do
      // backend — daí o bypass abaixo em vez de um proxy incondicional.
      '^/imoveis$': {
        target: backend,
        changeOrigin: true,
        bypass: (req) => { if (req.method !== 'POST') return req.url; },
      },
      '/interesses': { target: backend, changeOrigin: true },
      // A SPA TEM uma rota /avaliacoes (dashboard) — diferente de /imoveis,
      // aqui o GET exato precisa mesmo ir pra SPA (bypass), só o POST
      // (publicar avaliação) vai pro backend. Subcaminhos (/avaliacoes/anuncio/…,
      // /avaliacoes/minhas, /avaliacoes/{id}/resposta) não colidem com
      // nenhuma rota de SPA, então seguem como prefixo aberto.
      '^/avaliacoes$': {
        target: backend,
        changeOrigin: true,
        bypass: (req) => { if (req.method !== 'POST') return req.url; },
      },
      '^/avaliacoes/.+': { target: backend, changeOrigin: true },
      // Sem rota de SPA em /notificacoes (é um dropdown no Header, não uma
      // página própria) — prefixo aberto é seguro.
      '/notificacoes': { target: backend, changeOrigin: true },
      // A SPA tem a rota exata /admin (dashboard), mas o backend só expõe
      // subcaminhos (/admin/usuarios, /admin/denuncias, …) — sem rota exata
      // /admin no backend, então não há colisão e não precisa de bypass.
      '^/admin/.+': { target: backend, changeOrigin: true },
      '/oauth2': { target: backend, changeOrigin: true },
      // Idem: só o callback do OAuth2, nunca a rota /login da SPA.
      '^/login/oauth2/.*': { target: backend, changeOrigin: true },
    },
  },
});
