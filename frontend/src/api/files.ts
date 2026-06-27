import { API_BASE_URL } from '../config';
import { getToken } from '../auth/session';
import { api } from './client';
import { ApiClientError } from './types';
import type { SourceFile } from './types';

export const listFiles = (projectId: number) =>
  api.get<SourceFile[]>(`/api/projects/${projectId}/files`);

export const getFile = (id: number) => api.get<SourceFile>(`/api/files/${id}`);

export function uploadFile(
  projectId: number,
  file: File,
  onProgress?: (percent: number) => void,
): Promise<SourceFile> {
  return upload<SourceFile>(`/api/projects/${projectId}/files`, file, onProgress);
}

export function uploadZip(
  projectId: number,
  file: File,
  onProgress?: (percent: number) => void,
): Promise<SourceFile[]> {
  return upload<SourceFile[]>(`/api/projects/${projectId}/files/zip`, file, onProgress);
}

// Uploads use XMLHttpRequest, not fetch, because it is the way to get real upload
// progress for the progress bar. We still attach the token and turn failures into
// the same ApiClientError the rest of the app uses.
function upload<T>(path: string, file: File, onProgress?: (percent: number) => void): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const request = new XMLHttpRequest();
    const form = new FormData();
    form.append('file', file);
    request.open('POST', `${API_BASE_URL}${path}`);
    const token = getToken();
    if (token) {
      request.setRequestHeader('Authorization', `Bearer ${token}`);
    }
    request.upload.onprogress = (event) => {
      if (onProgress && event.lengthComputable) {
        onProgress(Math.round((event.loaded / event.total) * 100));
      }
    };
    request.onload = () => {
      if (request.status >= 200 && request.status < 300) {
        try {
          resolve(JSON.parse(request.responseText) as T);
        } catch {
          reject(new ApiClientError('We could not read the server response.', request.status));
        }
        return;
      }
      if (request.status === 401) {
        window.dispatchEvent(new Event('auth:expired'));
        reject(new ApiClientError('Your session has ended. Please sign in again.', 401));
        return;
      }
      reject(new ApiClientError(messageFromXhr(request), request.status));
    };
    request.onerror = () =>
      reject(new ApiClientError('We could not reach the server. Check your connection and try again.', 0));
    request.send(form);
  });
}

function messageFromXhr(request: XMLHttpRequest): string {
  try {
    const body = JSON.parse(request.responseText);
    if (body && typeof body.message === 'string' && body.message.trim() !== '') {
      return body.message;
    }
  } catch {
    // Not JSON.
  }
  return 'That upload did not work. Please try again.';
}

// The file content is plain text, not JSON, so we fetch it directly.
export async function fetchFileContent(id: number): Promise<string> {
  const token = getToken();
  const response = await fetch(`${API_BASE_URL}/api/files/${id}/content`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (response.status === 401) {
    window.dispatchEvent(new Event('auth:expired'));
    throw new ApiClientError('Your session has ended. Please sign in again.', 401);
  }
  if (!response.ok) {
    throw new ApiClientError('We could not load that file.', response.status);
  }
  return response.text();
}
