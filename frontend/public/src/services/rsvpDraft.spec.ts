import { describe, expect, it } from 'vitest';
import { draftFromRsvp, emptyDraft, isDraftChanged, isDraftComplete, toRsvpAnswer, type RsvpDraft } from './rsvpDraft';

const laVieEnRose = { deezerId: 3135556, title: 'La Vie en rose', artist: 'Edith Piaf', link: 'https://www.deezer.com/track/3135556' };
const otherSong = { ...laVieEnRose, deezerId: 1 };

const savedRsvp = {
  id: 'rsvp-1',
  version: 1,
  creationDate: '2026-06-13T10:00:00',
  updateDate: '2026-06-13T10:00:00',
  attendance: 'ATTENDING' as const,
  meal: 'MEAT' as const,
  song: laVieEnRose,
};

const attending = (overrides: Partial<RsvpDraft> = {}): RsvpDraft => ({
  attendance: 'ATTENDING',
  meal: 'MEAT',
  song: null,
  ...overrides,
});

describe('rsvpDraft', () => {
  it('starts empty when the guest has not responded', () => {
    expect(draftFromRsvp(null)).toEqual(emptyDraft());
  });

  it('is prefilled from a saved response', () => {
    expect(draftFromRsvp(savedRsvp)).toEqual({ attendance: 'ATTENDING', meal: 'MEAT', song: laVieEnRose });
  });

  it.each([
    ['without an attendance', emptyDraft(), false],
    ['attending without a meal', attending({ meal: null }), false],
    ['attending with a meal', attending(), true],
    ['declined', { attendance: 'DECLINED', meal: null, song: null } as RsvpDraft, true],
  ])('is %s: complete is %s', (_label, draft, complete) => {
    expect(isDraftComplete(draft)).toBe(complete);
  });

  it.each([
    ['the same answer', attending({ song: laVieEnRose }), false],
    ['another attendance', { ...attending({ song: laVieEnRose }), attendance: 'DECLINED' } as RsvpDraft, true],
    ['another meal', attending({ meal: 'FISH', song: laVieEnRose }), true],
    ['another song', attending({ song: otherSong }), true],
    ['a cleared song', attending(), true],
  ])('detects %s against the saved answer', (_label, draft, changed) => {
    expect(isDraftChanged(draft, draftFromRsvp(savedRsvp))).toBe(changed);
  });

  it('ignores the meal and song of a declined answer when comparing', () => {
    const saved: RsvpDraft = { attendance: 'DECLINED', meal: null, song: null };

    expect(isDraftChanged({ attendance: 'DECLINED', meal: 'FISH', song: laVieEnRose }, saved)).toBe(false);
  });

  it('answers with the meal and the song when attending', () => {
    expect(toRsvpAnswer(attending({ song: laVieEnRose }))).toEqual({ attendance: 'ATTENDING', meal: 'MEAT', song: laVieEnRose });
  });

  it('clears the song explicitly when attending without one', () => {
    expect(toRsvpAnswer(attending())).toEqual({ attendance: 'ATTENDING', meal: 'MEAT', song: null });
  });

  it('answers with the attendance only when declined', () => {
    expect(toRsvpAnswer({ attendance: 'DECLINED', meal: 'FISH', song: laVieEnRose })).toEqual({ attendance: 'DECLINED' });
  });
});

