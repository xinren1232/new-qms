<template>
  <div>
    <tr-form
      ref="form"
      v-model="form"
      :items="items"
      label-width="130px"
      :button-position="{ buttonSpan: 8 }"
      @query="handleQuery"
      @reset="resetQuery"
    >
      <span slot="createdBy">
        <tr-user v-model="createdByUser" />
      </span>
    </tr-form>

    <el-button
      type="primary"
      @click="onClickAdd"
    >
      <tr-svg-icon
        class="bussiness"
        style="margin-right: 3px; vertical-align: top"
        icon-class="add"
      />
      新增
    </el-button>

    <tr-table
      ref="table"
      v-model="tableData"
      :show-pager="true"
      :show-loading="false"
      :http-request="httpRequest"
      :params="{ count: true, param: form }"
      :height="maxHeight"
      :query-completed="(response) => queryUsers(response.data, 'createdBy')"
    >
      <el-table-column
        label="平台编码"
        prop="platformCode"
        sortable
        align="center"
      />
      <el-table-column
        label="状态"
        prop="stateCode"
        sortable
        align="center"
      >
        <template slot-scope="{ row }">
          <tr-switch
            v-model="row.stateCode"
            :params="{ platformCode: row.platformCode, bid: row.bid }"
            :http-request="changeState"
            :params-code="'stateCode'"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="序号"
        prop="serialNo"
        sortable
        align="center"
      />
      <el-table-column
        label="创建人"
        prop="createdBy"
        align="center"
      >
        <template slot-scope="{ row }">{{ userRender(row, 'createdBy') }}</template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        prop="createdTime"
        sortable
        align="center"
      />
      <el-table-column
        label="操作"
        align="center"
      >
        <template slot-scope="{ row }">
          <el-button
            type="text"
            @click="onClickEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.stateCode === 'off'"
            type="text"
            @click="onClickDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </tr-table>

    <add-dialog
      ref="addDialog"
      @addPlatform="handleQuery()"
    />
    <edit-dialog
      ref="editDialog"
      @editPlatform="handleQuery()"
    />
  </div>
</template>
<script>
import { deleteLifecycleState, queryPlatformCode, updateStateByPlatformCode } from '@/api/platform-code.js'
import TrSelect from '@/components/feature/TrSelect'
import userMin from '@/views/mixin/user'
import TrUser from '@@/bussiness/TrUser'
import TrForm from '@@/feature/TrForm'
import TrSwitch from '@@/feature/TrSwitch'
import TrTable from '@@/feature/TrTable'

import addDialog from './components/add-dialog.vue'
import editDialog from './components/edit-dialog.vue'

export default {
  components: { TrForm, TrTable, TrSwitch, addDialog, editDialog, TrUser },
  mixins: [userMin],
  data() {
    return {
      form: {
        platformCode: '',
        serialNo: '',
        createdBy: '',
        createdTime: '',
        time: []
      },
      tableData: [],
      createdByUser: {}
    }
  },
  computed: {
    items() {
      return [
        { label: '平台编码', prop: 'platformCode', span: 8 },
        { label: '序号', prop: 'serialNo', span: 8 },
        {
          label: '状态',
          prop: 'stateCode',
          span: 8,
          component: TrSelect,
          attrs: {
            data: [
              { label: '启用', value: 'enable' },
              { label: '未启用', value: 'off' },
              { label: '禁用', value: 'disable' }
            ]
          }
        },
        { label: '创建人', prop: 'createdBy', span: 8 },
        {
          label: '创建时间',
          prop: 'time',
          span: 8,
          type: 'daterange',
          attrs: { unlinkPanels: true, defaultTime: ['00:00:00', '23:59:59'] }
        }
        // {label: '创建时间', prop: 'startTime', type: 'date'},
        // {label: '到', prop: 'endTime', type: 'date'}
        // { label: '计划起止时间', prop: 'time', type: 'daterange', span: 24, attrs: { disabled, unlinkPanels: true, defaultTime: ['00:00:00', '23:59:59'] }}
      ]
    },
    maxHeight() {
      return document.body.offsetHeight - 260
    }
  },
  watch: {
    createdByUser: {
      handler(val) {
        this.form.createdBy = val.employeeNo
      }
    }
  },

  methods: {
    httpRequest: queryPlatformCode,
    changeState: updateStateByPlatformCode,
    // 编辑
    onClickEdit(row) {
      this.$refs.editDialog.show(row)
    },
    // 新增
    onClickAdd() {
      this.$refs.addDialog.show()
    },
    // 删除
    onClickDelete(row) {
      this.$confirm(`是否确认删除【 ${row.platformCode} 】`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        closeOnClickModal: false
      }).then(async () => {
        await deleteLifecycleState(row.platformCode)
        this.$message.success('删除成功')
        this.handleQuery()
      })
    },
    // 表单查询
    handleQuery(form) {
      if (form?.time?.length) {
        form.startTime = form.time[0]
        form.endTime = form.time[1]
      }
      // let created = ''
      // if (form?.createdBy) {
      //   const userMap = this.$store.getters.userMap
      //   for (const key in userMap) {
      //     if (userMap[key] === form.createdBy) created = key
      //   }
      // }
      // form.createdBy = created || form.createdBy
      this.$refs.table.query()
    },
    resetQuery() {
      this.createdByUser = {}
      this.form.startTime = ''
      this.form.endTime = ''
      this.$refs.table.query()
    }
  }
}
</script>
