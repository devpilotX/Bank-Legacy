// Shapes the backend gives us, and a single error type the whole app can rely on.

export type ApiUser = {
  id: number;
  email: string;
  fullName: string;
  role: string;
  status: string;
  createdAt?: string | null;
};

export type LoginResult = {
  token: string;
  expiresAt: string;
  user: ApiUser;
};

/**
 * Every failed API call throws this. It carries a plain, ready-to-show message and
 * the HTTP status (0 means we could not reach the server at all).
 */
export class ApiClientError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
  }
}
