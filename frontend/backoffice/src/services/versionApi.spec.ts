import { afterEach, describe, expect, it, vi } from 'vitest';
import { FALLBACK_APP_VERSION, getAppVersion } from './versionApi';
import { backofficeOrigin, mockFetchResponse } from '../testFixtures/httpTestHelpers';

describe('versionApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('retrieves the application version from backend', async () => {
    const fetchMock = mockFetchResponse({ ok: true, body: { version: '1.3.0' } });

    await expect(getAppVersion()).resolves.toBe('1.3.0');
    expect(fetchMock).toHaveBeenCalledWith(`${backofficeOrigin}/app/version`);
  });

  it('falls back when the backend returns no version', async () => {
    mockFetchResponse({ ok: true, body: {} });

    await expect(getAppVersion()).resolves.toBe(FALLBACK_APP_VERSION);
  });

  it('fails when the backend responds with an error', async () => {
    mockFetchResponse({ ok: false, body: {} });

    await expect(getAppVersion()).rejects.toThrow('Unable to retrieve the application version.');
  });
});

