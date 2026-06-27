import { fetchFileContent } from '../../api/files';
import { CodeBlock } from '../../components/CodeBlock';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { useApi } from '../../hooks/useApi';

/** Loads a file's text and shows it with line numbers. */
export function CodeViewer({ fileId }: { fileId: number }) {
  const state = useApi<string>(() => fetchFileContent(fileId), [fileId]);

  if (state.status === 'loading') {
    return <LoadingState label="Loading the code..." />;
  }
  if (state.status === 'error') {
    return <ErrorState message={state.error ?? 'We could not load the code.'} onRetry={state.reload} />;
  }
  return <CodeBlock text={state.data ?? ''} />;
}
