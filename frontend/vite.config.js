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
      // Só o POST exato de criação de imóvel — a rota /imoveis/:id é da SPA
      // (página de detalhe do anúncio).
      '^/imoveis$': { target: backend, changeOrigin: true },
      '/interesses': { target: backend, changeOrigin: true },
      '/oauth2': { target: backend, changeOrigin: true },
      // Idem: só o callback do OAuth2, nunca a rota /login da SPA.
      '^/login/oauth2/.*': { target: backend, changeOrigin: true },
    },
  },
});
