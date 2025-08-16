<template>
  <el-tree
    v-loading="loading"
    class="user-organization"
    :props="defaultProps"
    :lazy="true"
    :load="loadChildren"
  >
    <span
      slot-scope="{ node, data }"
      class="user-organization-content"
    >
      <span v-if="data.type === 0">
        <span>
          <i class="el-icon-folder" />
          {{ node.label }}
        </span>
        <span
          v-if="multiple && data.deptId"
          class="organization-check"
        >
          <el-button
            size="mini"
            icon="el-icon-check"
            circle
            @click="organizationCheck(data, $event)"
          />
        </span>
      </span>
      <el-row
        v-else
        type="flex"
        justify="start"
        :gutter="10"
        @click.native="handleRowClick(data)"
        @dblclick.native="handleRowDbClick(user)"
      >
        <el-col :span="2">
          <user-avatar
            :user="data"
            :size="20"
            img-size="S"
          />
        </el-col>
        <el-col
          :span="6"
          class="overflow-ellispe"
        >
          {{ data.employeeName }}
        </el-col>
        <el-col
          :span="8"
          class="overflow-ellispe"
        >
          {{ data.employeeNo }}
        </el-col>
        <el-col
          :span="8"
          class="overflow-ellispe"
        >
          {{ data.deptName }}
        </el-col>
      </el-row>
    </span>
  </el-tree>
</template>

<script>
import UserAvatar from './UserAvatar'

export default {
  name: 'UserOrganization',
  components: { UserAvatar },
  inject: ['httpService', 'multiple'],
  props: {
    activeIndex: {
      type: String,
      default: 'organization'
    },
    root: {
      type: Object,
      default: () => ({
        id: 0,
        name: '深圳传音控股',
        parentId: 0,
        parentName: null,
        deptName: '深圳传音控股',
        type: 0,
        deptId: 0,
        children: []
      })
    }
  },
  data() {
    return {
      loading: false,
      treeData: [],
      defaultProps: {
        label: (data) => (data.type === 1 ? data.employeeName : data.deptName),
        isLeaf: (data) => data.leafType === 1,
        children: 'children'
      },
      rootNode: null
    }
  },
  created() {
    if (this.activeIndex === 'organization') {
      this.loading = true
      this.httpService
        .queryRootDeptList()
        .then((data) => {
          if (data[0].deptId !== '55999999') {
            // 把传音控股放到最前面
            const index = data.findIndex((item) => item.deptId === '55999999')

            if (index > -1) {
              data.unshift(data[index])
              data.splice(index + 1, 1)
            }
          }
          this.treeData = data || []
          // 默认展开第一层
          if (this.rootNode) {
            const node = this.rootNode.childNodes[0]

            node.expanded = true
            node.loadData()
          }
        })
        .finally(() => {
          this.loading = false
        })
    }
  },
  methods: {
    parseRootData(originData) {
      return (originData || []).map((s) => ({
        ...s,
        deptName: s.name,
        type: 0,
        deptId: s.id
      }))
    },
    async loadChildren(node, resolve) {
      if (node.level === 0) {
        this.rootNode = node
        resolve([this.root])
      } else if (node.level === 1 && this.activeIndex === 'organization') {
        resolve(this.treeData)
      } else {
        const data = await this.httpService.queryChildDeptAndEmployee(node.data.deptId)

        resolve(data || [])
      }
    },
    handleRowClick(user) {
      this.$emit('handleUserSelect', user)
    },
    handleRowDbClick(user) {
      this.$emit('dblclick', user)
    },
    organizationCheck(data, e) {
      e.stopPropagation()
      this.$emit('handleOrganizationSelect', data)
    }
  }
}
</script>

<style scoped>
.user-organization ::v-deep .el-tree-node__content:hover {
  background-color: #ecf8fe;
}

.organization-check {
  display: none;
  position: absolute;
  right: 10px;
  top: -5px;
}

.user-organization ::v-deep .el-tree-node__content:hover .organization-check {
  display: inline-block;
}

.user-organization ::v-deep .el-tree-node__content {
  height: 32px;
}

.user-organization-content {
  display: inline-block;
  width: 100%;
  position: relative;
}
</style>
