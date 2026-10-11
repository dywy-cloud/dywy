import { describe, expect, it } from 'vitest';
import { getContrastRatioOnWhite, isScannableOnWhite, MIN_QR_CONTRAST_RATIO } from './colorContrast';

describe('colorContrast', () => {
  it.each([
    ['#000000', 21],
    ['#ffffff', 1],
  ])('computes the contrast ratio of %s on white as %s', (hex, expected) => {
    expect(getContrastRatioOnWhite(hex)).toBeCloseTo(expected, 5);
  });

  it('accepts the default QR color', () => {
    expect(getContrastRatioOnWhite('#37474f')).toBeGreaterThanOrEqual(MIN_QR_CONTRAST_RATIO);
    expect(isScannableOnWhite('#37474f')).toBe(true);
  });

  it.each(['#ffeb3b', '#cccccc'])('rejects the pale color %s', (hex) => {
    expect(isScannableOnWhite(hex)).toBe(false);
  });

  it.each(['', 'red', '#fff', '#12345g', '37474f'])('handles the invalid color "%s"', (hex) => {
    expect(getContrastRatioOnWhite(hex)).toBeNull();
    expect(isScannableOnWhite(hex)).toBe(false);
  });
});

