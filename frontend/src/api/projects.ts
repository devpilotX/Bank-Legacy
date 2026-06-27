import { api } from './client';
import type { Project } from './types';

export const PROJECT_STAGES = ['intake', 'mapping', 'modernizing', 'verifying', 'done'] as const;

export const listProjects = (clientId?: number) =>
  api.get<Project[]>(clientId ? `/api/projects?clientId=${clientId}` : '/api/projects');

export const getProject = (id: number) => api.get<Project>(`/api/projects/${id}`);

export const createProject = (body: { clientId: number; name: string; description?: string }) =>
  api.post<Project>('/api/projects', body);

export const updateProject = (id: number, body: { name: string; description?: string }) =>
  api.put<Project>(`/api/projects/${id}`, body);

export const updateProjectStatus = (id: number, status: string) =>
  api.put<Project>(`/api/projects/${id}/status`, { status });
