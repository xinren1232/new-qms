import { VXETable } from 'vxe-table'
import store from '@/store'
import UserSelect from '../components/user-select/index.vue'
import defaultIcon from '@/assets/images/default-user.png'

VXETable.renderer.add('userEditor', {
  renderEdit (h, renderOpts, { row, column }) {
    return <UserSelect v-model={row[column.field]} />
  },
  renderCell (h, renderOpts, { row, column }) {
    return [
      <div style="display: flex;align-items: center">
        <el-avatar style="margin-right:3px" src={renderUserAvatar(row[column.field])} size={22}>
          <img src={defaultIcon} alt="" />
        </el-avatar>
        <span>{ renderUserName(row[column.field]) }</span>
      </div>
    ]
  }

})
const renderUserAvatar = jobNumber => {
  if (!jobNumber) return ''
  const a1 = jobNumber.slice(0, 4)
  const a2 = jobNumber.slice(4, 8)

  return `https://pfresource.transsion.com:19997/${a1}/${a2}/H_${jobNumber}_D.png`
}
const renderUserName = jobNumber => {
  const userInfoMap = store.getters.userInfoMap
  const userInfo = userInfoMap[jobNumber]

  return userInfo ? userInfo?.name || userInfo?.realName : jobNumber
}
