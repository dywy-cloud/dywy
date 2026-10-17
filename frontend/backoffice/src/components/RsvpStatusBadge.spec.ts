import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import RsvpStatusBadge from './RsvpStatusBadge.vue';
import type { RsvpSummary } from '../services/invitationApi';
import { createRsvpSummary } from '../testFixtures/invitationFixtures';

const mountBadge = (summary: RsvpSummary) => mount(RsvpStatusBadge, { props: { summary } });

describe('RsvpStatusBadge', () => {
  it('shows a slate gradient accepted badge with the attending count when everyone accepted', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'ACCEPTED_ALL', attending: 3, pending: 0 }));

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Accepted 3/3');
    expect(wrapper.get('[data-test="rsvp-status-icon"]').text()).toBe('✓');
    expect(wrapper.classes()).toContain('bg-badge-gradient');
  });

  it('shows a slate partially accepted badge when answers are mixed', () => {
    const wrapper = mountBadge(
      createRsvpSummary({ status: 'PARTIALLY_ACCEPTED', attending: 2, declined: 1, pending: 0 })
    );

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Partially accepted 2/3');
    expect(wrapper.get('[data-test="rsvp-status-icon"]').text()).toBe('◐');
    expect(wrapper.classes()).toContain('bg-primary/15');
  });

  it('shows a rose declined badge when everyone declined', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'DECLINED_ALL', declined: 3, pending: 0 }));

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Declined');
    expect(wrapper.get('[data-test="rsvp-status-icon"]').text()).toBe('✕');
    expect(wrapper.classes()).toContain('text-accent-dark');
  });

  it('shows a neutral awaiting reply badge when nobody answered', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'PENDING' }));

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Awaiting reply');
    expect(wrapper.classes()).toContain('border-secondary');
  });

  it('shows a neutral replied badge with the answered count when some guests have not answered', () => {
    const wrapper = mountBadge(
      createRsvpSummary({ status: 'PARTIALLY_ANSWERED', attending: 1, declined: 1, pending: 1 })
    );

    expect(wrapper.get('[data-test="rsvp-status-label"]').text()).toBe('Replied 2/3');
    expect(wrapper.classes()).toContain('border-secondary');
  });

  it('shows the icon and the counter, with the full text in the tooltip', () => {
    const wrapper = mountBadge(
      createRsvpSummary({ status: 'PARTIALLY_ACCEPTED', attending: 2, declined: 1, pending: 0 })
    );

    expect(wrapper.get('[data-test="rsvp-status-counter"]').text()).toBe('2/3');
    expect(wrapper.get('[data-test="rsvp-status-tooltip"]').text()).toBe('Partially accepted 2/3');
    expect(wrapper.get('[data-test="rsvp-status-tooltip"]').classes()).toContain('group-hover:block');
    expect(wrapper.get('[data-test="rsvp-status-label"]').classes()).toContain('sr-only');
  });

  it('shows the matching counter for each status', () => {
    const counterOf = (summary: RsvpSummary) => mountBadge(summary).get('[data-test="rsvp-status-counter"]').text();

    expect(counterOf(createRsvpSummary({ status: 'DECLINED_ALL', declined: 3, pending: 0 }))).toBe('3/3');
    expect(counterOf(createRsvpSummary({ status: 'PARTIALLY_ANSWERED', attending: 1, declined: 1, pending: 1 }))).toBe('2/3');
    expect(counterOf(createRsvpSummary({ status: 'PENDING' }))).toBe('0/3');
  });

  it('shows only the icon in compact mode and keeps the tooltip', () => {
    const wrapper = mount(RsvpStatusBadge, {
      props: { summary: createRsvpSummary({ status: 'ACCEPTED_ALL', attending: 1, pending: 0, total: 1 }), compact: true }
    });

    expect(wrapper.find('[data-test="rsvp-status-counter"]').exists()).toBe(false);
    expect(wrapper.get('[data-test="rsvp-status-icon"]').text()).toBe('✓');
    expect(wrapper.get('[data-test="rsvp-status-tooltip"]').text()).toBe('Accepted 1/1');
  });

  it('hides the icon from assistive technologies and keeps the text', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'DECLINED_ALL', declined: 3, pending: 0 }));

    expect(wrapper.get('[data-test="rsvp-status-icon"]').attributes('aria-hidden')).toBe('true');
    expect(wrapper.text()).toContain('Declined');
  });

  it('is not a keyboard tab stop in either mode', () => {
    const summary = createRsvpSummary({ status: 'PENDING' });
    const full = mountBadge(summary);
    const compact = mount(RsvpStatusBadge, { props: { summary, compact: true } });

    expect(full.attributes('tabindex')).toBeUndefined();
    expect(compact.attributes('tabindex')).toBeUndefined();
    expect(full.find('[tabindex]').exists()).toBe(false);
    expect(compact.find('[tabindex]').exists()).toBe(false);
  });

  it('exposes the status for the parent views', () => {
    const wrapper = mountBadge(createRsvpSummary({ status: 'PARTIALLY_ANSWERED', attending: 1, pending: 2 }));

    expect(wrapper.attributes('data-status')).toBe('PARTIALLY_ANSWERED');
  });
});
