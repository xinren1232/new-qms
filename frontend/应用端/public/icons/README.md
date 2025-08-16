# 图标文件夹

这个文件夹包含QMS系统中使用的各种图标文件。

## 图标列表

### AI模型图标
- `openai-logo.png` - OpenAI GPT模型图标
- `google-logo.png` - Google Gemini模型图标
- `anthropic-logo.png` - Anthropic Claude模型图标
- `deepseek-logo.png` - DeepSeek模型图标

### 集成服务图标
- `feishu-logo.png` - 飞书集成图标
- `github-logo.png` - GitHub集成图标
- `mcp-logo.png` - MCP协议图标
- `coze-logo.png` - Coze Studio图标

### 功能图标
- `filesystem-icon.png` - 文件系统图标
- `database-icon.png` - 数据库图标
- `git-icon.png` - Git仓库图标
- `chart-logo.png` - 数据分析图标
- `pdf-logo.png` - PDF处理图标

## 使用说明

这些图标文件可以在Vue组件中通过以下方式引用：

```vue
<img src="/icons/feishu-logo.png" alt="飞书" />
```

或者在CSS中使用：

```css
.icon {
  background-image: url('/icons/feishu-logo.png');
}
```

## 图标规范

- 推荐尺寸：48x48px 或 64x64px
- 格式：PNG（支持透明背景）
- 文件大小：建议小于50KB
- 命名规范：使用小写字母和连字符
