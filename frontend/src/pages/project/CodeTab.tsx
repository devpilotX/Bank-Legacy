import { useRef, useState } from 'react';
import type { ChangeEvent } from 'react';
import {
  Button,
  InlineNotification,
  ProgressBar,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@carbon/react';
import { Archive, Upload } from '@carbon/icons-react';
import { listFiles, uploadFile, uploadZip } from '../../api/files';
import { ApiClientError } from '../../api/types';
import type { SourceFile } from '../../api/types';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { StatusTag } from '../../components/StatusTag';
import { useApi } from '../../hooks/useApi';
import { CodeViewer } from './CodeViewer';
import { ExplanationPanel } from './ExplanationPanel';
import { useProjectContext } from './useProjectContext';

function formatBytes(bytes?: number | null): string {
  if (bytes == null) return '';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

export function CodeTab() {
  const { project } = useProjectContext();
  const filesState = useApi<SourceFile[]>(() => listFiles(project.id), [project.id]);

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [message, setMessage] = useState<{ kind: 'success' | 'error'; text: string } | null>(null);

  const fileInput = useRef<HTMLInputElement>(null);
  const zipInput = useRef<HTMLInputElement>(null);

  async function handlePick(event: ChangeEvent<HTMLInputElement>, isZip: boolean) {
    const chosen = event.target.files?.[0];
    event.target.value = '';
    if (!chosen) return;

    setUploading(true);
    setProgress(0);
    setMessage(null);
    try {
      if (isZip) {
        const result = await uploadZip(project.id, chosen, setProgress);
        setMessage({ kind: 'success', text: `Took in ${result.length} file(s) from ${chosen.name}.` });
      } else {
        await uploadFile(project.id, chosen, setProgress);
        setMessage({ kind: 'success', text: `Took in ${chosen.name}.` });
      }
      filesState.reload();
    } catch (caught) {
      setMessage({
        kind: 'error',
        text: caught instanceof ApiClientError ? caught.message : 'That upload did not work.',
      });
    } finally {
      setUploading(false);
    }
  }

  const selectedFile = filesState.data?.find((file) => file.id === selectedId) ?? null;

  return (
    <div>
      <div className="code-tab__bar">
        <input ref={fileInput} type="file" hidden onChange={(event) => handlePick(event, false)} />
        <input ref={zipInput} type="file" accept=".zip" hidden onChange={(event) => handlePick(event, true)} />
        <Button renderIcon={Upload} onClick={() => fileInput.current?.click()} disabled={uploading}>
          Upload a file
        </Button>
        <Button kind="tertiary" renderIcon={Archive} onClick={() => zipInput.current?.click()} disabled={uploading}>
          Upload a zip
        </Button>
        {uploading && (
          <div className="code-tab__progress">
            <ProgressBar label="Uploading" helperText={`${progress}%`} value={progress} max={100} size="small" />
          </div>
        )}
      </div>

      {message && (
        <InlineNotification
          kind={message.kind}
          lowContrast
          onCloseButtonClick={() => setMessage(null)}
          title={message.kind === 'success' ? 'Done' : 'Upload failed'}
          subtitle={message.text}
        />
      )}

      {filesState.status === 'loading' && <LoadingState label="Loading files..." />}
      {filesState.status === 'error' && (
        <ErrorState message={filesState.error ?? 'We could not load the files.'} onRetry={filesState.reload} />
      )}

      {filesState.status === 'ok' && filesState.data && (
        filesState.data.length === 0 ? (
          <p className="page__muted">No files yet. Upload some old code to begin.</p>
        ) : (
          <div className="code-tab__layout">
            <Table aria-label="Source files" size="sm">
              <TableHead>
                <TableRow>
                  <TableHeader>Name</TableHeader>
                  <TableHeader>Type</TableHeader>
                  <TableHeader>Size</TableHeader>
                  <TableHeader>Lines</TableHeader>
                  <TableHeader>Status</TableHeader>
                </TableRow>
              </TableHead>
              <TableBody>
                {filesState.data.map((file) => (
                  <TableRow
                    key={file.id}
                    className="row--clickable"
                    onClick={() => setSelectedId(file.id)}
                  >
                    <TableCell>{file.filename}</TableCell>
                    <TableCell>{file.language}</TableCell>
                    <TableCell>{formatBytes(file.byteSize)}</TableCell>
                    <TableCell>{file.lineCount ?? ''}</TableCell>
                    <TableCell>
                      <StatusTag status={file.status} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {selectedFile ? (
              <div className="code-tab__detail">
                <div>
                  <h3 className="section-title">{selectedFile.filename}</h3>
                  <CodeViewer fileId={selectedFile.id} />
                </div>
                <ExplanationPanel file={selectedFile} />
              </div>
            ) : (
              <p className="page__muted">Pick a file to read it and see its explanation.</p>
            )}
          </div>
        )
      )}
    </div>
  );
}
