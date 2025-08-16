@echo off
setlocal enabledelayedexpansion

REM QMS-AI服务管理器 - 无需Docker版本
title QMS-AI服务管理器

:menu
cls
echo ==========================================
echo 🚀 QMS-AI服务管理器 (无Docker版本)
echo ==========================================
echo.
echo 当前状态:
call :check_services
echo.
echo 请选择操作:
echo 1. 启动所有服务
echo 2. 停止所有服务
echo 3. 重启所有服务
echo 4. 启动聊天服务
echo 5. 启动应用端
echo 6. 启动配置端
echo 7. 查看服务状态
echo 8. 打开服务地址
echo 9. 查看日志目录
echo 0. 退出
echo.
set /p choice=请输入选择 (0-9): 

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto stop_all
if "%choice%"=="3" goto restart_all
if "%choice%"=="4" goto start_chat
if "%choice%"=="5" goto start_frontend
if "%choice%"=="6" goto start_config
if "%choice%"=="7" goto status
if "%choice%"=="8" goto open_urls
if "%choice%"=="9" goto show_logs
if "%choice%"=="0" goto exit

echo 无效选择，请重新输入
pause
goto menu

:start_all
echo 🚀 启动所有QMS-AI服务...
echo.

REM 启动聊天服务
echo 📡 启动聊天服务...
cd /d "%~dp0backend\nodejs"
start "QMS聊天服务" cmd /k "set NODE_ENV=development && set PORT=3004 && set DB_TYPE=sqlite && set CACHE_ENABLED=false && node chat-service.js"

REM 等待2秒
timeout /t 2 /nobreak >nul

REM 启动应用端
echo 🌐 启动应用端...
cd /d "%~dp0frontend\应用端"
start "QMS应用端" cmd /k "npm run dev"

REM 等待2秒
timeout /t 2 /nobreak >nul

REM 启动配置端
echo ⚙️ 启动配置端...
cd /d "%~dp0frontend\配置端"
start "QMS配置端" cmd /k "npm run serve"

echo.
echo ✅ 所有服务启动命令已执行
echo ⏳ 请等待1-2分钟让服务完全启动
echo.
echo 📍 访问地址:
echo   🌐 应用端: http://localhost:8080
echo   ⚙️ 配置端: http://localhost:8072
echo   🤖 聊天服务: http://localhost:3004
echo.
goto wait_and_menu

:stop_all
echo 🛑 停止所有QMS-AI服务...

REM 停止Node.js进程
taskkill /f /im node.exe 2>nul
if %errorlevel% equ 0 (
    echo ✅ Node.js服务已停止
) else (
    echo ⚠️ 没有运行中的Node.js服务
)

REM 停止npm进程
taskkill /f /im npm.cmd 2>nul
taskkill /f /im npm 2>nul

echo ✅ 所有服务停止完成
goto wait_and_menu

:restart_all
echo 🔄 重启所有服务...
call :stop_all
timeout /t 3 /nobreak >nul
call :start_all
goto wait_and_menu

:start_chat
echo 📡 启动聊天服务...
cd /d "%~dp0backend\nodejs"
start "QMS聊天服务" cmd /k "set NODE_ENV=development && set PORT=3004 && set DB_TYPE=sqlite && set CACHE_ENABLED=false && node chat-service.js"
echo ✅ 聊天服务启动完成
echo 📍 访问地址: http://localhost:3004
goto wait_and_menu

:start_frontend
echo 🌐 启动应用端...
cd /d "%~dp0frontend\应用端"
start "QMS应用端" cmd /k "npm run dev"
echo ✅ 应用端启动完成
echo 📍 访问地址: http://localhost:8080
goto wait_and_menu

:start_config
echo ⚙️ 启动配置端...
cd /d "%~dp0frontend\配置端"
start "QMS配置端" cmd /k "npm run serve"
echo ✅ 配置端启动完成
echo 📍 访问地址: http://localhost:8072
goto wait_and_menu

:status
echo 📊 检查服务状态...
call :check_services
goto wait_and_menu

:open_urls
echo 🌐 打开服务地址...
start http://localhost:8080
start http://localhost:8072
start http://localhost:3004/health
echo ✅ 已在浏览器中打开所有服务地址
goto wait_and_menu

:show_logs
echo 📋 日志目录:
echo   后端日志: %~dp0backend\nodejs\logs\
echo   前端构建日志: 查看对应的命令行窗口
if exist "%~dp0backend\nodejs\logs\" (
    start explorer "%~dp0backend\nodejs\logs\"
) else (
    echo ⚠️ 日志目录不存在，请先启动服务
)
goto wait_and_menu

:check_services
REM 检查端口占用情况
netstat -ano | findstr :3004 >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✅ 聊天服务 (端口3004): 运行中
) else (
    echo   ❌ 聊天服务 (端口3004): 未运行
)

netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✅ 应用端 (端口8080): 运行中
) else (
    echo   ❌ 应用端 (端口8080): 未运行
)

netstat -ano | findstr :8072 >nul 2>&1
if %errorlevel% equ 0 (
    echo   ✅ 配置端 (端口8072): 运行中
) else (
    echo   ❌ 配置端 (端口8072): 未运行
)
goto :eof

:wait_and_menu
echo.
pause
goto menu

:exit
echo 🔄 正在停止所有服务...
call :stop_all
echo 👋 再见！
exit /b 0
