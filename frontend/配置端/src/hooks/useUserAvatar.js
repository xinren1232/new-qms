/**
 * @name: generateUserAvatar
 * @description: 生成用户头像
 * @param {String} jobNumber 工号
 */
export function generateUserAvatar(jobNumber, size = 'H') {
  if (!jobNumber) return ''
  const a1 = jobNumber.slice(0, 4)
  const a2 = jobNumber.slice(4, 8)

  return `https://pfresource.transsion.com:19997/${a1}/${a2}/${size}_${jobNumber}_D.png`
}
