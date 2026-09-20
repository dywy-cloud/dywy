/** @type {import('tailwindcss').Config} */
export default {
  // Tailwind v4 uses the CSS-first setup from `style.css` via `@theme`.
  // Keep this file minimal for optional legacy/tooling compatibility only.
  content: [
    "./index.html",
    "./public/**/*.{vue,js,ts,jsx,tsx}",
    "./backoffice/**/*.{vue,js,ts,jsx,tsx}",
  ],
  plugins: [],
}
