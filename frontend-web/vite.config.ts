import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: { alias: { '@': path.resolve(__dirname, './src') } },
  server: {
    port: 5173,
    allowedHosts: ['charting-bullhorn-sanitary.ngrok-free.dev'],
    // Le backend Spring Boot tourne sur 8080. On proxifie /api pour éviter tout souci de CORS en dev.
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
