/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        navy: {
          50: "#eef2fb",
          100: "#d7e0f5",
          200: "#b3c4ea",
          300: "#8aa4dd",
          400: "#5f80cc",
          500: "#3d5fb8",
          600: "#2c479a",
          700: "#24397c",
          800: "#1d2c5f",
          900: "#141d42",
        },
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [],
};
