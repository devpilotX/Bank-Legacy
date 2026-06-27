import { api } from './client';
import type { VerificationCase, VerificationResults } from './types';

export const listCases = (workUnitId: number) =>
  api.get<VerificationCase[]>(`/api/work-units/${workUnitId}/verification-cases`);

export const addCase = (
  workUnitId: number,
  body: { name: string; input?: string; expectedOutput: string },
) => api.post<VerificationCase>(`/api/work-units/${workUnitId}/verification-cases`, body);

export const draftCases = (workUnitId: number) =>
  api.post<VerificationCase[]>(`/api/work-units/${workUnitId}/verification-cases/ai-draft`);

export const confirmCase = (caseId: number) =>
  api.post<VerificationCase>(`/api/verification-cases/${caseId}/confirm`);

export const runVerification = (
  workUnitId: number,
  results: Array<{ caseId: number; actualOutput: string }>,
) => api.post<VerificationResults>(`/api/work-units/${workUnitId}/verify`, { results });

export const getVerification = (workUnitId: number) =>
  api.get<VerificationResults>(`/api/work-units/${workUnitId}/verification`);
