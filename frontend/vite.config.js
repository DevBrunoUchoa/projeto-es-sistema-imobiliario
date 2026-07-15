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
      '/oauth2': { target: backend, changeOrigin: true },
      '/login': { target: backend, changeOrigin: true },
    },
  },
});
