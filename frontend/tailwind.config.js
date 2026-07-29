/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'green-900': 'var(--green-900)',
        'green-800': 'var(--green-800)',
        'green-700': 'var(--green-700)',
        'green-600': 'var(--green-600)',
        'green-500': 'var(--green-500)',
        'green-100': 'var(--green-100)',
        'green-50': 'var(--green-50)',
        'border': 'var(--border)',
        'text': 'var(--text)',
        'muted': 'var(--muted)',
        'bg': 'var(--bg)',
        'panel': 'var(--panel)',
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      }
    },
  },
  plugins: [],
}
