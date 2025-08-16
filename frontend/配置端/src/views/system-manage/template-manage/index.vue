<template>
  <div class="manage-box">
    <div class="render-lt">
      <template-tree
        ref="tree"
        :tree-data="treeData"
        @update-data="handleUpdateData"
      />
    </div>
    <div class="render-rt table-box">
      <tr-card class="box-card">
        <tr-form
          ref="form"
          v-model="form"
          :collapsible="false"
          :items="formItems"
          label-width="120px"
          @query="handleQuery"
          @reset="handleReset"
        />
        <el-button
          v-if="isDcpDeliverableAdmin"
          icon="el-icon-circle-plus-outline"
          type="primary"
          @click="onAddBtnClick"
        >
          新增
        </el-button>
        <tr-table
          ref="tableRef"
          v-model="tableData"
          :http-request="getTableData"
          :show-pager="true"
          :height="tableHeight"
          :immediate="false"
          :params="{ count: true, param: form }"
        >
          <el-table-column
            align="center"
            label="交付物模板名称"
            prop="deliverablesName"
            min-width="120"
          >
            <template slot-scope="{ row }">
              <el-button
                type="text"
                @click="btnClickHandler('view', row)"
              >
                {{ row.deliverablesName }}
              </el-button>
            </template>
          </el-table-column>
          -
          <el-table-column
            align="center"
            label="状态"
            prop="statusCode"
            min-width="140"
          >
            <template slot-scope="{ row }">
              <tr-switch
                v-model="row.statusCode"
                :params="setParams(row)"
                :disabled="!isDcpDeliverableAdmin"
                params-code="statusCode"
                :http-request="updateDcpDeliverables"
              />
            </template>
          </el-table-column>
          <el-table-column
            align="center"
            label="更新人"
            prop="updatedBy"
            min-width="100"
          />
          <el-table-column
            align="center"
            label="更新时间"
            prop="updatedTime"
            min-width="120"
          />
          <el-table-column
            align="center"
            label="文件"
            prop="fileName"
            width="80"
          >
            <template slot-scope="{ row }">
              <el-button
                type="text"
                @click="downloadFile(row)"
              >
                {{ row.fileName }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column
            align="center"
            label="操作"
          >
            <template slot-scope="{ row }">
              <el-button
                :disabled="!isDcpDeliverableAdmin"
                size="mini"
                type="primary"
                icon="el-icon-edit"
                circle
                @click="btnClickHandler('edit', row)"
              />
              <el-button
                :disabled="!isDcpDeliverableAdmin || row.statusCode !== 'off'"
                size="mini"
                type="danger"
                icon="el-icon-delete"
                circle
                @click="btnClickHandler('del', row)"
              />
            </template>
          </el-table-column>
        </tr-table>
        <addOrEditCodingDialog
          ref="addOrEdit"
          :type-code="currentType"
          @updateData="onAddConfirm"
        />
      </tr-card>
    </div>
  </div>
</template>

<script>
import { getObjectDetails } from '@/api/relation-object'
import { getRoleUser4Out } from '@/api/role'
import { deleteDcpDeliverables, downLoadDcpFile, getTableData, updateDcpDeliverables } from '@/api/template-manage'
import TrCard from '@@/feature/TrCard'
import TrForm from '@@/feature/TrForm'
import TrSwitch from '@@/feature/TrSwitch'
import TrTable from '@@/feature/TrTable'

import addOrEditCodingDialog from './components/add-or-edit-dialog'
import TemplateTree from './components/tree'

export default {
  components: {
    TemplateTree,
    TrCard,
    TrForm,
    TrTable,
    TrSwitch,
    addOrEditCodingDialog
  },
  data() {
    return {
      isDcpDeliverableAdmin: false, // 是否为DCP交付物管理员
      tableData: [],
      currentType: '',
      form: {
        deliverablesName: ''
      },
      currentTaskInfo: {},
      treeData: [
        {
          name: 'DCP交付物模板',
          bid: '984036445008302080'
        }
      ]
    }
  },
  computed: {
    loginUser({ $store }) {
      return $store.getters.user
    },
    formItems() {
      return [{ label: '交付物模板名称:', prop: 'deliverablesName', span: 8 }]
    },
    tableHeight() {
      const windowHeight = document.documentElement.clientHeight || document.body.clientHeight

      return windowHeight - 200
    }
  },
  async mounted() {
    this.getPermision()
    this.$nextTick(() => {
      this.$refs.tree.selectNode(this.treeData[0])
    })
  },
  methods: {
    // 获取DCP交付物管理员的权限，查询出来的数据为DCP交付物管理员的数组，需要取出登录人工号去匹配
    async getPermision() {
      try {
        const { data } = await getRoleUser4Out('dcp_deliverable_admin')
        const { employeeNo } = this.loginUser

        this.isDcpDeliverableAdmin = data.some(({ userId }) => userId === employeeNo)
        console.log(this.isDcpDeliverableAdmin)
      } catch (error) {
        console.log(error)
      }
    },
    // 文件下载
    downloadFile({ bid, statusCode }) {
      if (!this.isDcpDeliverableAdmin && statusCode !== 'enable') {
        this.$message.info('该模板不可用')

        return
      }
      downLoadDcpFile({ bid })
    },
    // 状态修改
    setParams(row) {
      return {
        bid: row.bid,
        statusCode: row.statusCode
      }
    },
    updateDcpDeliverables,
    getTableData,
    // 新增
    onAddBtnClick() {
      this.$refs.addOrEdit.show(this.currentTaskInfo, 'addModel')
    },
    // 操作
    async btnClickHandler(str, row) {
      switch (str) {
        case 'edit':
          row.data_bid = row.dataBid
          this.$refs.addOrEdit.show(row, 'editModel')
          break
        case 'view':
          if (!this.isDcpDeliverableAdmin && row.statusCode !== 'enable') {
            this.$message.info('该模板不可用')

            return
          }
          row.data_bid = row.dataBid
          this.$refs.addOrEdit.show(row, 'view')
          break
        case 'del':
          this.$confirm('是否删除该模板?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
            .then(() => {
              deleteDcpDeliverables({ bid: row.bid }).then(() => {
                this.$message.success('删除成功')
                this.handleQuery()
              })
            })
            .catch()
          break
      }
    },
    // 确定
    onAddConfirm() {
      this.handleQuery()
    },
    // 重置
    handleReset() {
      this.$refs.form.resetFields()
      this.handleQuery()
    },
    // 查看
    handleQuery() {
      this.$nextTick(() => {
        this.$refs.tableRef.query({ param: this.form, count: true })
      })
    },
    // 树菜单选择
    async handleUpdateData({ data }) {
      const res = await getObjectDetails({ bid: data.bid, version: 'lastest' })

      this.currentTaskInfo = res.data
      this.handleQuery()
    }
  }
}
</script>

<style lang="scss" scoped>
.manage-box {
  display: flex;
  height: 100%;
  width: 100%;
  .render-rt {
    flex: 1;
    overflow: auto;
  }
  .table-box {
    margin-left: 10px;
    height: 100%;
    width: 100%;
    .box-card {
      width: 100%;
    }
  }
}
</style>
