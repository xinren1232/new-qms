/*
 * @Author: BanLi
 * @Date: 2021-04-16 18:24:07
 * @LastEditors: BanLi
 * @LastEditTime: 2021-04-27 15:27:11
 * @Description: 组件搜索过滤数据
 */

export default {
  data() {
    return {
      keyword: '',
      showFilter: true
    }
  },

  watch: { keyword: '_filterNode' },

  methods: {
    /**
     * 当关键词发生变化时节流执行过滤方法
     * @param {string} keyword 新输入的内容
     * @param {string} oldValue 原先输入的内容
     */
    _filterNode(keyword, oldValue) {
      if (!oldValue && !keyword) return

      this._debounceEvent(this.$refs.tree, 'filter', keyword)
    },

    /**
     * 数据过滤方法 输入时忽略大小写
     * @param {string} value 过滤时的关键IC
     * @param {object} data 数据对象
     * @returns {boolean} 返回数据时候符合规则
     */
    _filterNodeMethods(value, data) {
      if (!value) return true

      // 忽略大小写
      return !!~data[this.labelField].toLowerCase().indexOf(value.toLowerCase())
    },

    /**
     * TOOL: 直接过滤节点数据 不出现搜索输入框
     * @param {string} keyword 想要设置的过滤关键字
     */
    setKeyword(keyword) {
      this.keyword = keyword
    },

    /**
     * TOOL: 调用时会打开搜素框并输入传入的关键词
     * @param {string} keyword 过滤节点的关键词
     */
    searchNode(keyword) {
      this.showFilter = !this.showFilter

      this.showFilter &&
        this.$nextTick(() => {
          this.showFilter && this.$refs.filterInput.select()
          this.setKeyword(keyword)
        })
    }
  }
}
