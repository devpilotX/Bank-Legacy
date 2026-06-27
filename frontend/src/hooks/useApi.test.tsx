import { renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ApiClientError } from '../api/types';
import { useApi } from './useApi';

describe('useApi', () => {
  it('moves from loading to ok and holds the data', async () => {
    const { result } = renderHook(() => useApi(() => Promise.resolve('hello')));

    expect(result.current.status).toBe('loading');
    await waitFor(() => expect(result.current.status).toBe('ok'));
    expect(result.current.data).toBe('hello');
    expect(result.current.error).toBeNull();
  });

  it('moves from loading to error and shows the API message', async () => {
    const { result } = renderHook(() =>
      useApi(() => Promise.reject(new ApiClientError('Could not load that.', 500))),
    );

    await waitFor(() => expect(result.current.status).toBe('error'));
    expect(result.current.error).toBe('Could not load that.');
    expect(result.current.data).toBeNull();
  });
});
