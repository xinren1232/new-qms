@echo off
echo 🔍 QMS-AI 服务状态检查
echo ========================================
echo.

echo 📊 当前运行的服务端口:
echo ----------------------------------------
netstat -ano | findstr ":3003 :3004 :3005 :3009 :8072 :8081 :8084 :8085" | findstr LISTENING

echo.
echo 🔍 Node.js 进程状态:
echo ----------------------------------------
tasklist | findstr node.exe

echo.
echo 🌐 服务访问地址:
echo ----------------------------------------
echo • 配置中心:     http://localhost:3003
echo • 聊天服务:     http://localhost:3004
echo • Coze Studio:  http://localhost:3005
echo • 高级功能:     http://localhost:3009
echo • 认证服务:     http://localhost:8084
echo • API网关:      http://localhost:8085
echo • 应用端:       http://localhost:8081
echo • 配置端:       http://localhost:8072
echo ----------------------------------------

echo.
echo 📋 服务状态总结:
echo ----------------------------------------

REM 检查各个端口
for %%p in (3003 3004 3005 3009 8072 8081 8084 8085) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 端口%%p: 运行中
    ) else (
        echo ❌ 端口%%p: 未运行
    )
)

echo.
pause
