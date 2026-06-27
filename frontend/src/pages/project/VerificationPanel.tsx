import { useState } from 'react';
import type { ChangeEvent } from 'react';
import { Button, InlineNotification, Modal, Tag, TextArea, TextInput, Tile } from '@carbon/react';
import { addCase, confirmCase, draftCases, getVerification, runVerification } from '../../api/verification';
import { ApiClientError } from '../../api/types';
import type { VerificationResults } from '../../api/types';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { useApi } from '../../hooks/useApi';

/**
 * Verification for one unit. Cases are an input and the output the old system gave.
 * The AI can draft cases, shown as suggestions a person confirms. To run a check, the
 * engineer enters what the new Java produced; we compare it to the expected output
 * and show a clear pass or fail, with the reason on a failure.
 */
export function VerificationPanel({ unitId }: { unitId: number }) {
  const state = useApi<VerificationResults>(() => getVerification(unitId), [unitId]);
  const [actuals, setActuals] = useState<Record<number, string>>({});
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [open, setOpen] = useState(false);
  const [name, setName] = useState('');
  const [input, setInput] = useState('');
  const [expected, setExpected] = useState('');
  const [saving, setSaving] = useState(false);

  function fail(caught: unknown, fallback: string) {
    setError(caught instanceof ApiClientError ? caught.message : fallback);
  }

  async function run() {
    const results = (state.data?.cases ?? [])
      .filter((item) => item.verificationCase.status === 'confirmed')
      .map((item) => ({
        caseId: item.verificationCase.id,
        actualOutput: actuals[item.verificationCase.id] ?? '',
      }));
    if (results.length === 0) {
      setError('Confirm at least one case before running the checks.');
      return;
    }
    setBusy(true);
    setError(null);
    try {
      await runVerification(unitId, results);
      state.reload();
    } catch (caught) {
      fail(caught, 'We could not run the checks.');
    } finally {
      setBusy(false);
    }
  }

  async function confirm(caseId: number) {
    setError(null);
    try {
      await confirmCase(caseId);
      state.reload();
    } catch (caught) {
      fail(caught, 'We could not confirm the case.');
    }
  }

  async function draft() {
    setBusy(true);
    setError(null);
    try {
      await draftCases(unitId);
      state.reload();
    } catch (caught) {
      fail(caught, 'The AI could not draft cases right now.');
    } finally {
      setBusy(false);
    }
  }

  async function createCase() {
    if (!name.trim() || !expected.trim()) {
      setError('Please add a name and the expected output.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await addCase(unitId, {
        name: name.trim(),
        input: input.trim() || undefined,
        expectedOutput: expected,
      });
      setOpen(false);
      state.reload();
    } catch (caught) {
      fail(caught, 'We could not add the case.');
    } finally {
      setSaving(false);
    }
  }

  const data = state.data;

  return (
    <div>
      <div className="map-toolbar">
        <Button
          size="sm"
          onClick={() => {
            setName('');
            setInput('');
            setExpected('');
            setError(null);
            setOpen(true);
          }}
        >
          Add a case
        </Button>
        <Button size="sm" kind="tertiary" onClick={draft} disabled={busy}>
          Let the AI draft cases
        </Button>
        <Button size="sm" onClick={run} disabled={busy}>
          {busy ? 'Working...' : 'Run checks'}
        </Button>
      </div>

      {data && (
        <p className="verify-summary">
          <Tag type="green" size="sm">
            {data.passed} passed
          </Tag>
          <Tag type="red" size="sm">
            {data.failed} failed
          </Tag>
          <Tag type="cool-gray" size="sm">
            {data.notRun} not run
          </Tag>
          <span className="page__muted">{data.total} case(s)</span>
        </p>
      )}

      {error && (
        <InlineNotification
          kind="error"
          lowContrast
          onCloseButtonClick={() => setError(null)}
          title="Could not do that"
          subtitle={error}
        />
      )}

      {state.status === 'loading' && <LoadingState label="Loading cases..." />}
      {state.status === 'error' && (
        <ErrorState message={state.error ?? 'We could not load the cases.'} onRetry={state.reload} />
      )}

      {state.status === 'ok' &&
        data &&
        (data.cases.length === 0 ? (
          <p className="page__muted">No cases yet. Add one, or let the AI draft some for you to confirm.</p>
        ) : (
          <div className="explain__list">
            {data.cases.map(({ verificationCase: item, latestRun }) => {
              const suggested = item.status === 'suggested';
              return (
                <Tile key={item.id} className="verify-case">
                  <div className="explain__item-head">
                    <strong>{item.name}</strong>
                    {suggested ? (
                      <Tag type="blue" size="sm">
                        AI draft, confirm to use
                      </Tag>
                    ) : (
                      <Tag type="cool-gray" size="sm">
                        Ready
                      </Tag>
                    )}
                    {item.origin === 'ai' && !suggested ? (
                      <span className="page__muted">from the AI, confirmed by a person</span>
                    ) : null}
                    {latestRun ? (
                      <Tag type={latestRun.passed ? 'green' : 'red'} size="sm">
                        {latestRun.passed ? 'Passed' : 'Failed'}
                      </Tag>
                    ) : null}
                  </div>

                  {item.input ? (
                    <p className="verify-field">
                      <span className="page__muted">Input</span>
                      <code>{item.input}</code>
                    </p>
                  ) : null}
                  <p className="verify-field">
                    <span className="page__muted">Expected output</span>
                    <code>{item.expectedOutput}</code>
                  </p>

                  {suggested ? (
                    <Button size="sm" onClick={() => confirm(item.id)}>
                      Confirm this case
                    </Button>
                  ) : (
                    <TextArea
                      id={`actual-${item.id}`}
                      labelText="What the new Java produced"
                      rows={3}
                      value={actuals[item.id] ?? ''}
                      onChange={(event: ChangeEvent<HTMLTextAreaElement>) =>
                        setActuals((current) => ({ ...current, [item.id]: event.target.value }))
                      }
                    />
                  )}

                  {latestRun && !latestRun.passed && latestRun.detail ? (
                    <InlineNotification
                      kind="error"
                      lowContrast
                      hideCloseButton
                      title="This check failed"
                      subtitle={latestRun.detail}
                    />
                  ) : null}
                </Tile>
              );
            })}
          </div>
        ))}

      <Modal
        open={open}
        modalHeading="Add a test case"
        primaryButtonText={saving ? 'Adding...' : 'Add'}
        secondaryButtonText="Cancel"
        primaryButtonDisabled={saving}
        onRequestClose={() => setOpen(false)}
        onRequestSubmit={createCase}
      >
        <div className="form-stack">
          {error && (
            <InlineNotification
              kind="error"
              lowContrast
              hideCloseButton
              title="Could not add"
              subtitle={error}
            />
          )}
          <TextInput
            id="case-name"
            labelText="Name"
            value={name}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setName(event.target.value)}
          />
          <TextArea
            id="case-input"
            labelText="Input (optional)"
            rows={3}
            value={input}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setInput(event.target.value)}
          />
          <TextArea
            id="case-expected"
            labelText="Expected output"
            rows={3}
            value={expected}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setExpected(event.target.value)}
          />
        </div>
      </Modal>
    </div>
  );
}
