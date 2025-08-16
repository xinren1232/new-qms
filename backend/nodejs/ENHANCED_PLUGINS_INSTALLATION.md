# QMS-AI增强插件安装指南

## 📦 依赖安装

### 核心统计库
```bash
npm install simple-statistics
npm install d3-array
npm install ml-matrix
```

### 可选增强库
```bash
# OCR功能
npm install tesseract.js

# 机器学习
npm install @tensorflow/tfjs

# 自然语言处理
npm install natural
```

## 🔧 集成步骤

### 1. 复制增强插件文件
```bash
cp enhanced-spc-plugin.js services/
cp enhanced-fmea-plugin.js services/
cp integrate-enhanced-plugins.js services/
```

### 2. 修改插件生态系统
在 `plugin-ecosystem.js` 中添加增强插件支持：

```javascript
const EnhancedSPCController = require('./enhanced-spc-plugin');
const { EnhancedFMEAAnalyzer } = require('./enhanced-fmea-plugin');

// 在executePlugin方法中添加
case 'enhanced_spc':
  return await this.executeEnhancedSPC(input, plugin.config);
case 'enhanced_fmea':
  return await this.executeEnhancedFMEA(input, plugin.config);
```

### 3. 更新数据库
```sql
-- 添加增强插件记录
INSERT INTO plugins (id, name, type, version, capabilities) VALUES
('enhanced_spc', '专业SPC统计过程控制', 'data_analyzer', '2.0.0', 
 '["I-MR控制图","X-bar & R控制图","过程能力分析","趋势分析"]'),
('enhanced_fmea', '专业FMEA失效模式分析', 'quality_tool', '2.0.0',
 '["手机制造业知识库","IEC 60812标准","风险评估矩阵","改进措施库"]');
```

### 4. 前端界面更新
在插件选择界面添加增强版本选项，让用户可以选择使用原始版本或增强版本。

## 🎯 使用示例

### SPC分析
```javascript
const result = await pluginManager.executePlugin('enhanced_spc', {
  series: [98.5, 99.2, 100.1, 99.8, 100.3],
  chartType: 'i_mr',
  specifications: { usl: 102, lsl: 98 }
});
```

### FMEA分析
```javascript
const result = await pluginManager.executePlugin('enhanced_fmea', {
  components: [{
    name: 'screen',
    failure_modes: [{
      mode: '屏幕破裂',
      severity: 8,
      occurrence: 3,
      detection: 4
    }]
  }]
});
```

## 📊 性能对比

| 功能 | 原始插件 | 增强插件 | 提升 |
|------|----------|----------|------|
| SPC控制图类型 | 1种 | 7种 | 700% |
| FMEA知识库 | 无 | 手机制造业专业库 | 新增 |
| 风险评估 | 基础RPN | IEC 60812标准 | 专业化 |
| 改进建议 | 简单 | 智能化结构化 | 显著提升 |

## 🔄 迁移策略

1. **并行运行**: 同时保留原始和增强插件
2. **用户选择**: 让用户决定使用哪个版本
3. **渐进迁移**: 逐步引导用户使用增强版本
4. **数据兼容**: 确保数据格式向后兼容

## 🛠️ 故障排除

### 常见问题
1. **依赖缺失**: 确保安装了所有必需的npm包
2. **内存不足**: 增强插件需要更多内存，建议至少2GB
3. **性能问题**: 大数据集可能需要更长处理时间

### 解决方案
1. 检查package.json中的依赖
2. 增加Node.js内存限制: `node --max-old-space-size=4096`
3. 实施数据分批处理
