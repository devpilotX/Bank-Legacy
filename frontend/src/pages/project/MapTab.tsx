import { useState } from 'react';
import { Button, Checkbox, InlineNotification, Tile } from '@carbon/react';
import { useNavigate } from 'react-router-dom';
import { buildDependencyMap, confirmLink, getDependencyMap, rejectLink } from '../../api/dependency';
import { ApiClientError } from '../../api/types';
import type { DependencyMap } from '../../api/types';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { useApi } from '../../hooks/useApi';
import { DependencyGraph } from './DependencyGraph';
import { useProjectContext } from './useProjectContext';

export function MapTab() {
  const { project } = useProjectContext();
  const navigate = useNavigate();
  const state = useApi<DependencyMap>(() => getDependencyMap(project.id), [project.id]);

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [includeAi, setIncludeAi] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function build() {
    setBusy(true);
    setError(null);
    try {
      await buildDependencyMap(project.id, includeAi);
      state.reload();
    } catch (caught) {
      setError(caught instanceof ApiClientError ? caught.message : 'We could not build the map.');
    } finally {
      setBusy(false);
    }
  }

  async function decide(edgeId: number, confirm: boolean) {
    setError(null);
    try {
      await (confirm ? confirmLink(edgeId) : rejectLink(edgeId));
      state.reload();
    } catch (caught) {
      setError(caught instanceof ApiClientError ? caught.message : 'We could not update that link.');
    }
  }

  const nodes = state.data?.nodes ?? [];
  const edges = state.data?.edges ?? [];
  const nodeById = new Map(nodes.map((node) => [node.id, node]));
  const selectedNode = selectedId != null ? (nodeById.get(selectedId) ?? null) : null;
  const suggested = edges.filter((edge) => edge.origin === 'ai' && edge.status === 'suggested');

  return (
    <div>
      <div className="map-toolbar">
        <Checkbox
          id="include-ai"
          labelText="Include AI suggestions"
          checked={includeAi}
          onChange={(_event, data: { checked: boolean }) => setIncludeAi(data.checked)}
        />
        <Button onClick={build} disabled={busy}>
          {busy ? 'Building...' : 'Build or refresh map'}
        </Button>
      </div>

      {error && (
        <InlineNotification
          kind="error"
          lowContrast
          onCloseButtonClick={() => setError(null)}
          title="Could not do that"
          subtitle={error}
        />
      )}

      {state.status === 'loading' && <LoadingState label="Loading the map..." />}
      {state.status === 'error' && (
        <ErrorState message={state.error ?? 'We could not load the map.'} onRetry={state.reload} />
      )}

      {state.status === 'ok' &&
        (nodes.length === 0 ? (
          <p className="page__muted">No map yet. Upload some code, then build the map.</p>
        ) : (
          <div className="map-layout">
            <div className="map-graph">
              <DependencyGraph nodes={nodes} edges={edges} selectedId={selectedId} onSelect={setSelectedId} />
              <div className="map-legend">
                <span className="map-legend__item">
                  <span className="legend-line legend-line--solid" /> From the code
                </span>
                <span className="map-legend__item">
                  <span className="legend-line legend-line--dashed" /> AI suggestion
                </span>
              </div>
            </div>

            <div className="map-side">
              <Tile>
                <h3 className="section-title">Selected part</h3>
                {selectedNode ? (
                  <>
                    <p>
                      <strong>{selectedNode.name}</strong>
                    </p>
                    <p className="page__muted">{selectedNode.kind}</p>
                    {selectedNode.sourceFileId ? (
                      <Button
                        size="sm"
                        kind="tertiary"
                        onClick={() =>
                          navigate(`/projects/${project.id}/code?file=${selectedNode.sourceFileId}`)
                        }
                      >
                        View in Code
                      </Button>
                    ) : (
                      <p className="page__muted">Not taken in as a file yet.</p>
                    )}
                  </>
                ) : (
                  <p className="page__muted">Click a part of the map to see it here.</p>
                )}
              </Tile>

              <Tile>
                <h3 className="section-title">AI-suggested links</h3>
                {suggested.length === 0 ? (
                  <p className="page__muted">
                    None waiting. AI suggestions show up here for you to confirm or reject.
                  </p>
                ) : (
                  <div className="explain__list">
                    {suggested.map((edge) => (
                      <div key={edge.id} className="map-suggestion">
                        <p>
                          {nodeById.get(edge.fromNodeId)?.name}{' '}
                          <span className="page__muted">{edge.kind}</span> {nodeById.get(edge.toNodeId)?.name}
                        </p>
                        {edge.detail ? <p className="page__muted">{edge.detail}</p> : null}
                        <div className="explain__actions">
                          <Button size="sm" onClick={() => decide(edge.id, true)}>
                            Confirm
                          </Button>
                          <Button size="sm" kind="danger--ghost" onClick={() => decide(edge.id, false)}>
                            Reject
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </Tile>
            </div>
          </div>
        ))}
    </div>
  );
}
