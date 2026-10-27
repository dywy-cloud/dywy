import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import DashboardStatCard from './DashboardStatCard.vue';

describe('DashboardStatCard', () => {
  const mountCard = () => mount(DashboardStatCard, {
    props: {
      title: 'Meals',
      total: 7,
      totalTest: 'card-total',
      rowTestPrefix: 'card-row',
      rows: [
        { key: 'MEAT', label: 'Meat', count: 4 },
        { key: 'FISH', label: 'Fish', count: 0 }
      ]
    }
  });

  it('should show the title and the total', () => {
    const wrapper = mountCard();

    expect(wrapper.get('h3').text()).toBe('Meals');
    expect(wrapper.get('[data-test="card-total"]').text()).toBe('7');
  });

  it('should show every row, including the zero counts', () => {
    const wrapper = mountCard();

    expect(wrapper.get('[data-test="card-row-MEAT"]').text()).toBe('Meat4');
    expect(wrapper.get('[data-test="card-row-FISH"]').text()).toBe('Fish0');
  });
});
