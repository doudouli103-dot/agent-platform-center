import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiTarget = env.VITE_AGENT_CENTER_API_PROXY_TARGET || 'http://windows.tentest.cn:8080';

  return {
    plugins: [react(), tailwindcss()],
    server: {
      port: 5176,
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
        },
      },
    },
  };
});
