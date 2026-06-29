import { api } from './client';
import type { VerificationCase, VerificationResults } from './types';

export const listCases = (workUnitId: number) =>
  api.get<VerificationCase[]>(`/api/work-units/${workUnitId}/verification-cases`);

// A case is its inputs now. The engine gets the expected output by running the COBOL.
export const addCase = (
  workUnitId: number,
  body: { name: string; input?: string; inputFiles?: string },
) => api.post<VerificationCase>(`/api/work-units/${workUnitId}/verification-cases`, body);

export const draftCases = (workUnitId: number) =>
  api.post<VerificationCase[]>(`/api/work-units/${workUnitId}/verification-cases/ai-draft`);

export const confirmCase = (caseId: number) =>
  api.post<VerificationCase>(`/api/verification-cases/${caseId}/confirm`);

// Press run and the engine compiles and runs both sides itself. No outputs are sent.
export const runVerification = (
  workUnitId: number,
  options?: { caseIds?: number[]; trimTrailingSpace?: boolean; numericTolerance?: number },
) => api.post<VerificationResults>(`/api/work-units/${workUnitId}/verify`, options ?? {});

export const getVerification = (workUnitId: number) =>
  api.get<VerificationResults>(`/api/work-units/${workUnitId}/verification`);
