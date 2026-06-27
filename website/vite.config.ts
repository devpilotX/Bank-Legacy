import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The public site is its own app. It runs on a different port from the internal
// tool so both can run at once during development, and it shares no code with it.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,
  },
});
