// The backend's base URL. Empty means "same origin", which is what we want in
// development: the Vite dev server proxies /api and /health to the backend on port
// 8080. In production, set VITE_API_BASE_URL to the real API origin at build time.
export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? '';
