import { afterEach, describe, expect, it, vi } from 'vitest';
import { getDashboardStatistics } from './dashboardApi';
import { createDashboardStatisticsResponse } from '../testFixtures/dashboardFixtures';
import { backofficeApiBaseUrl, clearCsrfCookie, expectCsrfHeader, getFirstRequest, mockFetchResponse, setCsrfCookie } from '../testFixtures/httpTestHelpers';

describe('dashboardApi', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    clearCsrfCookie();
  });

  it('calls backend dashboard endpoint with credentials and csrf header', async () => {
    setCsrfCookie();
    const fetchMock = mockFetchResponse({ ok: true, body: createDashboardStatisticsResponse() });

    await getDashboardStatistics();

    const [url, options] = getFirstRequest(fetchMock);

    expect(url).toBe(`${backofficeApiBaseUrl}/dashboard`);
    expect(options).toMatchObject({ method: 'GET', credentials: 'include' });
    expectCsrfHeader(options);
  });

  it('returns the dashboard statistics', async () => {
    const statistics = createDashboardStatisticsResponse({ totalInvitations: 10, totalGuests: 25 });
    mockFetchResponse({ ok: true, body: statistics });

    await expect(getDashboardStatistics()).resolves.toEqual(statistics);
  });

  it('fails when the backend responds with an error', async () => {
    mockFetchResponse({ ok: false, status: 403 });

    await expect(getDashboardStatistics()).rejects.toThrow('Unable to retrieve dashboard statistics at the moment.');
  });
});


