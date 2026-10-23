import { expect, test } from '@playwright/test';
import { fulfillJson } from './fixtures/httpHelpers';
import { UI_TIMEOUT_MS } from './fixtures/timeouts';

const PUBLIC_BASE_URL = 'http://127.0.0.1:4174';
const VALID_TOKEN = '957f8251-f50b-48ca-9cd1-998e71ffd2e9';

// The invitation GET is not a credentialed request, so a wildcard origin is enough.
const PUBLIC_CORS = { 'access-control-allow-origin': '*' };

// The magic-link request and the `/me` session lookup are sent with
// `credentials: 'include'`, so the browser requires the exact origin to be
// echoed back together with `allow-credentials`.
const CREDENTIALED_CORS = {
  'access-control-allow-origin': PUBLIC_BASE_URL,
  'access-control-allow-credentials': 'true',
};

const PREFLIGHT_CORS = {
  ...CREDENTIALED_CORS,
  'access-control-allow-methods': 'GET,POST,OPTIONS',
  'access-control-allow-headers': 'content-type,x-xsrf-token',
};

const invitationBody = {
  label: 'Famille Martin',
  guestCount: 2,
  guests: [
    { id: 'guest-1', firstName: 'Alice', lastName: 'Martin' },
    { id: 'guest-2', firstName: 'Bob', lastName: 'Martin' },
  ],
};

const mockInvitation = async (page: import('@playwright/test').Page) => {
  await page.route('**/api/guest-access/invitations/*', async (route) => {
    await fulfillJson(route, invitationBody, 200, PUBLIC_CORS);
  });
};

