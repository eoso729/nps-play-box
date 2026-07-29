/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        nps: {
          900: '#0b2818',
          800: '#0f3a22',
          700: '#15803d',
          600: '#16a34a',
          500: '#22a05a',
          100: '#e6f6ec',
          50: '#f3faf5',
        },
        border: '#e2e8e6',
        text: '#111827',
        muted: '#6b7280',
        bg: '#f7faf8',
        panel: '#ffffff',
      },
      fontFamily: {
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'sans-serif'],
        mono: ['JetBrains Mono', 'Courier New', 'monospace'],
      }
    },
  },
  plugins: [],
}
