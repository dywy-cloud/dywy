import type { Page, Route } from '@playwright/test';

export const fulfillJson = async (
  route: Route,
  body: unknown,
  status = 200,
  headers?: Record<string, string>
) => {
  await route.fulfill({
    status,
    contentType: 'application/json',
    headers,
    body: JSON.stringify(body)
  });
};

export const mockEmptyGuestList = async (page: Page) => {
  await page.route('**/api/guests*', async (route) => {
    await fulfillJson(route, { items: [], page: 0, size: 20, totalItems: 0, totalPages: 0 });
  });
};
