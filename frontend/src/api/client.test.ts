import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { api } from './client';
import { ApiClientError } from './types';

const TOKEN_KEY = 'modernization.token';

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

beforeEach(() => {
  localStorage.clear();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('api client', () => {
  it('attaches the token and returns parsed JSON', async () => {
    localStorage.setItem(TOKEN_KEY, 'tok-1');
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({ value: 42 }));
    vi.stubGlobal('fetch', fetchMock);

    const out = await api.get<{ value: number }>('/api/thing');

    expect(out).toEqual({ value: 42 });
    const headers = (fetchMock.mock.calls[0][1] as RequestInit).headers as Headers;
    expect(headers.get('Authorization')).toBe('Bearer tok-1');
    expect(headers.get('Accept')).toBe('application/json');
  });

  it('does not attach Authorization when there is no token', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({}));
    vi.stubGlobal('fetch', fetchMock);

    await api.get('/api/thing');

    const headers = (fetchMock.mock.calls[0][1] as RequestInit).headers as Headers;
    expect(headers.has('Authorization')).toBe(false);
  });

  it('serializes the body and sets Content-Type on POST', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({ id: 1 }));
    vi.stubGlobal('fetch', fetchMock);

    await api.post('/api/clients', { name: 'Acme' });

    const init = fetchMock.mock.calls[0][1] as RequestInit;
    expect(init.method).toBe('POST');
    expect(init.body).toBe(JSON.stringify({ name: 'Acme' }));
    expect((init.headers as Headers).get('Content-Type')).toBe('application/json');
  });

  it('on 401 clears the token, signals the app, and throws', async () => {
    localStorage.setItem(TOKEN_KEY, 'tok-1');
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('', { status: 401 })));
    let signalled = false;
    const onExpired = () => {
      signalled = true;
    };
    window.addEventListener('auth:expired', onExpired);

    await expect(api.get('/api/me')).rejects.toBeInstanceOf(ApiClientError);

    window.removeEventListener('auth:expired', onExpired);
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
    expect(signalled).toBe(true);
  });

  it('uses the backend message on a normal error', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ message: 'Name is required.' }, 400)));

    await expect(api.post('/api/clients', {})).rejects.toMatchObject({
      status: 400,
      message: 'Name is required.',
    });
  });

  it('reports a reach-the-server error when fetch fails', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('network down')));

    await expect(api.get('/api/thing')).rejects.toMatchObject({ status: 0 });
  });
});
