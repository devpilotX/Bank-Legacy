import { api } from './client';
import type { WorkUnit } from './types';

export const WORK_UNIT_STATUSES = ['todo', 'in_progress', 'in_review', 'done'] as const;

export const listWorkUnits = (projectId: number) =>
  api.get<WorkUnit[]>(`/api/projects/${projectId}/work-units`);

export const getWorkUnit = (id: number) => api.get<WorkUnit>(`/api/work-units/${id}`);

export const createWorkUnit = (
  projectId: number,
  body: { title: string; originalCode: string; sourceFileId?: number; ownerId?: number; notes?: string },
) => api.post<WorkUnit>(`/api/projects/${projectId}/work-units`, body);

export const aiDraftTranslation = (id: number) => api.post<WorkUnit>(`/api/work-units/${id}/ai-draft`);

export const saveWorkUnitJava = (id: number, java: string) =>
  api.put<WorkUnit>(`/api/work-units/${id}/java`, { java });

export const moveWorkUnitStatus = (id: number, status: string) =>
  api.put<WorkUnit>(`/api/work-units/${id}/status`, { status });

export const approveWorkUnit = (id: number) => api.post<WorkUnit>(`/api/work-units/${id}/approve`);
