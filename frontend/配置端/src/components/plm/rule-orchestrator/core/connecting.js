import FlowGraph from './index'

// 连接点半径大小
const edgeR = 5

// 连接点
// TODO: 增加点到8个
const createPorts = (shape = 'rect') => {
  const portAttrs = {
    circle: {
      r: edgeR,
      magnet: true,
      stroke: '#cdcdcd',
      strokeWidth: 1,
      fill: '#fff',
      style: {
        visibility: 'hidden'
      }
    }
  }

  const items = [
    { group: 'top' },
    { group: 'left' },
    { group: 'right' },
    { group: 'bottom' }

  ]

  // 菱形会裁切路径, 鼠标放在空的地方会隐藏, 所以只需要保证有4个点即可
  // 而矩形需要有8个点
  if (shape === 'rect') {
    items.unshift(
      { group: 'top' },
      { group: 'top' }
    )
    items.push(
      { group: 'bottom' },
      { group: 'bottom' }
    )
  }

  return {
    items,
    groups: {
      top: {
        position: 'top',
        attrs: portAttrs
      },
      right: {
        position: 'right',
        attrs: portAttrs
      },
      bottom: {
        position: 'bottom',
        attrs: portAttrs
      },
      left: {
        position: 'left',
        attrs: portAttrs
      }
    }
  }
}

const NODE_RADIUS = 18

export const createGraphNode = (item, event) => {
  const targetEl = event.target
  const nodeStyle = getComputedStyle(event.target)

  const node = FlowGraph.graph.createNode({
    width: 150,
    height: 36,
    shape: item.shape || 'rect',
    attrs: {
      body: {
        rx: item.radius ?? NODE_RADIUS,
        ry: item.radius ?? NODE_RADIUS,
        fill: nodeStyle['backgroundColor'],
        stroke: nodeStyle['borderColor'],
        strokeWidth: 1,
        // 创建多边形时的剪切路径
        refPoints: '0,10 10,0 20,10 10,20',
        // refPoints: '0,5 0,15 5,20 15,20 20,15 20,5 15,0 5,0',
        style: {
          cursor: 'pointer'
        },
        filter: {
          name: 'dropShadow',
          args: {
            dx: 2,
            dy: 2,
            blur: 5,
            color: 'rgba(0,0,0,.07)'
          }
        }
      },
      label: {
        text: targetEl.innerText,
        fill: '#242424',
        fontSize: 14,
        fontWeight: '400',
        textWrap: {
          text: targetEl.innerText,
          // width: -10,
          // height: -10,
          ellipsis: true
        }
      }
    },
    ports: createPorts(item.shape),
    data: { ...item },
    id: item.bid
  })

  FlowGraph.dnd.start(node, event)
}
