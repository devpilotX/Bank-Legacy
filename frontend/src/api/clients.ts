import { api } from './client';
import type { Client } from './types';

export type ClientInput = { name: string; status?: string; notes?: string };

export const listClients = () => api.get<Client[]>('/api/clients');
export const getClient = (id: number) => api.get<Client>(`/api/clients/${id}`);
export const createClient = (body: ClientInput) => api.post<Client>('/api/clients', body);
export const updateClient = (id: number, body: ClientInput) => api.put<Client>(`/api/clients/${id}`, body);
