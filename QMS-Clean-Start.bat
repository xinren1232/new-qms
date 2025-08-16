@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul
title QMS-AI 清洁启动器

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    QMS-AI 清洁启动器                         ║
echo ║                避免环境冲突的专用启动方案                     ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: 设置工作目录
cd /d "D:\QMS01"

:: 检查Redis状态
echo [1/6] 检查Redis状态...
netstat -ano | findstr ":6379" >nul 2>&1
if errorlevel 1 (
    echo   启动Redis服务...
    start "Redis Server" cmd /k "redis\redis-server.exe redis\redis.windows.conf"
    timeout /t 3 >nul
) else (
    echo   ✅ Redis已运行
)

:: 启动配置中心服务
echo [2/6] 启动配置中心服务 (端口3003)...
start "QMS配置中心" cmd /k "cd /d D:\QMS01\backend\nodejs && set NODE_ENV=development && node lightweight-config-service.js"
timeout /t 8 >nul

:: 启动聊天服务
echo [3/6] 启动聊天服务 (端口3004)...
start "QMS聊天服务" cmd /k "cd /d D:\QMS01\backend\nodejs && set NODE_ENV=development && node chat-service.js"
timeout /t 8 >nul

:: 启动认证服务
echo [4/6] 启动认证服务 (端口8084)...
start "QMS认证服务" cmd /k "cd /d D:\QMS01\backend\nodejs && set NODE_ENV=development && node auth-service.js"
timeout /t 8 >nul

:: 启动API网关
echo [5/6] 启动API网关 (端口8085)...
start "QMS网关" cmd /k "cd /d D:\QMS01\backend\nodejs && set NODE_ENV=development && node api-gateway.js"
timeout /t 5 >nul

:: 检查服务状态
echo [6/6] 检查服务状态...
timeout /t 5 >nul

echo.
echo 🔍 服务状态检查:
netstat -ano | findstr ":3003" >nul 2>&1 && echo   ✅ 配置中心 (3003) || echo   ❌ 配置中心 (3003)
netstat -ano | findstr ":3004" >nul 2>&1 && echo   ✅ 聊天服务 (3004) || echo   ❌ 聊天服务 (3004)
netstat -ano | findstr ":8084" >nul 2>&1 && echo   ✅ 认证服务 (8084) || echo   ❌ 认证服务 (8084)
netstat -ano | findstr ":8085" >nul 2>&1 && echo   ✅ API网关 (8085) || echo   ❌ API网关 (8085)
netstat -ano | findstr ":6379" >nul 2>&1 && echo   ✅ Redis缓存 (6379) || echo   ❌ Redis缓存 (6379)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    后端服务启动完成                          ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  🔧 配置中心: http://localhost:3003                          ║
echo ║  💬 聊天服务: http://localhost:3004                          ║
echo ║  🔐 认证服务: http://localhost:8084                          ║
echo ║  🌐 API网关: http://localhost:8085                           ║
echo ║  💾 Redis缓存: localhost:6379                                ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

set /p frontend=是否启动前端应用？(y/N): 
if /i "!frontend!"=="y" (
    echo.
    echo 启动前端应用...
    start "QMS应用端" cmd /k "cd /d D:\QMS01\frontend\应用端 && npm run dev"
    start "QMS配置端" cmd /k "cd /d D:\QMS01\frontend\配置端 && npm run serve"
    echo   前端应用启动中，请等待...
)

echo.
echo 🎉 QMS-AI系统启动完成！
echo 💡 提示: 各服务在独立的cmd窗口中运行，关闭窗口即可停止对应服务
pause
