import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';
import { describe, expect, it } from 'vitest';
import { useConfirmDialog } from '../../composables/useConfirmDialog';
import ConfirmDialog from './ConfirmDialog.vue';

const mountDialog = () => mount(ConfirmDialog, { global: { stubs: { teleport: true } } });

describe('ConfirmDialog', () => {
  it('renders nothing while no confirmation is requested', () => {
    const wrapper = mountDialog();

    expect(wrapper.text()).toBe('');
  });

  it('shows the requested title, message and labels', async () => {
    const wrapper = mountDialog();
    const { openConfirm, cancel } = useConfirmDialog();

    const result = openConfirm({ title: 'Delete guest', message: 'Are you sure?', confirmLabel: 'Delete', cancelLabel: 'Keep' });
    await nextTick();

    expect(wrapper.text()).toContain('Delete guest');
    expect(wrapper.text()).toContain('Are you sure?');
    const buttons = wrapper.findAll('button');
    expect(buttons.map((button) => button.text())).toEqual(['Keep', 'Delete']);

    cancel();
    await result;
  });

  it('resolves true and closes on confirm', async () => {
    const wrapper = mountDialog();
    const { openConfirm } = useConfirmDialog();

    const result = openConfirm({ title: 'Title', message: 'Message' });
    await nextTick();
    await wrapper.findAll('button')[1].trigger('click');

    expect(await result).toBe(true);
    await nextTick();
    expect(wrapper.text()).toBe('');
  });

  it('resolves false and closes on cancel', async () => {
    const wrapper = mountDialog();
    const { openConfirm } = useConfirmDialog();

    const result = openConfirm({ title: 'Title', message: 'Message' });
    await nextTick();
    await wrapper.findAll('button')[0].trigger('click');

    expect(await result).toBe(false);
    await nextTick();
    expect(wrapper.text()).toBe('');
  });

  it('resolves false when the backdrop is clicked', async () => {
    const wrapper = mountDialog();
    const { openConfirm } = useConfirmDialog();

    const result = openConfirm({ title: 'Title', message: 'Message' });
    await nextTick();
    await wrapper.get('.fixed').trigger('click');

    expect(await result).toBe(false);
  });

  it('uses the danger style for dangerous confirmations', async () => {
    const wrapper = mountDialog();
    const { openConfirm, cancel } = useConfirmDialog();

    const result = openConfirm({ title: 'Title', message: 'Message', isDangerous: true });
    await nextTick();

    expect(wrapper.findAll('button')[1].classes()).toContain('bg-red-600');

    cancel();
    await result;
  });
});

