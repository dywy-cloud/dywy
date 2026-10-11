import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { useToast } from '../../composables/useToast';
import ToastContainer from './ToastContainer.vue';

const mountContainer = () => mount(ToastContainer, { global: { stubs: { teleport: true } } });

describe('ToastContainer', () => {
  afterEach(() => {
    const { toasts } = useToast();
    toasts.value = [];
    vi.useRealTimers();
  });

  it('renders no toast by default', () => {
    const wrapper = mountContainer();

    expect(wrapper.findAll('[data-test="toast-message"]')).toHaveLength(0);
  });

  it('renders success and error toasts with their own style', async () => {
    const wrapper = mountContainer();
    const { showToast } = useToast();

    showToast('Saved', 'success');
    showToast('Failed', 'error');
    await nextTick();

    const toasts = wrapper.findAll('[data-test="toast-message"]');
    expect(toasts.map((toast) => toast.get('p').text())).toEqual(['Saved', 'Failed']);
    expect(toasts[0].classes()).toContain('bg-green-50');
    expect(toasts[1].classes()).toContain('bg-red-50');
  });

  it('removes a toast when it is dismissed', async () => {
    const wrapper = mountContainer();
    const { showToast } = useToast();

    showToast('Saved');
    await nextTick();
    await wrapper.get('button[aria-label="Dismiss notification"]').trigger('click');

    expect(wrapper.findAll('[data-test="toast-message"]')).toHaveLength(0);
  });

  it('removes a toast after its duration', async () => {
    vi.useFakeTimers();
    const wrapper = mountContainer();
    const { showToast } = useToast();

    showToast('Saved', 'success', 1000);
    await nextTick();
    expect(wrapper.findAll('[data-test="toast-message"]')).toHaveLength(1);

    vi.advanceTimersByTime(1000);
    await nextTick();

    expect(wrapper.findAll('[data-test="toast-message"]')).toHaveLength(0);
  });
});

