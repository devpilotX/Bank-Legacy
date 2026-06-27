import { Tag, Tile } from '@carbon/react';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { useApi } from '../hooks/useApi';

type Health = { status: string; version: string };

/** The first screen after sign-in. It also shows whether the backend is reachable,
 * which doubles as a live example of the loading and error pattern every data screen
 * uses. */
export function DashboardPage() {
  const { user } = useAuth();
  const health = useApi<Health>(() => api.get<Health>('/health'));

  return (
    <section className="page">
      <h1 className="page__title">Welcome{user ? `, ${user.fullName}` : ''}</h1>
      <p className="page__intro">
        This is the console for reading and modernizing old COBOL. Pick a section from the left
        to begin.
      </p>

      <Tile className="card">
        <h2 className="card__title">Backend</h2>
        <p className="card__help">The API this console talks to.</p>
        {health.status === 'loading' && <LoadingState label="Checking the backend..." />}
        {health.status === 'error' && (
          <ErrorState message={health.error ?? 'We could not reach the backend.'} onRetry={health.reload} />
        )}
        {health.status === 'ok' && health.data && (
          <div className="card__row">
            <Tag type="green">Reachable</Tag>
            <span className="page__muted">version {health.data.version}</span>
          </div>
        )}
      </Tile>
    </section>
  );
}
