# 📖 Coze Studio 用户指南

## 🎯 概述

Coze Studio 是一个集成在QMS AI系统中的AI Agent开发平台，基于开源Coze设计理念构建。它提供了可视化的界面来创建、配置和管理AI助手、工作流、知识库和插件。

### ✨ 核心特性

- 🤖 **AI Agent Builder**: 可视化构建智能助手
- 🔄 **Workflow Designer**: 拖拽式工作流设计
- 📚 **Knowledge Manager**: 智能知识库管理
- 🔧 **Plugin Manager**: 插件生态系统
- 🎨 **现代化UI**: 基于Vue 3 + Element Plus

---

## 🚀 快速开始

### 1. 启动Coze Studio

```bash
# 运行启动脚本
scripts/start-coze-studio.bat

# 或手动访问
http://localhost:8082/coze-plugins/studio
```

### 2. 创建第一个项目

1. 点击右上角 **"创建"** 按钮
2. 选择项目类型 (Agent/Workflow/Knowledge/Plugin)
3. 填写基本信息
4. 配置初始参数
5. 点击 **"创建项目"**

---

## 🤖 AI Agent Builder

### 功能概述

AI Agent Builder 是用于创建和配置智能助手的可视化工具。

### 主要功能

#### **基本信息配置**
- **名称**: Agent的显示名称
- **描述**: Agent的功能描述
- **头像**: 自定义Agent头像
- **标签**: 分类和搜索标签

#### **模型配置**
```javascript
// 支持的AI模型
const models = [
  'GPT-4o',      // OpenAI最新模型
  'Claude-3',    // Anthropic Claude
  'DeepSeek',    // 开源模型
  // 更多模型...
]
```

#### **提示词工程**
- **系统提示词**: 定义Agent角色和行为
- **用户提示词**: 用户交互模板
- **示例对话**: 训练对话样例

#### **功能开关**
- ✅ 记忆功能 - 记住对话历史
- ✅ 网络搜索 - 实时信息检索
- ✅ 图像生成 - AI绘图能力
- ✅ 代码执行 - Python代码运行

### 使用步骤

1. **创建Agent项目**
   ```
   项目类型 → AI Agent
   填写基本信息 → 下一步
   配置模型参数 → 创建
   ```

2. **配置提示词**
   ```
   系统提示词: "你是一个专业的质量管理专家..."
   用户提示词: "用户问题: {question}"
   添加示例对话
   ```

3. **测试Agent**
   ```
   右侧预览面板 → 输入测试消息
   查看回复效果 → 调整配置
   ```

4. **发布Agent**
   ```
   点击"测试" → 验证功能
   点击"发布" → 上线使用
   ```

---

## 🔄 Workflow Designer

### 功能概述

Workflow Designer 提供拖拽式的工作流设计界面，支持复杂的业务流程自动化。

### 节点类型

#### **基础节点**
- 🟢 **开始节点**: 工作流入口
- 🔴 **结束节点**: 工作流出口
- 🔀 **条件节点**: 逻辑分支判断
- 🔄 **循环节点**: 重复执行逻辑

#### **AI节点**
- 🤖 **LLM对话**: 大语言模型交互
- 📊 **文本嵌入**: 向量化处理
- 🏷️ **文本分类**: 内容分类
- 📝 **文本摘要**: 内容摘要

#### **工具节点**
- 🌐 **HTTP请求**: API调用
- 🗄️ **数据库查询**: 数据操作
- 📁 **文件操作**: 文件处理
- 🔍 **网络搜索**: 信息检索

### 设计流程

1. **创建工作流**
   ```
   新建项目 → Workflow
   设置触发方式 → 手动/定时/事件/API
   ```

2. **设计流程**
   ```
   从左侧拖拽节点到画布
   连接节点定义数据流
   配置节点参数
   ```

3. **测试执行**
   ```
   点击"执行" → 查看运行结果
   调试节点 → 优化流程
   ```

### 配置示例

```javascript
// LLM节点配置
{
  "model": "gpt-4o",
  "prompt": "分析以下质量问题: {input}",
  "temperature": 0.7,
  "maxTokens": 1000
}

// 条件节点配置
{
  "condition": "output.confidence > 0.8",
  "trueAction": "approve",
  "falseAction": "review"
}
```

