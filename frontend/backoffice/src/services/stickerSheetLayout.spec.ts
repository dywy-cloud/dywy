import { describe, expect, it } from 'vitest';
import { getPageCount, getStickerPosition } from './stickerSheetLayout';

describe('sticker sheet layout', () => {
  it.each([
    [0, { page: 0, x: 7.5, y: 18 }],
    [2, { page: 0, x: 142.5, y: 18 }],
    [3, { page: 0, x: 7.5, y: 85 }],
    [11, { page: 0, x: 142.5, y: 219 }],
    [12, { page: 1, x: 7.5, y: 18 }],
    [23, { page: 1, x: 142.5, y: 219 }]
  ])('places sticker %i', (index, expected) => {
    expect(getStickerPosition(index)).toEqual(expected);
  });

  it.each([
    [0, 0],
    [1, 1],
    [12, 1],
    [13, 2],
    [24, 2]
  ])('needs the right number of pages for %i stickers', (stickerCount, expectedPages) => {
    expect(getPageCount(stickerCount)).toBe(expectedPages);
  });
});

