import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import DashboardView from './DashboardView.vue';
import { BACKOFFICE_ROUTE_NAMES } from '../router/routeNames';
import { RsvpStatus } from '../services/rsvpStatus';
import { createDashboardStatisticsResponse, createEmptyDashboardStatisticsResponse } from '../testFixtures/dashboardFixtures';

const getDashboardStatisticsMock = vi.hoisted(() => vi.fn());

vi.mock('../services/dashboardApi', () => ({
  getDashboardStatistics: getDashboardStatisticsMock
}));

describe('DashboardView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    getDashboardStatisticsMock.mockResolvedValue(createDashboardStatisticsResponse());
  });

  const mountDashboard = async () => {
    const placeholder = defineComponent({ template: '<div />' });
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: DashboardView },
        { path: '/invitations', name: BACKOFFICE_ROUTE_NAMES.invitationList, component: placeholder },
        { path: '/guests', name: BACKOFFICE_ROUTE_NAMES.guestList, component: placeholder }
      ]
    });

    await router.push('/');
    await router.isReady();

    const wrapper = mount(DashboardView, { global: { plugins: [router] } });
    await flushPromises();

    return { wrapper, router };
  };

  it('should show the invitation and meal totals', async () => {
    const { wrapper } = await mountDashboard();

    expect(wrapper.get('[data-test="dashboard-total-invitations"]').text()).toBe('3');
    expect(wrapper.get('[data-test="dashboard-total-meals"]').text()).toBe('2');
  });

  it.each([
    ['dashboard-invitation-status-ACCEPTED_ALL', 'Accepted1'],
    ['dashboard-invitation-status-PARTIALLY_ACCEPTED', 'Partially accepted1'],
    ['dashboard-invitation-status-DECLINED_ALL', 'Declined1'],
    ['dashboard-invitation-status-PARTIALLY_ANSWERED', 'Partially replied0'],
    ['dashboard-invitation-status-PENDING', 'Awaiting reply0'],
    ['dashboard-meal-MEAT', 'Meat1'],
    ['dashboard-meal-FISH', 'Fish1'],
    ['dashboard-meal-VEGGIE', 'Veggie0']
  ])('should show the breakdown row %s', async (dataTest, expectedText) => {
    const { wrapper } = await mountDashboard();

    expect(wrapper.get(`[data-test="${dataTest}"]`).text()).toBe(expectedText);
  });

  it('should display custom meal counts and keep zeros', async () => {
    getDashboardStatisticsMock.mockResolvedValue(createDashboardStatisticsResponse({
      mealCounts: { MEAT: 5, FISH: 3, VEGGIE: 0 }
    }));

    const { wrapper } = await mountDashboard();

    expect(wrapper.get('[data-test="dashboard-meal-MEAT"]').text()).toBe('Meat5');
    expect(wrapper.get('[data-test="dashboard-meal-FISH"]').text()).toBe('Fish3');
    expect(wrapper.get('[data-test="dashboard-meal-VEGGIE"]').text()).toBe('Veggie0');
  });

  it('should size the progress bar segments from the guest attendance', async () => {
    getDashboardStatisticsMock.mockResolvedValue(createDashboardStatisticsResponse({
      totalGuests: 42,
      guestsByAttendance: { ATTENDING: 24, DECLINED: 6, PENDING: 12 }
    }));

    const { wrapper } = await mountDashboard();

    expect(wrapper.get('[data-test="dashboard-progress-ATTENDING"]').attributes('style')).toContain('width: 57.14');
    expect(wrapper.get('[data-test="dashboard-progress-DECLINED"]').attributes('style')).toContain('width: 14.28');
    expect(wrapper.get('[data-test="dashboard-progress-PENDING"]').attributes('style')).toContain('width: 28.57');
    expect(wrapper.get('[data-test="dashboard-legend-ATTENDING"]').text()).toBe('Attending 24 (57%)');
    expect(wrapper.get('[role="img"]').attributes('aria-label')).toBe('24 attending, 6 declined, 12 pending out of 42 guests');
  });

  it('should link to the invitations and guests lists', async () => {
    const { wrapper } = await mountDashboard();

    expect(wrapper.get('[data-test="dashboard-invitations-link"]').attributes('href')).toBe('/invitations');
    expect(wrapper.get('[data-test="dashboard-guests-link"]').attributes('href')).toBe('/guests');
  });

  it('should link the invitation status rows to the invitations list filtered by status', async () => {
    const { wrapper } = await mountDashboard();

    expect(wrapper.get('[data-test="dashboard-invitation-status-ACCEPTED_ALL-link"]').attributes('href')).toBe('/invitations?rsvpStatus=' + RsvpStatus.ACCEPTED_ALL);
    expect(wrapper.get('[data-test="dashboard-invitation-status-PENDING-link"]').attributes('href')).toBe('/invitations?rsvpStatus=' + RsvpStatus.PENDING);
  });

  it('should show a loading message while the statistics are loading', async () => {
    getDashboardStatisticsMock.mockReturnValue(new Promise(() => undefined));

    const { wrapper } = await mountDashboard();

    expect(wrapper.text()).toContain('Loading dashboard...');
    expect(wrapper.find('[data-test="dashboard-invitations-card"]').exists()).toBe(false);
  });

  it('should show the error and retry loading', async () => {
    getDashboardStatisticsMock.mockRejectedValueOnce(new Error('Unable to retrieve dashboard statistics at the moment.'));

    const { wrapper } = await mountDashboard();

    expect(wrapper.get('[role="alert"]').text()).toBe('Unable to retrieve dashboard statistics at the moment.');

    await wrapper.get('button').trigger('click');
    await flushPromises();

    expect(wrapper.find('[role="alert"]').exists()).toBe(false);
    expect(wrapper.get('[data-test="dashboard-total-invitations"]').text()).toBe('3');
  });

  it('should hide the progress bar and keep the links when there is no guest', async () => {
    getDashboardStatisticsMock.mockResolvedValue(createEmptyDashboardStatisticsResponse());

    const { wrapper } = await mountDashboard();

    expect(wrapper.get('[data-test="dashboard-no-guests"]').text()).toBe('No guest yet. Add guests to follow their replies.');
    expect(wrapper.find('[role="img"]').exists()).toBe(false);
    expect(wrapper.find('[data-test="dashboard-invitations-link"]').exists()).toBe(true);
    expect(wrapper.find('[data-test="dashboard-guests-link"]').exists()).toBe(true);
  });
});

