import request from '@/app/request'

const BASE_API_CONFIG = process.env.VUE_APP_BASE_API

export const getViewInfoByViewId = (viewId) => request.get(`${BASE_API_CONFIG}ipm/view/getOne/${viewId}`)
