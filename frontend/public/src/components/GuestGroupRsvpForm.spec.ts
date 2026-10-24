import { flushPromises, mount } from '@vue/test-utils';
import { afterEach, describe, expect, it, vi } from 'vitest';
import GuestGroupRsvpForm from './GuestGroupRsvpForm.vue';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';

vi.mock('../services/guestAccessSecuredApi', () => ({
  fetchInvitationRsvps: vi.fn(),
  submitInvitationRsvps: vi.fn(),
  searchSongs: vi.fn(),
}));

import { fetchInvitationRsvps, submitInvitationRsvps } from '../services/guestAccessSecuredApi';

const fetchInvitationRsvpsMock = vi.mocked(fetchInvitationRsvps);
const submitInvitationRsvpsMock = vi.mocked(submitInvitationRsvps);
const { t } = useGuestAccessI18n();

const rsvp = (attendance: 'ATTENDING' | 'DECLINED', meal: 'MEAT' | 'FISH' | 'VEGGIE' | null = null) => ({
  id: 'rsvp-1',
  version: 1,
  creationDate: '2026-06-13T10:00:00',
  updateDate: '2026-06-13T10:00:00',
  attendance,
  meal,
  song: null,
});

const alicePending = { guestId: 'guest-alice', firstName: 'Alice', rsvp: null };
const bobPending = { guestId: 'guest-bob', firstName: 'Bob', rsvp: null };
const aliceAnswered = { guestId: 'guest-alice', firstName: 'Alice', rsvp: rsvp('ATTENDING', 'MEAT') };
const bobDeclined = { guestId: 'guest-bob', firstName: 'Bob', rsvp: rsvp('DECLINED') };

const mountForm = () => mount(GuestGroupRsvpForm);

const tab = (wrapper: ReturnType<typeof mountForm>, firstName: string) =>
  wrapper.findAll('nav button').find((button) => button.text().includes(firstName))!;

const submitButton = (wrapper: ReturnType<typeof mountForm>) => wrapper.find('button[type="submit"]');

