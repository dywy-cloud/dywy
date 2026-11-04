import { RsvpStatus } from '../../backoffice/src/services/rsvpStatus';

export const dashboardStatistics = {
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
  deliveryMethodCounts: { HAND_DELIVERED: 1, POSTED: 2 }
};
