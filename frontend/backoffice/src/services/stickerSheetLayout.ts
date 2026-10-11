const A4_HEIGHT_MM = 297;
const COLUMNS = 3;
const ROWS = 4;
const SIDE_MARGIN_MM = 7.5;
const COLUMN_GAP_MM = 7.5;
const TOP_MARGIN_MM = 18;

export const STICKER_SIZE_MM = 60;
export const STICKERS_PER_PAGE = COLUMNS * ROWS;

const ROW_GAP_MM = (A4_HEIGHT_MM - 2 * TOP_MARGIN_MM - ROWS * STICKER_SIZE_MM) / (ROWS - 1);

export interface StickerPosition {
  page: number;
  x: number;
  y: number;
}

export const getStickerPosition = (index: number): StickerPosition => {
  const slot = index % STICKERS_PER_PAGE;
  const column = slot % COLUMNS;
  const row = Math.floor(slot / COLUMNS);

  return {
    page: Math.floor(index / STICKERS_PER_PAGE),
    x: SIDE_MARGIN_MM + column * (STICKER_SIZE_MM + COLUMN_GAP_MM),
    y: TOP_MARGIN_MM + row * (STICKER_SIZE_MM + ROW_GAP_MM)
  };
};

export const getPageCount = (stickerCount: number): number => Math.ceil(stickerCount / STICKERS_PER_PAGE);