---

## 📚 Knowledge Manager

### 功能概述

Knowledge Manager 用于构建和管理智能知识库，支持文档上传、索引构建和语义搜索。

### 支持格式

- 📄 **PDF**: PDF文档
- 📝 **DOC/DOCX**: Word文档
- 📋 **TXT**: 纯文本文件
- 📖 **MD**: Markdown文档

### 核心功能

#### **文档管理**
- 批量上传文档
- 文档预览和编辑
- 文档分类和标签
- 版本控制

#### **索引构建**
```javascript
// 索引配置
{
  "chunkSize": 1000,        // 分块大小
  "chunkOverlap": 200,      // 重叠长度
  "embeddingModel": "ada-002", // 向量模型
  "autoIndex": true         // 自动索引
}
```

#### **语义搜索**
- 自然语言查询
- 相似度排序
- 上下文提取
- 答案生成

### 使用流程

1. **创建知识库**
   ```
   新建项目 → Knowledge
   配置向量模型 → 设置分块参数
   ```

2. **上传文档**
   ```
   点击"上传文档" → 选择文件
   等待索引完成 → 查看状态
   ```

3. **测试搜索**
   ```
   输入查询问题 → 查看搜索结果
   调整参数 → 优化效果
   ```

---

## 🔧 Plugin Manager

### 功能概述

Plugin Manager 提供插件生态系统，支持安装、开发和管理各种功能插件。

### 插件分类

#### **已安装插件**
- 查看已安装插件列表
- 启用/禁用插件
- 配置插件参数
- 卸载插件

#### **插件市场**
- 浏览可用插件
- 查看插件评分和下载量
- 安装免费/付费插件
- 查看插件详情

#### **我的插件**
- 创建自定义插件
- 编辑插件代码
- 发布到市场
- 版本管理

### 插件开发

#### **创建插件**
```javascript
// 插件基本结构
{
  "name": "HTTP请求工具",
  "description": "发送HTTP请求并处理响应",
  "category": "api",
  "version": "1.0.0",
  "entryPoint": "main",
  "dependencies": ["axios"]
}
```

#### **插件代码**
```javascript
// main.js
async function main(input) {
  const { url, method, data } = input
  
  try {
    const response = await axios({
      url,
      method,
      data
    })
    
    return {
      success: true,
      data: response.data
    }
  } catch (error) {
    return {
      success: false,
      error: error.message
    }
  }
}

module.exports = { main }
```

---

## ⚙️ 系统设置

### 通用设置
- 语言选择 (中文/英文/日文)
- 主题模式 (亮色/暗色/自动)
- 自动保存配置

### AI模型配置
- API密钥管理
- 模型参数设置
- 默认配置

### 安全设置
- 访问控制
- 数据加密
- 审计日志

---

## 🔧 故障排除

### 常见问题

#### **1. 页面无法访问**
```bash
# 检查服务状态
curl http://localhost:8082/health

# 重启应用端
cd frontend/应用端
npm run dev
```

#### **2. AI功能不可用**
```bash
# 检查聊天服务
curl http://localhost:3004/health

# 重启聊天服务
cd backend/nodejs
node chat-service.js
```

#### **3. 项目保存失败**
- 检查浏览器控制台错误
- 确认网络连接正常
- 清除浏览器缓存

### 日志查看

```bash
# 应用端日志
frontend/应用端/logs/

# 后端服务日志
backend/nodejs/logs/

# 浏览器控制台
F12 → Console
```

---

## 📞 技术支持

### 获取帮助

- 📧 **邮箱**: support@qms-ai.com
- 💬 **在线客服**: 系统内置聊天功能
- 📖 **文档**: 查看完整技术文档
- 🐛 **问题反馈**: GitHub Issues

### 更新日志

- **v1.0.0** (2024-01-20)
  - 🎉 首次发布
  - 🤖 AI Agent Builder
  - 🔄 Workflow Designer
  - 📚 Knowledge Manager
  - 🔧 Plugin Manager

---

## 📄 许可证

本项目基于 MIT 许可证开源，详情请查看 [LICENSE](../LICENSE) 文件。

---

**🎉 感谢使用 Coze Studio！**
