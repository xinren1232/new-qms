import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: true
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: true
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `
          @import "@/styles/variables.scss";
          @import "@/styles/responsive.scss";
          @import "@/styles/modern-theme.scss";
        `
      }
    }
  },
  build: {
    target: 'es2015',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
        manualChunks: {
          'element-plus': ['element-plus'],
          'echarts': ['echarts'],
          'lodash': ['lodash-es']
        }
      }
    }
  },
  server: {
    host: '0.0.0.0',
    port: 8081,
    open: true,
    cors: true,
    proxy: {
      // 直接连接到各个后端服务
      '/api/auth': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        timeout: 30000,
        pathRewrite: {
          '^/api': '/api'
        },
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('认证服务代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('认证服务代理请求:', req.method, req.url);
          });
        }
      },
      '/api/chat': {
        target: 'http://localhost:3004',
        changeOrigin: true,
        timeout: 0, // 解除超时限制，支持长时间AI响应
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('聊天服务代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('聊天服务代理请求:', req.method, req.url);
          });
        }
      },
      '/api/config': {
        target: 'http://localhost:3003',
        changeOrigin: true,
        timeout: 30000,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('配置服务代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('配置服务代理请求:', req.method, req.url);
          });
        }
      },
      '/api/ai/models': {
        target: 'http://localhost:3003',
        changeOrigin: true,
        timeout: 30000,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('AI模型配置代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('AI模型配置代理请求:', req.method, req.url);
          });
        }
      },
      '/api/ai': {
        target: 'http://localhost:3005',
        changeOrigin: true,
        timeout: 30000,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('AI服务代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('AI服务代理请求:', req.method, req.url);
          });
        }
      },
      '/api/workflows': {
        target: 'http://localhost:3005',
        changeOrigin: true,
        timeout: 30000,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('工作流服务代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('工作流服务代理请求:', req.method, req.url);
          });
        }
      },
      '/api/traces': {
        target: 'http://localhost:3005',
        changeOrigin: true,
        timeout: 30000,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('Traces代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Traces代理请求:', req.method, req.url);
          });
        }
      },
      '/api/coze-studio': {
        target: 'http://localhost:3005',
        changeOrigin: true,
        timeout: 30000,
        rewrite: (path) => path.replace(/^\/api\/coze-studio/, '/api'),
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('Coze Studio代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Coze Studio代理请求:', req.method, req.url);
          });
        }
      },
      '/health': {
        target: 'http://localhost:3004',
        changeOrigin: true,
        timeout: 10000,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('健康检查代理错误:', err.message);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('健康检查代理请求:', req.method, req.url);
          });
        }
      }
    }
  }
})