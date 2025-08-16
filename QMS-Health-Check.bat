@echo off
title QMS-AI系统健康检查
color 0B

echo.
echo ==========================================
echo 🏥 QMS-AI系统健康检查
echo ==========================================
echo.

REM 检查Node.js环境
echo 🔍 检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Node.js: 已安装
    node --version
) else (
    echo ❌ Node.js: 未安装
)

REM 检查npm环境
echo.
echo 🔍 检查npm环境...
npm --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ npm: 已安装
    npm --version
) else (
    echo ❌ npm: 未安装
)

REM 检查端口占用情况
echo.
echo 🔍 检查端口占用情况...

netstat -ano | findstr :3004 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 端口3004: 已占用 (聊天服务运行中)
) else (
    echo ⚠️ 端口3004: 空闲 (聊天服务未运行)
)

netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 端口8080: 已占用 (应用端运行中)
) else (
    echo ⚠️ 端口8080: 空闲 (应用端未运行)
)

netstat -ano | findstr :8072 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 端口8072: 已占用 (配置端运行中)
) else (
    echo ⚠️ 端口8072: 空闲 (配置端未运行)
)

REM 检查进程状态
echo.
echo 🔍 检查进程状态...

tasklist /fi "imagename eq node.exe" 2>nul | findstr node.exe >nul
if %errorlevel% equ 0 (
    echo ✅ Node.js进程: 运行中
    tasklist /fi "imagename eq node.exe" | findstr node.exe
) else (
    echo ⚠️ Node.js进程: 未运行
)

REM 检查目录结构
echo.
echo 🔍 检查目录结构...

if exist "backend\nodejs" (
    echo ✅ 后端目录: 存在
) else (
    echo ❌ 后端目录: 不存在
)

if exist "frontend\应用端" (
    echo ✅ 应用端目录: 存在
) else (
    echo ❌ 应用端目录: 不存在
)

if exist "frontend\配置端" (
    echo ✅ 配置端目录: 存在
) else (
    echo ❌ 配置端目录: 不存在
)

REM 检查依赖安装
echo.
echo 🔍 检查依赖安装...

if exist "backend\nodejs\node_modules" (
    echo ✅ 后端依赖: 已安装
) else (
    echo ❌ 后端依赖: 未安装
)

if exist "frontend\应用端\node_modules" (
    echo ✅ 应用端依赖: 已安装
) else (
    echo ⚠️ 应用端依赖: 未安装
)

if exist "frontend\配置端\node_modules" (
    echo ✅ 配置端依赖: 已安装
) else (
    echo ⚠️ 配置端依赖: 未安装
)

REM 检查核心文件
echo.
echo 🔍 检查核心文件...

if exist "backend\nodejs\chat-service.js" (
    echo ✅ 聊天服务: 存在
) else (
    echo ❌ 聊天服务: 不存在
)

if exist "QMS-Quick-Start.bat" (
    echo ✅ 启动脚本: 存在
) else (
    echo ❌ 启动脚本: 不存在
)

if exist "QMS-Stop-All.bat" (
    echo ✅ 停止脚本: 存在
) else (
    echo ❌ 停止脚本: 不存在
)

REM 尝试HTTP健康检查
echo.
echo 🔍 HTTP服务健康检查...

curl -s http://localhost:3004/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 聊天服务API: 响应正常
) else (
    echo ⚠️ 聊天服务API: 无响应
)

curl -s http://localhost:8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 应用端: 响应正常
) else (
    echo ⚠️ 应用端: 无响应
)

curl -s http://localhost:8072 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 配置端: 响应正常
) else (
    echo ⚠️ 配置端: 无响应
)

REM 系统资源检查
echo.
echo 🔍 系统资源检查...

REM 检查磁盘空间
for /f "tokens=3" %%a in ('dir /-c ^| findstr "bytes free"') do (
    echo ✅ 磁盘空间: %%a bytes 可用
)

REM 检查内存使用
for /f "skip=1 tokens=4" %%a in ('wmic OS get TotalVisibleMemorySize /value') do (
    if not "%%a"=="" (
        echo ✅ 系统内存: 检查完成
    )
)

echo.
echo ==========================================
echo 🎊 健康检查完成
echo ==========================================
echo.

REM 生成建议
echo 💡 系统建议:
echo.

REM 检查是否所有服务都在运行
set all_running=1

netstat -ano | findstr :3004 >nul 2>&1
if %errorlevel% neq 0 set all_running=0

netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% neq 0 set all_running=0

netstat -ano | findstr :8072 >nul 2>&1
if %errorlevel% neq 0 set all_running=0

if %all_running% equ 1 (
    echo ✅ 所有服务运行正常，系统状态良好！
    echo 📍 访问地址:
    echo   🌐 应用端: http://localhost:8080
    echo   ⚙️ 配置端: http://localhost:8072
    echo   🤖 聊天服务: http://localhost:3004
) else (
    echo ⚠️ 部分服务未运行，建议执行以下操作:
    echo   1. 运行 QMS-Quick-Start.bat 启动所有服务
    echo   2. 检查端口是否被其他程序占用
    echo   3. 确保所有依赖已正确安装
)

echo.
pause
