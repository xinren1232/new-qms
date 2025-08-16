import { ref, watch } from 'vue'

export const useAsyncData = ({ config, emit, props }) => {
  const tableData = ref([])
  const loading = ref(false)

  // 如果外部数据变化则更新组件内部数据
  watch(
    () => props.data,
    (value) => {
      tableData.value = value?.length ? [...value] : []
    },
    { deep: true, immediate: true }
  )

  const query = async (_params = null) => {
    if (!props.httpRequest) return
    const params = Object.assign({}, props.params, _params)

    if (props.showPager) {
      params.current = config.currentPage
      params.size = config.pageSize
    }
    loading.value = true
    const { data } = await props.httpRequest(params).finally(() => {
      loading.value = false
    })

    if (!data) return

    if (config.currentPage > 1 && !data.data?.length) {
      config.currentPage = 1
      await query()
    } else {
      config.total = ~~data?.total ?? data.length
      const _data = data.data ?? data ?? []

      tableData.value = [..._data]

      emit('input', tableData.value)
      emit('completed', tableData.value)
    }
  }

  const onSizeChange = async (size) => {
    config.pageSize = size
    await query()
  }

  const onPageChange = async (current) => {
    config.currentPage = current
    await query()
  }

  props.immediate && query()

  return {
    loading,
    query,
    tableData,
    onSizeChange,
    onPageChange
  }
}
