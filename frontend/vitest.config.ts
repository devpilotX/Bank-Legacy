/// <reference types="vitest/config" />
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

// Tests run in a jsdom browser-like environment so we can use window, localStorage,
// and React rendering. App code is unaffected; this config is only for tests.
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    css: false,
  },
});
