import { expect, test } from '@playwright/test';
import { allowAdminSession } from './fixtures/authSetup';
import { fulfillJson } from './fixtures/httpHelpers';
import { UI_TIMEOUT_MS } from './fixtures/timeouts';

const TOTAL_PAGES = 2;

const buildInvitation = (index: number) => ({
  id: `inv-${index}`,
  accessToken: `token-inv-${index}`,
  version: 1,
  creationDate: '2026-07-03T10:00:00Z',
  updateDate: '2026-07-03T10:00:00Z',
  label: `Table ${index}`,
  description: `Table ${index}`,
  guests: [],
  guestCount: 0,
  rsvpSummary: { status: 'PENDING', attending: 0, declined: 0, pending: 0, total: 0 }
});

test.describe('Print stickers', () => {
  test('downloads a PDF with the invitations of every API page', async ({ page }) => {
    await allowAdminSession(page);

    const requestedPages: number[] = [];

    await page.route('**/api/invitations*', async (route) => {
      const requestedPage = Number(new URL(route.request().url()).searchParams.get('page') ?? '0');
      requestedPages.push(requestedPage);

      await fulfillJson(route, {
        items: [buildInvitation(requestedPage * 2 + 1), buildInvitation(requestedPage * 2 + 2)],
        page: requestedPage,
        size: 2,
        totalItems: TOTAL_PAGES * 2,
        totalPages: TOTAL_PAGES
      });
    });

    await page.goto('/invitations');

    await expect(page.locator('[data-test="invitation-card"]')).toHaveCount(4, { timeout: UI_TIMEOUT_MS });

    requestedPages.length = 0;

    await page.locator('[data-test="print-stickers-cta"]').click();
    await page.locator('[data-test="color-swatch-#166534"]').click();

    const downloadPromise = page.waitForEvent('download');
    await page.locator('[data-test="print-stickers-confirm"]').click();
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toBe('invitation-stickers.pdf');
    expect([...new Set(requestedPages)].sort()).toEqual([0, 1]);
  });

  test('prints only the selected invitations and offers to select every invitation', async ({ page }) => {
    await allowAdminSession(page);

    await page.route('**/api/invitations*', async (route) => {
      const requestedPage = Number(new URL(route.request().url()).searchParams.get('page') ?? '0');

      await fulfillJson(route, {
        items: [buildInvitation(requestedPage * 2 + 1), buildInvitation(requestedPage * 2 + 2)],
        page: requestedPage,
        size: 2,
        totalItems: TOTAL_PAGES * 2,
        totalPages: TOTAL_PAGES
      });
    });

    await page.goto('/invitations');

    await expect(page.locator('[data-test="invitation-card"]')).toHaveCount(4, { timeout: UI_TIMEOUT_MS });

    await page.locator('[data-test="print-stickers-cta"]').click();
    await expect(page.locator('[data-test="print-stickers-scope"]')).toHaveText('All 4 invitations will be printed.');
    await page.locator('[data-test="print-stickers-cancel"]').click();

    await page.locator('[data-test="invitation-select"]').first().check();
    await expect(page.locator('[data-test="invitation-selection-count"]')).toHaveText('1 selected');

    await page.locator('[data-test="select-all-invitations"]').click();
    await expect(page.locator('[data-test="invitation-selection-count"]')).toHaveText('All 4 selected');

    await page.locator('[data-test="invitation-select"]').nth(1).uncheck();
    await expect(page.locator('[data-test="invitation-selection-count"]')).toHaveText('3 selected');

    await page.locator('[data-test="print-stickers-cta"]').click();
    await expect(page.locator('[data-test="print-stickers-confirm"]')).toHaveText('Print 3 stickers');

    const downloadPromise = page.waitForEvent('download');
    await page.locator('[data-test="print-stickers-confirm"]').click();
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toBe('invitation-stickers.pdf');
  });
});


