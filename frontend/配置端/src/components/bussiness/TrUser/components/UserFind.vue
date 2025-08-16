<template>
  <el-input
    ref="input"
    v-model="userName"
    clearable
    prefix-icon="el-icon-search"
    :placeholder="$t('component.trUser.search')"
    @input="findUser"
  />
</template>

<script>
import { debounce } from 'lodash'

export default {
  name: 'UserFind',
  props: {
    httpService: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      userName: ''
    }
  },
  async mounted() {
    await this.$nextTick()
    this.$refs.input.focus()
  },
  methods: {
    findUser: debounce(function () {
      this.$emit('handleUserFind', this.userName)
    }, 500)
  }
}
</script>
