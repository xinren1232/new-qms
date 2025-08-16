export default {
  name: 'loadmore',
  bind(el, binding) {
    const selectWrap = el.querySelector('.el-table__body-wrapper') || el.querySelector('.vxe-table--body-wrapper')

    selectWrap.addEventListener('scroll', function () {
      const sign = 1
      const scrollDistance = this.scrollHeight - this.scrollTop - this.clientHeight

      if (scrollDistance <= sign) {
        binding.value()
      }
    })
  }
}
