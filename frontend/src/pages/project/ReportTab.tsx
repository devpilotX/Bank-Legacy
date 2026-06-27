import { Button } from '@carbon/react';
import { getDependencyMap } from '../../api/dependency';
import { listExplanations } from '../../api/explanations';
import { listFiles } from '../../api/files';
import type { DependencyEdge, DependencyMap, Explanation, SourceFile } from '../../api/types';
import { ErrorState } from '../../components/ErrorState';
import { LoadingState } from '../../components/LoadingState';
import { useApi } from '../../hooks/useApi';
import { useProjectContext } from './useProjectContext';

type ReportData = {
  files: SourceFile[];
  documented: Array<{ file: SourceFile; explanations: Explanation[] }>;
  map: DependencyMap;
};

/**
 * The mapping report, our foot in the door. It is built only from work a person has
 * approved: the approved explanations and the confirmed map. It reads in plain words,
 * because this is what the bank sees. "Export to PDF" uses the browser's print, and
 * the print styles hide the app so only the report prints.
 */
export function ReportTab() {
  const { project } = useProjectContext();
  const state = useApi<ReportData>(async () => {
    const files = await listFiles(project.id);
    const documented = await Promise.all(
      files.map(async (file) => ({
        file,
        explanations: (await listExplanations(file.id)).filter((item) => item.status === 'approved'),
      })),
    );
    const map = await getDependencyMap(project.id);
    return { files, documented, map };
  }, [project.id]);

  if (state.status === 'loading') {
    return <LoadingState label="Building the report..." />;
  }
  if (state.status === 'error') {
    return <ErrorState message={state.error ?? 'We could not build the report.'} onRetry={state.reload} />;
  }

  const data = state.data;
  if (!data) {
    return null;
  }

  const nodeById = new Map(data.map.nodes.map((node) => [node.id, node]));
  const confirmedEdges = data.map.edges.filter((edge) => edge.status === 'confirmed');
  const suggestedEdges = data.map.edges.filter((edge) => edge.origin === 'ai' && edge.status === 'suggested');

  const linksByFrom = new Map<number, DependencyEdge[]>();
  confirmedEdges.forEach((edge) => {
    const list = linksByFrom.get(edge.fromNodeId) ?? [];
    list.push(edge);
    linksByFrom.set(edge.fromNodeId, list);
  });

  const incoming = new Map<number, number>();
  confirmedEdges.forEach((edge) => incoming.set(edge.toNodeId, (incoming.get(edge.toNodeId) ?? 0) + 1));
  const heavilyUsed = [...incoming.entries()]
    .filter(([, count]) => count >= 2)
    .sort((a, b) => b[1] - a[1])
    .map(([id, count]) => ({ node: nodeById.get(id), count }))
    .filter((entry) => entry.node);

  const documentedFiles = data.documented.filter((item) => item.explanations.length > 0);
  const notDocumented = data.documented
    .filter((item) => item.explanations.length === 0)
    .map((item) => item.file);

  const nothingToFlag =
    heavilyUsed.length === 0 && notDocumented.length === 0 && suggestedEdges.length === 0;

  return (
    <div className="report">
      <div className="report__actions">
        <Button onClick={() => window.print()}>Export to PDF</Button>
        <Button kind="ghost" onClick={state.reload}>
          Refresh
        </Button>
      </div>

      <article className="report__doc">
        <header>
          <p className="report__muted">System mapping report</p>
          <h1>{project.name}</h1>
          <p className="report__muted">Prepared {new Date().toLocaleDateString()}</p>
        </header>

        <section>
          <h2>Overview</h2>
          <p>
            We took in {data.files.length} file(s) for this system. {documentedFiles.length} of them
            have a plain-English explanation that one of our engineers checked and approved. The map
            shows {data.map.nodes.length} part(s) and {confirmedEdges.length} confirmed connection(s)
            between them.
          </p>
        </section>

        <section>
          <h2>What the system does</h2>
          {documentedFiles.length === 0 ? (
            <p>
              No approved explanations yet. Once an engineer approves explanations in the Code
              section, they show up here in plain words.
            </p>
          ) : (
            documentedFiles.map((item) => (
              <div key={item.file.id} className="report__block">
                <h3>{item.file.filename}</h3>
                {item.explanations.map((explanation) => (
                  <p key={explanation.id}>{explanation.content}</p>
                ))}
              </div>
            ))
          )}
        </section>

        <section>
          <h2>How the parts connect</h2>
          {confirmedEdges.length === 0 ? (
            <p>No confirmed connections yet. Build the map in the Map section to fill this in.</p>
          ) : (
            <ul>
              {[...linksByFrom.entries()].map(([fromId, links]) => {
                const from = nodeById.get(fromId);
                if (!from) return null;
                const calls = links
                  .filter((link) => link.kind === 'calls')
                  .map((link) => nodeById.get(link.toNodeId)?.name)
                  .filter(Boolean);
                const includes = links
                  .filter((link) => link.kind === 'includes')
                  .map((link) => nodeById.get(link.toNodeId)?.name)
                  .filter(Boolean);
                return (
                  <li key={fromId}>
                    <strong>{from.name}</strong>
                    {calls.length > 0 ? ` calls ${calls.join(', ')}.` : ''}
                    {includes.length > 0 ? ` Uses ${includes.join(', ')}.` : ''}
                  </li>
                );
              })}
            </ul>
          )}
        </section>

        <section>
          <h2>Where to be careful</h2>
          <p>These are the spots that deserve extra care before anyone makes a change.</p>

          {heavilyUsed.length > 0 && (
            <div className="report__block">
              <h3>Used in many places</h3>
              <p>Changing these affects the most other parts, so test them the hardest.</p>
              <ul>
                {heavilyUsed.map((entry) => (
                  <li key={entry.node?.id}>
                    {entry.node?.name} is used by {entry.count} other part(s).
                  </li>
                ))}
              </ul>
            </div>
          )}

          {notDocumented.length > 0 && (
            <div className="report__block">
              <h3>Not documented yet</h3>
              <p>We have not finished a checked explanation for these, so treat them as unknowns for now.</p>
              <ul>
                {notDocumented.map((file) => (
                  <li key={file.id}>{file.filename}</li>
                ))}
              </ul>
            </div>
          )}

          {suggestedEdges.length > 0 && (
            <div className="report__block">
              <h3>Connections to confirm</h3>
              <p>The AI suggested these links, but a person has not confirmed them, so do not rely on them yet.</p>
              <ul>
                {suggestedEdges.map((edge) => (
                  <li key={edge.id}>
                    {nodeById.get(edge.fromNodeId)?.name} {edge.kind} {nodeById.get(edge.toNodeId)?.name}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {nothingToFlag && (
            <p>Nothing stands out right now. Keep documenting and confirming as the work goes on.</p>
          )}
        </section>
      </article>
    </div>
  );
}
