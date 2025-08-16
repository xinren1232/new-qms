
// 用于 color-picker 的内置颜色
export const universalColors = [
  'rgba(255, 0, 0, 0.11)',
  'rgba(255, 105, 37, 0.11)',
  'rgba(255, 166, 0, 0.11)',
  'rgba(244, 140, 75, 0.11)',
  'rgba(133, 63, 63, 0.11)',
  'rgba(0, 25, 255, 0.11)',
  'rgba(23, 180, 26, 0.11)',
  'rgba(30, 144, 255, 0.11)',
  'rgba(19, 209, 167, 0.11)',
  'rgba(198, 31, 231, 0.11)',
  'rgba(70, 101, 170, 0.11)',
  'rgba(128, 197, 55, 0.11)',
  'rgba(111, 111, 111, 0.11)',
  'rgba(0, 0, 0, 0.1)'
]

export function replaceOpacity(color) {
  if (!color) {
    return ''
  }

  return color.replace(/\((.*),\s?(.*),\s?(.*),\s?(.*)\)/, '($1, $2, $3, 1)')
}
