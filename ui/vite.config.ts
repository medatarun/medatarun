import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    rolldownOptions: {
      input: {
        main: path.resolve(__dirname, "index.html"),
        login: path.resolve(__dirname, "login.html"),
      },
      output: {
        codeSplitting: {
          groups: [
            {
              name(id) {
                if (
                  id.includes("@seij/common-ui") ||
                  id.includes("@seij/common-ui-auth")
                ) {
                  return "seij-common-ui";
                }
                return null;

              }
            },
            {
              name(id) {
                if (
                  id.includes("node_modules")
                ) {
                  return "vendor";
                }
                return null
              },
            },

          ]
        },
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": "http://localhost:8080",
      "/ui": "http://localhost:8080",
      "/.well-known": "http://localhost:8080",
      "/auth/": "http://localhost:8080",
    },
  },
});
