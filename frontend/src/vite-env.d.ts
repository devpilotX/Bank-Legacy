/// <reference types="vite/client" />

interface ImportMetaEnv {
  // Where the backend API lives. Empty in dev (the Vite proxy handles it).
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
