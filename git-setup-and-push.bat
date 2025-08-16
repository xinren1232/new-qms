@echo off
echo 🚀 QMS-AI系统GitHub推送脚本
echo ================================

:: 检查Git是否安装
git --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Git未安装，请先安装Git
    pause
    exit /b 1
)

echo ✅ Git已安装

:: 检查是否在Git仓库中
if not exist ".git" (
    echo 📁 初始化Git仓库...
    git init
    git config user.name "QMS-AI-System"
    git config user.email "qms-ai@example.com"
    echo ✅ Git仓库初始化完成
)

:: 检查远程仓库
git remote | findstr origin >nul
if errorlevel 1 (
    echo.
    echo 🔗 请输入GitHub仓库URL:
    echo 例如: https://github.com/username/qms-ai.git
    set /p repoUrl="GitHub仓库URL: "
    
    if defined repoUrl (
        git remote add origin !repoUrl!
        echo ✅ 远程仓库已添加
    ) else (
        echo ❌ 未提供仓库URL，无法继续
        pause
        exit /b 1
    )
)

:: 添加所有文件
echo 📦 添加文件到Git...
git add .

:: 提交更改
echo 💾 提交更改...
git commit -m "🎯 QMS-AI插件验证系统完整实现 - 18个插件全部完成测试文件导入功能"

:: 推送到GitHub
echo 🚀 推送到GitHub...
git push -u origin main
if errorlevel 1 (
    echo ⚠️ 推送到main分支失败，尝试master分支...
    git push -u origin master
    if errorlevel 1 (
        echo ❌ 推送失败，请检查网络连接和权限
        pause
        exit /b 1
    )
)

echo.
echo ✅ 成功推送到GitHub!
echo 🎉 QMS-AI系统已保存到GitHub

:: 显示仓库信息
for /f "tokens=*" %%i in ('git remote get-url origin') do set remoteUrl=%%i
echo 🔗 GitHub仓库: %remoteUrl%

echo.
echo 📋 系统包含:
echo   • 完整的前端应用 (Vue3 + Element Plus)
echo   • 后端微服务 (Node.js + Express)
echo   • 18个插件验证系统
echo   • 8个AI模型集成
echo   • 配置驱动架构
echo   • 完整的部署脚本

pause
