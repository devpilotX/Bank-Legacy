import { useState } from 'react';
import type { ChangeEvent } from 'react';
import { Button, InlineNotification, TextArea, TextInput, Tile } from '@carbon/react';
import { approveExplanation, editExplanation, explainFile, listExplanations } from '../../api/explanations';
import { ApiClientError } from '../../api/types';
import type { Explanation, SourceFile } from '../../api/types';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { ReviewBadge } from '../../components/ReviewBadge';
import { useApi } from '../../hooks/useApi';

/**
 * The AI reads the code; a person checks it. This panel asks the AI for an
 * explanation (whole file or a line range), lists what we have, and lets the
 * engineer edit and approve. The blue or green badge always says whether something is
 * still a draft or has been approved by a person.
 */
export function ExplanationPanel({ file }: { file: SourceFile }) {
  const state = useApi<Explanation[]>(() => listExplanations(file.id), [file.id]);
  const [startLine, setStartLine] = useState('');
  const [endLine, setEndLine] = useState('');
  const [asking, setAsking] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [draftText, setDraftText] = useState('');

  function fail(caught: unknown, fallback: string) {
    setError(caught instanceof ApiClientError ? caught.message : fallback);
  }

  async function ask() {
    setAsking(true);
    setError(null);
    const body: { startLine?: number; endLine?: number } = {};
    if (startLine.trim()) body.startLine = Number(startLine);
    if (endLine.trim()) body.endLine = Number(endLine);
    try {
      await explainFile(file.id, body);
      state.reload();
    } catch (caught) {
      fail(caught, 'The AI could not explain this right now.');
    } finally {
      setAsking(false);
    }
  }

  async function approve(id: number) {
    setError(null);
    try {
      await approveExplanation(id);
      state.reload();
    } catch (caught) {
      fail(caught, 'We could not approve that.');
    }
  }

  async function saveEdit(id: number) {
    setError(null);
    try {
      await editExplanation(id, draftText);
      setEditingId(null);
      state.reload();
    } catch (caught) {
      fail(caught, 'We could not save your edit.');
    }
  }

  return (
    <div>
      <h3 className="section-title">AI explanation</h3>
      <p className="page__muted">
        Ask the AI to explain this file, or a range of lines. Read it, fix anything that is off, then
        approve it. Nothing here is trusted until a person approves it.
      </p>

      <div className="explain__ask">
        <TextInput
          id="explain-start"
          size="sm"
          labelText="From line (optional)"
          value={startLine}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setStartLine(event.target.value)}
        />
        <TextInput
          id="explain-end"
          size="sm"
          labelText="To line (optional)"
          value={endLine}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setEndLine(event.target.value)}
        />
        <Button onClick={ask} disabled={asking}>
          {asking ? 'Asking the AI...' : 'Ask the AI'}
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

      {state.status === 'loading' && <LoadingState label="Loading explanations..." />}
      {state.status === 'error' && (
        <ErrorState message={state.error ?? 'We could not load the explanations.'} onRetry={state.reload} />
      )}

      {state.status === 'ok' && state.data && (
        state.data.length === 0 ? (
          <p className="page__muted">No explanations yet. Ask the AI to write the first one.</p>
        ) : (
          <div className="explain__list">
            {state.data.map((explanation) => (
              <Tile key={explanation.id} className="explain__item">
                <div className="explain__item-head">
                  <ReviewBadge approved={explanation.status === 'approved'} />
                  <span className="page__muted">
                    {explanation.startLine
                      ? `lines ${explanation.startLine} to ${explanation.endLine ?? 'end'}`
                      : 'whole file'}
                    {explanation.model ? ` · ${explanation.model}` : ''}
                  </span>
                </div>

                {editingId === explanation.id ? (
                  <>
                    <TextArea
                      id={`explain-edit-${explanation.id}`}
                      labelText="Explanation"
                      value={draftText}
                      rows={8}
                      onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setDraftText(event.target.value)}
                    />
                    <div className="explain__actions">
                      <Button size="sm" onClick={() => saveEdit(explanation.id)}>
                        Save
                      </Button>
                      <Button size="sm" kind="ghost" onClick={() => setEditingId(null)}>
                        Cancel
                      </Button>
                    </div>
                  </>
                ) : (
                  <>
                    <p className="explain__text">{explanation.content}</p>
                    <div className="explain__actions">
                      <Button
                        size="sm"
                        kind="tertiary"
                        onClick={() => {
                          setEditingId(explanation.id);
                          setDraftText(explanation.content);
                        }}
                      >
                        Edit
                      </Button>
                      {explanation.status !== 'approved' && (
                        <Button size="sm" onClick={() => approve(explanation.id)}>
                          Approve
                        </Button>
                      )}
                    </div>
                  </>
                )}
              </Tile>
            ))}
          </div>
        )
      )}
    </div>
  );
}
