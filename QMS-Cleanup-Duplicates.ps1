# QMS-AI 重复文件清理工具 v2.0
Write-Host ""
Write-Host "========================================"
Write-Host "    QMS-AI 重复文件清理工具 v2.0"
Write-Host "========================================"
Write-Host ""

# 创建备份目录
$backupDir = "cleanup-backup\$(Get-Date -Format 'yyyyMMdd_HHmmss')"
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
Write-Host "📦 创建备份目录: $backupDir" -ForegroundColor Green
Write-Host ""

# 统计变量
$deletedFiles = 0
$deletedDirs = 0
$movedFiles = 0

# ==========================================
# 第一步：删除重复的启动脚本
# ==========================================
Write-Host "[1/6] 🗑️ 清理重复的启动脚本..." -ForegroundColor Cyan

$oldScripts = @(
    "start-qms.bat",
    "quick-start.bat", 
    "start-qms-ai.bat",
    "config-start.bat",
    "debug-config-start.bat"
)

foreach ($script in $oldScripts) {
    if (Test-Path $script) {
        Write-Host "备份并删除: $script" -ForegroundColor Yellow
        Copy-Item $script $backupDir -Force
        Remove-Item $script -Force
        $deletedFiles++
    }
}

# 删除VBS文件
$vbsFiles = Get-ChildItem -Filter "*.vbs" -File
foreach ($vbs in $vbsFiles) {
    Write-Host "删除VBS文件: $($vbs.Name)" -ForegroundColor Yellow
    Copy-Item $vbs.FullName $backupDir -Force
    Remove-Item $vbs.FullName -Force
    $deletedFiles++
}

# ==========================================
# 第二步：清理重复的后端启动脚本
# ==========================================
Write-Host ""
Write-Host "[2/6] 🔧 清理后端重复脚本..." -ForegroundColor Cyan

$backendScripts = @(
    "backend\nodejs\start-all.bat",
    "backend\nodejs\start-all.sh"
)

foreach ($script in $backendScripts) {
    if (Test-Path $script) {
        Write-Host "删除过时后端脚本: $script" -ForegroundColor Yellow
        Copy-Item $script $backupDir -Force
        Remove-Item $script -Force
        $deletedFiles++
    }
}

# ==========================================
# 第三步：删除重复的部署包目录
# ==========================================
Write-Host ""
Write-Host "[3/6] 📁 清理重复的部署包..." -ForegroundColor Cyan

$oldDirs = @(
    "qms-deploy-temp",
    "qms-update-package", 
    "pnpm-fix-package"
)

foreach ($dir in $oldDirs) {
    if (Test-Path $dir) {
        Write-Host "删除重复目录: $dir" -ForegroundColor Yellow
        Remove-Item $dir -Recurse -Force
        $deletedDirs++
    }
}

# ==========================================
# 第四步：清理测试和临时文件
# ==========================================
Write-Host ""
Write-Host "[4/6] 🧪 清理测试和临时文件..." -ForegroundColor Cyan

$testFiles = Get-ChildItem -Filter "test-*" -File
foreach ($test in $testFiles) {
    Write-Host "删除测试文件: $($test.Name)" -ForegroundColor Yellow
    Remove-Item $test.FullName -Force
    $deletedFiles++
}

# 删除特定临时文件
$tempFiles = @(
    "simple-chat-service.js",
    "performance-test-suite.js"
)

foreach ($temp in $tempFiles) {
    if (Test-Path $temp) {
        Write-Host "删除临时文件: $temp" -ForegroundColor Yellow
        Remove-Item $temp -Force
        $deletedFiles++
    }
}

# ==========================================
# 第五步：整理文档到archive
# ==========================================
Write-Host ""
Write-Host "[5/6] 📄 整理重复的文档..." -ForegroundColor Cyan

# 创建archive目录
if (!(Test-Path "docs\archive")) {
    New-Item -ItemType Directory -Path "docs\archive" -Force | Out-Null
}

