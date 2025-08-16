import Cookie from 'js-cookie'

import { parseString } from '@/utils/index.js'

const getters = {
  online: (state) => state.app.online,
  sidebar: (state) => state.app.sidebar,
  language: (state) => state.app.language,
  size: (state) => state.app.size,
  device: (state) => state.app.device,
  user: (state) => state.user.user,
  name: (state) => state.user.user?.name,
  uid: (state) => state.user.user?.uid,
  employeeNo: (state) => state.user.user?.employeeNo || parseString(Cookie.get('user'))?.employeeNo,
  userMap: (state) => state.user.userMap,
  userInfoMap: (state) => state.user.userInfoMap,
  dict: (state) => state.dict,
  token: (state) => state.user.token,
  rtoken: (state) => state.user.rtoken,
  appRoutes: (state) => state.permission.appRoutes,
  permission_routes: (state) => state.permission.routes
}

export default getters
