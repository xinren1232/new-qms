#!/bin/bash
# QMS-AI开源技术栈安装脚本

echo "🚀 开始安装QMS-AI开源技术栈..."

# 核心统计分析库
npm install simple-statistics
npm install d3-array
npm install ml-matrix

# OCR识别库
npm install tesseract.js

# 机器学习库
npm install @tensorflow/tfjs
npm install @tensorflow/tfjs-node

# 自然语言处理
npm install natural
npm install compromise

# 图像处理（可选，需要额外配置）
# npm install opencv4nodejs

# 数据可视化
npm install d3
npm install chart.js

echo "✅ 开源技术栈安装完成！"
echo "📚 参考文档已生成到 opensource-solutions-analysis.md"
echo "🔧 下一步：运行 node implement-opensource-plugins.js 开始集成"
