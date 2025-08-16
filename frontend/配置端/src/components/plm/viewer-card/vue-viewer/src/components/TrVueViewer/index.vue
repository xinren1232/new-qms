<script>
import { genStyleInjectionCode } from './utils/sfcParser/styleInjection'
import { isEmpty, extend } from './utils/util'
import { addStylesClient } from './utils/style-loader/addStylesClient'
import errorsMantle from './components/error-mantle.vue'

const compiler = require('vue-template-compiler')

export default {
  components: { errorsMantle },
  // eslint-disable-next-line vue/require-prop-types
  props: ['code', 'viewId'],
  data() {
    return {
      strError: '',
      dynamicComponent: {
        component: {
          template: '<div>Hello Transsioner!</div>'
        }
      }
    }
  },
  watch: {
    code() {
      this.init()
    }
  },
  created() {
    this.stylesUpdateHandler = addStylesClient(this.viewId, {})
  },
  mounted() {
    this.init()
  },
  methods: {
    async init() {
      const demoComponent = {}
      const compilerComp = compiler.parseComponent(this.code)
      const { template, script, styles, errors } = compilerComp

      // errors
      if (errors && errors.length) {
        const errorStr = `Error compiling template:\n\n` +
            errors.map((e) => `  - ${e}`).join('\n') +
            '\n\n'

        this.strError = errorStr

        return
      } else {
        this.strError = ''
      }

      const templateCode = template ? template.content.trim() : ``
      let scriptCode = script ? script.content.trim() : ``
      let styleCodes

      try {
        styleCodes = await genStyleInjectionCode(styles, this.viewId)
      } catch (error) {
        const { stack } = error

        this.strError = stack
      }

      // script
      if (!isEmpty(scriptCode)) {
        // eslint-disable-next-line prefer-const
        let componentScript = {}

        scriptCode = scriptCode.replace(
          /export\s+default/,
          'componentScript ='
        )
        // eslint-disable-next-line no-eval
        eval(scriptCode)
        // update component's content
        extend(demoComponent, componentScript)
      }

      // template
      demoComponent.template = `<section id="${this.viewId}" class="result-box" >
        ${templateCode}
      </section>`

      // style
      this.stylesUpdateHandler(styleCodes)
      this.dynamicComponent.component = demoComponent
    }
  },
  render() {
    const renderComponent = this.dynamicComponent.component

    return (<div class='vue-view-container'>
      <renderComponent></renderComponent>
      <errorsMantle v-show={this.strError} scopedSlots={{
        strError: props => {
          return (this.strError)
        }
      }}>
      </errorsMantle>
    </div>)
  }
}
</script>

<style lang="scss" scoped>
.vue-editor__mantle {
  background-color: rgba(0, 0, 0, 0.85);
  position: absolute;
  box-sizing: border-box;
  inset: 0px;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.85);
  color: rgb(232, 232, 232);
  font-family: Menlo, Consolas, monospace;
  font-size: large;
  padding: 10px;
  line-height: 1.2;
  white-space: pre-wrap;
  overflow: auto;
}
.vue-view-container {
  position: relative;
}

</style>
