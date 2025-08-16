import Cookie from 'js-cookie'

export default {
  computed: {
    employeeNo() {
      return JSON.parse(Cookie.get('user')).employeeNo
    }
  },
  mounted() {
    this.getUserCheckLayout()
  },
  methods: {
    // 获取localStorage记录的用户操作
    getUserCheckLayout() {
      this.$nextTick(() => {
        this.layout = this.userCheckLayout?.find((item) => item.employeeNo === this.employeeNo)?.layout || false
      })
    },
    setUserCheckLayout(val) {
      if (this.employeeNo && this.userCheckLayout) {
        if (this.userCheckLayout.some((item) => item.employeeNo === this.employeeNo)) {
          // 存在用户的记录
          this.userCheckLayout.forEach((item) => {
            if (item.employeeNo === this.employeeNo) item.layout = this.layout
          })
        } else {
          const _userCheckLayout = {
            employeeNo: this.employeeNo,
            layout: this.layout
          }

          this.userCheckLayout.push(_userCheckLayout)
        }
      }
      localStorage.setItem('tr.ipm.' + val + '.layout', JSON.stringify(this.userCheckLayout))
    }
  }
}
