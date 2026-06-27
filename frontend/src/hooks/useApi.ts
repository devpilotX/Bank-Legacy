import { useCallback, useEffect, useState } from 'react';
import { ApiClientError } from '../api/types';

export type AsyncStatus = 'loading' | 'ok' | 'error';

export type AsyncState<T> = {
  status: AsyncStatus;
  data: T | null;
  error: string | null;
  reload: () => void;
};

/**
 * Runs a backend call and tracks loading, success, and failure for a screen. Pairs
 * with LoadingState and ErrorState so every data screen behaves the same way: show a
 * loader, then the data, or a calm message with a way to try again.
 */
export function useApi<T>(loader: () => Promise<T>): AsyncState<T> {
  const [status, setStatus] = useState<AsyncStatus>('loading');
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [attempt, setAttempt] = useState(0);

  const reload = useCallback(() => setAttempt((current) => current + 1), []);

  useEffect(() => {
    let active = true;
    setStatus('loading');
    setError(null);
    loader()
      .then((result) => {
        if (active) {
          setData(result);
          setStatus('ok');
        }
      })
      .catch((caught) => {
        if (!active) {
          return;
        }
        setError(caught instanceof ApiClientError ? caught.message : 'Something went wrong. Please try again.');
        setStatus('error');
      });
    return () => {
      active = false;
    };
    // We re-run only when reload() bumps the attempt counter.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [attempt]);

  return { status, data, error, reload };
}
