@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM QMS-AI 增量更新部署脚本 (Windows版本)
REM 用于将最新更新推送到阿里云服务器

echo 🚀 QMS-AI 增量更新部署工具 v2.0
echo ========================================
echo.

REM 配置变量
set SERVER_IP=47.108.152.16
set SERVER_USER=root
set PROJECT_DIR=/opt/qms
set BACKUP_DIR=/opt/qms-backup-%date:~0,4%%date:~5,2%%date:~8,2%-%time:~0,2%%time:~3,2%%time:~6,2%
set GITHUB_REPO=https://github.com/xinren1232/qmsai.git
set BRANCH=clean-main

echo 📋 部署配置:
echo   服务器: %SERVER_IP%
echo   项目目录: %PROJECT_DIR%
echo   GitHub仓库: %GITHUB_REPO%
echo   分支: %BRANCH%
echo.

REM 检查SSH连接
echo 🔍 检查服务器连接...
ssh -o ConnectTimeout=10 %SERVER_USER%@%SERVER_IP% "echo 'SSH连接正常'" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 无法连接到服务器 %SERVER_IP%
    echo 请检查:
    echo   1. 服务器IP地址是否正确
    echo   2. SSH密钥是否配置
    echo   3. 网络连接是否正常
    pause
    exit /b 1
)
echo ✅ 服务器连接正常
echo.

REM 创建增量更新脚本
echo 📝 创建服务器端更新脚本...
(
echo #!/bin/bash
echo.
echo # QMS-AI 服务器端增量更新脚本
echo set -e
echo.
echo echo "🚀 开始QMS-AI增量更新..."
echo.
echo # 检查项目目录
echo if [ ! -d "%PROJECT_DIR%" ]; then
echo     echo "❌ 项目目录不存在: %PROJECT_DIR%"
echo     exit 1
echo fi
echo.
echo # 备份现有系统
echo echo "💾 备份现有系统..."
echo mkdir -p %BACKUP_DIR%
echo.
echo # 停止服务
echo echo "🛑 停止现有服务..."
echo cd %PROJECT_DIR%
echo if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
echo     docker-compose -f deployment/aliyun-deploy-optimized.yml down 2^>/dev/null ^|^| true
echo fi
echo.
echo # 备份关键文件
echo echo "📁 备份关键文件..."
echo cp -r backend %BACKUP_DIR%/ 2^>/dev/null ^|^| true
echo cp -r frontend %BACKUP_DIR%/ 2^>/dev/null ^|^| true
echo cp -r deployment %BACKUP_DIR%/ 2^>/dev/null ^|^| true
echo cp .env %BACKUP_DIR%/ 2^>/dev/null ^|^| true
echo.
echo # 拉取最新代码
echo echo "📥 拉取最新代码..."
echo if [ ! -d ".git" ]; then
echo     git init
echo     git remote add origin %GITHUB_REPO%
echo fi
echo.
echo git add . 2^>/dev/null ^|^| true
echo git stash 2^>/dev/null ^|^| true
echo git fetch origin %BRANCH%
echo git checkout %BRANCH% 2^>/dev/null ^|^| git checkout -b %BRANCH% origin/%BRANCH%
echo git pull origin %BRANCH%
echo.
echo # 恢复配置
echo echo "⚙️ 恢复配置文件..."
echo if [ -f "%BACKUP_DIR%/.env" ]; then
echo     cp %BACKUP_DIR%/.env ./
echo fi
echo.
echo # 更新依赖
echo echo "📦 更新依赖..."
echo if [ -f "backend/nodejs/package.json" ]; then
echo     cd backend/nodejs
echo     npm install --production
echo     cd ../..
echo fi
echo.
echo # 重新构建和启动
echo echo "🚀 重新构建和启动服务..."
echo docker system prune -f
echo if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
echo     docker-compose -f deployment/aliyun-deploy-optimized.yml build --no-cache
echo     docker-compose -f deployment/aliyun-deploy-optimized.yml up -d
echo fi
echo.
echo # 等待服务启动
echo echo "⏳ 等待服务启动..."
echo sleep 30
echo.
echo # 验证服务
echo echo "🔍 验证服务状态..."
echo docker-compose -f deployment/aliyun-deploy-optimized.yml ps
echo.
echo echo "✅ 增量更新完成！"
echo echo "🌐 访问地址:"
echo echo "  主应用: http://%SERVER_IP%:8081"
echo echo "  配置管理: http://%SERVER_IP%:8072"
echo echo "  API网关: http://%SERVER_IP%:8085"
echo.
echo echo "💾 备份位置: %BACKUP_DIR%"
) > temp_update_script.sh

REM 上传并执行更新脚本
echo 📤 上传更新脚本到服务器...
scp temp_update_script.sh %SERVER_USER%@%SERVER_IP%:%PROJECT_DIR%/update_script.sh
if %errorlevel% neq 0 (
    echo ❌ 上传脚本失败
    del temp_update_script.sh
    pause
    exit /b 1
)

echo ✅ 脚本上传成功
echo.

echo 🚀 在服务器上执行更新...
ssh %SERVER_USER%@%SERVER_IP% "cd %PROJECT_DIR% && chmod +x update_script.sh && ./update_script.sh"

if %errorlevel% equ 0 (
    echo.
    echo 🎉 QMS-AI增量更新完成！
    echo ========================================
    echo.
    echo 📋 更新内容:
    echo   ✅ 全面服务管理器
    echo   ✅ AI智能功能优化
    echo   ✅ 前端界面改进
    echo   ✅ 系统性能优化
    echo.
    echo 🆕 新增功能:
    echo   🤖 AutoGPT执行器
    echo   🤝 CrewAI协调器
    echo   🧠 LangChain内存管理
    echo   🎯 智能模型选择器
    echo   🔧 插件生态系统
    echo.
    echo 🌐 访问地址:
    echo   主应用: http://%SERVER_IP%:8081
    echo   配置管理: http://%SERVER_IP%:8072
    echo   API网关: http://%SERVER_IP%:8085
    echo.
    echo 🔧 管理命令:
    echo   查看状态: ssh %SERVER_USER%@%SERVER_IP% "cd %PROJECT_DIR% && docker-compose -f deployment/aliyun-deploy-optimized.yml ps"
    echo   查看日志: ssh %SERVER_USER%@%SERVER_IP% "cd %PROJECT_DIR% && docker-compose -f deployment/aliyun-deploy-optimized.yml logs -f"
    echo.
) else (
    echo ❌ 更新过程中出现错误
    echo 请检查服务器日志或手动登录服务器查看问题
)

REM 清理临时文件
del temp_update_script.sh

echo.
echo 按任意键退出...
pause >nul
