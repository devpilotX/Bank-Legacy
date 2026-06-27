import { useEffect, useState } from 'react';
import type { ChangeEvent } from 'react';
import { Select, SelectItem } from '@carbon/react';
import { listWorkUnits } from '../../api/workUnits';
import type { WorkUnit } from '../../api/types';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { useApi } from '../../hooks/useApi';
import { useProjectContext } from './useProjectContext';
import { VerificationPanel } from './VerificationPanel';

export function VerifyTab() {
  const { project } = useProjectContext();
  const state = useApi<WorkUnit[]>(() => listWorkUnits(project.id), [project.id]);
  const [unitId, setUnitId] = useState('');

  useEffect(() => {
    if (state.data && state.data.length > 0 && !unitId) {
      setUnitId(String(state.data[0].id));
    }
  }, [state.data, unitId]);

  return (
    <div>
      {state.status === 'loading' && <LoadingState label="Loading units..." />}
      {state.status === 'error' && (
        <ErrorState message={state.error ?? 'We could not load the units.'} onRetry={state.reload} />
      )}

      {state.status === 'ok' &&
        (state.data && state.data.length > 0 ? (
          <>
            <div className="filters">
              <Select
                id="verify-unit"
                labelText="Unit"
                value={unitId}
                onChange={(event: ChangeEvent<HTMLSelectElement>) => setUnitId(event.target.value)}
              >
                {state.data.map((unit) => (
                  <SelectItem key={unit.id} value={String(unit.id)} text={unit.title} />
                ))}
              </Select>
            </div>
            {unitId && <VerificationPanel key={unitId} unitId={Number(unitId)} />}
          </>
        ) : (
          <p className="page__muted">No units yet. Create a unit in Modernize first, then verify it here.</p>
        ))}
    </div>
  );
}
