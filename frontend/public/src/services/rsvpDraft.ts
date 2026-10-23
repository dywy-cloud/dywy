import type { GuestRsvpResponse, Meal, RsvpAnswer, RsvpAttendance, Song } from './guestAccessSecuredApi';

export interface RsvpDraft {
  attendance: RsvpAttendance | null;
  meal: Meal | null;
  song: Song | null;
}

export const emptyDraft = (): RsvpDraft => ({ attendance: null, meal: null, song: null });

export const draftFromRsvp = (rsvp: GuestRsvpResponse | null): RsvpDraft => ({
  attendance: rsvp?.attendance ?? null,
  meal: rsvp?.meal ?? null,
  song: rsvp?.song ?? null,
});

export const isDraftComplete = (draft: RsvpDraft): boolean =>
  draft.attendance !== null && (draft.attendance !== 'ATTENDING' || draft.meal !== null);

const songId = (draft: RsvpDraft): number | null => draft.song?.deezerId ?? null;

export const isDraftChanged = (draft: RsvpDraft, saved: RsvpDraft): boolean =>
  draft.attendance !== saved.attendance ||
  (draft.attendance === 'ATTENDING' && (draft.meal !== saved.meal || songId(draft) !== songId(saved)));

export const toRsvpAnswer = (draft: RsvpDraft): RsvpAnswer =>
  draft.attendance === 'ATTENDING'
    ? { attendance: 'ATTENDING', meal: draft.meal as Meal, song: draft.song }
    : { attendance: 'DECLINED' };

