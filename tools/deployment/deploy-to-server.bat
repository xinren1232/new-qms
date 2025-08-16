@echo off
chcp 65001 >nul
title QMS-AI系统服务器部署

echo.
echo 🚀 QMS-AI系统服务器部署脚本
echo ================================
echo.

:: 检查Docker安装
echo [1/6] 检查Docker环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker未安装，请先安装Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
) else (
    echo ✅ Docker已安装
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose未安装
    pause
    exit /b 1
) else (
    echo ✅ Docker Compose已安装
)

:: 检查端口占用
echo.
echo [2/6] 检查端口占用...
set PORTS=80 443 3003 3004 6379 8081 8072 8084 8085
for %%p in (%PORTS%) do (
    netstat -ano | findstr ":%%p " >nul 2>&1
    if errorlevel 1 (
        echo ✅ 端口 %%p 可用
    ) else (
        echo ⚠️ 端口 %%p 已被占用
    )
)

:: 创建必要目录
echo.
echo [3/6] 准备部署环境...
if not exist logs mkdir logs
if not exist data mkdir data
if not exist data\redis mkdir data\redis
if not exist ssl mkdir ssl
if not exist backup mkdir backup
echo ✅ 目录结构创建完成

:: 停止现有服务
echo.
echo [4/6] 停止现有服务...
docker-compose down 2>nul
echo ✅ 现有服务已停止

:: 清理旧容器
echo 清理旧容器和镜像...
docker container prune -f >nul 2>&1
docker image prune -f >nul 2>&1
echo ✅ 清理完成

:: 构建和启动服务
echo.
echo [5/6] 构建和启动服务...
echo 正在构建Docker镜像...
docker-compose build --no-cache
if errorlevel 1 (
    echo ❌ 镜像构建失败
    pause
    exit /b 1
)

echo 正在启动服务...
docker-compose up -d
if errorlevel 1 (
    echo ❌ 服务启动失败
    pause
    exit /b 1
)

echo ✅ 服务启动完成

:: 等待服务就绪
echo.
echo 等待服务就绪...
timeout /t 30 /nobreak >nul

:: 验证部署
echo.
echo [6/6] 验证部署状态...
echo.
echo 📊 容器状态:
docker-compose ps

echo.
echo 🔍 服务健康检查:
curl -s -f http://localhost:3003/health >nul 2>&1 && echo ✅ 配置中心: 健康 || echo ❌ 配置中心: 异常
curl -s -f http://localhost:3004/health >nul 2>&1 && echo ✅ 聊天服务: 健康 || echo ❌ 聊天服务: 异常
curl -s -f http://localhost:8084/health >nul 2>&1 && echo ✅ 认证服务: 健康 || echo ❌ 认证服务: 异常
curl -s -f http://localhost:8085/health >nul 2>&1 && echo ✅ API网关: 健康 || echo ❌ API网关: 异常

:: 显示部署结果
echo.
echo 🎉 QMS-AI系统部署完成！
echo ========================
echo.
echo 📱 前端应用:
echo   主应用: http://localhost:8081
echo   配置端: http://localhost:8072
echo.
echo 🔧 后端服务:
echo   配置中心: http://localhost:3003
echo   聊天服务: http://localhost:3004
echo   认证服务: http://localhost:8084
echo   API网关: http://localhost:8085
echo.
echo 💾 数据服务:
echo   Redis缓存: localhost:6379
echo.
echo 📋 管理命令:
echo   查看日志: docker-compose logs -f [service_name]
echo   重启服务: docker-compose restart [service_name]
echo   停止服务: docker-compose down
echo   更新服务: docker-compose pull ^&^& docker-compose up -d
echo.
echo 📁 重要目录:
echo   日志目录: .\logs
echo   数据目录: .\data
echo   备份目录: .\backup
echo.

echo ✅ 部署完成！按任意键退出...
pause >nul
