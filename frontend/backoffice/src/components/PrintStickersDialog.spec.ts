import { mount } from '@vue/test-utils';
import { defineComponent, h } from 'vue';
import { describe, expect, it } from 'vitest';
import PrintStickersDialog from './PrintStickersDialog.vue';

const QrcodeCanvasStub = defineComponent({
  props: {
    value: { type: String, required: true },
    foreground: { type: String, default: '#000000' }
  },
  setup: (props) => () => h('canvas', { 'data-foreground': props.foreground })
});

const mountDialog = () =>
  mount(PrintStickersDialog, {
    global: { stubs: { QrcodeCanvas: QrcodeCanvasStub, teleport: true } }
  });

const previewColor = (wrapper: ReturnType<typeof mountDialog>) =>
  wrapper.get('[data-test="sticker-preview-qr"]').attributes('data-foreground');

describe('PrintStickersDialog', () => {
  it('selects the default color', () => {
    const wrapper = mountDialog();

    expect(previewColor(wrapper)).toBe('#37474f');
    expect((wrapper.get('[data-test="custom-color-input"]').element as HTMLInputElement).value).toBe('#37474f');
    expect(wrapper.find('[data-test="low-contrast-warning"]').exists()).toBe(false);
    expect(wrapper.get('[data-test="print-stickers-confirm"]').attributes('disabled')).toBeUndefined();
  });

  it('updates the preview when a swatch is selected', async () => {
    const wrapper = mountDialog();

    await wrapper.get('[data-test="color-swatch-#1e3a8a"]').trigger('click');

    expect(previewColor(wrapper)).toBe('#1e3a8a');
    expect(wrapper.get('[data-test="sticker-preview-label"]').attributes('style')).toContain('color: rgb(30, 58, 138)');
  });

  it('updates the preview when a custom color is entered', async () => {
    const wrapper = mountDialog();

    await wrapper.get('[data-test="custom-color-input"]').setValue('#006400');

    expect(previewColor(wrapper)).toBe('#006400');
  });

  it('warns and disables Print when the color has too little contrast', async () => {
    const wrapper = mountDialog();

    await wrapper.get('[data-test="custom-color-input"]').setValue('#ffeb3b');

    expect(wrapper.find('[data-test="low-contrast-warning"]').exists()).toBe(true);
    expect(wrapper.get('[data-test="print-stickers-confirm"]').attributes('disabled')).toBeDefined();

    await wrapper.get('[data-test="print-stickers-confirm"]').trigger('click');

    expect(wrapper.emitted('print')).toBeUndefined();
  });

  it('emits the chosen color on Print', async () => {
    const wrapper = mountDialog();

    await wrapper.get('[data-test="color-swatch-#166534"]').trigger('click');
    await wrapper.get('[data-test="print-stickers-confirm"]').trigger('click');

    expect(wrapper.emitted('print')).toEqual([['#166534']]);
    expect(wrapper.emitted('cancel')).toBeUndefined();
  });

  it('emits cancel without printing on Cancel', async () => {
    const wrapper = mountDialog();

    await wrapper.get('[data-test="print-stickers-cancel"]').trigger('click');

    expect(wrapper.emitted('cancel')).toHaveLength(1);
    expect(wrapper.emitted('print')).toBeUndefined();
  });

  it('emits cancel when the dialog is dismissed', async () => {
    const wrapper = mountDialog();

    await wrapper.get('dialog').trigger('click');

    expect(wrapper.emitted('cancel')).toHaveLength(1);
    expect(wrapper.emitted('print')).toBeUndefined();
  });
});
