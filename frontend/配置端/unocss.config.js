import {
  defineConfig,
  presetIcons,
  presetMini,
  transformerDirectives,
  transformerVariantGroup
} from 'unocss'

export default defineConfig({
  content: {
    pipeline: {
      exclude: ['src/content/**']
    }
  },
  rules: [
    // va--2px => vertical-align: -2px
    [/^va-(.+)$/, ([, d]) => ({ 'vertical-align': d })],
    [/^rotate-y-full$/, () => ({ transform: 'rotateY(180deg)' })],
    [/^rotate-x-full$/, () => ({ transform: 'rotateX(180deg)' })],
    [/^letter-spacing-(.+)$/, ([, d]) => ({ 'letter-spacing': d })]
  ],

  // shortcuts: [
  // ],

  presets: [
    presetMini(),
    presetIcons({
      scale: 1,
      warn: true,
      extraProperties: {
        display: 'inline-block',
        'vertical-align': '-2px'
      }
    })
  ],
  transformers: [
    transformerDirectives(),
    transformerVariantGroup()
  ]
})