test.describe('Magic-link UX flow', () => {
  test('completes the full flow: request link then unlock the secured area', async ({ page }) => {
    await mockInvitation(page);
    await page.route('**/magic-link-requests', async (route) => {
      await fulfillJson(
        route,
        { message: 'If the request is valid, you will receive an email shortly.' },
        202,
        CREDENTIALED_CORS,
      );
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/${VALID_TOKEN}`);
    await expect(page.getByRole('button', { name: /Alice Martin/ })).toBeVisible({ timeout: UI_TIMEOUT_MS });

    await page.getByRole('button', { name: /Alice Martin/ }).click();

    await expect(page.getByText(/tu recevras un email|you will receive an email/i)).toBeVisible();

    // The magic link in the email points to the backend, which verifies the token,
    // sets the `guest_session` cookie and redirects to the secured area. That
    // top-level navigation cannot be exercised here, so we land on the secured
    // area directly and let the `/me` lookup confirm the (now valid) session.
    await page.route('**/api/guest-access/secured/me', async (route) => {
      await fulfillJson(
        route,
        { guestId: 'guest-1', invitationId: 'invitation-1', firstName: 'Alice', lastName: 'Martin', language: 'FR' },
        200,
        CREDENTIALED_CORS,
      );
    });

    // The RSVP form fetches the current answer on mount; the guest has not
    // responded yet, so the endpoint replies `204 No Content`.
    await page.route('**/api/guest-access/secured/rsvp', async (route) => {
      await route.fulfill({ status: 204, headers: CREDENTIALED_CORS });
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/secured-area`);

    await expect(page.getByText(/Bonjour Alice|Hello Alice/)).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect(page.getByText(/célébrer notre amour|celebrate our love/i)).toBeVisible();
  });

  test('requests the magic link in GROUP mode after choosing to answer for everyone', async ({ page }) => {
    await mockInvitation(page);
    await page.route('**/magic-link-requests', async (route) => {
      await fulfillJson(route, {}, 202, CREDENTIALED_CORS);
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/${VALID_TOKEN}`);
    await expect(page.getByRole('button', { name: /Alice Martin/ })).toBeVisible({ timeout: UI_TIMEOUT_MS });

    await page.getByRole('button', { name: /Répondre pour tout le monde|Answer for everyone/ }).click();
    const requestPromise = page.waitForRequest('**/magic-link-requests');
    await page.getByRole('button', { name: /Alice Martin/ }).click();

    expect((await requestPromise).postDataJSON()).toEqual({ mode: 'GROUP' });
    await expect(page.getByText(/répondre pour tout le groupe|answer for the whole party/i)).toBeVisible();
  });

  test('answers for everyone through the group magic link', async ({ page }) => {
    await mockInvitation(page);
    await page.route('**/magic-link-requests', async (route) => {
      await fulfillJson(route, {}, 202, CREDENTIALED_CORS);
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/${VALID_TOKEN}`);
    await page.getByRole('button', { name: /Répondre pour tout le monde|Answer for everyone/ }).click();
    const linkRequest = page.waitForRequest('**/magic-link-requests');
    await page.getByRole('button', { name: /Alice Martin/ }).click();
    expect((await linkRequest).postDataJSON()).toEqual({ mode: 'GROUP' });

    await page.route('**/api/guest-access/secured/me', async (route) => {
      await fulfillJson(
        route,
        { guestId: 'guest-1', invitationId: 'invitation-1', firstName: 'Alice', lastName: 'Martin', language: 'FR' },
        200,
        CREDENTIALED_CORS,
      );
    });
    const answered = (attendance: 'ATTENDING' | 'DECLINED', meal: 'MEAT' | null) => ({
      id: 'rsvp-1',
      version: 1,
      creationDate: '2026-06-13T10:00:00',
      updateDate: '2026-06-13T10:00:00',
      attendance,
      meal,
    });
    await page.route('**/api/guest-access/secured/invitation/rsvps', async (route) => {
      const method = route.request().method();

      if (method === 'OPTIONS') {
        await route.fulfill({ status: 204, headers: PREFLIGHT_CORS });
        return;
      }

      const guests =
        method === 'GET'
          ? [
              { guestId: 'guest-1', firstName: 'Alice', rsvp: null },
              { guestId: 'guest-2', firstName: 'Bob', rsvp: null },
            ]
          : [
              { guestId: 'guest-1', firstName: 'Alice', rsvp: answered('ATTENDING', 'MEAT') },
              { guestId: 'guest-2', firstName: 'Bob', rsvp: answered('DECLINED', null) },
            ];
      await fulfillJson(route, { guests }, 200, CREDENTIALED_CORS);
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/secured-area?mode=group`);

    const guestTab = (name: string) => page.getByRole('navigation').getByRole('button', { name: new RegExp(name) });
    const submit = page.getByRole('button', { name: /Envoyer les réponses de tout le monde|Send everyone's answers/ });
    await expect(guestTab('Bob')).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect(submit).toBeDisabled();

    await page.locator('input[value="ATTENDING"]').check();
    await page.locator('input[value="MEAT"]').check();
    await guestTab('Bob').click();
    await page.locator('input[value="DECLINED"]').check();
    await expect(submit).toBeEnabled();

    const submission = page.waitForRequest((request) => request.method() === 'POST');
    await submit.click();

    expect((await submission).postDataJSON()).toEqual({
      rsvps: [
        { guestId: 'guest-1', rsvp: { attendance: 'ATTENDING', meal: 'MEAT', song: null } },
        { guestId: 'guest-2', rsvp: { attendance: 'DECLINED' } },
      ],
    });
    await expect(page.getByText(/Merci du fond du cœur|Thank you from the bottom of our hearts/i)).toBeVisible();
  });

  test('shows a recoverable message when the magic-link request is rate limited', async ({ page }) => {
    await mockInvitation(page);
    await page.route('**/magic-link-requests', async (route) => {
      await fulfillJson(route, {}, 429, { ...CREDENTIALED_CORS, 'retry-after': '60' });
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/${VALID_TOKEN}`);
    await expect(page.getByRole('button', { name: /Alice Martin/ })).toBeVisible({ timeout: UI_TIMEOUT_MS });

    await page.getByRole('button', { name: /Alice Martin/ }).click();

    await expect(page.getByText(/Trop de demandes|Too many requests/i)).toBeVisible();
  });

  test('shows a recoverable expired-link state in the secured area', async ({ page }) => {
    await page.route('**/api/guest-access/secured/me', async (route) => {
      await fulfillJson(route, {}, 401, CREDENTIALED_CORS);
    });

    await page.goto(`${PUBLIC_BASE_URL}/guest-access/secured-area`);

    await expect(page.getByText(/expiré ou invalide|expired or invalid/i)).toBeVisible({ timeout: UI_TIMEOUT_MS });
    await expect(page.getByRole('link', { name: /Recommencer|Start over/i })).toBeVisible();
  });
});


