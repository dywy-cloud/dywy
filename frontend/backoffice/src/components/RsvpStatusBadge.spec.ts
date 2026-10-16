import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import RsvpStatusBadge from './RsvpStatusBadge.vue';
import type { RsvpSummary } from '../services/invitationApi';
import { createRsvpSummary } from '../testFixtures/invitationFixtures';

const mountBadge = (summary: RsvpSummary) => mount(RsvpStatusBadge, { props: { summary } });

describe('RsvpStatusBadge', () => {
  it('shows a green accepted badge with the attending count when everyone accepted', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'ACCEPTED_ALL', attending: 3, pending: 0 }));

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Accepted 3/3');
    expect(wrapper.get('[data-test="rsvp-status-icon"]').text()).toBe('✓');
    expect(wrapper.classes()).toContain('text-green-800');
  });

  it('shows an amber partially accepted badge when answers are mixed', () => {
    const wrapper = mountBadge(
      createRsvpSummary({ status: 'PARTIALLY_ACCEPTED', attending: 2, declined: 1, pending: 0 })
    );

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Partially accepted 2/3');
    expect(wrapper.get('[data-test="rsvp-status-icon"]').text()).toBe('◐');
    expect(wrapper.classes()).toContain('text-amber-800');
  });

  it('shows a red declined badge when everyone declined', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'DECLINED_ALL', declined: 3, pending: 0 }));

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Declined');
    expect(wrapper.get('[data-test="rsvp-status-icon"]').text()).toBe('✕');
    expect(wrapper.classes()).toContain('text-red-800');
  });

  it('shows a grey awaiting reply badge when nobody answered', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'PENDING' }));

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Awaiting reply');
    expect(wrapper.classes()).toContain('text-gray-700');
  });

  it('shows a grey replied badge with the answered count when some guests have not answered', () => {
    const wrapper = mountBadge(
      createRsvpSummary({ status: 'PARTIALLY_ANSWERED', attending: 1, declined: 1, pending: 1 })
    );

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Replied 2/3');
    expect(wrapper.classes()).toContain('text-gray-700');
  });

  it('hides the icon from assistive technologies and keeps the text', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'DECLINED_ALL', declined: 3, pending: 0 }));

    expect(wrapper.get('[data-test="rsvp-status-icon"]').attributes('aria-hidden')).toBe('true');
    expect(wrapper.text()).toContain('Declined');
  });

  it('exposes the status for the parent views', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'PARTIALLY_ANSWERED', attending: 1, pending: 2 }));

    expect(wrapper.attributes('data-status')).toBe('PARTIALLY_ANSWERED');
  });
});

