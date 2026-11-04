import { expect, test } from '@playwright/test';
import { allowAdminSession } from './fixtures/authSetup';
import { fulfillJson } from './fixtures/httpHelpers';
import { UI_TIMEOUT_MS } from './fixtures/timeouts';
import { RsvpStatus } from '../backoffice/src/services/rsvpStatus';

const TOTAL_INVITATIONS = 25;

const rsvpStatusFor = (index: number) => {
  if (index % 3 === 0) {
    return RsvpStatus.ACCEPTED_ALL;
  }

  return index % 3 === 2 ? RsvpStatus.DECLINED_ALL : RsvpStatus.PENDING;
};

const rsvpSummaryFor = (index: number) => {
  const status = rsvpStatusFor(index);
  const attending = status === RsvpStatus.ACCEPTED_ALL ? 1 : 0;
  const declined = status === RsvpStatus.DECLINED_ALL ? 1 : 0;

  return { status, attending, declined, pending: 0, total: attending + declined };
};

const buildInvitation = (index: number) => ({
  id: `inv-${index}`,
  accessToken: `token-inv-${index}`,
  version: 1,
  creationDate: '2026-07-03T10:00:00Z',
  updateDate: '2026-07-03T10:00:00Z',
  label: index % 5 === 0 ? `Family ${index}` : `Table ${index}`,
  description: '',
  guests: [],
  guestCount: 0,
  rsvpSummary: rsvpSummaryFor(index)
});

const allInvitations = Array.from({ length: TOTAL_INVITATIONS }, (_, index) => buildInvitation(index + 1));

test.describe('Invitation list paging', () => {
  test('loads invitations 10 at a time while scrolling', async ({ page }) => {
    await allowAdminSession(page);

    const requestedSizes = new Set<string>();

    await page.route('**/api/invitations*', async (route) => {
      const params = new URL(route.request().url()).searchParams;
      const requestedPage = Number(params.get('page') ?? '0');
      const size = Number(params.get('size') ?? '10');
      requestedSizes.add(String(size));

      await fulfillJson(route, {
        items: allInvitations.slice(requestedPage * size, (requestedPage + 1) * size),
        page: requestedPage,
        size,
        totalItems: TOTAL_INVITATIONS,
        totalPages: Math.ceil(TOTAL_INVITATIONS / size)
      });
    });

    await page.goto('/invitations');

    const cards = page.locator('[data-test="invitation-card"]');

    await expect(cards).toHaveCount(10, { timeout: UI_TIMEOUT_MS });

    await page.locator('[data-test="invitation-load-more-sentinel"]').scrollIntoViewIfNeeded();
    await expect(cards).toHaveCount(20, { timeout: UI_TIMEOUT_MS });

    await page.locator('[data-test="invitation-load-more-sentinel"]').scrollIntoViewIfNeeded();
    await expect(cards).toHaveCount(TOTAL_INVITATIONS, { timeout: UI_TIMEOUT_MS });
    await expect(page.locator('[data-test="invitation-load-more-sentinel"]')).toHaveCount(0);
    await expect(page.locator('[data-test="invitation-list-summary"]')).toContainText('Showing 25 of 25 invitations');
    expect([...requestedSizes]).toEqual(['10']);
  });

  test('filters invitations by label', async ({ page }) => {
    await allowAdminSession(page);

    await page.route('**/api/invitations*', async (route) => {
      const params = new URL(route.request().url()).searchParams;
      const requestedPage = Number(params.get('page') ?? '0');
      const size = Number(params.get('size') ?? '10');
      const label = (params.get('label') ?? '').toLowerCase();
      const matching = allInvitations.filter((invitation) => invitation.label.toLowerCase().includes(label));

      await fulfillJson(route, {
        items: matching.slice(requestedPage * size, (requestedPage + 1) * size),
        page: requestedPage,
        size,
        totalItems: matching.length,
        totalPages: Math.ceil(matching.length / size)
      });
    });

    await page.goto('/invitations');

    const cards = page.locator('[data-test="invitation-card"]');

    await expect(cards).toHaveCount(10, { timeout: UI_TIMEOUT_MS });

    await page.locator('[data-test="invitation-label-filter"]').fill('family');

    await expect(cards).toHaveCount(5, { timeout: UI_TIMEOUT_MS });
    await expect(page.locator('[data-test="invitation-card-label"]').first()).toHaveText('Family 5');

    await page.locator('[data-test="invitation-label-filter"]').fill('nothing');

    await expect(page.locator('[data-test="empty-no-match"]')).toBeVisible({ timeout: UI_TIMEOUT_MS });
  });

  test('filters invitations by rsvp status from the url, the select and a cleaned url', async ({ page }) => {
    await allowAdminSession(page);

    await page.route('**/api/invitations*', async (route) => {
      const params = new URL(route.request().url()).searchParams;
      const requestedPage = Number(params.get('page') ?? '0');
      const size = Number(params.get('size') ?? '10');
      const rsvpStatus = params.get('rsvpStatus');
      const matching = rsvpStatus
        ? allInvitations.filter((invitation) => invitation.rsvpSummary.status === rsvpStatus)
        : allInvitations;

      await fulfillJson(route, {
        items: matching.slice(requestedPage * size, (requestedPage + 1) * size),
        page: requestedPage,
        size,
        totalItems: matching.length,
        totalPages: Math.ceil(matching.length / size)
      });
    });

    const acceptedCount = allInvitations.filter((invitation) => invitation.rsvpSummary.status === RsvpStatus.ACCEPTED_ALL).length;
    const pendingCount = allInvitations.filter((invitation) => invitation.rsvpSummary.status === RsvpStatus.PENDING).length;
    const cards = page.locator('[data-test="invitation-card"]');
    const statusFilter = page.locator('[data-test="invitation-rsvp-status-filter"]');

    await page.goto('/invitations?rsvpStatus=' + RsvpStatus.ACCEPTED_ALL);

    await expect(cards).toHaveCount(acceptedCount, { timeout: UI_TIMEOUT_MS });
    await expect(statusFilter).toHaveValue(RsvpStatus.ACCEPTED_ALL);

    await statusFilter.selectOption(RsvpStatus.PENDING);

    await expect(cards).toHaveCount(pendingCount, { timeout: UI_TIMEOUT_MS });
    await expect(page).toHaveURL(new RegExp('rsvpStatus=' + RsvpStatus.PENDING));

    await statusFilter.selectOption('');

    await expect(cards).toHaveCount(10, { timeout: UI_TIMEOUT_MS });
    await expect(page).toHaveURL(/\/invitations$/);
    await expect(statusFilter).toHaveValue('');
  });
});


