import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// Vite 配置
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 开发代理：前端请求 /api/xxx 会被转发到后端 8080
    // 好处：
    //   1. 浏览器看来是同源请求（localhost:5173），不会有 CORS 问题
    //   2. Session Cookie 能正常携带
    //   3. 后端不需要额外配置跨域
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
