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
      '/oauth2': { target: backend, changeOrigin: true },
      // Idem: só o callback do OAuth2, nunca a rota /login da SPA.
      '^/login/oauth2/.*': { target: backend, changeOrigin: true },
    },
  },
});
