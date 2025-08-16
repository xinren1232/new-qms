import { useStorageRef } from '@/composables/storage-ref.js'

const cacheKey = 'transcend.saved.theme'

export const THEME_MAPS = {
  Transsion: { name: 'Transsion', color: 'rgba(84, 0, 200, 1)' },
  TECNO: { name: 'TECNO', color: 'rgba(37, 99, 235, 1)' },
  Infinix: { name: 'Infinix', color: 'rgba(0, 188, 112, 1)' },
  itel: { name: 'itel', color: 'rgba(241, 32, 0, 1)' }
}

export function useSystemTheme() {
  const theme = useStorageRef({ cacheKey, defaultValue: 'Transsion' })

  const bodyStyle = document.body.style
  const THEME_SCALES = [.9, .8, .7, .6, .5, .4, .3, .12, .08]

  function onChangeThemeColor(themeKey) {
    if (!themeKey || !THEME_MAPS[themeKey]) {
      themeKey = 'Transsion'
    }

    if (themeKey === 'Transsion') {
      bodyStyle.cssText = ''

      return
    }

    let cssText = `--primary: ${THEME_MAPS[themeKey].color};`

    THEME_SCALES.forEach((scale, index) => {
      cssText += `--primary-${index + 1}: ${THEME_MAPS[themeKey].color.slice(0, -2)}${scale});`
    })

    bodyStyle.cssText = cssText
  }

  onChangeThemeColor(theme.value)

  return {
    theme,
    onChange: onChangeThemeColor
  }
}
