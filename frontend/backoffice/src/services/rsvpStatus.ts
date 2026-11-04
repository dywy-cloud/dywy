export enum RsvpStatus {
  ACCEPTED_ALL = 'ACCEPTED_ALL',
  PARTIALLY_ACCEPTED = 'PARTIALLY_ACCEPTED',
  DECLINED_ALL = 'DECLINED_ALL',
  PARTIALLY_ANSWERED = 'PARTIALLY_ANSWERED',
  PENDING = 'PENDING'
}

export const rsvpStatusLabel: Record<RsvpStatus, string> = {
  [RsvpStatus.ACCEPTED_ALL]: 'Accepted',
  [RsvpStatus.PARTIALLY_ACCEPTED]: 'Partially accepted',
  [RsvpStatus.DECLINED_ALL]: 'Declined',
  [RsvpStatus.PARTIALLY_ANSWERED]: 'Partially replied',
  [RsvpStatus.PENDING]: 'Awaiting reply'
};
