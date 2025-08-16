import { Graph, Shape } from '@antv/x6'
import { Stencil } from '@antv/x6-plugin-stencil'
import { Snapline } from '@antv/x6-plugin-snapline'
import { Dnd } from '@antv/x6-plugin-dnd'
import { Selection } from '@antv/x6-plugin-selection'
import { History } from '@antv/x6-plugin-history'
// import { Clipboard } from '@antv/x6-plugin-clipboard'
// import { MiniMap } from '@antv/x6-plugin-minimap'
import { Export } from '@antv/x6-plugin-export'
import config from '../config'
import { canUndo,canRedo,hasSelected,activeCellState } from './state'


// 连接桩显示时机
function showPorts(ports, show) {
  for (let i = 0, len = ports.length; i < len; i = i + 1) {
    ports[i].style.visibility = show ? 'visible' : 'hidden'
  }
}

export default class FlowGraph {
  static graph = null
  static stencil = null
  static dnd = null
  /**
   * 初始化方法
   * @param {*} dom 画板容器
   * @param {*} width 容器宽度
   * @param {*} height 容器高度
   * @param {*} autoResize 默认为true，传入false不自动适应画板
   * @returns
   */
  static init(dom, width, height, autoResize = true) {
    this.graph = new Graph({
      background: {
        image:
          'data:image/jpeg;base64,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',
        repeat: 'watermark',
        opacity: 0.07
      },
      container: dom,
      autoResize: autoResize,
      grid: {
        visible: true,
        type: 'doubleMesh',
        size: 20,
        args: [
          {
            color: config.mainGridColor, // 主网格线颜色
            thickness: 1 // 主网格线宽度
          },
          {
            color: config.subGridColor, // 次网格线颜色
            thickness: 1, // 次网格线宽度
            factor: 4 // 主次网格线间隔
          }
        ]
      },
      // scroller: {
      //   enabled: false,
      //   pageVisible: false,
      //   pageBreak: false,
      //   pannable: false
      // },
      // 开启画布缩放
      mousewheel: {
        enabled: true,
        modifiers: ['ctrl', 'meta'],
        minScale: 0.5,
        maxScale: 10
      },
      interacting: {
        nodeMovable: true, // 节点是否可以被移动。
        edgeMovable: true, // 边是否可以被移动。
        edgeLabelMovable: true, // 边的标签是否可以被移动。
        arrowheadMovable: true, // 边的起始/终止箭头是否可以被移动
        vertexMovable: true, // 边的路径点是否可以被移动。
        vertexAddable: true, // 是否可以添加边的路径点。
        vertexDeletable: true // 边的路径点是否可以被删除。
      },
      connecting: {
        snap: true, // 是否自动吸附
        allowMulti: true, // 是否允许在相同的起始节点和终止之间创建多条边
        allowNode: false, // 是否允许边链接到节点（非节点上的链接桩）
        allowBlank: false, // 是否允许连接到空白点
        allowLoop: false, // 是否允许创建循环连线，即边的起始节点和终止节点为同一节点，
        allowEdge: false, // 是否允许边链接到另一个边
        highlight: true, // 拖动边时，是否高亮显示所有可用的连接桩或节点
        connectionPoint: 'anchor', // 指定连接点
        anchor: 'center', // 指定被连接的节点的锚点
        createEdge() {
          // 使用随机颜色绘制连接线, 避免线条颜色相同
          const color = `hsl(${Math.floor(Math.random() * 360)}, 50%, 20%)`

          // X6 的 Shape 命名空间中内置 Edge、DoubleEdge、ShadowEdge 三种边
          return new Shape.Edge({
            attrs: {
              line: {
                strokeWidth: 2,
                stroke: color,
                offset: 100,
                // 线段间隔
                // strokeDasharray: 0,
                style: {
                  animation: 'ant-line 50s infinite linear'
                },
                sourceMarker: {
                  name: 'circle',
                  args: { r: 2 }
                },
                targetMarker: {
                  name: 'classic',
                  args: { size: 11 }
                }
              },
              outline: {
                stroke: 'var(--primary)',
                strokeWidth: 1,
                outLineWidth: 1
              }
            },
            zIndex: -1, // 避免在一个端点连接多条线时因为层级不对无法连线
            router: {
              // name: 'manhattan',
              name: 'metro',
              args: {}
            },
            connector: {
              // jumpover 遇到重叠的线会断开, 避免相交
              name: 'jumpover',
              args: {
                type: 'gap',
                size: 8,
                radius: 12
              }
            }
          })
        },
        validateConnection({
          sourceView,
          targetView,
          sourceMagnet,
          targetMagnet
        }) {
          if (sourceView === targetView) {
            return false
          }

          if (!sourceMagnet) {
            return false
          }

          return targetMagnet
        }
      },
      highlighting: {
        magnetAvailable: {
          name: 'stroke',
          args: {
            padding: 4,
            attrs: {
              strokeWidth: 4,
              stroke: 'var(--primary)'
            }
          }
        }
      },
      // 开启拖拽平移（防止冲突，按下修饰键并点击鼠标才能触发画布拖拽）
      panning: true,
      resizing: true,
      rotating: true,
      snapline: true,
      history: true,
      clipboard: true,
      keyboard: true,
      embedding: {
        enabled: true,
        findParent({ node }) {
          const bbox = node.getBBox()

          return this.getNodes().filter((node) => {
            // 只有 data.parent 为 true 的节点才是父节点
            const data = node.getData()

            if (data && data.parent) {
              const targetBBox = node.getBBox()

              return bbox.isIntersectWithRect(targetBBox)
            }

            return false
          })
        }
      }
    })
    // 注册对齐线
    this.graph.use(new Snapline())

    // 回退
    this.graph.use(new History())

    // // 复制粘贴
    // this.graph.use(new Clipboard())
    // 框选
    this.graph.use(
      new Selection({
        enabled: true,
        // multiple: true,
        rubberband: true,
        // movable: true,
        showNodeSelectionBox: true,
        className: 'x6-selection',
        modifiers: 'ctrl' // 组合键
      })
    )
    // 导出
    this.graph.use(new Export())
    // // 迷你地图
    // this.graph.use(
    //   new MiniMap({
    //     container: document.getElementById('minimap'),
    //     width: 200,
    //     height: 160,
    //     padding: 10
    //   })
    // )
    // this.initStencil()
    this.initDnd()
    // this.initShape()
    this.initEvent()

    // this.initGraphShape(graphData)
    return this.graph
  }
  static initStencil() {
    this.stencil = new Stencil({
      title: '节点选择',
      target: this.graph,
      stencilGraphWidth: 200,
      search: false,
      collapsable: true,
      groups: [
        {
          name: 'basic',
          title: '基础节点',
          graphHeight: 200
        }
      ]
    })
    const stencilContainer = document.querySelector('#stencil')

    stencilContainer?.appendChild(this.stencil.container)
  }
  static initDnd() {
    this.dnd = new Dnd({
      target: this.graph,
      scaled: false,
      dndContainer: document.querySelector('#stencil'),
      getDragNode: (node) => node.clone({ keepId: true }),
      getDropNode: (node) => node.clone({ keepId: true })
    })
  }

