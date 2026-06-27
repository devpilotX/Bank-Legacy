import { API_BASE_URL } from '../config';
import { clearToken, getToken } from '../auth/session';
import { ApiClientError } from './types';

/**
 * The one place that talks to the backend. It attaches the sign-in token, sends and
 * expects JSON, and turns every failure into one ApiClientError with a plain message
 * the screens can show as is. On a 401 it clears the token and tells the app the
 * session has ended, so the user is sent back to sign in.
 */
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');

  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (options.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
  } catch {
    throw new ApiClientError('We could not reach the server. Check your connection and try again.', 0);
  }

  if (response.status === 401) {
    clearToken();
    window.dispatchEvent(new Event('auth:expired'));
    throw new ApiClientError('Your session has ended. Please sign in again.', 401);
  }

  if (!response.ok) {
    throw new ApiClientError(await messageFrom(response), response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

async function messageFrom(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (body && typeof body.message === 'string' && body.message.trim() !== '') {
      return body.message;
    }
  } catch {
    // The error body was not JSON. Fall through to a generic message.
  }
  return 'Something went wrong. Please try again.';
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body) }),
};
