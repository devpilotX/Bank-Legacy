import { api } from './client';
import type { DependencyEdge, DependencyMap } from './types';

export const getDependencyMap = (projectId: number) =>
  api.get<DependencyMap>(`/api/projects/${projectId}/dependency-map`);

export const buildDependencyMap = (projectId: number, includeAi: boolean) =>
  api.post<DependencyMap>(`/api/projects/${projectId}/dependency-map?ai=${includeAi}`);

export const confirmLink = (edgeId: number) =>
  api.post<DependencyEdge>(`/api/dependency-links/${edgeId}/confirm`);

export const rejectLink = (edgeId: number) =>
  api.post<DependencyEdge>(`/api/dependency-links/${edgeId}/reject`);
