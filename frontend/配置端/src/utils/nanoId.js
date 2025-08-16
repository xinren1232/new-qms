import { nanoid } from 'nanoid'

/**
@name: nanoid
@description: 自定义数据体生成ID
@author: jiaqiang.zhang
@time: 2022-06-29 14:13:55
**/
const nanoId = (size = 16) => {
  return nanoid(size) + '_'
}

export { nanoId }
