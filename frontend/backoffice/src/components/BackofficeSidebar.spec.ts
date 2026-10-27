import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import { describe, expect, it } from 'vitest';
import BackofficeSidebar from './BackofficeSidebar.vue';

const createSidebarWrapper = async (path: string) => {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/dashboard', component: { template: '<div />' } },
      { path: '/invitations', component: { template: '<div />' } },
      { path: '/invitations/new', component: { template: '<div />' } },
      { path: '/guests', component: { template: '<div />' } },
      { path: '/guests/new', component: { template: '<div />' } }
    ]
  });

  await router.push(path);
  await router.isReady();

  return mount(BackofficeSidebar, {
    global: {
      plugins: [router]
    }
  });
};

describe('BackofficeSidebar', () => {
  it('renders menu entries in the expected order with the expected routes', async () => {
    const wrapper = await createSidebarWrapper('/guests');

    const links = wrapper.findAll('a');
    const icons = wrapper.findAll('.sidebar-link-icon svg');

    expect(links).toHaveLength(3);
    expect(icons).toHaveLength(3);
    expect(links.map((link) => link.text())).toEqual(['Dashboard', 'Invitations', 'Guests']);
    expect(links.map((link) => link.attributes('href'))).toEqual(['/dashboard', '/invitations', '/guests']);
  });

  it.each([
    ['/dashboard', 0],
    ['/invitations', 1],
    ['/invitations/new', 1],
    ['/guests', 2],
    ['/guests/new', 2]
  ])('marks only the matching link as active on %s', async (path, activeIndex) => {
    const wrapper = await createSidebarWrapper(path);

    wrapper.findAll('a').forEach((link, index) => {
      const isActive = index === activeIndex;

      expect(link.classes().includes('bg-badge-gradient')).toBe(isActive);
      expect(link.attributes('aria-current')).toBe(isActive ? 'page' : undefined);
    });
  });
});
