import Vue from 'vue'

import { debounceClick, outsideClick } from './click'
import dialogDrag from './drag-dialog'
import select from './select'
import loadmore from './table-loadmore'

const directives = [ debounceClick, outsideClick, loadmore, dialogDrag, select]

directives.forEach((dir) => {
  Vue.directive(dir.name, dir)
})
