/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#6C5CE7',
          variant: '#5A4AD1',
        },
        secondary: {
          DEFAULT: '#28C76F',
        },
        error: '#E6656A',
        background: '#FFFFFF',
        surface: '#F1F5F9',
        text: {
          primary: '#1E293B',
          secondary: '#64748B',
        }
      },
      fontFamily: {
        sans: ['"Public Sans"', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
