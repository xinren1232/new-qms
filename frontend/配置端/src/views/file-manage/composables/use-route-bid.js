
// 当首次保存以后讲 add 替换为数据的 bid，这样保存以后刷新页面就是编辑了
export function replaceAddToBid(dataBid) {
  // 把 add 替换成 bid
  const fullPath = location.href.replace('add', dataBid)

  // 然后替换当前url地址
  window.history.replaceState(null, '', fullPath)
}
