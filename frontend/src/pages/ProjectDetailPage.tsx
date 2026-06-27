import { useState } from 'react';
import type { ChangeEvent } from 'react';
import { Select, SelectItem, Tab, TabList, Tabs } from '@carbon/react';
import { Outlet, useLocation, useNavigate, useParams } from 'react-router-dom';
import { getClient } from '../api/clients';
import { getProject, PROJECT_STAGES, updateProjectStatus } from '../api/projects';
import type { Client, Project } from '../api/types';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { useApi } from '../hooks/useApi';

const TABS = [
  { path: 'code', label: 'Code' },
  { path: 'map', label: 'Map' },
  { path: 'modernize', label: 'Modernize' },
  { path: 'verify', label: 'Verify' },
  { path: 'report', label: 'Report' },
];

type Data = { project: Project; client: Client | null };

export function ProjectDetailPage() {
  const { projectId } = useParams();
  const id = Number(projectId);
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [savingStage, setSavingStage] = useState(false);

  const state = useApi<Data>(async () => {
    const project = await getProject(id);
    let client: Client | null = null;
    try {
      client = await getClient(project.clientId);
    } catch {
      client = null;
    }
    return { project, client };
  }, [id]);

  const selectedIndex = Math.max(
    0,
    TABS.findIndex((tab) => pathname.endsWith(`/${tab.path}`)),
  );

  async function changeStage(status: string) {
    setSavingStage(true);
    try {
      await updateProjectStatus(id, status);
      state.reload();
    } finally {
      setSavingStage(false);
    }
  }

  return (
    <section className="page page--wide">
      {state.status === 'loading' && <LoadingState label="Loading the project..." />}
      {state.status === 'error' && (
        <ErrorState message={state.error ?? 'We could not load the project.'} onRetry={state.reload} />
      )}

      {state.status === 'ok' && state.data && (
        <>
          <div className="project-head">
            <div>
              <p className="page__muted">{state.data.client?.name ?? 'Unknown client'}</p>
              <h1 className="page__title">{state.data.project.name}</h1>
              {state.data.project.description ? (
                <p className="page__intro">{state.data.project.description}</p>
              ) : null}
            </div>
            <div className="project-head__stage">
              <Select
                id="project-stage"
                labelText="Stage"
                value={state.data.project.status}
                disabled={savingStage}
                onChange={(event: ChangeEvent<HTMLSelectElement>) => changeStage(event.target.value)}
              >
                {PROJECT_STAGES.map((stage) => (
                  <SelectItem key={stage} value={stage} text={stage.charAt(0).toUpperCase() + stage.slice(1)} />
                ))}
              </Select>
            </div>
          </div>

          <Tabs
            selectedIndex={selectedIndex}
            onChange={({ selectedIndex: index }) => navigate(`/projects/${id}/${TABS[index].path}`)}
          >
            <TabList aria-label="Project sections" contained>
              {TABS.map((tab) => (
                <Tab key={tab.path}>{tab.label}</Tab>
              ))}
            </TabList>
          </Tabs>

          <div className="tab-content">
            <Outlet context={{ project: state.data.project, reload: state.reload }} />
          </div>
        </>
      )}
    </section>
  );
}
