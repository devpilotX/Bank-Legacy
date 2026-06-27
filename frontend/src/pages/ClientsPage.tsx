import { useState } from 'react';
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
import { createClient, listClients, updateClient } from '../api/clients';
import { ApiClientError } from '../api/types';
import type { Client } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { PageHeader } from '../components/PageHeader';
import { StatusTag } from '../components/StatusTag';
import { useApi } from '../hooks/useApi';

const STATUSES = ['prospect', 'active', 'archived'];

export function ClientsPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'admin';
  const state = useApi<Client[]>(listClients);

  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Client | null>(null);
  const [name, setName] = useState('');
  const [status, setStatus] = useState('active');
  const [notes, setNotes] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  function openAdd() {
    setEditing(null);
    setName('');
    setStatus('active');
    setNotes('');
    setError(null);
    setOpen(true);
  }

  function openEdit(client: Client) {
    setEditing(client);
    setName(client.name);
    setStatus(client.status);
    setNotes(client.notes ?? '');
    setError(null);
    setOpen(true);
  }

  async function save() {
    if (!name.trim()) {
      setError('Please enter the bank name.');
      return;
    }
    setSaving(true);
    setError(null);
    const body = { name: name.trim(), status, notes: notes.trim() || undefined };
    try {
      if (editing) {
        await updateClient(editing.id, body);
      } else {
        await createClient(body);
      }
      setOpen(false);
      state.reload();
    } catch (caught) {
      setError(caught instanceof ApiClientError ? caught.message : 'We could not save the client. Please try again.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page">
      <PageHeader
        title="Clients"
        subtitle="The banks and credit unions we work for."
        actions={isAdmin ? <Button onClick={openAdd}>Add client</Button> : undefined}
      />

      {!isAdmin && <p className="page__muted">Only admins can add or change clients.</p>}

      {state.status === 'loading' && <LoadingState label="Loading clients..." />}
      {state.status === 'error' && (
        <ErrorState message={state.error ?? 'We could not load the clients.'} onRetry={state.reload} />
      )}

      {state.status === 'ok' && state.data && (
        state.data.length === 0 ? (
          <p className="page__muted">No clients yet.</p>
        ) : (
          <Table aria-label="Clients">
            <TableHead>
              <TableRow>
                <TableHeader>Name</TableHeader>
                <TableHeader>Status</TableHeader>
                <TableHeader>Notes</TableHeader>
                {isAdmin && <TableHeader>Actions</TableHeader>}
              </TableRow>
            </TableHead>
            <TableBody>
              {state.data.map((client) => (
                <TableRow key={client.id}>
                  <TableCell>{client.name}</TableCell>
                  <TableCell>
                    <StatusTag status={client.status} />
                  </TableCell>
                  <TableCell>{client.notes ?? ''}</TableCell>
                  {isAdmin && (
                    <TableCell>
                      <Button kind="ghost" size="sm" onClick={() => openEdit(client)}>
                        Edit
                      </Button>
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )
      )}

      <Modal
        open={open}
        modalHeading={editing ? 'Edit client' : 'Add client'}
        primaryButtonText={saving ? 'Saving...' : 'Save'}
        secondaryButtonText="Cancel"
        primaryButtonDisabled={saving}
        onRequestClose={() => setOpen(false)}
        onRequestSubmit={save}
      >
        <div className="form-stack">
          {error && (
            <InlineNotification kind="error" lowContrast hideCloseButton title="Could not save" subtitle={error} />
          )}
          <TextInput
            id="client-name"
            labelText="Bank name"
            value={name}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setName(event.target.value)}
          />
          <Select
            id="client-status"
            labelText="Status"
            value={status}
            onChange={(event: ChangeEvent<HTMLSelectElement>) => setStatus(event.target.value)}
          >
            {STATUSES.map((value) => (
              <SelectItem key={value} value={value} text={value.charAt(0).toUpperCase() + value.slice(1)} />
            ))}
          </Select>
          <TextArea
            id="client-notes"
            labelText="Notes (optional)"
            value={notes}
            rows={3}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setNotes(event.target.value)}
          />
        </div>
      </Modal>
    </section>
  );
}
