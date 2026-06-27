import { api } from './client';
import type { Explanation } from './types';

export const listExplanations = (fileId: number) =>
  api.get<Explanation[]>(`/api/files/${fileId}/explanations`);

export const explainFile = (fileId: number, body: { startLine?: number; endLine?: number }) =>
  api.post<Explanation>(`/api/files/${fileId}/explanations`, body);

export const editExplanation = (id: number, content: string) =>
  api.put<Explanation>(`/api/explanations/${id}`, { content });

export const approveExplanation = (id: number) =>
  api.post<Explanation>(`/api/explanations/${id}/approve`);
