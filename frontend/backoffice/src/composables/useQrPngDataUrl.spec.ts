import { afterEach, describe, expect, it, vi } from 'vitest';
import { DEFAULT_QR_COLOR, useQrPngDataUrl } from './useQrPngDataUrl';

describe('useQrPngDataUrl', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('returns a PNG data URL for a url and a color', () => {
    const toDataUrl = vi.spyOn(HTMLCanvasElement.prototype, 'toDataURL').mockReturnValue('data:image/png;base64,qr');

    const { toPngDataUrl } = useQrPngDataUrl();

    expect(toPngDataUrl('http://localhost:4173/guest-access/token-1', '#112233')).toBe('data:image/png;base64,qr');
    expect(toDataUrl).toHaveBeenCalledWith('image/png');
  });

  it('uses the default color when none is given', () => {
    vi.spyOn(HTMLCanvasElement.prototype, 'toDataURL').mockReturnValue('data:image/png;base64,qr');

    const { toPngDataUrl } = useQrPngDataUrl();

    expect(DEFAULT_QR_COLOR).toBe('#37474f');
    expect(toPngDataUrl('http://localhost:4173/guest-access/token-1')).toBe('data:image/png;base64,qr');
  });
});

