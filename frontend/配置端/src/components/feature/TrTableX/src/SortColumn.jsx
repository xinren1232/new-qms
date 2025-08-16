// eslint-disable-next-line no-unused-vars
import { h } from 'vue'

import { SORT_CLASS_NAME } from '@/composables/sortable.js'

export default {
  label: '',
  prop: 'sortable',
  align: 'center',
  width: 35,
  slots: {
    default: () => {
      return (
        <div class={'select-none ' + SORT_CLASS_NAME}>
          <span
            class="el-icon-rank cursor-pointer font-bold"
            title="拖动排序"
            style="font-size:14px"
          ></span>
        </div>
      )
    }
  }
}
