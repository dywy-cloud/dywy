import { RsvpStatus } from '../services/invitationApi';
import type { DashboardStatisticsResponse } from '../services/dashboardApi';

export const createDashboardStatisticsResponse = (
  overrides: Partial<DashboardStatisticsResponse> = {}
): DashboardStatisticsResponse => ({
  totalInvitations: 3,
  totalGuests: 4,
  invitationsByRsvpStatus: {
    [RsvpStatus.ACCEPTED_ALL]: 1,
    [RsvpStatus.PARTIALLY_ACCEPTED]: 1,
    [RsvpStatus.DECLINED_ALL]: 1,
    [RsvpStatus.PARTIALLY_ANSWERED]: 0,
    [RsvpStatus.PENDING]: 0
  },
  guestsByAttendance: { ATTENDING: 2, DECLINED: 2, PENDING: 0 },
  mealCounts: { MEAT: 1, FISH: 1, VEGGIE: 0 },
  deliveryMethodCounts: { HAND_DELIVERED: 1, POSTED: 2 },
  ...overrides
});

export const createEmptyDashboardStatisticsResponse = (): DashboardStatisticsResponse => createDashboardStatisticsResponse({
  totalInvitations: 0,
  totalGuests: 0,
  invitationsByRsvpStatus: {
    [RsvpStatus.ACCEPTED_ALL]: 0,
    [RsvpStatus.PARTIALLY_ACCEPTED]: 0,
    [RsvpStatus.DECLINED_ALL]: 0,
    [RsvpStatus.PARTIALLY_ANSWERED]: 0,
    [RsvpStatus.PENDING]: 0
  },
  guestsByAttendance: { ATTENDING: 0, DECLINED: 0, PENDING: 0 },
  mealCounts: { MEAT: 0, FISH: 0, VEGGIE: 0 },
  deliveryMethodCounts: { HAND_DELIVERED: 0, POSTED: 0 }
});
