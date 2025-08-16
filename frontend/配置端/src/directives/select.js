/**
 * 应用在element的输入框里面，聚焦时全选
 */
export default {
  name: 'select',
  inserted(el) {
    const inputEl = el.getElementsByTagName('input')[0]

    inputEl.onfocus = function() {
      inputEl.select()
    }
  }
}
