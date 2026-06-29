import { useState } from 'react';
import type { ChangeEvent } from 'react';
import { Button, Checkbox, InlineNotification, Modal, Tag, TextArea, TextInput, Tile } from '@carbon/react';
import { addCase, confirmCase, draftCases, getVerification, runVerification } from '../../api/verification';
import { ApiClientError } from '../../api/types';
import type { VerificationResults, VerificationRun } from '../../api/types';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { useApi } from '../../hooks/useApi';

/**
 * Verification for one unit. A case is a set of inputs. When you press run, the engine
 * runs the original COBOL to get the expected output, runs the new Java on the same
 * input, and compares them. We show a real pass or fail, and on a failure we show the
 * diff and what each side produced.
 */
export function VerificationPanel({ unitId }: { unitId: number }) {
  const state = useApi<VerificationResults>(() => getVerification(unitId), [unitId]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Comparison settings, always visible so a result is never a silent guess.
  const [trimTrailingSpace, setTrimTrailingSpace] = useState(true);
  const [numericTolerance, setNumericTolerance] = useState('0');

  // Add-a-case modal.
  const [open, setOpen] = useState(false);
  const [name, setName] = useState('');
  const [input, setInput] = useState('');
  const [inputFiles, setInputFiles] = useState('');
  const [saving, setSaving] = useState(false);

  function fail(caught: unknown, fallback: string) {
    setError(caught instanceof ApiClientError ? caught.message : fallback);
  }

  async function run() {
    setBusy(true);
    setError(null);
    try {
      const tolerance = Number(numericTolerance);
      await runVerification(unitId, {
        trimTrailingSpace,
        numericTolerance: Number.isFinite(tolerance) ? tolerance : 0,
      });
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
    if (!name.trim()) {
      setError('Please add a name for the case.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await addCase(unitId, {
        name: name.trim(),
        input: input.trim() || undefined,
        inputFiles: inputFiles.trim() || undefined,
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
  const confirmedCount = (data?.cases ?? []).filter(
    (item) => item.verificationCase.status === 'confirmed',
  ).length;

  return (
    <div>
      <div className="map-toolbar">
        <Button
          size="sm"
          onClick={() => {
            setName('');
            setInput('');
            setInputFiles('');
            setError(null);
            setOpen(true);
          }}
        >
          Add a case
        </Button>
        <Button size="sm" kind="tertiary" onClick={draft} disabled={busy}>
          Let the AI draft input cases
        </Button>
        <Button size="sm" onClick={run} disabled={busy || confirmedCount === 0}>
          {busy ? 'Running...' : 'Run checks'}
        </Button>
      </div>

      <div className="verify-settings">
        <Checkbox
          id="trim-trailing"
          labelText="Trim trailing spaces before comparing"
          checked={trimTrailingSpace}
          onChange={(_event: unknown, data2: { checked: boolean }) =>
            setTrimTrailingSpace(data2.checked)
          }
        />
        <TextInput
          id="numeric-tolerance"
          labelText="Allowed number difference (0 means exact)"
          value={numericTolerance}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setNumericTolerance(event.target.value)}
        />
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
          <p className="page__muted">
            No cases yet. Add one, or let the AI draft some inputs for you to confirm.
          </p>
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
                  </div>

                  <p className="verify-field">
                    <span className="page__muted">Input</span>
                    <code>{item.input ? item.input : '(no input)'}</code>
                  </p>
                  {item.inputFiles ? (
                    <p className="verify-field">
                      <span className="page__muted">Input files</span>
                      <code>provided</code>
                    </p>
                  ) : null}

                  {suggested ? (
                    <Button size="sm" onClick={() => confirm(item.id)}>
                      Confirm this case
                    </Button>
                  ) : null}

                  {latestRun ? <RunView run={latestRun} /> : (
                    <p className="page__muted">Not run yet.</p>
                  )}
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
          <p className="page__muted">
            A case is just the inputs. We get the expected output by running the original COBOL.
          </p>
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
            labelText="Input on standard input (optional)"
            rows={3}
            value={input}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setInput(event.target.value)}
          />
          <TextArea
            id="case-input-files"
            labelText='Input files as JSON, for programs that read files (optional). For example {"ACCTMAST": "..."}'
            rows={3}
            value={inputFiles}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setInputFiles(event.target.value)}
          />
        </div>
      </Modal>
    </div>
  );
}

/** Shows one run's result: the pass or fail, the plain message, and the diff on a fail. */
function RunView({ run }: { run: VerificationRun }) {
  const formattingOnly = run.passed && run.differenceKind === 'formatting';
  return (
    <div className="verify-run">
      <div className="explain__item-head">
        <Tag type={run.passed ? 'green' : 'red'} size="sm">
          {run.passed ? 'Passed' : 'Failed'}
        </Tag>
        {formattingOnly ? (
          <Tag type="teal" size="sm">
            Formatting only
          </Tag>
        ) : null}
      </div>
      {run.detail ? <p>{run.detail}</p> : null}

      {!run.passed && run.diff ? (
        <>
          <p className="page__muted">What differs</p>
          <pre className="verify-output">{run.diff}</pre>
        </>
      ) : null}

      {!run.passed && (run.cobolOutput || run.javaOutput) ? (
        <details className="verify-details">
          <summary>See both outputs</summary>
          <p className="page__muted">COBOL produced</p>
          <pre className="verify-output">{run.cobolOutput ?? ''}</pre>
          <p className="page__muted">Java produced</p>
          <pre className="verify-output">{run.javaOutput ?? ''}</pre>
        </details>
      ) : null}
    </div>
  );
}
