<template>
  <div class="conversation-config">
    <el-card class="config-card">
      <div slot="header" class="card-header">
        <h3>AI对话记录配置</h3>
        <p>配置AI对话记录页面的字段、权限和功能</p>
      </div>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 字段配置 -->
        <el-tab-pane label="字段配置" name="fields">
          <div class="field-config">
            <div class="config-toolbar">
              <el-button type="primary" @click="addField">添加字段</el-button>
              <el-button @click="resetFields">重置默认</el-button>
            </div>
            
            <el-table :data="fieldConfig" border>
              <el-table-column prop="prop" label="字段名" width="150" />
              <el-table-column prop="label" label="显示名称" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.label" size="mini" />
                </template>
              </el-table-column>
              <el-table-column prop="type" label="字段类型" width="120">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.type" size="mini">
                    <el-option label="文本" value="text" />
                    <el-option label="标签" value="tag" />
                    <el-option label="评分" value="rate" />
                    <el-option label="日期时间" value="datetime" />
                    <el-option label="链接" value="link" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="width" label="宽度" width="100">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.width" size="mini" :min="80" />
                </template>
              </el-table-column>
              <el-table-column prop="visible" label="显示" width="80">
                <template slot-scope="scope">
                  <el-switch v-model="scope.row.visible" />
                </template>
              </el-table-column>
              <el-table-column prop="sortable" label="排序" width="80">
                <template slot-scope="scope">
                  <el-switch v-model="scope.row.sortable" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template slot-scope="scope">
                  <el-button type="text" @click="editField(scope.row)">编辑</el-button>
                  <el-button type="text" @click="deleteField(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 权限配置 -->
        <el-tab-pane label="权限配置" name="permissions">
          <div class="permission-config">
            <el-form :model="permissionConfig" label-width="120px">
              <el-form-item label="查看权限">
                <el-select v-model="permissionConfig.view" multiple>
                  <el-option label="管理员" value="admin" />
                  <el-option label="普通用户" value="user" />
                  <el-option label="分析师" value="analyst" />
                </el-select>
              </el-form-item>
              <el-form-item label="导出权限">
                <el-select v-model="permissionConfig.export" multiple>
                  <el-option label="管理员" value="admin" />
                  <el-option label="分析师" value="analyst" />
                </el-select>
              </el-form-item>
              <el-form-item label="分析权限">
                <el-select v-model="permissionConfig.analyze" multiple>
                  <el-option label="管理员" value="admin" />
                  <el-option label="分析师" value="analyst" />
                </el-select>
              </el-form-item>
              <el-form-item label="删除权限">
                <el-select v-model="permissionConfig.delete" multiple>
                  <el-option label="管理员" value="admin" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 功能配置 -->
        <el-tab-pane label="功能配置" name="functions">
          <div class="function-config">
            <el-form :model="functionConfig" label-width="120px">
              <el-form-item label="显示选择框">
                <el-switch v-model="functionConfig.showSelection" />
              </el-form-item>
              <el-form-item label="显示序号">
                <el-switch v-model="functionConfig.showIndex" />
              </el-form-item>
              <el-form-item label="分页大小">
                <el-input-number v-model="functionConfig.pageSize" :min="10" :max="100" />
              </el-form-item>
              <el-form-item label="表格高度">
                <el-input-number v-model="functionConfig.tableHeight" :min="300" />
              </el-form-item>
              <el-form-item label="行点击操作">
                <el-select v-model="functionConfig.rowClickAction">
                  <el-option label="无操作" value="none" />
                  <el-option label="显示详情" value="detail" />
                  <el-option label="编辑" value="edit" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 搜索配置 -->
        <el-tab-pane label="搜索配置" name="search">
          <div class="search-config">
            <div class="config-toolbar">
              <el-button type="primary" @click="addSearchField">添加搜索字段</el-button>
            </div>
            
            <el-table :data="searchConfig" border>
              <el-table-column prop="key" label="字段Key" width="150" />
              <el-table-column prop="label" label="显示名称" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.label" size="mini" />
                </template>
              </el-table-column>
              <el-table-column prop="type" label="组件类型" width="120">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.type" size="mini">
                    <el-option label="输入框" value="input" />
                    <el-option label="下拉框" value="select" />
                    <el-option label="日期范围" value="daterange" />
                    <el-option label="数字输入" value="number" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="placeholder" label="占位符" width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.placeholder" size="mini" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template slot-scope="scope">
                  <el-button type="text" @click="editSearchField(scope.row)">编辑</el-button>
                  <el-button type="text" @click="deleteSearchField(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="config-actions">
        <el-button type="primary" @click="saveConfig">保存配置</el-button>
        <el-button @click="previewConfig">预览效果</el-button>
        <el-button @click="resetConfig">重置配置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ConversationConfig',
  data() {
    return {
      activeTab: 'fields',
      
      // 字段配置
      fieldConfig: [
        {
          prop: 'conversationId',
          label: '对话ID',
          type: 'text',
          width: 150,
          visible: true,
          sortable: true
        },
        {
          prop: 'userId',
          label: '用户ID',
          type: 'text',
          width: 120,
          visible: true,
          sortable: true
        },
        {
          prop: 'userQuestion',
          label: '用户问题',
          type: 'text',
          minWidth: 300,
          visible: true,
          sortable: false
        },
        {
          prop: 'conversationStatus',
          label: '对话状态',
          type: 'tag',
          width: 100,
          visible: true,
          sortable: true
        },
        {
          prop: 'satisfactionScore',
          label: '满意度',
          type: 'rate',
          width: 100,
          visible: true,
          sortable: false
        },
        {
          prop: 'conversationTime',
          label: '对话时间',
          type: 'datetime',
          width: 160,
          visible: true,
          sortable: true
        }
      ],
      
      // 权限配置
      permissionConfig: {
        view: ['admin', 'user'],
        export: ['admin'],
        analyze: ['admin', 'analyst'],
        delete: ['admin']
      },
      
      // 功能配置
      functionConfig: {
        showSelection: true,
        showIndex: true,
        pageSize: 20,
        tableHeight: null,
        rowClickAction: 'detail'
      },
      
      // 搜索配置
      searchConfig: [
        {
          key: 'userId',
          label: '用户ID',
          type: 'input',
          placeholder: '请输入用户ID'
        },
        {
          key: 'status',
          label: '对话状态',
          type: 'select',
          placeholder: '请选择状态'
        },
        {
          key: 'dateRange',
          label: '时间范围',
          type: 'daterange',
          placeholder: '请选择时间范围'
        }
      ]
    }
  },
  
  methods: {
    // 字段配置相关方法
    addField() {
      this.fieldConfig.push({
        prop: 'newField',
        label: '新字段',
        type: 'text',
        width: 120,
        visible: true,
        sortable: false
      })
    },
    
    editField(field) {
      // 打开字段编辑对话框
      console.log('编辑字段:', field)
    },
    
    deleteField(index) {
      this.fieldConfig.splice(index, 1)
    },
    
    resetFields() {
      // 重置为默认字段配置
      this.loadDefaultFieldConfig()
    },
    
    // 搜索配置相关方法
    addSearchField() {
      this.searchConfig.push({
        key: 'newSearchField',
        label: '新搜索字段',
        type: 'input',
        placeholder: '请输入'
      })
    },
    
    editSearchField(field) {
      console.log('编辑搜索字段:', field)
    },
    
    deleteSearchField(index) {
      this.searchConfig.splice(index, 1)
    },
    
    // 配置操作方法
    saveConfig() {
      const config = {
        fields: this.fieldConfig,
        permissions: this.permissionConfig,
        functions: this.functionConfig,
        search: this.searchConfig
      }
      
      console.log('保存配置:', config)
      this.$message.success('配置保存成功')
      
      // 这里应该调用API保存到配置端
      this.syncToApplication(config)
    },
    
    previewConfig() {
      // 预览配置效果
      console.log('预览配置')
    },
    
    resetConfig() {
      // 重置所有配置
      this.loadDefaultConfig()
    },
    
    // 同步配置到应用端
    syncToApplication(config) {
      // 这里应该调用配置同步API
      console.log('同步配置到应用端:', config)
    },
    
    // 加载默认配置
    loadDefaultConfig() {
      this.loadDefaultFieldConfig()
      // 加载其他默认配置...
    },
    
    loadDefaultFieldConfig() {
      // 从配置端的默认配置加载
    }
  }
}
</script>

<style scoped>
.conversation-config {
  padding: 20px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.config-toolbar {
  margin-bottom: 16px;
}

.config-actions {
  margin-top: 20px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.field-config,
.permission-config,
.function-config,
.search-config {
  padding: 16px;
}
</style>
