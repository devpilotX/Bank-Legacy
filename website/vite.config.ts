import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

// The public site is its own app. It runs on a different port from the internal
// tool so both can run at once in development, and it shares no code with it.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      // "@/..." points at the src folder. shadcn/ui components rely on this.
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5174,
  },
  build: {
    // The only large chunk is the 3D hero (three.js), and it is lazy-loaded, so it never
    // lands on the first paint. Raise the warning limit a little so the build output stays
    // clean and honest about that one intentional chunk.
    chunkSizeWarningLimit: 1000,
  },
});
