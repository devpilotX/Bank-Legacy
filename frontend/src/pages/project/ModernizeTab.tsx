import { useState } from 'react';
import type { ChangeEvent } from 'react';
import { Button, InlineNotification, Modal, TextArea, TextInput } from '@carbon/react';
import { createWorkUnit, listWorkUnits } from '../../api/workUnits';
import { ApiClientError } from '../../api/types';
import type { WorkUnit } from '../../api/types';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { StatusTag } from '../../components/StatusTag';
import { useApi } from '../../hooks/useApi';
import { useProjectContext } from './useProjectContext';
import { WorkUnitEditor } from './WorkUnitEditor';

export function ModernizeTab() {
  const { project } = useProjectContext();
  const state = useApi<WorkUnit[]>(() => listWorkUnits(project.id), [project.id]);

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [open, setOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [code, setCode] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  function openNew() {
    setTitle('');
    setCode('');
    setError(null);
    setOpen(true);
  }

  async function create() {
    if (!title.trim() || !code.trim()) {
      setError('Please add a title and the old code to rewrite.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const created = await createWorkUnit(project.id, { title: title.trim(), originalCode: code });
      setOpen(false);
      setSelectedId(created.id);
      state.reload();
    } catch (caught) {
      setError(caught instanceof ApiClientError ? caught.message : 'We could not create the unit.');
    } finally {
      setSaving(false);
    }
  }

  const units = state.data ?? [];
  const selected = units.find((unit) => unit.id === selectedId) ?? null;

  return (
    <div className="modernize">
      <div className="modernize__list">
        <div className="modernize__list-head">
          <h3 className="section-title">Units</h3>
          <Button size="sm" onClick={openNew}>
            New unit
          </Button>
        </div>

        {state.status === 'loading' && <LoadingState label="Loading units..." />}
        {state.status === 'error' && (
          <ErrorState message={state.error ?? 'We could not load the units.'} onRetry={state.reload} />
        )}
        {state.status === 'ok' && (
          units.length === 0 ? (
            <p className="page__muted">
              No units yet. Create one from a small piece of old code to start rewriting.
            </p>
          ) : (
            <ul className="unit-list">
              {units.map((unit) => (
                <li key={unit.id}>
                  <button
                    type="button"
                    className={`unit-list__item${unit.id === selectedId ? ' is-selected' : ''}`}
                    onClick={() => setSelectedId(unit.id)}
                  >
                    <span>{unit.title}</span>
                    <StatusTag status={unit.status} />
                  </button>
                </li>
              ))}
            </ul>
          )
        )}
      </div>

      <div className="modernize__work">
        {selected ? (
          <WorkUnitEditor key={selected.id} unit={selected} onChanged={state.reload} />
        ) : (
          <p className="page__muted">Pick a unit on the left, or create one.</p>
        )}
      </div>

      <Modal
        open={open}
        modalHeading="New unit of work"
        primaryButtonText={saving ? 'Creating...' : 'Create'}
        secondaryButtonText="Cancel"
        primaryButtonDisabled={saving}
        onRequestClose={() => setOpen(false)}
        onRequestSubmit={create}
      >
        <div className="form-stack">
          {error && (
            <InlineNotification kind="error" lowContrast hideCloseButton title="Could not create" subtitle={error} />
          )}
          <TextInput
            id="new-unit-title"
            labelText="Title"
            value={title}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setTitle(event.target.value)}
          />
          <TextArea
            id="new-unit-code"
            labelText="Old code to rewrite"
            value={code}
            rows={8}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setCode(event.target.value)}
          />
        </div>
      </Modal>
    </div>
  );
}
