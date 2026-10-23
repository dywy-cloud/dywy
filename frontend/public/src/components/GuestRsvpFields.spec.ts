import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, describe, expect, it, vi } from 'vitest';
import GuestRsvpFields from './GuestRsvpFields.vue';
import { emptyDraft, type RsvpDraft } from '../services/rsvpDraft';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

vi.mock('../services/guestAccessSecuredApi', () => ({
  searchSongs: vi.fn(),
}));

import { searchSongs } from '../services/guestAccessSecuredApi';

const searchSongsMock = vi.mocked(searchSongs);
const { t } = useGuestAccessI18n();

const laVieEnRose = {
  deezerId: 3135556,
  title: 'La Vie en rose',
  artist: 'Edith Piaf',
  link: 'https://www.deezer.com/track/3135556',
};

const attending = (overrides: Partial<RsvpDraft> = {}): RsvpDraft => ({ ...emptyDraft(), attendance: 'ATTENDING', ...overrides });

const mountFields = (modelValue: RsvpDraft, namePrefix?: string) =>
  mount(GuestRsvpFields, { props: { modelValue, namePrefix } });

const lastEmitted = (wrapper: ReturnType<typeof mountFields>) => wrapper.emitted('update:modelValue')?.at(-1)?.[0];

describe('GuestRsvpFields', () => {
  afterEach(() => {
    vi.clearAllMocks();
    vi.useRealTimers();
  });

  it('only asks the attendance question while nothing is answered', () => {
    const wrapper = mountFields(emptyDraft());

    expect(wrapper.text()).toContain(t('rsvp.question'));
    expect(wrapper.text()).not.toContain(t('rsvp.meal.question'));
    expect(wrapper.find('input[name="song"]').exists()).toBe(false);
  });

  it('asks for the meal and the song when the guest attends', () => {
    const wrapper = mountFields(attending());

    expect(wrapper.text()).toContain(t('rsvp.meal.question'));
    expect(wrapper.find('input[name="song"]').exists()).toBe(true);
  });

  it('hides the meal and the song when the guest declines', () => {
    const wrapper = mountFields({ ...emptyDraft(), attendance: 'DECLINED' });

    expect(wrapper.text()).not.toContain(t('rsvp.meal.question'));
    expect(wrapper.find('input[name="song"]').exists()).toBe(false);
  });

  it('checks the radios matching the draft', () => {
    const wrapper = mountFields(attending({ meal: 'FISH' }));

    expect((wrapper.find('input[value="ATTENDING"]').element as HTMLInputElement).checked).toBe(true);
    expect((wrapper.find('input[value="FISH"]').element as HTMLInputElement).checked).toBe(true);
    expect((wrapper.find('input[value="MEAT"]').element as HTMLInputElement).checked).toBe(false);
  });

  it('emits the draft with the chosen attendance', async () => {
    const wrapper = mountFields(emptyDraft());

    await wrapper.find('input[value="DECLINED"]').trigger('change');

    expect(lastEmitted(wrapper)).toEqual({ ...emptyDraft(), attendance: 'DECLINED' });
  });

  it('emits the draft with the chosen meal and keeps the rest', async () => {
    const wrapper = mountFields(attending({ song: laVieEnRose }));

    await wrapper.find('input[value="VEGGIE"]').trigger('change');

    expect(lastEmitted(wrapper)).toEqual(attending({ meal: 'VEGGIE', song: laVieEnRose }));
  });

  it('prefixes the radio names so several guests can be answered side by side', () => {
    const wrapper = mountFields(attending(), 'guest-1-');

    expect(wrapper.find('input[value="ATTENDING"]').attributes('name')).toBe('guest-1-attendance');
    expect(wrapper.find('input[value="MEAT"]').attributes('name')).toBe('guest-1-meal');
  });

  it('shows the chosen song and emits an empty song when it is removed', async () => {
    const wrapper = mountFields(attending({ meal: 'MEAT', song: laVieEnRose }));

    expect(wrapper.text()).toContain('La Vie en rose');

    await wrapper.get(`[aria-label="${t('rsvp.song.clear')}"]`).trigger('click');

    expect(lastEmitted(wrapper)).toEqual(attending({ meal: 'MEAT', song: null }));
  });

  it('emits the suggested song once the guest picks it', async () => {
    vi.useFakeTimers();
    searchSongsMock.mockResolvedValue([laVieEnRose]);
    const wrapper = mountFields(attending({ meal: 'MEAT' }));

    await wrapper.find('input[name="song"]').setValue('piaf');
    vi.advanceTimersByTime(300);
    await flushPromises();
    await wrapper.find('ul li button').trigger('click');

    expect(lastEmitted(wrapper)).toEqual(attending({ meal: 'MEAT', song: laVieEnRose }));
  });
});

