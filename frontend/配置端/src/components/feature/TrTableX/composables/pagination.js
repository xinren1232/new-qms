import { computed, reactive } from 'vue'

export const usePagerVisible = (props) => {
  const { pagination } = props
  // 分页器配置
  const paginationConfig = reactive(Object.assign({}, {
    total: props.data.length,
    currentPage: 1,
    pageSize: pagination?.pageSize || pagination?.pageSizes[0] || 20,
    layout: 'total,prev,pager,sizes,next'
  }, props.pagination))

  // 是否显示分页器
  const pagerVisible = computed(() => {
    if (props.showPager === 'auto') {
      return props.data.length >= paginationConfig.pageSize
    }

    return props.showPager
  })

  return {
    pagerVisible,
    paginationConfig
  }
}
