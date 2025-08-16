'use strict'

const webpack = require('webpack')
// const { name } = require('./package')

// 开发环境下显示版本信息
if (process.env.NODE_ENV === 'development') {
  console.log('====node=====webpack====', process.version, webpack.version)
}
const { join } = require('path')

// const ModuleFederationPlugin = require('webpack').container.ModuleFederationPlugin // 引入webpack 模块联邦插件
const SpeedMeasurePlugin = require('speed-measure-webpack-plugin')
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin')// monaco-editor 代码智能提示

const UnoCSS = require('@unocss/webpack').default
const AutoImport = require('unplugin-auto-import/webpack')

// 获取本机ip
// const getLocalIp = () => {
//   let ip = ''
//   const os = require('os')

//   const networkInterfaces = os.networkInterfaces()

//   for (const key in networkInterfaces) {
//     const networkInterface = networkInterfaces[key]
//     const iface = networkInterface.find(item => item.family === 'IPv4' && item.address !== '')

//     if (iface) {
//       ip = iface.address
//       break
//     } else {
//       ip = 'localhost'
//     }
//   }
//   return ip
// }

function resolve(dir) {return join(__dirname, dir)}

const isBuild = process.argv[2] === 'build'
const publicPath = process.env.VUE_APP_PUBLIC_PATH
const isProd = process.env.NODE_ENV === 'production'
// 压缩插件
const CompressionPlugin = require('compression-webpack-plugin')
// const FileManagerPlugin = require('filemanager-webpack-plugin')

const outputDir = 'dist'

