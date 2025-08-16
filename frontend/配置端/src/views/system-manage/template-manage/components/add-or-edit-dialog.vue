<template>
  <el-dialog
    ref="dialog"
    top="1%"
    width="1100px"
    :visible.sync="visible"
    append-to-body
    :close-on-click-modal="false"
    class="add-dialog"
    destroy-on-close
    @close="close"
  >
    <relation-object-render
      v-if="visible"
      :handle-type="handleType"
      :obj-instance-info="objInstance"
      obj-type="dcpdeliverables"
      :type-code="typeCode"
      obj-bid="dcpdeliverables"
      :current-instance-info="currentInstanceInfo"
      :show-btn="showBtn"
      @addSuccess="addSuccess"
    />
  </el-dialog>
</template>

<script>
import RelationObjectRender from '@@/special/relation-object-render'

export default {
  name: 'HandleRelationDialog',
  components: {
    RelationObjectRender
  },
  props: {
    typeCode: {
      type: String,
      default: '',
      comment: '项目类型'
    },
    relationObjInfo: {
      type: Object,
      default: () => ({}),
      command: '关系对象信息'
    },
    teamList: {
      type: Array,
      default: () => [],
      command: '域团队成员'
    },
    domainBid: {
      type: [Number, String],
      default: '',
      command: '域ID'
    },
    checkOutUser: {
      type: Object,
      default: () => {},
      command: '当前对象检出人'
    }
  },
  data() {
    return {
      handleType: '',
      visible: false,
      objInstance: {},
      showBtn: {
        temporarySave: true,
        checkIn: true,
        checkOut: true,
        revise: true,
        startAssess: false,
        statePromote: false,
        taskSave: false
      }
    }
  },
  methods: {
    show(row, handleType) {
      this.handleType = handleType
      this.objInstance = {
        ...row,
        sourceModel: 'dcpdeliverables',
        targetObjBid: 'dcpdeliverables',
        targetObjVersion: 'dcpdeliverables',
        relationObjVersion: 'dcpdeliverables',
        relationObjBid: 'dcpdeliverables'
      }
      this.currentInstanceInfo = {
        bid: row.bid,
        data_bid: row.data_bid,
        objType: 'dcpdeliverables'
      }
      Object.keys(row ?? {}).forEach((key) => {
        this.objInstance['target_' + key] = row[key]
      })
      this.visible = true
    },
    close() {
      this.visible = false
      this.$emit('updateData')
    },
    addSuccess(data, handleType) {
      this.show(data, handleType)
      // this.close()
    }
  }
}
</script>
<style scoped lang="scss">
.add-dialog {
  ::v-deep {
    .el-dialog {
      overflow: hidden;
      max-height: 90%;
      overflow-y: auto;
    }
    .el-dialog__body {
      overflow: hidden;
      overflow-y: auto;
      max-height: 90%;
    }
  }
}
</style>
