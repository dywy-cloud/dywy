export const MIN_QR_CONTRAST_RATIO = 4.5;

const HEX_COLOR_PATTERN = /^#([0-9a-f]{6})$/i;

const toLinear = (channel: number): number => {
  const value = channel / 255;
  return value <= 0.03928 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4;
};

const getLuminance = (hex: string): number => {
  const value = parseInt(hex.slice(1), 16);
  const red = toLinear((value >> 16) & 255);
  const green = toLinear((value >> 8) & 255);
  const blue = toLinear(value & 255);
  return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
};

export const getContrastRatioOnWhite = (hex: string): number | null => {
  if (!HEX_COLOR_PATTERN.test(hex)) {
    return null;
  }
  return 1.05 / (getLuminance(hex) + 0.05);
};

export const isScannableOnWhite = (hex: string): boolean => {
  const ratio = getContrastRatioOnWhite(hex);
  return ratio !== null && ratio >= MIN_QR_CONTRAST_RATIO;
};

