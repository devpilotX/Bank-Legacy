import { useState } from 'react';
import type { ChangeEvent } from 'react';
import { Button, InlineNotification, Select, SelectItem, TextArea } from '@carbon/react';
import {
  aiDraftTranslation,
  approveWorkUnit,
  moveWorkUnitStatus,
  saveWorkUnitJava,
  WORK_UNIT_STATUSES,
} from '../../api/workUnits';
import { ApiClientError } from '../../api/types';
import type { WorkUnit } from '../../api/types';
import { CodeBlock } from '../../components/CodeBlock';
import { ReviewBadge } from '../../components/ReviewBadge';
import { StatusTag } from '../../components/StatusTag';

function statusLabel(status: string): string {
  const spaced = status.replace('_', ' ');
  return spaced.charAt(0).toUpperCase() + spaced.slice(1);
}

/**
 * Where a piece of old code becomes Java. The old code sits on the left. The AI can
 * write a first draft, clearly marked as a starting point only. The engineer's Java
 * is on the right: they own it, save it, and approve it. Approval needs the human
 * Java to be there first.
 */
export function WorkUnitEditor({ unit, onChanged }: { unit: WorkUnit; onChanged: () => void }) {
  const [java, setJava] = useState(unit.humanJava ?? '');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const approved = Boolean(unit.approvedAt);

  async function run(action: () => Promise<unknown>, fallback: string) {
    setBusy(true);
    setError(null);
    try {
      await action();
      onChanged();
    } catch (caught) {
      setError(caught instanceof ApiClientError ? caught.message : fallback);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="unit-editor">
      <div className="unit-editor__head">
        <div>
          <h3 className="section-title">{unit.title}</h3>
          <div className="unit-editor__badges">
            <StatusTag status={unit.status} />
            <ReviewBadge approved={approved} draftText="Not approved yet" />
          </div>
        </div>
        <div className="unit-editor__controls">
          <Select
            id="unit-status"
            labelText="Status"
            value={unit.status}
            disabled={busy}
            onChange={(event: ChangeEvent<HTMLSelectElement>) =>
              run(() => moveWorkUnitStatus(unit.id, event.target.value), 'Could not change the status.')
            }
          >
            {WORK_UNIT_STATUSES.map((value) => (
              <SelectItem key={value} value={value} text={statusLabel(value)} />
            ))}
          </Select>
          <Button
            disabled={busy || !unit.humanJava}
            onClick={() => run(() => approveWorkUnit(unit.id), 'Could not approve this unit.')}
          >
            Approve
          </Button>
        </div>
      </div>

      {!unit.humanJava && <p className="page__muted">Save your Java before you can approve this unit.</p>}

      {error && (
        <InlineNotification
          kind="error"
          lowContrast
          onCloseButtonClick={() => setError(null)}
          title="Could not do that"
          subtitle={error}
        />
      )}

      <div className="unit-grid">
        <div>
          <h4 className="unit-col-title">Old code</h4>
          <CodeBlock text={unit.originalCode} />
        </div>

        <div>
          <div className="unit-ai">
            <ReviewBadge approved={false} draftText="AI draft, a starting point only" />
            <Button
              size="sm"
              disabled={busy}
              onClick={() =>
                run(() => aiDraftTranslation(unit.id), 'The AI could not draft a translation right now.')
              }
            >
              Ask the AI for a first draft
            </Button>
            {unit.aiDraftJava ? (
              <>
                <CodeBlock text={unit.aiDraftJava} />
                <Button size="sm" kind="ghost" onClick={() => setJava(unit.aiDraftJava ?? '')}>
                  Use this as my starting point
                </Button>
              </>
            ) : (
              <p className="page__muted">No AI draft yet.</p>
            )}
          </div>

          <h4 className="unit-col-title">Your Java</h4>
          <TextArea
            id="unit-java"
            labelText="Java (you write and own this)"
            value={java}
            rows={12}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setJava(event.target.value)}
          />
          <div className="explain__actions">
            <Button
              size="sm"
              disabled={busy}
              onClick={() => run(() => saveWorkUnitJava(unit.id, java), 'Could not save your Java.')}
            >
              Save my Java
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
