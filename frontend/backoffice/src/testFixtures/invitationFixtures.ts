import type { InvitationGuestResponse, InvitationResponse } from '../services/invitationApi';

export const createInvitationGuestResponse = (
  overrides: Partial<InvitationGuestResponse> = {}
): InvitationGuestResponse => ({
  id: 'guest-1',
  firstName: 'Alice',
  lastName: 'Martin',
  email: 'alice@example.com',
  ...overrides
});

export const createInvitationResponse = (overrides: Partial<InvitationResponse> = {}): InvitationResponse => {
  const guests = overrides.guests ?? [createInvitationGuestResponse()];

  return {
    id: 'inv-1',
    accessToken: 'token-invitation-1234567890',
    version: 1,
    creationDate: '2026-07-03T10:00:00Z',
    updateDate: '2026-07-04T10:00:00Z',
    label: 'Family table',
    description: 'Main family table',
    guests,
    guestCount: guests.length,
    ...overrides
  };
};

