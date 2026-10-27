import { csrfHeaders, getApiBaseUrl } from './http';
import type { DeliveryMethod, RsvpStatus } from './invitationApi';

export type RsvpAttendance = 'ATTENDING' | 'DECLINED' | 'PENDING';

export type Meal = 'MEAT' | 'FISH' | 'VEGGIE';

export interface DashboardStatisticsResponse {
  totalInvitations: number;
  totalGuests: number;
  invitationsByRsvpStatus: Record<RsvpStatus, number>;
  guestsByAttendance: Record<RsvpAttendance, number>;
  mealCounts: Record<Meal, number>;
  deliveryMethodCounts: Record<DeliveryMethod, number>;
}

const dashboardApiBaseUrl = getApiBaseUrl({ includeApiPath: true });

export const getDashboardStatistics = async (): Promise<DashboardStatisticsResponse> => {
  const headers = csrfHeaders();

  const response = await fetch(`${dashboardApiBaseUrl}/dashboard`, {
    method: 'GET',
    credentials: 'include',
    headers
  });

  if (!response.ok) {
    throw new Error('Unable to retrieve dashboard statistics at the moment.');
  }

  return await response.json() as DashboardStatisticsResponse;
};

