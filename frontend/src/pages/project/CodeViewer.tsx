import { fetchFileContent } from '../../api/files';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { useApi } from '../../hooks/useApi';

/** Shows a file's text with line numbers. Plain monospace, easy on the eyes for old
 * code. The line numbers line up so an engineer can point at a spot. */
export function CodeViewer({ fileId }: { fileId: number }) {
  const state = useApi<string>(() => fetchFileContent(fileId), [fileId]);

  if (state.status === 'loading') {
    return <LoadingState label="Loading the code..." />;
  }
  if (state.status === 'error') {
    return <ErrorState message={state.error ?? 'We could not load the code.'} onRetry={state.reload} />;
  }

  const lines = (state.data ?? '').split('\n');
  return (
    <pre className="code-view">
      <code>
        {lines.map((line, index) => (
          // The list is a static render of file lines, so the index key is fine here.
          // eslint-disable-next-line react/no-array-index-key
          <span className="code-view__line" key={index}>
            <span className="code-view__num">{index + 1}</span>
            <span className="code-view__text">{line === '' ? ' ' : line}</span>
          </span>
        ))}
      </code>
    </pre>
  );
}
