<template>
  <el-card class="tree-directory">
    <tr-edit-tree
      ref="tree"
      node-key="bid"
      layout="search,collapse,expand"
      :data="treeDataList"
      :props="{ children: 'children', label: 'name', removeField: 'remove', createField: 'isCreate' }"
      :create-custom-editing="false"
      :allow-drop="allowDrop"
      @select="handleSelectNode"
    />
  </el-card>
</template>

<script>
import TrEditTree from '@@/feature/TrEditTree'

export default {
  name: 'TreeDirectory',
  components: { TrEditTree },
  props: {
    treeData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {}
  },
  computed: {
    treeDataList({ treeData }) {
      return [{ bid: '0', isRoot: true, name: '模板管理', children: treeData }]
    }
  },
  methods: {
    allowDrop() {
      return false
    },
    selectNode(data) {
      this.$nextTick(() => {
        this.$refs.tree.selectNode(data)
      })
    },
    // 选中节点
    handleSelectNode({ data, node }) {
      this.$emit('update-data', { node, data, parent: this.lastParent })
    }
  }
}
</script>

<style lang="scss" scoped>
.tree-directory {
  height: 100%;
  width: 100%;

  ::v-deep .el-card__body {
    height: 100%;
  }

  .lock-icon {
    transform: translate(-4px, 2px);
  }
}
</style>
