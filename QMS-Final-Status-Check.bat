@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo    QMS-AI 系统状态检查报告
echo ========================================
echo.

echo 🔍 检查端口监听状态...
echo.

set "ports=3003 3004 3005 3008 3009 8072 8081 8084 8085"
set "services=配置中心 聊天服务 Coze-Studio 导出服务 高级功能 配置端 应用端 认证服务 API网关"

set /a index=0
for %%p in (%ports%) do (
    set /a index+=1
    for /f "tokens=!index!" %%s in ("%services%") do (
        netstat -ano | findstr ":%%p " | findstr LISTENING >nul
        if !errorlevel! equ 0 (
            echo   ✅ Port %%p ^(%%s^): LISTENING
        ) else (
            echo   ❌ Port %%p ^(%%s^): NOT LISTENING
        )
    )
)

echo.
echo 🌐 服务访问地址:
echo   • 配置中心:     http://localhost:3003
echo   • 聊天服务:     http://localhost:3004  
echo   • Coze Studio:  http://localhost:3005
echo   • 导出服务:     http://localhost:3008
echo   • 高级功能:     http://localhost:3009
echo   • 认证服务:     http://localhost:8084
echo   • API网关:      http://localhost:8085
echo   • 应用端:       http://localhost:8081
echo   • 配置端:       http://localhost:8072

echo.
echo 🔧 健康检查:
echo.

for %%u in (3003 3004 3005 3008 3009 8084 8085) do (
    echo 检查 http://localhost:%%u/health...
    curl -s -m 5 http://localhost:%%u/health >nul 2>&1
    if !errorlevel! equ 0 (
        echo   ✅ 健康检查通过
    ) else (
        echo   ⚠️  健康检查失败或超时
    )
)

echo.
echo 📊 Redis 状态:
netstat -ano | findstr ":6379 " | findstr LISTENING >nul
if !errorlevel! equ 0 (
    echo   ✅ Redis (6379): LISTENING
) else (
    echo   ❌ Redis (6379): NOT LISTENING
)

netstat -ano | findstr ":6380 " | findstr LISTENING >nul
if !errorlevel! equ 0 (
    echo   ✅ Redis (6380): LISTENING  
) else (
    echo   ❌ Redis (6380): NOT LISTENING
)

echo.
echo ========================================
echo    检查完成！
echo ========================================
echo.

pause
