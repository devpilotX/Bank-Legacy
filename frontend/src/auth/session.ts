import type { ApiUser } from '../api/types';

// Where we keep the sign-in token and who is signed in.
//
// We use localStorage so a refresh keeps you signed in. The trade-off is that a
// cross-site script could read it, so this suits an internal tool behind a login.
// If we ever need a higher bar, the move is httpOnly cookies plus CSRF, or holding
// the token only in memory with a refresh token. All token access goes through here.

const TOKEN_KEY = 'modernization.token';
const USER_KEY = 'modernization.user';

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch {
    // If storage is blocked, sign-in still works for this tab.
  }
}

export function clearToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY);
  } catch {
    // Nothing to do.
  }
}

export function getStoredUser(): ApiUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as ApiUser) : null;
  } catch {
    return null;
  }
}

export function setStoredUser(user: ApiUser): void {
  try {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } catch {
    // Nothing to do.
  }
}

export function clearStoredUser(): void {
  try {
    localStorage.removeItem(USER_KEY);
  } catch {
    // Nothing to do.
  }
}
