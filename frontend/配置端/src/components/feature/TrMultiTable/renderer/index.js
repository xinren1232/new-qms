// 动态引入 import renderer文件夹下的所有文件
const files = require.context('./', true, /\.js$/)

files.keys().forEach(key => {
  if (key === './index.js') return
  files(key)
})



