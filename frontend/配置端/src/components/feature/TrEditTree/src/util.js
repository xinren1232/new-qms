export function traverseNode(node, fun) {
  fun(node)
  if (node.childNodes) {
    node.childNodes.forEach((child) => {
      traverseNode(child, fun)
    })
  }
}

// 找到当前操作的最后一个不能为root 也不能为最外层的父节点
export function findLastParent(node) {
  if (!node || !Object.keys(node).length) {
    return {}
  } else if (node?.data?.isRoot || node?.parent?.data.isRoot) {
    return node
  } else {
    return findLastParent(node?.parent)
  }
}
