# Design Tokens - dywy.cloud Brand System

This document outlines the design tokens used in the dywy.cloud UI. All tokens are based on the official brand guidelines as defined in the dywy.cloud branding repository.

## Color Palette

### Primary Colors

- **Slate** (`#788f9c`): Primary brand color, used for key UI elements, headers, and emphasis
- **Charcoal** (`#37474f`): Main text color, ensures excellent readability for body text and UI labels

### Accent Colors

- **Rose Light** (`#e79aae`): Light accent color for large text and prominent features
- **Rose** (`#cf6f88`): Primary accent color for interactive elements and highlights
- **Rose Deep** (`#b25f78`): Hover and pressed state color for accent elements

### Supporting Colors

- **Sage** (`#bac2bd`): Secondary/neutral color for borders, dividers, and secondary UI elements
- **Cloud** (`#f4f5f5`): Off-white background color for high contrast areas

### Color Usage Guidelines

- **Slate & Charcoal**: Use together for maximum contrast and accessibility
- **Rose shades**: Rose is suitable for all text sizes and UI elements. Rose Light can be used for large text or prominent features.
- **Accessibility**: Charcoal should be used for body text and small UI text. Rose colors should be used for accents and larger elements to maintain sufficient contrast ratios.

## Typography

### Font Family

- **Headings**: Poppins 600/700 (font weights: 600 semibold, 700 bold)
- **Body & UI**: System UI font stack (no web font cost)
  - `-apple-system`
  - `BlinkMacSystemFont`
  - `Segoe UI`
  - `Roboto`
  - `Helvetica Neue`
  - `Noto Sans`
  - `Arial`
  - `sans-serif`

### Font Weights

- **Regular**: 400
- **Medium**: 500
- **Semibold**: 600 (for headings)
- **Bold**: 700 (for headings and emphasis)

## Gradients

### Badge Gradient

```
linear-gradient(135deg, #788f9c 0%, #37474f 100%)
```

Used for badge elements and branding mark overlays.

### Accent Gradient

```
linear-gradient(135deg, #e79aae 0%, #cf6f88 100%)
```

Used for accent elements and interactive highlights.

## Implementation

### Tailwind CSS v4 Theme Source

In this Tailwind v4 CSS-first setup, the authoritative theme tokens live in `style.css` under the `@theme` block. Those tokens generate the Tailwind utilities used across both apps and are also available as CSS custom properties:

```css
--color-primary: #788f9c;
--color-secondary: #bac2bd;
--color-accent: #cf6f88;
--color-accent-light: #e79aae;
--color-accent-dark: #b25f78;
--color-background: #f4f5f5;
--color-text: #37474f;

--font-sans: ...;
--font-serif: ...;
--font-heading: Poppins, sans-serif;

--bg-badge-gradient: linear-gradient(135deg, #788f9c 0%, #37474f 100%);
--bg-accent-gradient: linear-gradient(135deg, #e79aae 0%, #cf6f88 100%);
```

The legacy `tailwind.config.js` is kept minimal for optional compatibility only and is not the source of truth for these tokens.

## Best Practices

1. **Color Consistency**: Use the defined color tokens rather than hardcoding hex values
2. **Typography**: Apply heading font family to `h1-h6` elements and use appropriate font weights
3. **Contrast**: Ensure text contrast meets WCAG AA standards (4.5:1 for body text, 3:1 for large text)
4. **Accessibility**: Use Rose colors primarily for accents and large text; prefer Charcoal for body text
5. **Responsive**: Test color and typography across different devices and screen sizes

## References

- Brand Definition: [dywy.cloud/branding/BRAND.md](https://github.com/dywy-cloud/branding/blob/main/BRAND.md)
- Tailwind CSS: https://tailwindcss.com/
- Font: [Google Fonts - Poppins](https://fonts.google.com/specimen/Poppins)