module.exports = {
  publicPath: publicPath,
  assetsDir: 'static',
  lintOnSave: !isProd,
  outputDir,
  parallel: true, // 打包构建时使用多进程处理babel编译
  productionSourceMap: false,
  devServer: {
    port: 8072,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, PATCH, OPTIONS',
      'Access-Control-Allow-Headers':
        'X-Requested-With, content-type, Authorization'
    },
    client: {
      overlay: false
    },
    proxy: {
      // 所有API请求统一代理到API网关
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  },
  css: {
    extract: isProd ? { ignoreOrder: true } : false
  },
  configureWebpack: {
    name: 'Transcend System Manage',
    devtool: isBuild ? false : 'source-map',
    resolve: {
      alias: {
        '@root': resolve('/'),
        '@': resolve('src'),
        '@@': resolve('src/components')
      },
      fallback: {
        fs: false
      }
    },
    // rules: [
    //   { test: /\.m?js/, resolve: { fullySpecified: false } }
    // ],
    plugins: [
      UnoCSS(),
      AutoImport({
        dts: true,
        eslintrc: {
          enabled: true,
          filepath: './.eslint-globals.json'
        },
        imports: [
          'vue',
          {
            'vue-router/composables': ['useRoute', 'useRouter']
          }
        ]
      }),
      // new ModuleFederationPlugin({
      //   remotes: { // 导入
      //     trWebCommon: `trWebCommon@http://${getLocalIp()}:8070/transcend_plm_web_common.js`
      //   }
      //
      // }),
      // new webpack.ProvidePlugin({
      //   process: 'process/browser'
      // }),
      new SpeedMeasurePlugin(),
      new MonacoWebpackPlugin(
        {
          languages: ['javascript','json','java']
        }
      )
    ],
    optimization: {
      realContentHash: true
    }
  },
  chainWebpack(config) {
    config.module.rule('vue').uses.delete('cache-loader')
    config.module.rule('tsx').uses.delete('cache-loader')
    config.merge({
      cache: false
    })

    config.plugins.delete('prefetch')
    config.plugin('provide').use(webpack.ProvidePlugin, [{ introJs: ['intro.js'] }]) // intro.js
    config.cache(true) // 开启缓存
    config.module.rule('svg').exclude.add(resolve('src/icons')).end() // 添加 svg loader
    config.module
      .rule('icons')
      .test(/\.svg$/)
      .include.add(resolve('src/icons'))
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
      .end()
    config.optimization.splitChunks({
      chunks: 'async',
      cacheGroups: {
        libs: {
          name: 'chunk-libs',
          test: /[\\/]node_modules[\\/]/,
          priority: 10,
          chunks: 'async' // 仅包装最初依赖的第三方包
        },
        antv: {
          name: 'chunk-antv6', // 将 elementUI 拆分为单个包
          priority: 20, // 权重需要大于 libs 和 app ，否则将打包成 libs 或 app
          test: /[\\/]node_modules[\\/]_?@antv\/x6(.*)/, // 为了兼容 cnpm
          chunks: 'async' // 仅包装最初依赖的第三方包
        },
        monacoEditor: {
          name: 'chunk-monacoEditor', // 将 elementUI 拆分为单个包
          priority: 20, // 权重需要大于 libs 和 app ，否则将打包成 libs 或 app
          test: /[\\/]node_modules[\\/]_?monaco-editor(.*)/, // 为了兼容 cnpm
          chunks: 'async' // 仅包装最初依赖的第三方包
        },
        elementUI: {
          name: 'chunk-elementUI', // 将 elementUI 拆分为单个包
          priority: 20, // 权重需要大于 libs 和 app ，否则将打包成 libs 或 app
          test: /[\\/]node_modules[\\/]_?element-ui(.*)/, // 为了兼容 cnpm
          chunks: 'async' // 仅包装最初依赖的第三方包
        },
        xlsx: {
          name: 'chunk-xlsx', // 将 xlsx 拆分为单个包
          priority: 20, // 权重需要大于 libs 和 app ，否则将打包成 libs 或 app
          test: /[\\/]node_modules[\\/]_?xlsx(.*)/, // 为了兼容 cnpm
          chunks: 'async' // 仅包装最初依赖的第三方包
        },
        wangeditor: {
          name: 'chunk-wangeditor', // 将 wangeditor 拆分为单个包
          priority: 20, // 权重需要大于 libs 和 app ，否则将打包成 libs 或 app
          test: /[\\/]node_modules[\\/]_?@wangeditor\/editor(.*)/, // 为了兼容 cnpm
          chunks: 'async' // 仅包装最初依赖的第三方包
        },
        tableX: {
          name: 'chunk-tableX', // 将 tableX 拆分为单个包
          priority: 20, // 权重需要大于 libs 和 app ，否则将打包成 libs 或 app
          test: /[\\/]node_modules[\\/]_?vxe-table(.*)/, // 为了兼容 cnpm
          chunks: 'async' // 仅包装最初依赖的第三方包
        },
        commons: {
          name: 'chunk-commons',
          test: resolve('src/components'), // 可以自定义规则
          minChunks: 3,
          priority: 5,
          reuseExistingChunk: true,
          chunks: 'async' // 仅包装最初依赖的第三方包
        }
      }
    })
    // 开启js、css压缩
    config.when(isProd, (config) => {
      config.optimization.minimize(true) // 压缩代码
      config.plugin('compressionPlugin').use(
        new CompressionPlugin({
          filename: '[path].gz[query]',
          algorithm: 'gzip',
          test: /\.js$|\.html$|.\css/, // 匹配文件名
          threshold: 10240, // 对超过10k的数据压缩
          deleteOriginalAssets: false // 删除源文件  开启删除务必通知网管nginx同步开启gzip
        })
      )
      config
        .plugin('speed-measure-webpack-plugin')
        .use(require('speed-measure-webpack-plugin'))
      // config.optimization.minimizer('terser').tap((args) => {
      //   args[0].terserOptions.compress.drop_console = true
      //
      //   return args
      // })
      config.optimization.runtimeChunk('single') // 优化持久化缓存的,optimization.runtimeChunk 就是告诉 webpack是否要把这部分单独打包出来.
      // config
      //   .plugin('webpack-bundle-analyzer')
      //   .use(require('webpack-bundle-analyzer').BundleAnalyzerPlugin)
    })
  }
}
