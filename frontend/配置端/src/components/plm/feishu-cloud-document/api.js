import { getRToken, getToken } from '@/app/cookie'
import request from '@/app/request'

// 获取飞书鉴权参数
export const getUserAuthentication = () => {
  return request({
    url:
      process.env.VUE_APP_GATEWAY_URL +
      '/service-user-common/gl-user-common/LarkAuthenticationRemoteService/userAuthentication',
    method: 'post',
    data: {
      token: getToken(),
      rToken: getRToken(),
      url: 'ipm.transsion.com',
      app: 'IPM'
    }
  })
}

// 获取飞书app_access_token

export const getFeishuAppAccessToken = () => {
  fetch('https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      app_id: 'cli_a0f8b7c7b0b9100e',
      app_secret: 'ZjY5ZjY4ZjYtZjY5ZC00ZjY5LWI2ZjYtZjY5ZjY4ZjY5ZjY5'
    })
  })
    .then((res) => res.json())
    .then((json) => {
      console.log(json)
    }
    )
}


