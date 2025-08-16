@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

title QMS-AI服务停止
color 0C

echo.
echo ==========================================
echo 🛑 QMS-AI系统服务停止
echo ==========================================
echo.

echo 🔍 查找运行中的服务...

REM 显示当前运行的Node.js进程
echo.
echo 📊 当前Node.js进程:
tasklist /fi "imagename eq node.exe" 2>nul | findstr node.exe
if %errorlevel% neq 0 (
    echo   没有运行中的Node.js进程
)

echo.
echo 📊 当前npm进程:
tasklist /fi "imagename eq npm.cmd" 2>nul | findstr npm
if %errorlevel% neq 0 (
    echo   没有运行中的npm进程
)

echo.
echo 🛑 停止所有相关进程...

REM 停止Node.js进程
echo 停止Node.js进程...
taskkill /f /im node.exe 2>nul
if %errorlevel% equ 0 (
    echo ✅ Node.js进程已停止
) else (
    echo ⚠️ 没有需要停止的Node.js进程
)

REM 停止npm进程
echo 停止npm进程...
taskkill /f /im npm.cmd 2>nul
taskkill /f /im npm 2>nul
if %errorlevel% equ 0 (
    echo ✅ npm进程已停止
) else (
    echo ⚠️ 没有需要停止的npm进程
)

REM 检查端口释放
echo.
echo 🔍 检查端口释放情况...

netstat -ano | findstr :3004 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️ 端口3004仍被占用，尝试强制释放...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3004') do (
        taskkill /f /pid %%a 2>nul
    )
) else (
    echo ✅ 端口3004已释放
)

netstat -ano | findstr :8081 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️ 端口8081仍被占用，尝试强制释放...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do (
        taskkill /f /pid %%a 2>nul
    )
) else (
    echo ✅ 端口8081已释放
)

netstat -ano | findstr :8082 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️ 端口8082仍被占用，尝试强制释放...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8082') do (
        taskkill /f /pid %%a 2>nul
    )
) else (
    echo ✅ 端口8082已释放
)

netstat -ano | findstr :8084 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️ 端口8084仍被占用，尝试强制释放...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8084') do (
        taskkill /f /pid %%a 2>nul
    )
) else (
    echo ✅ 端口8084已释放
)

netstat -ano | findstr :6380 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️ 端口6380仍被占用，尝试强制释放...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :6380') do (
        taskkill /f /pid %%a 2>nul
    )
) else (
    echo ✅ 端口6380已释放
)


netstat -ano | findstr :8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️ 端口8080仍被占用，尝试强制释放...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
        taskkill /f /pid %%a 2>nul
    )
) else (
    echo ✅ 端口8080已释放
)

netstat -ano | findstr :8072 >nul 2>&1
if %errorlevel% equ 0 (
    echo ⚠️ 端口8072仍被占用，尝试强制释放...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8072') do (
        taskkill /f /pid %%a 2>nul
    )
) else (
    echo ✅ 端口8072已释放
)

echo.
echo 🎉 QMS-AI系统停止完成！
echo.
echo 💡 如果某些端口仍被占用，请:
echo   1. 手动关闭对应的命令行窗口
echo   2. 重启计算机（极端情况）
echo.
pause
