import { jsPDF } from 'jspdf';
import { useQrPngDataUrl } from '../composables/useQrPngDataUrl';
import { buildGuestAccessUrl } from './guestAccessUrl';
import type { InvitationResponse } from './invitationApi';
import { getStickerPosition, STICKER_SIZE_MM, STICKERS_PER_PAGE } from './stickerSheetLayout';

const QR_SIZE_MM = 38;
const QR_TOP_MM = 5;
const LABEL_BASELINE_MM = 50;
const LABEL_MAX_WIDTH_MM = 54;
const LABEL_FONT_SIZE_PT = 10;
const ELLIPSIS = '…';

const truncateToWidth = (pdf: jsPDF, text: string): string => {
  if (pdf.getTextWidth(text) <= LABEL_MAX_WIDTH_MM) {
    return text;
  }

  let truncated = text;
  while (truncated.length > 0 && pdf.getTextWidth(`${truncated}${ELLIPSIS}`) > LABEL_MAX_WIDTH_MM) {
    truncated = truncated.slice(0, -1);
  }

  return `${truncated.trimEnd()}${ELLIPSIS}`;
};

export const buildStickerPdf = async (invitations: InvitationResponse[], color: string): Promise<jsPDF> => {
  const pdf = new jsPDF({ unit: 'mm', format: 'a4' });
  const { toPngDataUrl } = useQrPngDataUrl();

  pdf.setFontSize(LABEL_FONT_SIZE_PT);
  pdf.setTextColor(color);

  for (const [index, invitation] of invitations.entries()) {
    const { x, y } = getStickerPosition(index);
    const guestAccessUrl = buildGuestAccessUrl(invitation.accessToken);
    const qrDataUrl = guestAccessUrl ? await toPngDataUrl(guestAccessUrl, color) : undefined;

    if (!qrDataUrl) {
      throw new Error(`Unable to generate the QR code of invitation "${invitation.label}".`);
    }

    if (index > 0 && index % STICKERS_PER_PAGE === 0) {
      pdf.addPage();
    }

    pdf.addImage(qrDataUrl, 'PNG', x + (STICKER_SIZE_MM - QR_SIZE_MM) / 2, y + QR_TOP_MM, QR_SIZE_MM, QR_SIZE_MM);
    pdf.text(truncateToWidth(pdf, invitation.label), x + STICKER_SIZE_MM / 2, y + LABEL_BASELINE_MM, { align: 'center' });
  }

  return pdf;
};


