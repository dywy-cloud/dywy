import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import { createRouter, createMemoryHistory } from 'vue-router';
import BaseButton from './BaseButton.vue';

describe('BaseButton', () => {
  it('renders a button of type button by default', () => {
    const wrapper = mount(BaseButton, { slots: { default: 'Save' } });

    expect(wrapper.element.tagName).toBe('BUTTON');
    expect(wrapper.attributes('type')).toBe('button');
    expect(wrapper.text()).toBe('Save');
  });

  it('uses the requested type', () => {
    const wrapper = mount(BaseButton, { props: { type: 'submit' } });

    expect(wrapper.attributes('type')).toBe('submit');
  });

  it('can be disabled', () => {
    const wrapper = mount(BaseButton, { props: { disabled: true } });

    expect(wrapper.attributes('disabled')).toBeDefined();
  });

  it.each([
    ['gradient', 'bg-accent-gradient'],
    ['solid', 'bg-badge-gradient'],
    ['danger', 'bg-red-600']
  ] as const)('applies the %s variant classes', (variant, expectedClass) => {
    const wrapper = mount(BaseButton, { props: { variant } });

    expect(wrapper.classes()).toContain(expectedClass);
  });

  it('renders a link when a route is given', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/guests', component: { template: '<div />' } }]
    });
    const wrapper = mount(BaseButton, {
      props: { to: '/guests' },
      slots: { default: 'Guests' },
      global: { plugins: [router] }
    });

    expect(wrapper.element.tagName).toBe('A');
    expect(wrapper.attributes('href')).toBe('/guests');
    expect(wrapper.attributes('type')).toBeUndefined();
  });
});

