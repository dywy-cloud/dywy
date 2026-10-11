import { afterEach, describe, expect, it, vi } from 'vitest';
import { DEFAULT_QR_COLOR, useQrPngDataUrl } from './useQrPngDataUrl';

const mockDrawingCanvas = () => {
  const context = { fillRect: vi.fn(), fill: vi.fn(), setTransform: vi.fn(), fillStyle: '' };
  vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(context as unknown as CanvasRenderingContext2D);
  vi.spyOn(HTMLCanvasElement.prototype, 'toDataURL').mockImplementation(() => {
    const drawn = context.fillRect.mock.calls.length > 0 || context.fill.mock.calls.length > 0;
    return drawn ? 'data:image/png;base64,qr' : 'data:image/png;base64,blank';
  });
  return context;
};

describe('useQrPngDataUrl', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('returns a PNG data URL of the drawn QR code for a url and a color', async () => {
    const context = mockDrawingCanvas();
    const { toPngDataUrl } = useQrPngDataUrl();

    await expect(toPngDataUrl('http://localhost:4173/guest-access/token-1', '#112233')).resolves.toBe('data:image/png;base64,qr');
    expect(context.fillStyle).toBe('#112233');
  });

  it('uses the default color when none is given', async () => {
    const context = mockDrawingCanvas();
    const { toPngDataUrl } = useQrPngDataUrl();

    await toPngDataUrl('http://localhost:4173/guest-access/token-1');

    expect(DEFAULT_QR_COLOR).toBe('#37474f');
    expect(context.fillStyle).toBe(DEFAULT_QR_COLOR);
  });
});
