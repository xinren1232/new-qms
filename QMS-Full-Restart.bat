@echo off
chcp 65001 >nul
title QMS-AI 全面重启系统
color 0E

echo.
echo ==========================================
echo 🔄 QMS-AI 全面重启系统
echo ==========================================
echo.

echo 📋 重启流程:
echo   1. 停止所有运行中的服务
echo   2. 清理端口和进程
echo   3. 检查环境和依赖
echo   4. 启动配置中心
echo   5. 启动后端服务
echo   6. 启动前端应用
echo   7. 验证服务状态
echo.

pause

:: ==========================================
:: 第一步：停止所有服务
:: ==========================================
echo.
echo [1/7] 🛑 停止所有运行中的服务...
echo.

echo 🔍 查找运行中的进程...
tasklist /fi "imagename eq node.exe" 2>nul | findstr node.exe >nul
if %errorlevel% equ 0 (
    echo 📊 发现Node.js进程，正在停止...
    taskkill /f /im node.exe 2>nul
    echo ✅ Node.js进程已停止
) else (
    echo ✅ 没有运行中的Node.js进程
)

tasklist /fi "imagename eq npm.cmd" 2>nul | findstr npm >nul
if %errorlevel% equ 0 (
    echo 📊 发现npm进程，正在停止...
    taskkill /f /im npm.cmd 2>nul
    taskkill /f /im npm 2>nul
    echo ✅ npm进程已停止
) else (
    echo ✅ 没有运行中的npm进程
)

:: ==========================================
:: 第二步：清理端口和进程
:: ==========================================
echo.
echo [2/7] 🧹 清理端口和进程...
echo.

:: 清理关键端口
set "ports=3004 8080 8072 8081 8082 3009"
for %%p in (%ports%) do (
    netstat -ano | findstr :%%p >nul 2>&1
    if !errorlevel! equ 0 (
        echo ⚠️ 端口%%p被占用，正在释放...
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%%p') do (
            taskkill /f /pid %%a 2>nul
        )
        echo ✅ 端口%%p已释放
    ) else (
        echo ✅ 端口%%p空闲
    )
)

:: 等待端口完全释放
echo 🕐 等待端口完全释放...
timeout /t 3 >nul

:: ==========================================
:: 第三步：检查环境和依赖
:: ==========================================
echo.
echo [3/7] 🔍 检查环境和依赖...
echo.

:: 检查Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js未安装或未添加到PATH
    echo 请先安装Node.js: https://nodejs.org/
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

:: 检查npm
npm --version >nul 2>&1
if errorlevel 1 (
    echo ❌ npm未安装或未添加到PATH
    pause
    exit /b 1
)
echo ✅ npm环境正常

:: 检查项目目录
if not exist "backend\nodejs" (
    echo ❌ 后端目录不存在: backend\nodejs
    pause
    exit /b 1
)
echo ✅ 后端目录存在

if not exist "frontend\配置端" (
    echo ❌ 配置端目录不存在: frontend\配置端
    pause
    exit /b 1
)
echo ✅ 配置端目录存在

if not exist "frontend\应用端" (
    echo ❌ 应用端目录不存在: frontend\应用端
    pause
    exit /b 1
)
echo ✅ 应用端目录存在

:: 检查配置目录
if not exist "backend\nodejs\config\data" (
    echo 📁 创建配置目录...
    mkdir "backend\nodejs\config\data" 2>nul
)
echo ✅ 配置目录准备完成

:: ==========================================
:: 第四步：启动配置中心
:: ==========================================
echo.
echo [4/7] ⚙️ 启动配置中心...
echo.

echo 🚀 启动轻量级配置中心 (端口8082)...
start "QMS配置中心" cmd /k "cd /d backend\nodejs && echo 🚀 启动配置中心服务... && node lightweight-config-service.js"

:: 等待配置中心启动
echo 🕐 等待配置中心启动...
timeout /t 5 >nul

:: 验证配置中心
echo 🔍 验证配置中心状态...
curl -s http://localhost:8082/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 配置中心启动成功
) else (
    echo ⚠️ 配置中心可能还在启动中...
)

