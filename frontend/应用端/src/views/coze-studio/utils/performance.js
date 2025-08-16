/**
 * Coze Studio 性能监控工具
 * 监控组件渲染、API调用、用户交互等性能指标
 */

class PerformanceMonitor {
  constructor() {
    this.metrics = {
      componentRender: [],
      apiCalls: [],
      userInteractions: [],
      memoryUsage: [],
      loadTimes: []
    }
    this.observers = new Map()
    this.isMonitoring = false
    this.setupPerformanceObserver()
  }

  /**
   * 设置性能观察器
   */
  setupPerformanceObserver() {
    if ('PerformanceObserver' in window) {
      // 监控导航时间
      const navObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.recordLoadTime(entry)
        }
      })
      navObserver.observe({ entryTypes: ['navigation'] })

      // 监控资源加载时间
      const resourceObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.recordResourceLoad(entry)
        }
      })
      resourceObserver.observe({ entryTypes: ['resource'] })

      // 监控用户交互
      const interactionObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.recordUserInteraction(entry)
        }
      })
      
      // 检查是否支持 event 类型
      try {
        interactionObserver.observe({ entryTypes: ['event'] })
      } catch (e) {
        console.warn('Event timing not supported')
      }
    }
  }

  /**
   * 开始监控
   */
  startMonitoring() {
    this.isMonitoring = true
    this.startMemoryMonitoring()
    console.log('Coze Studio 性能监控已启动')
  }

  /**
   * 停止监控
   */
  stopMonitoring() {
    this.isMonitoring = false
    this.observers.forEach(observer => observer.disconnect())
    this.observers.clear()
    console.log('Coze Studio 性能监控已停止')
  }

  /**
   * 记录组件渲染时间
   * @param {string} componentName 组件名称
   * @param {number} startTime 开始时间
   * @param {number} endTime 结束时间
   */
  recordComponentRender(componentName, startTime, endTime) {
    const renderTime = endTime - startTime
    this.metrics.componentRender.push({
      component: componentName,
      renderTime,
      timestamp: Date.now()
    })

    // 只保留最近100条记录
    if (this.metrics.componentRender.length > 100) {
      this.metrics.componentRender.shift()
    }

    // 如果渲染时间过长，发出警告
    if (renderTime > 100) {
      console.warn(`组件 ${componentName} 渲染时间过长: ${renderTime.toFixed(2)}ms`)
    }
  }

  /**
   * 记录API调用性能
   * @param {string} url API地址
   * @param {string} method HTTP方法
   * @param {number} duration 耗时
   * @param {boolean} success 是否成功
   */
  recordApiCall(url, method, duration, success) {
    this.metrics.apiCalls.push({
      url,
      method,
      duration,
      success,
      timestamp: Date.now()
    })

    // 只保留最近100条记录
    if (this.metrics.apiCalls.length > 100) {
      this.metrics.apiCalls.shift()
    }

    // 如果API调用时间过长，发出警告
    if (duration > 3000) {
      console.warn(`API调用耗时过长: ${url} - ${duration}ms`)
    }
  }

  /**
   * 记录页面加载时间
   * @param {PerformanceNavigationTiming} entry 性能条目
   */
  recordLoadTime(entry) {
    const loadTime = {
      domContentLoaded: entry.domContentLoadedEventEnd - entry.domContentLoadedEventStart,
      loadComplete: entry.loadEventEnd - entry.loadEventStart,
      domInteractive: entry.domInteractive - entry.navigationStart,
      firstPaint: 0,
      firstContentfulPaint: 0,
      timestamp: Date.now()
    }

    // 获取绘制时间
    const paintEntries = performance.getEntriesByType('paint')
    paintEntries.forEach(paint => {
      if (paint.name === 'first-paint') {
        loadTime.firstPaint = paint.startTime
      } else if (paint.name === 'first-contentful-paint') {
        loadTime.firstContentfulPaint = paint.startTime
      }
    })

    this.metrics.loadTimes.push(loadTime)
  }

  /**
   * 记录资源加载
   * @param {PerformanceResourceTiming} entry 资源性能条目
   */
  recordResourceLoad(entry) {
    // 只记录关键资源
    if (entry.name.includes('.js') || entry.name.includes('.css') || entry.name.includes('.vue')) {
      const resourceLoad = {
        name: entry.name,
        duration: entry.duration,
        size: entry.transferSize,
        type: this.getResourceType(entry.name),
        timestamp: Date.now()
      }

      if (!this.metrics.resourceLoads) {
        this.metrics.resourceLoads = []
      }

      this.metrics.resourceLoads.push(resourceLoad)

      // 只保留最近50条记录
      if (this.metrics.resourceLoads.length > 50) {
        this.metrics.resourceLoads.shift()
      }
    }
  }

  /**
   * 记录用户交互
   * @param {PerformanceEventTiming} entry 事件性能条目
   */
  recordUserInteraction(entry) {
    this.metrics.userInteractions.push({
      type: entry.name,
      duration: entry.duration,
      startTime: entry.startTime,
      timestamp: Date.now()
    })

    // 只保留最近50条记录
    if (this.metrics.userInteractions.length > 50) {
      this.metrics.userInteractions.shift()
    }
  }

  /**
   * 开始内存监控
   */
  startMemoryMonitoring() {
    if ('memory' in performance) {
      const recordMemory = () => {
        if (!this.isMonitoring) return

        this.metrics.memoryUsage.push({
          used: performance.memory.usedJSHeapSize,
          total: performance.memory.totalJSHeapSize,
          limit: performance.memory.jsHeapSizeLimit,
          timestamp: Date.now()
        })

        // 只保留最近100条记录
        if (this.metrics.memoryUsage.length > 100) {
          this.metrics.memoryUsage.shift()
        }

        // 检查内存使用率
        const usageRatio = performance.memory.usedJSHeapSize / performance.memory.jsHeapSizeLimit
        if (usageRatio > 0.8) {
          console.warn(`内存使用率过高: ${(usageRatio * 100).toFixed(1)}%`)
        }

        setTimeout(recordMemory, 5000) // 每5秒记录一次
      }

      recordMemory()
    }
  }

  /**
   * 获取资源类型
   * @param {string} url 资源URL
   * @returns {string} 资源类型
   */
  getResourceType(url) {
    if (url.includes('.js')) return 'javascript'
    if (url.includes('.css')) return 'stylesheet'
    if (url.includes('.vue')) return 'component'
    if (url.includes('.png') || url.includes('.jpg') || url.includes('.svg')) return 'image'
    return 'other'
  }

  /**
   * 获取性能报告
   * @returns {Object} 性能报告
   */
  getPerformanceReport() {
    const report = {
      summary: this.getSummary(),
      componentRender: this.getComponentRenderStats(),
      apiCalls: this.getApiCallStats(),
      memoryUsage: this.getMemoryStats(),
      loadTimes: this.getLoadTimeStats(),
      recommendations: this.getRecommendations()
    }

    return report
  }

  /**
   * 获取性能摘要
   * @returns {Object} 性能摘要
   */
  getSummary() {
    const avgComponentRender = this.getAverage(this.metrics.componentRender, 'renderTime')
    const avgApiCall = this.getAverage(this.metrics.apiCalls, 'duration')
    const currentMemory = this.metrics.memoryUsage[this.metrics.memoryUsage.length - 1]

    return {
      avgComponentRenderTime: avgComponentRender,
      avgApiCallTime: avgApiCall,
      currentMemoryUsage: currentMemory ? {
        used: this.formatBytes(currentMemory.used),
        usageRatio: ((currentMemory.used / currentMemory.limit) * 100).toFixed(1) + '%'
      } : null,
      totalMetrics: {
        componentRenders: this.metrics.componentRender.length,
        apiCalls: this.metrics.apiCalls.length,
        userInteractions: this.metrics.userInteractions.length
      }
    }
  }

  /**
   * 获取组件渲染统计
   * @returns {Object} 组件渲染统计
   */
  getComponentRenderStats() {
    const stats = {}
    this.metrics.componentRender.forEach(item => {
      if (!stats[item.component]) {
        stats[item.component] = {
          count: 0,
          totalTime: 0,
          avgTime: 0,
          maxTime: 0
        }
      }
      
      stats[item.component].count++
      stats[item.component].totalTime += item.renderTime
      stats[item.component].maxTime = Math.max(stats[item.component].maxTime, item.renderTime)
    })

    // 计算平均时间
    Object.keys(stats).forEach(component => {
      stats[component].avgTime = stats[component].totalTime / stats[component].count
    })

    return stats
  }

  /**
   * 获取API调用统计
   * @returns {Object} API调用统计
   */
  getApiCallStats() {
    const successCalls = this.metrics.apiCalls.filter(call => call.success)
    const failedCalls = this.metrics.apiCalls.filter(call => !call.success)

    return {
      total: this.metrics.apiCalls.length,
      success: successCalls.length,
      failed: failedCalls.length,
      successRate: this.metrics.apiCalls.length > 0 ? 
        ((successCalls.length / this.metrics.apiCalls.length) * 100).toFixed(1) + '%' : '0%',
      avgDuration: this.getAverage(successCalls, 'duration')
    }
  }

  /**
   * 获取内存统计
   * @returns {Object} 内存统计
   */
  getMemoryStats() {
    if (this.metrics.memoryUsage.length === 0) return null

    const latest = this.metrics.memoryUsage[this.metrics.memoryUsage.length - 1]
    const peak = Math.max(...this.metrics.memoryUsage.map(m => m.used))

    return {
      current: this.formatBytes(latest.used),
      peak: this.formatBytes(peak),
      limit: this.formatBytes(latest.limit),
      usageRatio: ((latest.used / latest.limit) * 100).toFixed(1) + '%'
    }
  }

  /**
   * 获取加载时间统计
   * @returns {Object} 加载时间统计
   */
  getLoadTimeStats() {
    if (this.metrics.loadTimes.length === 0) return null

    const latest = this.metrics.loadTimes[this.metrics.loadTimes.length - 1]
    return {
      domContentLoaded: latest.domContentLoaded,
      loadComplete: latest.loadComplete,
      domInteractive: latest.domInteractive,
      firstPaint: latest.firstPaint,
      firstContentfulPaint: latest.firstContentfulPaint
    }
  }

  /**
   * 获取性能建议
   * @returns {Array} 建议列表
   */
  getRecommendations() {
    const recommendations = []

    // 组件渲染建议
    const slowComponents = this.metrics.componentRender
      .filter(item => item.renderTime > 100)
      .map(item => item.component)
    
    if (slowComponents.length > 0) {
      recommendations.push({
        type: 'component',
        level: 'warning',
        message: `以下组件渲染较慢: ${[...new Set(slowComponents)].join(', ')}`
      })
    }

    // API调用建议
    const slowApis = this.metrics.apiCalls
      .filter(item => item.duration > 3000)
      .map(item => item.url)
    
    if (slowApis.length > 0) {
      recommendations.push({
        type: 'api',
        level: 'warning',
        message: `以下API调用较慢: ${[...new Set(slowApis)].join(', ')}`
      })
    }

    // 内存使用建议
    const currentMemory = this.metrics.memoryUsage[this.metrics.memoryUsage.length - 1]
    if (currentMemory && (currentMemory.used / currentMemory.limit) > 0.8) {
      recommendations.push({
        type: 'memory',
        level: 'error',
        message: '内存使用率过高，建议刷新页面或关闭不必要的功能'
      })
    }

    return recommendations
  }

  /**
   * 计算平均值
   * @param {Array} array 数组
   * @param {string} property 属性名
   * @returns {number} 平均值
   */
  getAverage(array, property) {
    if (array.length === 0) return 0
    const sum = array.reduce((acc, item) => acc + item[property], 0)
    return Math.round(sum / array.length)
  }

  /**
   * 格式化字节数
   * @param {number} bytes 字节数
   * @returns {string} 格式化后的大小
   */
  formatBytes(bytes) {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * 清空所有指标
   */
  clearMetrics() {
    Object.keys(this.metrics).forEach(key => {
      this.metrics[key] = []
    })
  }

  /**
   * 导出性能数据
   * @returns {string} JSON格式的性能数据
   */
  exportMetrics() {
    const exportData = {
      timestamp: new Date().toISOString(),
      metrics: this.metrics,
      report: this.getPerformanceReport()
    }
    
    return JSON.stringify(exportData, null, 2)
  }
}

// 创建单例实例
const performanceMonitor = new PerformanceMonitor()

// 自动启动监控
performanceMonitor.startMonitoring()

export default performanceMonitor
