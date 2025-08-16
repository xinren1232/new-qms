@echo off
chcp 65001 >nul
echo 🚀 QMS-AI 快速检查和启动脚本
echo ========================================
echo.

echo 🔍 第一步：检查运行环境
echo ----------------------------------------

REM 检查Node.js
echo 检查Node.js...
node --version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('node --version') do echo ✅ Node.js: %%i
) else (
    echo ❌ Node.js未安装，请先安装Node.js
    pause
    exit /b 1
)

REM 检查npm
echo 检查npm...
npm --version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('npm --version') do echo ✅ npm: %%i
) else (
    echo ❌ npm未安装
    pause
    exit /b 1
)

echo.
echo 🔍 第二步：检查项目依赖
echo ----------------------------------------

REM 检查后端依赖
echo 检查后端依赖...
if exist "backend\nodejs\node_modules" (
    echo ✅ 后端依赖已安装
) else (
    echo ❌ 后端依赖未安装，正在安装...
    cd backend\nodejs
    npm install
    if %errorlevel% neq 0 (
        echo ❌ 后端依赖安装失败
        cd ..\..
        pause
        exit /b 1
    )
    echo ✅ 后端依赖安装完成
    cd ..\..
)

REM 检查应用端依赖
echo 检查应用端依赖...
if exist "frontend\应用端\node_modules" (
    echo ✅ 应用端依赖已安装
) else (
    echo ❌ 应用端依赖未安装，正在安装...
    cd frontend\应用端
    npm install
    if %errorlevel% neq 0 (
        echo ❌ 应用端依赖安装失败
        cd ..\..
        pause
        exit /b 1
    )
    echo ✅ 应用端依赖安装完成
    cd ..\..
)

REM 检查配置端依赖
echo 检查配置端依赖...
if exist "frontend\配置端\node_modules" (
    echo ✅ 配置端依赖已安装
) else (
    echo ❌ 配置端依赖未安装，正在安装...
    cd frontend\配置端
    npm install
    if %errorlevel% neq 0 (
        echo ❌ 配置端依赖安装失败
        cd ..\..
        pause
        exit /b 1
    )
    echo ✅ 配置端依赖安装完成
    cd ..\..
)

echo.
echo 🔍 第三步：检查端口占用
echo ----------------------------------------

set ports=3003 3004 3005 3008 3009 8072 8081 8084 8085

for %%p in (%ports%) do (
    netstat -ano | findstr ":%%p " >nul 2>&1
    if !errorlevel! equ 0 (
        echo ⚠️ 端口%%p已被占用
    ) else (
        echo ✅ 端口%%p空闲
    )
)

echo.
echo 🚀 第四步：逐一启动服务
echo ----------------------------------------

echo.
echo 启动配置中心服务 (端口3003)...
start "QMS AI - 配置中心" cmd /k "cd /d %~dp0backend\nodejs && node config-center-service.js"
timeout /t 3 /nobreak >nul

echo 启动聊天服务 (端口3004)...
start "QMS AI - 聊天服务" cmd /k "cd /d %~dp0backend\nodejs && node chat-service.js"
timeout /t 3 /nobreak >nul

echo 启动认证服务 (端口8084)...
start "QMS AI - 认证服务" cmd /k "cd /d %~dp0backend\nodejs && node auth-service.js"
timeout /t 3 /nobreak >nul

echo 启动高级功能服务 (端口3009)...
start "QMS AI - 高级功能" cmd /k "cd /d %~dp0backend\nodejs && node advanced-features-service.js"
timeout /t 3 /nobreak >nul

echo 启动导出服务 (端口3008)...
start "QMS AI - 导出服务" cmd /k "cd /d %~dp0backend\nodejs && node export-service-standalone.js"
timeout /t 3 /nobreak >nul

echo 启动Coze Studio服务 (端口3005)...
start "QMS AI - Coze Studio" cmd /k "cd /d %~dp0backend\nodejs && node coze-studio-service.js"
timeout /t 3 /nobreak >nul

echo 启动API网关 (端口8085)...
start "QMS AI - API网关" cmd /k "cd /d %~dp0backend\nodejs && node api-gateway.js"
timeout /t 3 /nobreak >nul

echo 启动应用端 (端口8081)...
start "QMS AI - 应用端" cmd /k "cd /d %~dp0frontend\应用端 && npm run dev"
timeout /t 5 /nobreak >nul

echo 启动配置端 (端口8072)...
start "QMS AI - 配置端" cmd /k "cd /d %~dp0frontend\配置端 && npm run serve"
timeout /t 5 /nobreak >nul

echo.
echo ⏳ 等待所有服务启动完成...
timeout /t 10 /nobreak >nul

echo.
echo 🔍 第五步：验证服务状态
echo ----------------------------------------

for %%p in (%ports%) do (
    netstat -ano | findstr ":%%p " >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 端口%%p: 服务运行中
    ) else (
        echo ❌ 端口%%p: 服务未启动
    )
)

echo.
echo 🌐 服务访问地址:
echo ----------------------------------------
echo • 配置中心:     http://localhost:3003
echo • 聊天服务:     http://localhost:3004
echo • 认证服务:     http://localhost:8084
echo • 高级功能:     http://localhost:3009
echo • 导出服务:     http://localhost:3008
echo • Coze Studio:  http://localhost:3005
echo • API网关:      http://localhost:8085
echo • 应用端:       http://localhost:8081
echo • 配置端:       http://localhost:8072
echo ----------------------------------------

echo.
echo 🎉 QMS-AI系统启动完成！
echo.
pause
