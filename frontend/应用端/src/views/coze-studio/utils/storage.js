/**
 * Coze Studio 本地存储工具
 * 提供项目数据的持久化存储功能
 */

const STORAGE_KEYS = {
  PROJECTS: 'coze_studio_projects',
  SETTINGS: 'coze_studio_settings',
  WORKSPACE: 'coze_studio_workspace',
  RECENT_FILES: 'coze_studio_recent_files'
}

class CozeStorage {
  /**
   * 保存项目数据
   * @param {Object} project 项目对象
   */
  saveProject(project) {
    try {
      const projects = this.getProjects()
      const existingIndex = projects.findIndex(p => p.id === project.id)
      
      if (existingIndex >= 0) {
        projects[existingIndex] = {
          ...project,
          updatedAt: new Date().toISOString()
        }
      } else {
        projects.push({
          ...project,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        })
      }
      
      localStorage.setItem(STORAGE_KEYS.PROJECTS, JSON.stringify(projects))
      return true
    } catch (error) {
      console.error('保存项目失败:', error)
      return false
    }
  }

  /**
   * 获取所有项目
   * @returns {Array} 项目列表
   */
  getProjects() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.PROJECTS)
      return data ? JSON.parse(data) : []
    } catch (error) {
      console.error('获取项目失败:', error)
      return []
    }
  }

  /**
   * 获取单个项目
   * @param {string} projectId 项目ID
   * @returns {Object|null} 项目对象
   */
  getProject(projectId) {
    const projects = this.getProjects()
    return projects.find(p => p.id === projectId) || null
  }

  /**
   * 删除项目
   * @param {string} projectId 项目ID
   */
  deleteProject(projectId) {
    try {
      const projects = this.getProjects()
      const filteredProjects = projects.filter(p => p.id !== projectId)
      localStorage.setItem(STORAGE_KEYS.PROJECTS, JSON.stringify(filteredProjects))
      return true
    } catch (error) {
      console.error('删除项目失败:', error)
      return false
    }
  }

  /**
   * 导出项目
   * @param {string} projectId 项目ID
   */
  exportProject(projectId) {
    const project = this.getProject(projectId)
    if (!project) {
      throw new Error('项目不存在')
    }

    const exportData = {
      version: '1.0.0',
      exportTime: new Date().toISOString(),
      project: project
    }

    const dataStr = JSON.stringify(exportData, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)

    const link = document.createElement('a')
    link.href = url
    link.download = `${project.name}_${Date.now()}.coze`
    link.click()

    URL.revokeObjectURL(url)
  }

  /**
   * 导入项目
   * @param {File} file 项目文件
   * @returns {Promise<Object>} 导入的项目
   */
  importProject(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      
      reader.onload = (e) => {
        try {
          const importData = JSON.parse(e.target.result)
          
          if (!importData.project) {
            throw new Error('无效的项目文件格式')
          }

          const project = {
            ...importData.project,
            id: `imported_${Date.now()}`,
            name: `${importData.project.name}_导入`,
            importedAt: new Date().toISOString()
          }

          if (this.saveProject(project)) {
            resolve(project)
          } else {
            reject(new Error('保存导入项目失败'))
          }
        } catch (error) {
          reject(new Error(`导入失败: ${error.message}`))
        }
      }

      reader.onerror = () => {
        reject(new Error('文件读取失败'))
      }

      reader.readAsText(file)
    })
  }

  /**
   * 保存设置
   * @param {Object} settings 设置对象
   */
  saveSettings(settings) {
    try {
      localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(settings))
      return true
    } catch (error) {
      console.error('保存设置失败:', error)
      return false
    }
  }

  /**
   * 获取设置
   * @returns {Object} 设置对象
   */
  getSettings() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.SETTINGS)
      return data ? JSON.parse(data) : this.getDefaultSettings()
    } catch (error) {
      console.error('获取设置失败:', error)
      return this.getDefaultSettings()
    }
  }

  /**
   * 获取默认设置
   * @returns {Object} 默认设置
   */
  getDefaultSettings() {
    return {
      general: {
        language: 'zh-CN',
        theme: 'auto',
        autoSave: true,
        saveInterval: 60
      },
      ai: {
        defaultModel: 'gpt-4o',
        defaultTemperature: 0.7,
        maxTokens: 2048
      },
      workflow: {
        timeout: 300,
        retryCount: 3,
        concurrency: 5
      },
      security: {
        dataEncryption: true,
        auditLog: true
      }
    }
  }

  /**
   * 保存工作空间状态
   * @param {Object} workspace 工作空间状态
   */
  saveWorkspace(workspace) {
    try {
      localStorage.setItem(STORAGE_KEYS.WORKSPACE, JSON.stringify(workspace))
      return true
    } catch (error) {
      console.error('保存工作空间失败:', error)
      return false
    }
  }

  /**
   * 获取工作空间状态
   * @returns {Object} 工作空间状态
   */
  getWorkspace() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.WORKSPACE)
      return data ? JSON.parse(data) : {
        currentProject: null,
        activeTab: 'agent',
        sidebarCollapsed: false
      }
    } catch (error) {
      console.error('获取工作空间失败:', error)
      return {
        currentProject: null,
        activeTab: 'agent',
        sidebarCollapsed: false
      }
    }
  }

  /**
   * 添加最近文件
   * @param {Object} file 文件信息
   */
  addRecentFile(file) {
    try {
      const recentFiles = this.getRecentFiles()
      const existingIndex = recentFiles.findIndex(f => f.id === file.id)
      
      if (existingIndex >= 0) {
        recentFiles.splice(existingIndex, 1)
      }
      
      recentFiles.unshift({
        ...file,
        accessTime: new Date().toISOString()
      })
      
      // 只保留最近10个文件
      const limitedFiles = recentFiles.slice(0, 10)
      localStorage.setItem(STORAGE_KEYS.RECENT_FILES, JSON.stringify(limitedFiles))
      
      return true
    } catch (error) {
      console.error('添加最近文件失败:', error)
      return false
    }
  }

  /**
   * 获取最近文件
   * @returns {Array} 最近文件列表
   */
  getRecentFiles() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.RECENT_FILES)
      return data ? JSON.parse(data) : []
    } catch (error) {
      console.error('获取最近文件失败:', error)
      return []
    }
  }

  /**
   * 清空所有数据
   */
  clearAll() {
    try {
      Object.values(STORAGE_KEYS).forEach(key => {
        localStorage.removeItem(key)
      })
      return true
    } catch (error) {
      console.error('清空数据失败:', error)
      return false
    }
  }

  /**
   * 获取存储使用情况
   * @returns {Object} 存储统计
   */
  getStorageStats() {
    try {
      const stats = {}
      let totalSize = 0
      
      Object.entries(STORAGE_KEYS).forEach(([name, key]) => {
        const data = localStorage.getItem(key)
        const size = data ? new Blob([data]).size : 0
        stats[name] = {
          size,
          sizeFormatted: this.formatBytes(size)
        }
        totalSize += size
      })
      
      return {
        ...stats,
        total: {
          size: totalSize,
          sizeFormatted: this.formatBytes(totalSize)
        }
      }
    } catch (error) {
      console.error('获取存储统计失败:', error)
      return {}
    }
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
}

// 创建单例实例
const cozeStorage = new CozeStorage()

export default cozeStorage
export { STORAGE_KEYS }
