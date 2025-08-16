@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo    QMS-AI 代理问题诊断工具
echo ========================================
echo.

echo 🔍 第一步：检查网络连接
echo ----------------------------------------

echo 检查本地回环连接...
ping -n 1 127.0.0.1 >nul
if %errorlevel% equ 0 (
    echo ✅ 本地回环连接正常
) else (
    echo ❌ 本地回环连接异常
)

ping -n 1 localhost >nul
if %errorlevel% equ 0 (
    echo ✅ localhost解析正常
) else (
    echo ❌ localhost解析异常
)

echo.
echo 🔍 第二步：检查端口监听状态
echo ----------------------------------------

echo 正确的服务端口:
set "correct_ports=3003 3004 3005 8081 8084 8085"
for %%p in (%correct_ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul
    if !errorlevel! equ 0 (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p " ^| findstr LISTENING') do (
            echo ✅ 端口%%p: 正在监听 (PID: %%a)
        )
    ) else (
        echo ❌ 端口%%p: 未监听
    )
)

echo.
echo 错误的端口（应该为空）:
set "error_ports=1117 1684"
for %%p in (%error_ports%) do (
    netstat -ano | findstr ":%%p " >nul
    if !errorlevel! equ 0 (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p "') do (
            echo ⚠️ 端口%%p: 异常监听 (PID: %%a)
        )
    ) else (
        echo ✅ 端口%%p: 正常（未监听）
    )
)

echo.
echo 🔍 第三步：检查进程状态
echo ----------------------------------------

echo Node.js进程:
tasklist /fi "imagename eq node.exe" 2>nul | findstr node.exe
if %errorlevel% neq 0 (
    echo ❌ 没有Node.js进程运行
)

echo.
echo 🔍 第四步：检查配置文件
echo ----------------------------------------

echo 检查Vite配置...
if exist "frontend\应用端\vite.config.js" (
    echo ✅ Vite配置文件存在
    findstr "1117\|1684" "frontend\应用端\vite.config.js" >nul
    if !errorlevel! equ 0 (
        echo ⚠️ Vite配置中发现错误端口
        echo 错误端口配置:
        findstr "1117\|1684" "frontend\应用端\vite.config.js"
    ) else (
        echo ✅ Vite配置中未发现错误端口
    )
) else (
    echo ❌ Vite配置文件不存在
)

echo.
echo 检查环境变量文件...
if exist "frontend\应用端\.env" (
    echo ✅ .env文件存在
    findstr "1117\|1684" "frontend\应用端\.env" >nul
    if !errorlevel! equ 0 (
        echo ⚠️ .env文件中发现错误端口
        echo 错误端口配置:
        findstr "1117\|1684" "frontend\应用端\.env"
    ) else (
        echo ✅ .env文件中未发现错误端口
    )
) else (
    echo ℹ️ .env文件不存在（正常）
)

echo.
echo 🔍 第五步：检查缓存目录
echo ----------------------------------------

if exist "frontend\应用端\node_modules\.vite" (
    echo ⚠️ Vite缓存目录存在，可能包含旧配置
    dir "frontend\应用端\node_modules\.vite" /b
) else (
    echo ✅ Vite缓存目录不存在
)

if exist "frontend\应用端\dist" (
    echo ℹ️ 构建目录存在
) else (
    echo ✅ 构建目录不存在
)

echo.
echo 🔍 第六步：检查系统代理设置
echo ----------------------------------------

echo 检查系统代理配置...
reg query "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" /v ProxyEnable 2>nul | findstr "0x1" >nul
if !errorlevel! equ 0 (
    echo ⚠️ 系统代理已启用
    reg query "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings" /v ProxyServer 2>nul
) else (
    echo ✅ 系统代理未启用
)

echo.
echo 🔍 第七步：测试连接
echo ----------------------------------------

echo 测试正确端口连接...
for %%p in (3003 3004 3005 8084 8085) do (
    powershell -Command "try { $client = New-Object System.Net.Sockets.TcpClient; $client.Connect('localhost', %%p); $client.Close(); Write-Host '✅ 端口%%p: 连接成功' } catch { Write-Host '❌ 端口%%p: 连接失败' }"
)

echo.
echo 测试错误端口连接...
for %%p in (1117 1684) do (
    powershell -Command "try { $client = New-Object System.Net.Sockets.TcpClient; $client.Connect('localhost', %%p); $client.Close(); Write-Host '⚠️ 端口%%p: 异常连接成功' } catch { Write-Host '✅ 端口%%p: 连接失败（正常）' }"
)

echo.
echo ========================================
echo    诊断完成！
echo ========================================
echo.
echo 💡 根据诊断结果：
echo   1. 如果发现错误端口配置，请运行 fix-frontend-proxy-issue.bat
echo   2. 如果系统代理有问题，请检查网络设置
echo   3. 如果缓存有问题，请清理 node_modules\.vite 目录
echo   4. 如果进程异常，请重启所有服务
echo.

pause