:: ==========================================
:: 第五步：启动后端服务
:: ==========================================
echo.
echo [5/7] 🔧 启动后端服务...
echo.

echo 🚀 启动AI聊天服务 (端口3004)...
start "QMS聊天服务" cmd /k "cd /d backend\nodejs && echo 🤖 启动AI聊天服务... && node chat-service.js"
timeout /t 3 >nul

echo 🚀 启动高级功能服务 (端口3009)...
start "QMS高级功能" cmd /k "cd /d backend\nodejs && echo 🚀 启动高级功能服务... && node advanced-features-service.js"
timeout /t 2 >nul

:: ==========================================
:: 第六步：启动前端应用
:: ==========================================
echo.
echo [6/7] 🎨 启动前端应用...
echo.

echo 🚀 启动配置端 (端口8072)...
start "QMS配置端" cmd /k "cd /d frontend\配置端 && echo 🎛️ 启动配置端... && npx vue-cli-service serve --mode uat --port 8072"
timeout /t 4 >nul

echo 🚀 启动应用端 (端口8080)...
start "QMS应用端" cmd /k "cd /d frontend\应用端 && echo 🚀 启动应用端... && npx vite --port 8080"
timeout /t 4 >nul

:: ==========================================
:: 第七步：验证服务状态
:: ==========================================
echo.
echo [7/7] 🔍 验证服务状态...
echo.

echo 🕐 等待所有服务完全启动...
timeout /t 15 >nul

echo 📊 检查服务状态:

:: 检查配置中心
curl -s http://localhost:8082/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 配置中心 (8082) - 运行正常
) else (
    echo ❌ 配置中心 (8082) - 启动失败
)

:: 检查聊天服务
curl -s http://localhost:3004/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 聊天服务 (3004) - 运行正常
) else (
    echo ❌ 聊天服务 (3004) - 启动失败
)

:: 检查端口占用情况
echo.
echo 📊 端口占用情况:
netstat -ano | findstr :8082 >nul 2>&1 && echo ✅ 8082 (配置中心) - 已监听 || echo ❌ 8082 - 未监听
netstat -ano | findstr :3004 >nul 2>&1 && echo ✅ 3004 (聊天服务) - 已监听 || echo ❌ 3004 - 未监听
netstat -ano | findstr :3009 >nul 2>&1 && echo ✅ 3009 (高级功能) - 已监听 || echo ❌ 3009 - 未监听
netstat -ano | findstr :8072 >nul 2>&1 && echo ✅ 8072 (配置端) - 已监听 || echo ❌ 8072 - 未监听
netstat -ano | findstr :8080 >nul 2>&1 && echo ✅ 8080 (应用端) - 已监听 || echo ❌ 8080 - 未监听

:: ==========================================
:: 完成信息
:: ==========================================
echo.
echo ==========================================
echo 🎉 QMS-AI 全面重启完成！
echo ==========================================
echo.
echo 📋 服务访问地址:
echo   🎛️ 配置端:     http://localhost:8072/alm-transcend-configcenter-web/
echo   🚀 应用端:     http://localhost:8080/
echo   ⚙️ 配置中心:   http://localhost:8082/health
echo   🤖 AI聊天:     http://localhost:3004/health
echo   🔧 高级功能:   http://localhost:3009/health
echo.
echo 🔧 管理功能:
echo   📋 配置管理:   http://localhost:8082/api/configs
echo   🤖 AI模型:     http://localhost:8082/api/ai/models
echo   📡 配置通知:   http://localhost:8082/api/events/config-changes
echo.
echo 🧪 测试命令:
echo   node test-config-center.js
echo.
echo ⚠️ 注意事项:
echo   • 如果某个服务启动失败，请检查对应的命令行窗口
echo   • 配置变更会自动同步，无需重启服务
echo   • 使用 QMS-Stop-All.bat 可以停止所有服务
echo.
echo 按任意键关闭此窗口...
pause >nul