  // 根据json渲染节点和边
  static initGraphShape(gd) {
    this.graph.fromJSON(gd)
  }

  // 事件相关
  static initEvent() {
    const { graph } = this
    const container = document.getElementById('container')

    // 鼠标移入 显示连接桩
    graph.on(
      'node:mouseenter',
      () => {
        const ports = container.querySelectorAll('.x6-port-body')

        showPorts(ports, true)
      }
    )
    // 鼠标移出 隐藏连接桩
    graph.on('node:mouseleave', () => {
      const ports = container.querySelectorAll('.x6-port-body')

      showPorts(ports, false)
    })

    graph.on('node:collapse', ({ node, e }) => {
      e.stopPropagation()
      node.toggleCollapse()
      const collapsed = node.isCollapsed()
      const cells = node.getDescendants()

      cells.forEach((n) => {
        if (collapsed) {
          n.hide()
        } else {
          n.show()
        }
      })
    })
    // backspace
    // graph.bindKey('delete', () => {
    //   alert(111)
    //   const cells = graph.getSelectedCells()
    //   if (cells.length) {
    //     graph.removeCells(cells)
    //   }
    // })
    // 鼠标动态添加/删除小工具。
    // graph.on('edge:mouseenter', ({ cell }) => {
    //   /**
    //    * EdgeTool
    //    * vertices 路径点工具，在路径点位置渲染一个小圆点，拖动小圆点修改路径点位置，双击小圆点删除路径点，在边上单击添加路径点。
    //    * segments 线段工具。在边的每条线段的中心渲染一个工具条，可以拖动工具条调整线段两端的路径点的位置。
    //    * boundary 根据边的包围盒渲染一个包围边的矩形。注意，该工具仅仅渲染一个矩形，不带任何交互。
    //    * button 在指定位置处渲染一个按钮，支持自定义按钮的点击交互。
    //    * button-remove 在指定的位置处，渲染一个删除按钮，点击时删除对应的边。
    //    * source-arrowhead-和-target-arrowhead 在边的起点或终点渲染一个图形(默认是箭头)，拖动该图形来修改边的起点或终点。
    //    * edge-editor 提供边上文本编辑功能。
    //    */
    //   cell.addTools([
    //     {
    //       name: 'vertices',
    //       args: {
    //         attrs: { fill: 'var(--primary)' },
    //         // 移动路径点过程中的吸附半径。当路径点与邻近的路径点的某个坐标 (x, y) 距离在半径范围内时，将当前路径点的对应坐标 (x, y) 吸附到邻居路径的路径点。
    //         snapRadius: 20,
    //         // 在边上按下鼠标时，是否可以添加新的路径点。
    //         addable: true,
    //         // 是否可以通过双击移除路径点。
    //         removable: true,
    //         // 是否自动移除冗余的路径点。
    //         removeRedundancies: true,
    //         // 是否阻止工具上的鼠标事件冒泡到边视图上。阻止后鼠标与工具交互时将不会触发边的 mousedown、mousemove 和 mouseup 事件。
    //         stopPropagation: false
    //       }
    //     }
    //   ])
    // })
    // graph.on('edge:mouseleave', ({ cell }) => {
    //   // cell.removeTools()
    // })
    graph.on('edge:click', ({ /* e, x, y, */ edge, view }) => {
      activeCellState.value = { type: 'edge', cell: edge, view }
      // console.log('edge:click', e, x, y, edge, view)
      // graph.selectedNode = node
    })
    graph.on('node:click', ({ /* e, x, y, */ node, view }) => {
      activeCellState.value = { type: 'node', cell: node,view }
      // console.log('node:click', e, x, y, node, view)
      // graph.selectedNode = node
    })
    graph.on('cell:selected', ({ cell }) => {

      if (cell.isNode()) {
        cell.addTools([
          {
            name: 'button-remove',
            args: {
              x: 75,
              y: -15,
              onClick: (view) => {
                activeCellState.value = null
                graph.removeCells([view.cell])
              }
            }
          }
        ])
      } else if (cell.isEdge()) {
        cell.attr('line', {
          strokeWidth: 3,
          strokeDasharray: 5
        })
        cell.addTools([
          {
            name: 'button-remove',
            args: {
              x: 0, y: 0,
              offset: { x: 25, y: 0 },
              distance: '50%'
            }
          }
        ])
      }

      if (this.graph.getSelectedCells().length > 1) {
        this.graph.getSelectedCells().forEach((cell) => {
          cell.removeTools()
        })
      }
    })
    graph.on('cell:unselected', ({ cell }) => {
      cell.attr('line', {
        strokeWidth: 2,
        strokeDasharray: 0
      })
      cell.removeTools()
    })

    graph.on('blank:click', () => {
      // console.log('blank:click')
      activeCellState.value = null
    })

    graph.on('history:change', () => {
      canUndo.value = graph.canUndo()
      canRedo.value = graph.canRedo()
    })
    graph.on('selection:changed', ({ selected }) => {
      // console.log('selection:changed', added,removed,selected)
      hasSelected.value = !!selected.length
    })
  }
  // 销毁实例
  static destroy() {
    this.dnd?.dispose()
    this.graph?.dispose()
    this.stencil?.dispose()
  }
}
