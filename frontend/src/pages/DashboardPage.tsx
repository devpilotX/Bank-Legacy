import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow, Tile } from '@carbon/react';
import { useNavigate } from 'react-router-dom';
import { listClients } from '../api/clients';
import { listFiles } from '../api/files';
import { listProjects } from '../api/projects';
import type { Project } from '../api/types';
import { listWorkUnits } from '../api/workUnits';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { PageHeader } from '../components/PageHeader';
import { StatusTag } from '../components/StatusTag';
import { useApi } from '../hooks/useApi';

type Row = {
  project: Project;
  clientName: string;
  files: number;
  units: number;
  modernized: number;
};

type DashboardData = {
  rows: Row[];
  totals: { projects: number; files: number; modernized: number };
};

async function loadDashboard(): Promise<DashboardData> {
  const [projects, clients] = await Promise.all([listProjects(), listClients()]);
  const clientName = new Map(clients.map((client) => [client.id, client.name]));

  const rows = await Promise.all(
    projects.map(async (project): Promise<Row> => {
      const [files, units] = await Promise.all([listFiles(project.id), listWorkUnits(project.id)]);
      return {
        project,
        clientName: clientName.get(project.clientId) ?? 'Unknown client',
        files: files.length,
        units: units.length,
        modernized: units.filter((unit) => unit.status === 'done').length,
      };
    }),
  );

  return {
    rows,
    totals: {
      projects: projects.length,
      files: rows.reduce((sum, row) => sum + row.files, 0),
      modernized: rows.reduce((sum, row) => sum + row.modernized, 0),
    },
  };
}

export function DashboardPage() {
  const navigate = useNavigate();
  const state = useApi<DashboardData>(loadDashboard);

  return (
    <section className="page">
      <PageHeader title="Dashboard" subtitle="Your projects and where each one stands." />

      {state.status === 'loading' && <LoadingState label="Loading your projects..." />}
      {state.status === 'error' && (
        <ErrorState message={state.error ?? 'We could not load your projects.'} onRetry={state.reload} />
      )}

      {state.status === 'ok' && state.data && (
        <>
          <div className="tiles">
            <Tile className="tile-metric">
              <p className="tile-metric__num">{state.data.totals.projects}</p>
              <p className="tile-metric__label">Projects</p>
            </Tile>
            <Tile className="tile-metric">
              <p className="tile-metric__num">{state.data.totals.files}</p>
              <p className="tile-metric__label">Files taken in</p>
            </Tile>
            <Tile className="tile-metric">
              <p className="tile-metric__num">{state.data.totals.modernized}</p>
              <p className="tile-metric__label">Units modernized</p>
            </Tile>
          </div>

          {state.data.rows.length === 0 ? (
            <p className="page__muted">No projects yet. Add a client and start a project to see it here.</p>
          ) : (
            <Table aria-label="Your projects" size="lg">
              <TableHead>
                <TableRow>
                  <TableHeader>Project</TableHeader>
                  <TableHeader>Client</TableHeader>
                  <TableHeader>Stage</TableHeader>
                  <TableHeader>Files</TableHeader>
                  <TableHeader>Units</TableHeader>
                  <TableHeader>Modernized</TableHeader>
                </TableRow>
              </TableHead>
              <TableBody>
                {state.data.rows.map((row) => (
                  <TableRow
                    key={row.project.id}
                    className="row--clickable"
                    onClick={() => navigate(`/projects/${row.project.id}`)}
                  >
                    <TableCell>{row.project.name}</TableCell>
                    <TableCell>{row.clientName}</TableCell>
                    <TableCell>
                      <StatusTag status={row.project.status} />
                    </TableCell>
                    <TableCell>{row.files}</TableCell>
                    <TableCell>{row.units}</TableCell>
                    <TableCell>{row.modernized}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </>
      )}
    </section>
  );
}
