import { QrcodeCanvas } from 'qrcode.vue';
import { h, nextTick, render } from 'vue';

export const DEFAULT_QR_COLOR = '#37474f';

const EXPORT_SIZE = 384;

export const useQrPngDataUrl = () => {
  const toPngDataUrl = async (url: string, color: string = DEFAULT_QR_COLOR): Promise<string | undefined> => {
    const container = document.createElement('div');
    render(h(QrcodeCanvas, { value: url, size: EXPORT_SIZE, margin: 1, foreground: color, level: 'M' }), container);

    try {
      await nextTick();
      const dataUrl = container.querySelector('canvas')?.toDataURL('image/png');
      return dataUrl || undefined;
    } finally {
      render(null, container);
    }
  };

  return { toPngDataUrl };
};

