import { debounce } from 'lodash'
import { ref } from 'vue'

import request from '@/app/request.js'
import { typeOf } from '@/utils/index.js'

/**
 * @typedef RequestOptions
 * @property { number } delay 防抖的延迟时间
 * @property { boolean } raw 判断是否返回原本的后端返回体（包括 data、success、message 字段）
 *  如果是 false 则只保存 res.data 数据
 * @property { number } loadingDelay 延迟多久设置加载状态，可能有些接口的请求速度非常快，
 *  那么得到响应的时候 loading 会一闪而过，设置这个属性可以避免多余的加载
 * @property { boolean } immediate 是否在调用 useRequest 方法的时候就请求数据，
 *  如果是 false 则需要手动调用 run 方法
 */
/**
 * 对请求实例进行封装，在方法内部管理加载和请求数据状态，
 * 仅限于在 setup || script setup 的方式使用，optionsAPI 的方式不适用
 * @param {Promise|Function} requestPromise 数据请求 promise 或是一个请求函数
 * @param {RequestOptions} options 配置项
 * @example
 * import { useRequest } from '@/composables/request'
 * // 例子1: 当做 axios 使用(传入一个对象)
 * export function getData(data) {
 *   return useRequest({ url: '/ipm/CodingRules/query', method: 'POST', data })
 * }
 * // 例子2: 包装一个 axios 请求(传入一个 Promise)
 * export function getData(data) {
 *  return useRequest(request.post('/ipm/CodingRules/query', data))
 * }
 *
 * // 例子3: 包装已有的 axios 请求(传入一个返回 Promise 实例的函数)
 * import { queryCodingList } from '@/api/coding'
 * export function getData() {
 *  return useRequest(queryCodingList)
 * }
 *
 * // 使用
 * import { getData } from '@/api/coding'
 *
 * export default {
 *   setup() {
 *     const { data, loading } = getData()
 *
 *     return { data, loading }
 *   }
 * }
 */
export function useRequest(requestPromise, { delay = 300, immediate = true, loadingDelay = 300, raw = false } = {}) {
  let timeoutId
  const data = ref(null)
  const error = ref(false)
  const loading = ref(false)

  async function run() {
    // 这里重置数据是为了在再次调用 run 方法的时候还保留之前的数据
    error.value = null
    data.value = null
    // 延迟设置加载状态
    timeoutId = setTimeout(() => {
      loading.value = true
    }, loadingDelay)

    try {
      /**
       * @type {{ success: boolean, message: string, data: any }}
       */
      let result

      if (typeOf(requestPromise) === 'function') {
        result = await requestPromise()
      } else if (typeOf(requestPromise) === 'promise') {
        result = await requestPromise
      } else if (typeOf(requestPromise) === 'object') {
        // 如果传递的是一个对象，则可以直接传递 axios 实例参数的形式来直接请求数据
        result = await request(requestPromise)
      }

      data.value = raw ? result : result.data
    } catch (error) {
      error.value = error
    } finally {
      clearTimeout(timeoutId)
      loading.value = false
    }
  }

  immediate && run()

  return {
    loading,
    error,
    data,
    run: debounce(run, delay)
  }
}
