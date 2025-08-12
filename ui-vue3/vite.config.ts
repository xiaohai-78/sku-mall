import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { viteMockServe } from 'vite-plugin-mock';

export default defineConfig(({ command }) => {
  const enableMock = command === 'serve';
  return {
    plugins: [
      vue(),
      viteMockServe({
        mockPath: 'mock',
        enable: enableMock,
        logger: true,
        supportTs: true
      })
    ],
    server: {
      port: 5173,
      open: true
    }
  };
}); 