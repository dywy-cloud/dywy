import { expect, test } from '@playwright/test';
import { allowAdminSession } from './fixtures/authSetup';
import { fulfillJson, mockEmptyGuestList } from './fixtures/httpHelpers';
import { NAVIGATION_TIMEOUT_MS, UI_TIMEOUT_MS } from './fixtures/timeouts';

test.describe('Invitation edit', () => {
  test('edit invitation successfully and redirect to details', async ({ page }) => {
    await allowAdminSession(page);

    const invitationId = 'inv-1';

    // Mock API calls for editing invitation
    await page.route('**/api/invitations/inv-1', async (route) => {
      if (route.request().method() === 'GET') {
        await fulfillJson(route, {
          id: invitationId,
          accessToken: 'token-inv-1',
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-03T10:00:00Z',
          label: 'Family table',
          description: 'Main family table',
          guests: [
            {
              id: 'guest-1',
              firstName: 'Alice',
              lastName: 'Martin',
              email: 'alice@example.com'
            },
            {
              id: 'guest-2',
              firstName: 'Bob',
              lastName: 'Durand',
              email: 'bob@example.com'
            }
          ],
          guestCount: 2
        });
      } else if (route.request().method() === 'PUT') {
        await fulfillJson(route, {
          id: invitationId,
          accessToken: 'token-inv-1',
          version: 1,
          creationDate: '2026-07-03T10:00:00Z',
          updateDate: '2026-07-04T10:00:00Z',
          label: 'Family table updated',
          description: 'Updated family table',
          guests: [
            {
              id: 'guest-1',
              firstName: 'Alice',
              lastName: 'Martin',
              email: 'alice@example.com'
            }
          ],
          guestCount: 1
        });
      } else {
        await route.fallback();
      }
    });

    // Mock guests API
    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          },
          {
            id: 'guest-2',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Bob',
            lastName: 'Durand',
            email: 'bob@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 2,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await expect(page.getByRole('heading', { name: 'Edit invitation' })).toBeVisible({ timeout: UI_TIMEOUT_MS });

    const labelInput = page.locator('input[id="invitation-label"]');
    const descriptionInput = page.locator('textarea[id="invitation-description"]');

    await expect(labelInput).toHaveValue('Family table', { timeout: UI_TIMEOUT_MS });
    await expect(descriptionInput).toHaveValue('Main family table');

    await labelInput.fill('Family table updated');
    await descriptionInput.fill('Updated family table');

    // Uncheck the second guest
    const checkboxes = page.locator('input[type="checkbox"]');
    await checkboxes.nth(1).uncheck();

    await page.getByRole('button', { name: /Update invitation/ }).click();

    await expect(page).toHaveURL(`/invitations/${invitationId}`, { timeout: NAVIGATION_TIMEOUT_MS });
  });

  test('shows validation error when label is empty', async ({ page }) => {
    await allowAdminSession(page);

    const invitationId = 'inv-2';

    await page.route('**/api/invitations/inv-2', async (route) => {
      await fulfillJson(route, {
        id: invitationId,
        accessToken: 'token-inv-2',
        version: 1,
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [
          {
            id: 'guest-1',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        guestCount: 1
      });
    });

    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    const labelInput = page.locator('input[id="invitation-label"]');
    await labelInput.fill('   ', { timeout: UI_TIMEOUT_MS });

    await expect(page.getByRole('button', { name: /Update invitation/ })).toBeDisabled();

    await expect(page.locator('[data-test="invitation-validation-error"]')).toContainText('Label is required.', { timeout: UI_TIMEOUT_MS });
    await expect(page).toHaveURL(`/invitations/${invitationId}/edit`);
  });

  test('loads selectable guests using unassigned availability filter', async ({ page }) => {
    await allowAdminSession(page);

    const invitationId = 'inv-unassigned-filter';
    let guestAvailability: string | null = null;

    await page.route('**/api/invitations/inv-unassigned-filter', async (route) => {
      await fulfillJson(route, {
        id: invitationId,
        accessToken: 'token-inv-unassigned-filter',
        version: 1,
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [
          {
            id: 'guest-1',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        guestCount: 1
      });
    });

    await page.route('**/api/guests*', async (route) => {
      const requestUrl = new URL(route.request().url());
      guestAvailability = requestUrl.searchParams.get('availability');

      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await expect(page.getByRole('heading', { name: 'Edit invitation' })).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect.poll(() => guestAvailability).toBe('unassigned');
  });

  test('shows validation error when no guests are selected', async ({ page }) => {
    await allowAdminSession(page);

    const invitationId = 'inv-3';

    await page.route('**/api/invitations/inv-3', async (route) => {
      await fulfillJson(route, {
        id: invitationId,
        accessToken: 'token-inv-3',
        version: 1,
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [
          {
            id: 'guest-1',
            version: 1,
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        guestCount: 1
      });
    });

    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await page.locator('input[type="checkbox"]').first().uncheck({ timeout: UI_TIMEOUT_MS });

    await expect(page.getByRole('button', { name: /Update invitation/ })).toBeDisabled();

    await expect(page.locator('[data-test="invitation-validation-error"]')).toContainText('Select at least one guest.', { timeout: UI_TIMEOUT_MS });
    await expect(page).toHaveURL(`/invitations/${invitationId}/edit`);
  });

  test('shows error when invitation not found', async ({ page }) => {
    await allowAdminSession(page);

    const invitationId = 'inv-404';

    await page.route('**/api/invitations/inv-404', async (route) => {
      await fulfillJson(route, {}, 404);
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await expect(page.locator('[data-test="invitation-load-error"]')).toContainText('Invitation not found.', { timeout: UI_TIMEOUT_MS });
  });

  test('cancel button returns to invitation details', async ({ page }) => {
    await allowAdminSession(page);

    const invitationId = 'inv-5';

    await page.route('**/api/invitations/inv-5', async (route) => {
      await fulfillJson(route, {
        id: invitationId,
        accessToken: 'token-inv-5',
        version: 1,
        creationDate: '2026-07-03T10:00:00Z',
        updateDate: '2026-07-03T10:00:00Z',
        label: 'Family table',
        description: 'Main family table',
        guests: [
          {
            id: 'guest-1',
            version: 1,
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        guestCount: 1
      });
    });

    await page.route('**/api/guests*', async (route) => {
      await fulfillJson(route, {
        items: [
          {
            id: 'guest-1',
            version: 1,
            creationDate: '2026-06-25T00:00:00Z',
            updateDate: '2026-06-25T00:00:00Z',
            firstName: 'Alice',
            lastName: 'Martin',
            email: 'alice@example.com'
          }
        ],
        page: 0,
        size: 20,
        totalItems: 1,
        totalPages: 1
      });
    });

    await page.goto(`/invitations/${invitationId}/edit`);

    await expect(page.getByRole('heading', { name: 'Edit invitation' })).toBeVisible({ timeout: UI_TIMEOUT_MS });

    await page.getByRole('button', { name: 'Cancel' }).click();

    await expect(page).toHaveURL(`/invitations/${invitationId}`, { timeout: NAVIGATION_TIMEOUT_MS });
  });

  test.describe('delivery method', () => {
    const postalLine1Input = 'input[id="postal-address-line1"]';

    const buildInvitation = (id: string, overrides: Record<string, unknown> = {}) => ({
      id,
      accessToken: `token-${id}`,
      version: 1,
      creationDate: '2026-07-03T10:00:00Z',
      updateDate: '2026-07-03T10:00:00Z',
      label: 'Family table',
      description: 'Main family table',
      guests: [{ id: 'guest-1', firstName: 'Alice', lastName: 'Martin', email: 'alice@example.com' }],
      guestCount: 1,
      ...overrides
    });

    test('switches a posted invitation to hand delivered and hides the postal address', async ({ page }) => {
      await allowAdminSession(page);

      const invitationId = 'inv-posted';
      const invitation = buildInvitation(invitationId, {
        deliveryMethod: 'POSTED',
        postalAddress: { line1: '1 rue de la Paix', postalCode: '75002', locality: 'Paris', countryCode: 'FR' }
      });
      let updatePayload: Record<string, unknown> | null = null;

      await page.route(`**/api/invitations/${invitationId}`, async (route) => {
        if (route.request().method() === 'PUT') {
          updatePayload = route.request().postDataJSON() as Record<string, unknown>;
          await fulfillJson(route, { ...invitation, deliveryMethod: 'HAND_DELIVERED', postalAddress: null });
        } else {
          await fulfillJson(route, invitation);
        }
      });
      await mockEmptyGuestList(page);

      await page.goto(`/invitations/${invitationId}/edit`);

      const deliverySwitch = page.getByRole('switch', { name: 'Send by post' });

      await expect(deliverySwitch).toBeChecked({ timeout: UI_TIMEOUT_MS });
      await expect(page.locator(postalLine1Input)).toHaveValue('1 rue de la Paix');

      await deliverySwitch.click();

      await expect(page.locator(postalLine1Input)).toHaveCount(0);

      await page.getByRole('button', { name: /Update invitation/ }).click();

      await expect(page).toHaveURL(`/invitations/${invitationId}`, { timeout: NAVIGATION_TIMEOUT_MS });
      expect(updatePayload).toMatchObject({ deliveryMethod: 'HAND_DELIVERED' });
      expect(updatePayload).not.toHaveProperty('postalAddress');
    });

    test('requires a postal address when switching to posted', async ({ page }) => {
      await allowAdminSession(page);

      const invitationId = 'inv-hand';

      await page.route(`**/api/invitations/${invitationId}`, async (route) => {
        await fulfillJson(route, buildInvitation(invitationId, { deliveryMethod: 'HAND_DELIVERED' }));
      });
      await mockEmptyGuestList(page);

      await page.goto(`/invitations/${invitationId}/edit`);

      await expect(page.locator(postalLine1Input)).toHaveCount(0, { timeout: UI_TIMEOUT_MS });

      await page.getByRole('switch', { name: 'Send by post' }).click();

      await expect(page.locator(postalLine1Input)).toBeVisible();
      await expect(page.locator('[data-test="invitation-validation-error"]')).toContainText('Address line 1 is required.');
      await expect(page.getByRole('button', { name: /Update invitation/ })).toBeDisabled();
    });
  });
});
