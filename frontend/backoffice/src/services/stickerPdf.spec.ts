import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createInvitationResponse } from '../testFixtures/invitationFixtures';
import { buildStickerPdf } from './stickerPdf';
import { getStickerPosition } from './stickerSheetLayout';

const { toPngDataUrl, pdf } = vi.hoisted(() => {
  const state = { pages: 1 };

  return {
    toPngDataUrl: vi.fn(),
    pdf: {
      state,
      addImage: vi.fn(),
      text: vi.fn(),
      addPage: vi.fn(() => {
        state.pages += 1;
      }),
      line: vi.fn(),
      rect: vi.fn(),
      roundedRect: vi.fn(),
      setFontSize: vi.fn(),
      setTextColor: vi.fn(),
      getTextWidth: vi.fn((value: string) => value.length * 2),
      getNumberOfPages: vi.fn(() => state.pages)
    }
  };
});

vi.mock('jspdf', () => ({
  jsPDF: vi.fn(function () {
    return pdf;
  })
}));

vi.mock('../composables/useQrPngDataUrl', () => ({
  useQrPngDataUrl: () => ({ toPngDataUrl })
}));

const createInvitations = (count: number) =>
  Array.from({ length: count }, (_, index) => createInvitationResponse({ id: `inv-${index}`, accessToken: `token-${index}`, label: `Table ${index}` }));

describe('buildStickerPdf', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    pdf.state.pages = 1;
    toPngDataUrl.mockResolvedValue('data:image/png;base64,qr');
  });

  it.each([
    [1, 1],
    [12, 1],
    [13, 2],
    [25, 3]
  ])('builds %s sticker(s) on %s page(s)', async (invitationCount, pageCount) => {
    await buildStickerPdf(createInvitations(invitationCount), '#37474f');

    expect(pdf.getNumberOfPages()).toBe(pageCount);
    expect(pdf.addImage).toHaveBeenCalledTimes(invitationCount);
  });

  it.each([0, 5, 12])('places the QR code and the label of sticker %s at its layout position', async (index) => {
    await buildStickerPdf(createInvitations(13), '#37474f');

    const { x, y } = getStickerPosition(index);

    expect(pdf.addImage.mock.calls[index]).toEqual(['data:image/png;base64,qr', 'PNG', x + 11, y + 5, 38, 38]);
    expect(pdf.text.mock.calls[index]).toEqual([`Table ${index}`, x + 30, y + 50, { align: 'center' }]);
  });

  it('uses the chosen color for the QR code and the label', async () => {
    await buildStickerPdf(createInvitations(1), '#112233');

    expect(toPngDataUrl).toHaveBeenCalledWith(expect.stringContaining('token-0'), '#112233');
    expect(pdf.setTextColor).toHaveBeenCalledWith('#112233');
  });

  it('truncates long labels with an ellipsis to fit the sticker width', async () => {
    const label = 'A very long invitation label that can never fit on a sticker of sixty millimetres';

    await buildStickerPdf([createInvitationResponse({ label })], '#37474f');
    const drawnLabel = pdf.text.mock.calls[0][0] as string;

    expect(drawnLabel.endsWith('…')).toBe(true);
    expect(drawnLabel.length).toBeLessThan(label.length);
    expect(pdf.getTextWidth(drawnLabel)).toBeLessThanOrEqual(54);
  });

  it('keeps short labels untouched', async () => {
    await buildStickerPdf([createInvitationResponse({ label: 'Family' })], '#37474f');

    expect(pdf.text.mock.calls[0][0]).toBe('Family');
  });

  it('draws only images and text, no lines or rectangles', async () => {
    await buildStickerPdf(createInvitations(13), '#37474f');

    expect(pdf.line).not.toHaveBeenCalled();
    expect(pdf.rect).not.toHaveBeenCalled();
    expect(pdf.roundedRect).not.toHaveBeenCalled();
  });

  it('rejects when a QR code cannot be generated', async () => {
    toPngDataUrl.mockResolvedValue(undefined);

    await expect(buildStickerPdf(createInvitations(1), '#37474f')).rejects.toThrow('Unable to generate the QR code');
  });
});

