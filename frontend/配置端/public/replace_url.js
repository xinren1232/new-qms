(function () {
  const locationPath = ~location.href.indexOf('?') ? location.href.slice(location.href.indexOf('?')) : ''

  if (!locationPath) return

  const searchParams = new URLSearchParams(decodeURIComponent(locationPath))
  const params = Object.fromEntries(searchParams.entries())

  if (!params.token || !params.rtoken) {
    return
  }

  const currentDate = new Date()
  const endDate = new Date(currentDate.getTime() + 30 * 24 * 60 * 60 * 1000).toUTCString()
  // const domain = location.href.includes('ipm.transsion.com') ? 'ipm.transsion.com' : ''

  // 设置本地 cookie
  document.cookie = `ipm-token=${params.token}; expires=${endDate}; path=/;`
  document.cookie = `ipm-rtoken=${params.rtoken}; expires=${endDate}; path=/;`

  // 清空cookie，防止在重新地址的时候拼接上
  params.token = params.rtoken = params.utoken = ''

  const { hash, origin, pathname } = location

  const [path, _params] = hash.split('?')
  let replaceUrl = `${pathname}/${path}`.replace(/\/+/g, '/')

  replaceUrl += Object.keys(params).reduce((str, key) => {
    if (params[key]) {
      str += `${key}=${params[key]}&`
    }

    return str
  }, '?').slice(0, -1)

  history.replaceState(null, null, origin + replaceUrl)
})()
