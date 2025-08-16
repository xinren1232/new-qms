import { getMyTodoNumber, getRoleFLowBtnAuth } from '@/api/role-flow'
import RoleFlowList from '@/views/project-manage/modules/team-manage/component/role-flow/index'

export default {
  components: { RoleFlowList },
  data() {
    return {
      showRoleFlowAuth: false,
      todoNumber: 0
    }
  },
  mounted() {
    this.getMyTodoNumber()
    this.getRoleFLowBtnAuthPost()
  },
  created() {
    this.$store.dispatch('dict/queryDict', 'MEMBER_CHANGE_STATE_CODE')
  },
  methods: {
    async getRoleFLowBtnAuthPost() {
      const domainBid = this.$route.params.id
      const { data } = await getRoleFLowBtnAuth(domainBid)

      this.showRoleFlowAuth = data
    },
    showRoleFlow() {
      this.$refs.roleFlowList.show()
    },
    async getMyTodoNumber() {
      const { data } = await getMyTodoNumber(this.$route.params.id)

      this.todoNumber = data || 0
    }
  }
}
