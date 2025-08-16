@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo    QMS-AI 前端代理问题修复工具
echo ========================================
echo.

echo 🔍 第一步：检查当前服务状态
echo ----------------------------------------

REM 检查端口占用
echo 检查正确的服务端口...
set "correct_ports=3003 3004 3005 8081 8084 8085"
for %%p in (%correct_ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul
    if !errorlevel! equ 0 (
        echo ✅ 端口%%p: 正常监听
    ) else (
        echo ❌ 端口%%p: 未监听
    )
)

echo.
echo 检查错误的端口（应该为空）...
set "error_ports=1117 1684"
for %%p in (%error_ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul
    if !errorlevel! equ 0 (
        echo ⚠️ 端口%%p: 异常监听（需要清理）
    ) else (
        echo ✅ 端口%%p: 正常（未监听）
    )
)

echo.
echo 🧹 第二步：清理前端缓存
echo ----------------------------------------

echo 停止前端应用...
taskkill /f /im node.exe 2>nul
timeout /t 2 /nobreak >nul

echo 清理应用端缓存...
if exist "frontend\应用端\node_modules\.vite" (
    rmdir /s /q "frontend\应用端\node_modules\.vite"
    echo ✅ 清理Vite缓存完成
) else (
    echo ℹ️ Vite缓存目录不存在
)

if exist "frontend\应用端\dist" (
    rmdir /s /q "frontend\应用端\dist"
    echo ✅ 清理构建目录完成
) else (
    echo ℹ️ 构建目录不存在
)

echo.
echo 🔧 第三步：重新安装依赖
echo ----------------------------------------

echo 重新安装应用端依赖...
cd frontend\应用端
if exist "node_modules" (
    rmdir /s /q node_modules
    echo ✅ 清理旧依赖完成
)

npm install
if %errorlevel% neq 0 (
    echo ❌ 依赖安装失败
    cd ..\..
    pause
    exit /b 1
)
echo ✅ 依赖安装完成

cd ..\..

echo.
echo 🚀 第四步：重启服务
echo ----------------------------------------

echo 启动后端服务...
start "QMS-配置中心" cmd /k "cd /d %~dp0backend\nodejs && node config-center-service.js"
timeout /t 2 /nobreak >nul

start "QMS-聊天服务" cmd /k "cd /d %~dp0backend\nodejs && node chat-service.js"
timeout /t 2 /nobreak >nul

start "QMS-认证服务" cmd /k "cd /d %~dp0backend\nodejs && node auth-service.js"
timeout /t 2 /nobreak >nul

start "QMS-Coze Studio" cmd /k "cd /d %~dp0backend\nodejs && node coze-studio-service.js"
timeout /t 2 /nobreak >nul

start "QMS-API网关" cmd /k "cd /d %~dp0backend\nodejs && node api-gateway.js"
timeout /t 3 /nobreak >nul

echo 等待后端服务启动...
timeout /t 5 /nobreak >nul

echo 启动前端应用...
start "QMS-应用端" cmd /k "cd /d %~dp0frontend\应用端 && npm run dev"

echo.
echo ⏳ 等待所有服务启动完成...
timeout /t 10 /nobreak >nul

echo.
echo 🔍 第五步：验证修复结果
echo ----------------------------------------

echo 检查服务状态...
for %%p in (%correct_ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul
    if !errorlevel! equ 0 (
        echo ✅ 端口%%p: 正常运行
    ) else (
        echo ❌ 端口%%p: 启动失败
    )
)

echo.
echo 检查错误端口...
for %%p in (%error_ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul
    if !errorlevel! equ 0 (
        echo ⚠️ 端口%%p: 仍在监听（需要手动检查）
    ) else (
        echo ✅ 端口%%p: 已清理
    )
)

echo.
echo ========================================
echo    修复完成！
echo ========================================
echo.
echo 📍 访问地址:
echo   - 应用端: http://localhost:8081
echo   - API网关: http://localhost:8085
echo   - 配置中心: http://localhost:3003
echo   - 聊天服务: http://localhost:3004
echo   - 认证服务: http://localhost:8084
echo.
echo 💡 如果问题仍然存在，请：
echo   1. 检查浏览器缓存（Ctrl+Shift+Delete）
echo   2. 检查系统代理设置
echo   3. 重启浏览器
echo.

pause
