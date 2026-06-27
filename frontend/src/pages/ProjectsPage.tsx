import { useMemo, useState } from 'react';
import type { ChangeEvent } from 'react';
import {
  Button,
  InlineNotification,
  Modal,
  Select,
  SelectItem,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  TextArea,
  TextInput,
} from '@carbon/react';
import { useNavigate } from 'react-router-dom';
import { listClients } from '../api/clients';
import { createProject, listProjects, PROJECT_STAGES } from '../api/projects';
import { ApiClientError } from '../api/types';
import type { Client, Project } from '../api/types';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { PageHeader } from '../components/PageHeader';
import { StatusTag } from '../components/StatusTag';
import { useApi } from '../hooks/useApi';

type Data = { projects: Project[]; clients: Client[] };

async function loadProjects(): Promise<Data> {
  const [projects, clients] = await Promise.all([listProjects(), listClients()]);
  return { projects, clients };
}

export function ProjectsPage() {
  const navigate = useNavigate();
  const state = useApi<Data>(loadProjects);

  const [clientFilter, setClientFilter] = useState('all');
  const [stageFilter, setStageFilter] = useState('all');

  const [open, setOpen] = useState(false);
  const [clientId, setClientId] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const clientName = useMemo(() => {
    const map = new Map<number, string>();
    state.data?.clients.forEach((client) => map.set(client.id, client.name));
    return map;
  }, [state.data]);

  const visible = (state.data?.projects ?? []).filter((project) => {
    const clientOk = clientFilter === 'all' || project.clientId === Number(clientFilter);
    const stageOk = stageFilter === 'all' || project.status === stageFilter;
    return clientOk && stageOk;
  });

  function openAdd() {
    setClientId(state.data?.clients[0] ? String(state.data.clients[0].id) : '');
    setName('');
    setDescription('');
    setError(null);
    setOpen(true);
  }

  async function save() {
    if (!clientId) {
      setError('Please choose a client.');
      return;
    }
    if (!name.trim()) {
      setError('Please enter a project name.');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const created = await createProject({
        clientId: Number(clientId),
        name: name.trim(),
        description: description.trim() || undefined,
      });
      setOpen(false);
      navigate(`/projects/${created.id}`);
    } catch (caught) {
      setError(caught instanceof ApiClientError ? caught.message : 'We could not start the project. Please try again.');
    } finally {
      setSaving(false);
    }
  }

  const hasClients = (state.data?.clients.length ?? 0) > 0;

  return (
    <section className="page">
      <PageHeader
        title="Projects"
        subtitle="Each bank's body of work, from intake through done."
        actions={
          <Button onClick={openAdd} disabled={!hasClients}>
            Start a project
          </Button>
        }
      />

      {state.status === 'loading' && <LoadingState label="Loading projects..." />}
      {state.status === 'error' && (
        <ErrorState message={state.error ?? 'We could not load the projects.'} onRetry={state.reload} />
      )}

      {state.status === 'ok' && state.data && (
        <>
          {!hasClients && (
            <p className="page__muted">Add a client first, then you can start a project for them.</p>
          )}

          <div className="filters">
            <Select
              id="filter-client"
              labelText="Client"
              value={clientFilter}
              onChange={(event: ChangeEvent<HTMLSelectElement>) => setClientFilter(event.target.value)}
            >
              <SelectItem value="all" text="All clients" />
              {state.data.clients.map((client) => (
                <SelectItem key={client.id} value={String(client.id)} text={client.name} />
              ))}
            </Select>
            <Select
              id="filter-stage"
              labelText="Stage"
              value={stageFilter}
              onChange={(event: ChangeEvent<HTMLSelectElement>) => setStageFilter(event.target.value)}
            >
              <SelectItem value="all" text="All stages" />
              {PROJECT_STAGES.map((stage) => (
                <SelectItem key={stage} value={stage} text={stage.charAt(0).toUpperCase() + stage.slice(1)} />
              ))}
            </Select>
          </div>

          {visible.length === 0 ? (
            <p className="page__muted">No projects match these filters.</p>
          ) : (
            <Table aria-label="Projects">
              <TableHead>
                <TableRow>
                  <TableHeader>Project</TableHeader>
                  <TableHeader>Client</TableHeader>
                  <TableHeader>Stage</TableHeader>
                </TableRow>
              </TableHead>
              <TableBody>
                {visible.map((project) => (
                  <TableRow
                    key={project.id}
                    className="row--clickable"
                    onClick={() => navigate(`/projects/${project.id}`)}
                  >
                    <TableCell>{project.name}</TableCell>
                    <TableCell>{clientName.get(project.clientId) ?? 'Unknown client'}</TableCell>
                    <TableCell>
                      <StatusTag status={project.status} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </>
      )}

      <Modal
        open={open}
        modalHeading="Start a project"
        primaryButtonText={saving ? 'Starting...' : 'Start'}
        secondaryButtonText="Cancel"
        primaryButtonDisabled={saving}
        onRequestClose={() => setOpen(false)}
        onRequestSubmit={save}
      >
        <div className="form-stack">
          {error && (
            <InlineNotification kind="error" lowContrast hideCloseButton title="Could not start" subtitle={error} />
          )}
          <Select
            id="project-client"
            labelText="Client"
            value={clientId}
            onChange={(event: ChangeEvent<HTMLSelectElement>) => setClientId(event.target.value)}
          >
            {(state.data?.clients ?? []).map((client) => (
              <SelectItem key={client.id} value={String(client.id)} text={client.name} />
            ))}
          </Select>
          <TextInput
            id="project-name"
            labelText="Project name"
            value={name}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setName(event.target.value)}
          />
          <TextArea
            id="project-description"
            labelText="Description (optional)"
            value={description}
            rows={3}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setDescription(event.target.value)}
          />
        </div>
      </Modal>
    </section>
  );
}
