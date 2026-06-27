import { api } from './client';
import type { ApiUser, LoginResult } from './types';

export function login(email: string, password: string): Promise<LoginResult> {
  return api.post<LoginResult>('/api/auth/login', { email, password });
}

export function fetchCurrentUser(): Promise<ApiUser> {
  return api.get<ApiUser>('/api/auth/me');
}
