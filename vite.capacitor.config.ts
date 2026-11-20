// Build the native app's bundled web UI as a static TanStack Start SPA shell.
// The normal vite.config.ts remains responsible for the Cloudflare server build.
import { defineConfig } from "@lovable.dev/vite-tanstack-config";

export default defineConfig({
  // Capacitor packages the static client. Server functions stay on the deployed
  // backend and must not be treated as local APK server code.
  nitro: false,
  tanstackStart: {
    spa: {
      enabled: true,
      prerender: {
        outputPath: "/index.html",
      },
    },
    server: { entry: "server" },
  },
});