# 移动QMS-AI报告文档
$reports = Get-ChildItem -Filter "QMS-AI-*.md" -File
foreach ($report in $reports) {
    Write-Host "移动报告到archive: $($report.Name)" -ForegroundColor Yellow
    Move-Item $report.FullName "docs\archive\" -Force
    $movedFiles++
}

# 移动其他过时文档
$oldDocs = @(
    "AI对话记录管理问题分析报告.md",
    "AI对话记录问题诊断报告.md", 
    "Coze-Studio-*.md",
    "PDF解析插件修复完成报告.md"
)

foreach ($pattern in $oldDocs) {
    $docs = Get-ChildItem -Filter $pattern -File
    foreach ($doc in $docs) {
        if ($doc.Name -ne "README.md" -and $doc.Name -ne "QUICK-START-GUIDE.md") {
            Write-Host "移动文档到archive: $($doc.Name)" -ForegroundColor Yellow
            Move-Item $doc.FullName "docs\archive\" -Force
            $movedFiles++
        }
    }
}

# ==========================================
# 第六步：创建启动脚本使用指南
# ==========================================
Write-Host ""
Write-Host "[6/6] 📋 创建启动脚本使用指南..." -ForegroundColor Cyan

$guideContent = @"
# QMS-AI 启动脚本使用指南

## 🚀 推荐启动方式

### 1. 主启动器 (推荐)
``````bash
QMS-START.bat
``````
- 提供完整的菜单选项
- 支持多种启动模式
- 包含健康检查和重启功能

### 2. 快速启动
``````bash
QMS-Quick-Check-And-Start.bat
``````
- 一键启动所有服务
- 自动端口检查
- 适合日常开发使用

### 3. 服务管理器
``````bash
QMS-Service-Manager.bat
``````
- 单独管理各个服务
- 支持启动/停止/重启
- 详细的状态监控

## 🔧 工作空间启动 (开发推荐)
``````bash
start-with-pnpm.bat
``````
- 使用pnpm工作空间
- 统一依赖管理
- 支持并行启动

## 📊 状态检查
``````bash
QMS-Status-Check.bat
QMS-Final-Status-Check.ps1
``````

## 🛑 停止服务
``````bash
QMS-Stop-All.bat
``````

## ⚠️ 已清理的过时脚本
- start-qms.bat (功能重复)
- quick-start.bat (端口过时)
- *.vbs (过时的VBScript启动器)
- backend/nodejs/start-all.bat (端口配置错误)

## 📁 当前推荐的启动流程
1. 首次使用: `QMS-START.bat` → 选择 "1 全面启动"
2. 日常开发: `QMS-Quick-Check-And-Start.bat`
3. 问题排查: `QMS-Service-Manager.bat`
4. 状态检查: `QMS-Final-Status-Check.ps1`
"@

Set-Content -Path "START-GUIDE.md" -Value $guideContent -Encoding UTF8

Write-Host ""
Write-Host "✅ 清理完成！" -ForegroundColor Green
Write-Host ""
Write-Host "📊 清理统计:" -ForegroundColor Cyan
Write-Host "  • 删除文件: $deletedFiles 个" -ForegroundColor White
Write-Host "  • 删除目录: $deletedDirs 个" -ForegroundColor White
Write-Host "  • 移动文档: $movedFiles 个" -ForegroundColor White
Write-Host ""
Write-Host "📁 备份位置: $backupDir" -ForegroundColor Yellow
Write-Host "📋 启动指南: START-GUIDE.md" -ForegroundColor Yellow
Write-Host ""
Write-Host "🎉 现在你的项目更加整洁了！" -ForegroundColor Green
Write-Host "推荐使用: QMS-START.bat 或 QMS-Quick-Check-And-Start.bat" -ForegroundColor Green
Write-Host ""

Read-Host "按回车键继续"
