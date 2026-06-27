import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { fetchCurrentUser, login as loginRequest } from '../api/auth';
import { ApiClientError } from '../api/types';
import type { ApiUser } from '../api/types';
import { clearStoredUser, clearToken, getStoredUser, getToken, setStoredUser, setToken } from './session';

type AuthContextValue = {
  isAuthenticated: boolean;
  user: ApiUser | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() => getToken());
  const [user, setUser] = useState<ApiUser | null>(() => getStoredUser());

  const logout = useCallback(() => {
    clearToken();
    clearStoredUser();
    setTokenState(null);
    setUser(null);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const result = await loginRequest(email, password);
    setToken(result.token);
    setStoredUser(result.user);
    setTokenState(result.token);
    setUser(result.user);
  }, []);

  // The API client fires this when any call comes back 401, so an expired or bad
  // token signs the user out everywhere at once.
  useEffect(() => {
    const onExpired = () => {
      clearStoredUser();
      setTokenState(null);
      setUser(null);
    };
    window.addEventListener('auth:expired', onExpired);
    return () => window.removeEventListener('auth:expired', onExpired);
  }, []);

  // On load, if we have a token, confirm it still works and refresh the user. A 401
  // means it is no longer good, so we sign out.
  useEffect(() => {
    if (!getToken()) {
      return;
    }
    let active = true;
    fetchCurrentUser()
      .then((fresh) => {
        if (active) {
          setStoredUser(fresh);
          setUser(fresh);
        }
      })
      .catch((error) => {
        if (active && error instanceof ApiClientError && error.status === 401) {
          logout();
        }
      });
    return () => {
      active = false;
    };
  }, [logout]);

  const value = useMemo<AuthContextValue>(
    () => ({ isAuthenticated: token !== null, user, login, logout }),
    [token, user, login, logout],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside an AuthProvider.');
  }
  return context;
}
