import { useEffect, useState } from 'react';
import { Column, Grid, InlineLoading, Tag, Tile } from '@carbon/react';
import { fetchHealth, type HealthState } from '../api/health';

// The first screen. For now it is a calm landing page that tells the engineer
// whether the backend is reachable. Real work screens come next.
export function Dashboard() {
  const [health, setHealth] = useState<HealthState>({ status: 'loading' });

  useEffect(() => {
    let active = true;
    fetchHealth().then((result) => {
      if (active) {
        setHealth(result);
      }
    });
    return () => {
      active = false;
    };
  }, []);

  return (
    <Grid>
      <Column sm={4} md={8} lg={16}>
        <h1 className="page-heading">Modernization console</h1>
        <p className="page-intro">
          This is where our team reads old COBOL, maps how the parts fit together, and
          rewrites them into Java that behaves the same. More screens are on the way. For
          now, here is whether the backend is up.
        </p>
      </Column>

      <Column sm={4} md={4} lg={6}>
        <Tile>
          <h2 className="status-card__title">Backend</h2>
          <p className="status-card__help">The API this console talks to.</p>
          <div className="status-card__row">
            {health.status === 'loading' && (
              <InlineLoading description="Checking the backend..." />
            )}
            {health.status === 'ok' && (
              <>
                <Tag type="green">Reachable</Tag>
                <span className="status-card__meta">version {health.version}</span>
              </>
            )}
            {health.status === 'down' && (
              <>
                <Tag type="red">Not reachable</Tag>
                <span className="status-card__meta">{health.detail}</span>
              </>
            )}
          </div>
        </Tile>
      </Column>
    </Grid>
  );
}
