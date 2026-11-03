import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import DashboardStatCard from './DashboardStatCard.vue';

describe('DashboardStatCard', () => {
  const mountCard = (props?: Partial<InstanceType<typeof DashboardStatCard>['$props']>) =>
    mount(DashboardStatCard, {
      global: {
        stubs: { RouterLink: true }
      },
      props: {
        title: 'Meals',
        total: 7,
        totalTest: 'card-total',
        rowTestPrefix: 'card-row',
        rows: [
          { key: 'MEAT', label: 'Meat', count: 4 },
          { key: 'FISH', label: 'Fish', count: 0 }
        ],
        ...props
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

  it('should wrap a row with a link in a RouterLink and apply clickable styles', () => {
    const wrapper = mountCard({
      rows: [{ key: 'LINKED', label: 'Linked', count: 2, link: { name: 'list', query: { filter: 'LINKED' } } }]
    });

    const link = wrapper.find('[data-test="card-row-LINKED-link"]');
    expect(link.exists()).toBe(true);
    expect(link.classes()).toContain('cursor-pointer');
    expect(link.classes()).toContain('hover:bg-primary/10');
  });

  it('should not render a link for a row without a link property', () => {
    const wrapper = mountCard();

    expect(wrapper.find('[data-test$="-link"]').exists()).toBe(false);
  });
});
