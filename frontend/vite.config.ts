import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The dev server forwards backend calls to the Spring Boot app on port 8080, so
// the browser can reach the API without running into cross-origin trouble while
// we develop. In production the reverse proxy in front of us does the same job.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/health': 'http://localhost:8080',
      '/api': 'http://localhost:8080',
    },
  },
});
