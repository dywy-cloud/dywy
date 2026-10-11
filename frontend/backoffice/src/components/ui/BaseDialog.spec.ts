import { mount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';
import BaseDialog from './BaseDialog.vue';

const mountDialog = () =>
  mount(BaseDialog, {
    props: { title: 'Dialog title' },
    slots: {
      default: '<p data-test="content">Content</p>',
      actions: '<button data-test="action">Action</button>'
    },
    global: { stubs: { teleport: true } }
  });

describe('BaseDialog', () => {
  it('renders the title, content and actions', () => {
    const wrapper = mountDialog();

    expect(wrapper.get('[data-test="base-dialog-title"]').text()).toBe('Dialog title');
    expect(wrapper.get('[data-test="content"]').text()).toBe('Content');
    expect(wrapper.find('[data-test="base-dialog-actions"] [data-test="action"]').exists()).toBe(true);
  });

  it('opens as a modal dialog labelled by its title', () => {
    const showModal = vi.spyOn(HTMLDialogElement.prototype, 'showModal');
    showModal.mockClear();
    const wrapper = mountDialog();

    const titleId = wrapper.get('[data-test="base-dialog-title"]').attributes('id');

    expect(showModal).toHaveBeenCalledTimes(1);
    expect(wrapper.get('dialog').attributes('open')).toBeDefined();
    expect(wrapper.get('dialog').attributes('aria-labelledby')).toBe(titleId);
  });

  it('emits cancel on Escape without closing by itself', () => {
    const wrapper = mountDialog();

    const event = new Event('cancel', { cancelable: true });
    wrapper.get('dialog').element.dispatchEvent(event);

    expect(event.defaultPrevented).toBe(true);
    expect(wrapper.emitted('cancel')).toHaveLength(1);
  });

  it('emits cancel when the backdrop is clicked but not when the content is', async () => {
    const wrapper = mountDialog();

    await wrapper.get('[data-test="content"]').trigger('click');
    expect(wrapper.emitted('cancel')).toBeUndefined();

    await wrapper.get('dialog').trigger('click');
    expect(wrapper.emitted('cancel')).toHaveLength(1);
  });
});


