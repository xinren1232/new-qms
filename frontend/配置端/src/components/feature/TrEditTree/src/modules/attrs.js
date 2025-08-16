/*
 * @Author: BanLi
 * @Date: 2021-04-28 10:43:15
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2021-07-02 18:16:37
 * @Description: 组件内部所有接收的props, 单独保存是为了做一份汇总,
 *               因为多份mixins各有不同的poops, 查看定义需要反复横跳, 所以集中在一起
 *               使用时只需要关注用到了哪些参数即可
 */

const PROPS = {
  data: {
    type: Array,
    default: () => [],
    comment: '节点数据列表'
  },
  nodeKey: {
    type: String,
    default: 'bid',
    comment: '用哪个字段用作 node 的唯一标识'
  },
  draggable: {
    type: Boolean,
    default: true,
    comment: '是否启用节点拖拽功能'
  },
  dataTemplate: {
    type: Object,
    default: () => ({}),
    comment: '新建节点时的数据模板'
  },
  layout: {
    type: String,
    default: 'create,remove,search,moveUp,moveDown,collapse,expand',
    comment: '顶栏显示的按钮'
  },
  showButtons: {
    type: Boolean,
    default: true,
    comment: '是否展示操作按钮'
  },
  // nameValidate: {
  //   type: [RegExp, String, Array],
  //   default: () => [
  //     '^(\\w|[\\u4e00-\\u9fa5]|[^%&!@#$%*,;=?$\x22])+$',
  //     '名称只能包含汉字、字母、数字以及下划线、空格、引号、斜线、括号！'
  //   ],
  //   comment: '保存节点名称校验正则'
  // },
  extendsMenus: {
    type: Array,
    default: () => [],
    comment: `右上角扩展菜单
      {
        label: string, // 菜单文本
        handler: function // 点击触发时执行的事件
      }
    `
  },
  contextMenus: {
    type: Array,
    default: () => [],
    comment: `右键扩展菜单
      {
        label: string, // 菜单文本
        handler: function // 点击触发时执行的事件
      }
    `
  },
  menusStyle: {
    type: Object,
    default() {
      return {}
    }
  },
  createCustomEditing: {
    type: Boolean,
    default: true,
    comment: '是否自定义设置编辑状态'
  }
}

/**
 * 根据传入的属性名来返回相同名字的props
 * @param {string|array} props 需要返回的props
 * @returns 返回符合输入定义的props
 * @description 支持字符串和数组两种形式, 字符串形式以逗号分隔
 */
export function defineProps(props) {
  if (!props) return []

  if (typeof props === 'string') {
    props = props.split(',').map((prop) => prop.trim())
  }

  if (typeof props === 'object' && props.length === 1) {
    return { [props[0]]: PROPS[props[0]] }
  }

  return props.reduce((prev, prop) => {
    prev[prop] = PROPS[prop]

    return prev
  }, {})
}