describe('GuestGroupRsvpForm', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('shows a tab with the first name of every guest of the invitation', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([alicePending, bobPending]);
    const wrapper = mountForm();

    await flushPromises();

    expect(wrapper.findAll('nav button').map((button) => button.text())).toEqual([
      expect.stringContaining('Alice'),
      expect.stringContaining('Bob'),
    ]);
  });

  it('shows the form of the first guest and switches to the form of the selected guest', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([alicePending, bobPending]);
    const wrapper = mountForm();
    await flushPromises();

    expect(tab(wrapper, 'Alice').attributes('aria-current')).toBe('true');
    expect(wrapper.find('input[value="ATTENDING"]').attributes('name')).toBe('guest-alice-attendance');

    await tab(wrapper, 'Bob').trigger('click');

    expect(tab(wrapper, 'Bob').attributes('aria-current')).toBe('true');
    expect(wrapper.find('input[value="ATTENDING"]').attributes('name')).toBe('guest-bob-attendance');
  });

  it('marks the guests whose answer is complete and the others to complete', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobPending]);
    const wrapper = mountForm();

    await flushPromises();

    expect(tab(wrapper, 'Alice').find(`[aria-label="${t('groupRsvp.complete')}"]`).text()).toBe('✓');
    expect(tab(wrapper, 'Bob').find(`[aria-label="${t('groupRsvp.incomplete')}"]`).text()).toBe('●');
  });

  it('counts the guests whose answer is complete', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobPending]);
    const wrapper = mountForm();

    await flushPromises();

    expect(wrapper.text()).toContain(`1 ${t('groupRsvp.of')} 2 ${t('groupRsvp.answered')}`);
  });

  it('titles the form with the first name of the selected guest', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([alicePending, bobPending]);
    const wrapper = mountForm();
    await flushPromises();

    expect(wrapper.get('h3').text()).toBe('Alice');

    await tab(wrapper, 'Bob').trigger('click');

    expect(wrapper.get('h3').text()).toBe('Bob');
  });

  it('prefills the form of a guest who already answered', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobPending]);
    const wrapper = mountForm();

    await flushPromises();

    expect((wrapper.find('input[value="ATTENDING"]').element as HTMLInputElement).checked).toBe(true);
    expect((wrapper.find('input[value="MEAT"]').element as HTMLInputElement).checked).toBe(true);
  });

  it('keeps the answers of a guest when another guest is selected in the meantime', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([alicePending, bobPending]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await tab(wrapper, 'Bob').trigger('click');
    await tab(wrapper, 'Alice').trigger('click');

    expect((wrapper.find('input[value="DECLINED"]').element as HTMLInputElement).checked).toBe(true);
  });

  it('disables the submit until every guest is complete', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([alicePending, bobPending]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    expect(submitButton(wrapper).attributes('disabled')).toBeDefined();

    await tab(wrapper, 'Bob').trigger('click');
    await wrapper.find('input[value="DECLINED"]').trigger('change');
    expect(submitButton(wrapper).attributes('disabled')).toBeUndefined();
  });

  it('keeps the submit disabled when an attending guest has no meal yet', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([alicePending, bobDeclined]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');

    expect(submitButton(wrapper).attributes('disabled')).toBeDefined();
  });

  it('sends the answers of every guest in a single submission', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([alicePending, bobPending]);
    submitInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await tab(wrapper, 'Bob').trigger('click');
    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(submitInvitationRsvpsMock).toHaveBeenCalledTimes(1);
    expect(submitInvitationRsvpsMock).toHaveBeenCalledWith([
      { guestId: 'guest-alice', answer: { attendance: 'ATTENDING', meal: 'MEAT', song: null } },
      { guestId: 'guest-bob', answer: { attendance: 'DECLINED' } },
    ]);
  });

  it('confirms once every answer is saved', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    submitInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.saved'));
    expect(wrapper.find('[data-test="wedding-website-link"]').attributes('href')).toBe(
      'https://thecla-gregory.dywy.cloud',
    );
  });

  it('keeps the submit disabled while nothing changed across all the guests', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    const wrapper = mountForm();

    await flushPromises();

    expect(submitButton(wrapper).attributes('disabled')).toBeDefined();
  });

  it('enables the submit when the answer of a single guest changed', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    const wrapper = mountForm();
    await flushPromises();

    await tab(wrapper, 'Bob').trigger('click');
    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="FISH"]').trigger('change');

    expect(submitButton(wrapper).attributes('disabled')).toBeUndefined();
  });

  it('disables the submit again once the changed answer is restored', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await wrapper.find('input[value="ATTENDING"]').trigger('change');

    expect(submitButton(wrapper).attributes('disabled')).toBeDefined();
  });

  it('disables the submit again once the answers are saved', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([alicePending, bobPending]);
    submitInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="ATTENDING"]').trigger('change');
    await wrapper.find('input[value="MEAT"]').trigger('change');
    await tab(wrapper, 'Bob').trigger('click');
    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(submitButton(wrapper).attributes('disabled')).toBeDefined();
  });

  it('ignores an edit made while the answers are being saved', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    submitInvitationRsvpsMock.mockReturnValue(new Promise(() => {}));
    const wrapper = mountForm();
    await flushPromises();
    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await wrapper.find('form').trigger('submit');

    await wrapper.find('input[value="ATTENDING"]').trigger('change');

    expect((wrapper.find('input[value="DECLINED"]').element as HTMLInputElement).checked).toBe(true);
    expect((wrapper.find('input[value="ATTENDING"]').element as HTMLInputElement).checked).toBe(false);
  });

  it('reports a failed submission and keeps the answers', async () => {
    fetchInvitationRsvpsMock.mockResolvedValue([aliceAnswered, bobDeclined]);
    submitInvitationRsvpsMock.mockRejectedValue(new Error('boom'));
    const wrapper = mountForm();
    await flushPromises();

    await wrapper.find('input[value="DECLINED"]').trigger('change');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.submitError'));
    expect(submitButton(wrapper).attributes('disabled')).toBeUndefined();
  });

  it('offers to retry when the guests cannot be loaded', async () => {
    fetchInvitationRsvpsMock.mockRejectedValueOnce(new Error('boom'));
    fetchInvitationRsvpsMock.mockResolvedValueOnce([alicePending, bobPending]);
    const wrapper = mountForm();
    await flushPromises();

    expect(wrapper.text()).toContain(t('rsvp.loadError'));

    await wrapper.find('button').trigger('click');
    await flushPromises();

    expect(wrapper.findAll('nav button')).toHaveLength(2);
  });
});



