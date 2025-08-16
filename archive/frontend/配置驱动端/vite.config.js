import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia']
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 8090,
    host: '0.0.0.0',
    proxy: {
      // 配置端API代理 - 修正为8083端口
      '/config-api': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/config-api/, ''),
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('配置端代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('配置端代理请求:', req.method, req.url);
          });
        }
      },
      // 应用端API代理 - 修正为8083端口
      '/app-api': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/app-api/, ''),
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('应用端代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('应用端代理请求:', req.method, req.url);
          });
        }
      },
      // 聊天服务代理 - 修正为8084端口
      '/api/chat': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('聊天服务代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('聊天服务代理请求:', req.method, req.url);
          });
        }
      },
      // 健康检查和通用API代理 - 修正为8083端口
      '/health': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('健康检查代理错误:', err.message);
          });
        }
      },
      // 认证API代理 - 修正为8083端口
      '/auth': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('认证API代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('认证API代理请求:', req.method, req.url);
          });
        }
      },
      // 直接代理到后端API服务 - 修正为8083端口
      '/api': {
        target: 'http://localhost:8083',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('API代理错误:', err.message);
          });
        }
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 移除全局变量导入，避免重复导入问题
      }
    }
  },
  define: {
    __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: 'false'
  }
})
