<template>
  <div>
    <tr-table
      v-if="type === 'table'"
      :data="data"
      :columns="columns"
    />
    <tr-remote-search
      v-if="type === 'form'"
      v-model="selectData"
      :value="selectData"
      :disabled="disabled"
      :remote-method="getDicValues"
      v-bind="$attrs"
    />
  </div>
</template>

<script>
import { getDicValues } from '@/api/reusable-properties'
import TrRemoteSearch from '@@/feature/TrRemoteSearch'
import TrTable from '@@/feature/TrTable'

export default {
  components: { TrTable, TrRemoteSearch },
  props: {
    // values: {
    //   type: String,
    //   default: ''
    // },
    type: {
      type: String,
      default: 'form'
    },
    data: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      selectData: '',
      getDicValues,
      columns: [
        { label: '选项key', prop: 'key', align: 'center' },
        { label: '选项值', prop: 'value', align: 'center' },
        { label: '说明', prop: 'note', align: 'center' }
      ]
    }
  },
  watch: {
    data(val) {
      this.selectData = val
    }
  }
}
</script>

<style></style>
