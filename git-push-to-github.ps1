# QMS-AI系统GitHub推送脚本
Write-Host "🚀 开始将QMS-AI系统推送到GitHub..." -ForegroundColor Green

# 检查Git是否安装
try {
    $gitVersion = git --version
    Write-Host "✅ Git已安装: $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Git未安装，请先安装Git" -ForegroundColor Red
    exit 1
}

# 检查是否在Git仓库中
if (-not (Test-Path ".git")) {
    Write-Host "📁 初始化Git仓库..." -ForegroundColor Yellow
    git init
    
    # 配置Git用户信息
    git config user.name "QMS-AI-System"
    git config user.email "qms-ai@example.com"
    
    Write-Host "✅ Git仓库初始化完成" -ForegroundColor Green
}

# 检查远程仓库
$remotes = git remote
if ($remotes -notcontains "origin") {
    Write-Host "🔗 请输入GitHub仓库URL (例如: https://github.com/username/qms-ai.git):" -ForegroundColor Yellow
    $repoUrl = Read-Host "GitHub仓库URL"
    
    if ($repoUrl) {
        git remote add origin $repoUrl
        Write-Host "✅ 远程仓库已添加: $repoUrl" -ForegroundColor Green
    } else {
        Write-Host "❌ 未提供仓库URL，无法继续" -ForegroundColor Red
        exit 1
    }
}

# 添加所有文件
Write-Host "📦 添加文件到Git..." -ForegroundColor Yellow
git add .

# 检查是否有文件被添加
$status = git status --porcelain
if ($status) {
    Write-Host "📝 准备提交的文件:" -ForegroundColor Yellow
    git status --short
    
    # 提交更改
    $commitMessage = @"
🎯 QMS-AI插件验证系统完整实现

✅ 完成所有18个插件的测试文件导入功能
- 文档解析类: 7个插件 (PDF、CSV、JSON、XML、XLSX、DOCX、Excel分析器)
- 数据分析类: 5个插件 (统计分析器、SPC控制器、数据清洗器、异常检测器、文本摘要器)  
- 质量工具类: 3个插件 (FMEA分析器、MSA计算器、缺陷检测器)
- AI处理类: 1个插件 (OCR识别器)
- 外部连接类: 2个插件 (API连接器、数据库查询器)

🔧 技术实现:
- 统一配置所有插件支持预制测试文件选择
- 16个专用测试文件覆盖所有插件类型
- 完整的验证界面和用户体验优化
- 详细的测试数据和使用场景

📊 验证成果:
- 100%插件覆盖率
- 统一的用户界面
- 完整的测试文档
- 系统性的验证流程

🚀 系统特性:
- Vue3 + Node.js 微服务架构
- 8个AI模型集成 (6个内部 + 2个外部)
- 配置驱动的动态功能管理
- 完整的插件生态系统
- 实时监控和健康检查
"@

    Write-Host "💾 提交更改..." -ForegroundColor Yellow
    git commit -m $commitMessage
    
    # 推送到GitHub
    Write-Host "🚀 推送到GitHub..." -ForegroundColor Yellow
    try {
        git push -u origin main
        Write-Host "✅ 成功推送到GitHub!" -ForegroundColor Green
        
        # 显示仓库信息
        $remoteUrl = git remote get-url origin
        Write-Host "🔗 GitHub仓库: $remoteUrl" -ForegroundColor Cyan
        
    } catch {
        Write-Host "⚠️ 推送失败，尝试推送到master分支..." -ForegroundColor Yellow
        try {
            git push -u origin master
            Write-Host "✅ 成功推送到GitHub (master分支)!" -ForegroundColor Green
        } catch {
            Write-Host "❌ 推送失败，请检查:" -ForegroundColor Red
            Write-Host "  1. GitHub仓库URL是否正确" -ForegroundColor Red
            Write-Host "  2. 是否有推送权限" -ForegroundColor Red
            Write-Host "  3. 网络连接是否正常" -ForegroundColor Red
            Write-Host "  4. 是否需要身份验证" -ForegroundColor Red
        }
    }
    
} else {
    Write-Host "ℹ️ 没有新的更改需要提交" -ForegroundColor Blue
    
    # 仍然尝试推送
    Write-Host "🚀 尝试推送现有提交..." -ForegroundColor Yellow
    try {
        git push origin main
        Write-Host "✅ 推送完成!" -ForegroundColor Green
    } catch {
        try {
            git push origin master
            Write-Host "✅ 推送完成 (master分支)!" -ForegroundColor Green
        } catch {
            Write-Host "❌ 推送失败" -ForegroundColor Red
        }
    }
}

Write-Host "`n🎉 QMS-AI系统GitHub推送操作完成!" -ForegroundColor Green
Write-Host "📋 系统包含:" -ForegroundColor Cyan
Write-Host "  • 完整的前端应用 (Vue3 + Element Plus)" -ForegroundColor White
Write-Host "  • 后端微服务 (Node.js + Express)" -ForegroundColor White
Write-Host "  • 18个插件验证系统" -ForegroundColor White
Write-Host "  • 8个AI模型集成" -ForegroundColor White
Write-Host "  • 配置驱动架构" -ForegroundColor White
Write-Host "  • 完整的部署脚本" -ForegroundColor White

pause
