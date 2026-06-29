// Shapes the backend gives us, and a single error type the whole app relies on.

export type ApiUser = {
  id: number;
  email: string;
  fullName: string;
  role: string;
  status: string;
  createdAt?: string | null;
};

export type LoginResult = {
  token: string;
  expiresAt: string;
  user: ApiUser;
};

export type Client = {
  id: number;
  name: string;
  status: string;
  notes?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type Project = {
  id: number;
  clientId: number;
  name: string;
  description?: string | null;
  status: string;
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type SourceFile = {
  id: number;
  projectId: number;
  filename: string;
  language: string;
  byteSize?: number | null;
  lineCount?: number | null;
  checksum?: string | null;
  status: string;
  uploadedBy?: number | null;
  createdAt?: string | null;
};

export type Explanation = {
  id: number;
  sourceFileId: number;
  startLine?: number | null;
  endLine?: number | null;
  content: string;
  model?: string | null;
  status: string;
  createdBy?: number | null;
  approvedBy?: number | null;
  approvedAt?: string | null;
  createdAt?: string | null;
};

export type DependencyNode = {
  id: number;
  projectId: number;
  name: string;
  kind: string;
  sourceFileId?: number | null;
};

export type DependencyEdge = {
  id: number;
  projectId: number;
  fromNodeId: number;
  toNodeId: number;
  kind: string;
  origin: string;
  status: string;
  detail?: string | null;
};

export type DependencyMap = {
  nodes: DependencyNode[];
  edges: DependencyEdge[];
};

export type WorkUnit = {
  id: number;
  projectId: number;
  sourceFileId?: number | null;
  title: string;
  originalCode: string;
  aiDraftJava?: string | null;
  humanJava?: string | null;
  status: string;
  ownerId?: number | null;
  notes?: string | null;
  createdBy?: number | null;
  approvedBy?: number | null;
  approvedAt?: string | null;
  createdAt?: string | null;
};

export type VerificationCase = {
  id: number;
  workUnitId: number;
  name: string;
  input?: string | null;
  inputFiles?: string | null;
  origin: string;
  status: string;
  createdBy?: number | null;
  createdAt?: string | null;
};

export type VerificationRun = {
  id: number;
  caseId: number;
  passed: boolean;
  outcome?: string | null;
  detail?: string | null;
  cobolOutput?: string | null;
  javaOutput?: string | null;
  diff?: string | null;
  differenceKind?: string | null;
  normalizedTrailingSpace?: boolean | null;
  numericTolerance?: number | null;
  createdAt?: string | null;
};

export type VerificationResults = {
  total: number;
  passed: number;
  failed: number;
  notRun: number;
  cases: Array<{ verificationCase: VerificationCase; latestRun?: VerificationRun | null }>;
};

/**
 * Every failed API call throws this. It carries a plain, ready-to-show message and
 * the HTTP status (0 means we could not reach the server at all).
 */
export class ApiClientError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
  }
}
