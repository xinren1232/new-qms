
export function useLazyLoading({ delay = 300, defaultValue = false } = {}) {
  let timeoutId

  const loading = shallowRef(defaultValue)

  function startLoading() {
    timeoutId = window.setTimeout(() => {
      loading.value = true
    }, delay)
  }

  function cancelLoading() {
    clearTimeout(timeoutId)
    loading.value = false
  }

  return {
    loading,
    startLoading,
    cancelLoading
  }
}
