export default {
  handleInputCustomEvent(value,row,column) {
    if (column.onInput) {
      let customFn = new Function('value','row','column', column.onInput)

      customFn.call(this, value, row, column)
    }
  },
  handleChangeCustomEvent(value,row,column) {
    if (column.onInput) {
      let customFn = new Function('value','row','column', column.onInput)

      customFn.call(this, value, row, column)
    }
  },
  syncUpdateFormModel(value,row,column) {
    row[column.field] = value
  }
}
