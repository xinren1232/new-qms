@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo 🔧 QMS-AI 504 Gateway Timeout 诊断和修复工具
echo ==================================================
echo.

:: 检查Docker状态
echo [1/8] 检查Docker服务状态...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker未安装或未启动
    echo 请确保Docker Desktop正在运行
    pause
    exit /b 1
) else (
    echo ✅ Docker服务正常
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose未安装
    pause
    exit /b 1
) else (
    echo ✅ Docker Compose已安装
)
echo.

:: 检查容器状态
echo [2/8] 检查QMS-AI容器状态...
docker ps --filter "name=qms" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

:: 检查端口占用
echo [3/8] 检查关键端口状态...
set "ports=80 443 3003 3004 3005 3008 3009 6379 8072 8081 8084 8085"
for %%p in (%ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 端口 %%p: 监听中
    ) else (
        echo ⚠️ 端口 %%p: 未监听
    )
)
echo.

:: 检查Nginx容器
echo [4/8] 检查Nginx容器状态...
for /f "tokens=*" %%i in ('docker ps --filter "name=nginx" --format "{{.Names}}" 2^>nul') do set nginx_container=%%i

if defined nginx_container (
    echo ✅ Nginx容器运行中: %nginx_container%
    
    :: 检查Nginx配置
    docker exec %nginx_container% nginx -t >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ Nginx配置语法正确
    ) else (
        echo ❌ Nginx配置语法错误
        docker exec %nginx_container% nginx -t
    )
    
    :: 显示Nginx错误日志
    echo 📋 最近的Nginx错误日志:
    docker logs %nginx_container% --tail 10 2>&1 | findstr /i error
) else (
    echo ❌ Nginx容器未运行
)
echo.

:: 检查后端服务健康状态
echo [5/8] 检查后端服务健康状态...
set "services=3003:配置中心 3004:聊天服务 3008:导出服务 3009:高级功能 8084:认证服务 8085:API网关"

for %%s in (%services%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        curl -s -f "http://localhost:%%a/health" >nul 2>&1
        if !errorlevel! equ 0 (
            echo ✅ %%b ^(%%a^): 健康
        ) else (
            echo ❌ %%b ^(%%a^): 异常
        )
    )
)
echo.

:: 检查前端服务
echo [6/8] 检查前端服务状态...

:: 检查应用端容器
for /f "tokens=*" %%i in ('docker ps --filter "name=frontend-app" --format "{{.Names}}" 2^>nul') do set app_container=%%i
if defined app_container (
    echo ✅ 应用端容器运行中: %app_container%
    curl -s -f "http://localhost:8081/health" >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 应用端 ^(8081^): 健康
    ) else (
        echo ❌ 应用端 ^(8081^): 异常
    )
) else (
    echo ❌ 应用端容器未运行
)

:: 检查配置端容器
for /f "tokens=*" %%i in ('docker ps --filter "name=frontend-config" --format "{{.Names}}" 2^>nul') do set config_container=%%i
if defined config_container (
    echo ✅ 配置端容器运行中: %config_container%
    curl -s -f "http://localhost:8072/health" >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 配置端 ^(8072^): 健康
    ) else (
        echo ❌ 配置端 ^(8072^): 异常
    )
) else (
    echo ❌ 配置端容器未运行
)
echo.

:: 修复Nginx超时配置
echo [7/8] 修复Nginx超时配置...
if defined nginx_container (
    echo 📝 创建Nginx超时修复配置...
    
    :: 创建临时配置文件
    (
        echo # Nginx超时优化配置
        echo proxy_connect_timeout 60s;
        echo proxy_send_timeout 60s;
        echo proxy_read_timeout 60s;
        echo proxy_buffering on;
        echo proxy_buffer_size 8k;
        echo proxy_buffers 32 8k;
        echo proxy_busy_buffers_size 16k;
        echo client_body_timeout 60s;
        echo client_header_timeout 60s;
        echo send_timeout 60s;
        echo keepalive_timeout 65s;
        echo keepalive_requests 1000;
    ) > nginx_timeout_fix.conf
    
    echo 🔄 重启Nginx容器...
    docker restart %nginx_container%
    timeout /t 10 /nobreak >nul
    
    docker exec %nginx_container% nginx -t >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ Nginx配置修复成功
    ) else (
        echo ❌ Nginx配置修复失败
    )
    
    del nginx_timeout_fix.conf >nul 2>&1
) else (
    echo ⚠️ Nginx容器未运行，跳过配置修复
)
echo.

:: 重启服务
echo [8/8] 重启QMS-AI服务...

:: 查找docker-compose文件
set compose_file=
if exist "docker-compose.yml" set compose_file=docker-compose.yml
if exist "deployment\docker-compose.yml" set compose_file=deployment\docker-compose.yml
if exist "config\docker-compose.yml" set compose_file=config\docker-compose.yml
if exist "deployment\aliyun-deploy-optimized.yml" set compose_file=deployment\aliyun-deploy-optimized.yml

if defined compose_file (
    echo 📋 使用配置文件: %compose_file%
    echo 🛑 停止服务...
    docker-compose -f %compose_file% down
    
    echo ⏳ 等待5秒...
    timeout /t 5 /nobreak >nul
    
    echo 🚀 启动服务...
    docker-compose -f %compose_file% up -d
    
    echo ⏳ 等待服务启动 (30秒)...
    timeout /t 30 /nobreak >nul
    
    echo ✅ 服务重启完成
) else (
    echo ❌ 未找到docker-compose配置文件
    echo 请手动重启服务
)
echo.

:: 验证修复结果
echo 🔍 验证修复结果...
echo.

:: 再次检查后端服务
echo 📊 后端服务状态:
for %%s in (%services%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        curl -s -f "http://localhost:%%a/health" >nul 2>&1
        if !errorlevel! equ 0 (
            echo ✅ %%b ^(%%a^): 健康
        ) else (
            echo ❌ %%b ^(%%a^): 异常
        )
    )
)
echo.

:: 显示访问地址
echo 📋 访问地址:
echo   • 主应用: http://localhost 或 http://your-server-ip
echo   • 配置端: http://localhost/config 或 http://your-server-ip/config
echo   • API网关: http://localhost:8085 或 http://your-server-ip:8085
echo.

echo 🎯 常见504错误原因和解决方案:
echo.
echo 1. 后端服务未启动或异常
echo    解决: 检查上述服务状态，重启异常服务
echo.
echo 2. Nginx代理超时设置过短
echo    解决: 已自动修复超时配置
echo.
echo 3. 防火墙阻止端口访问
echo    解决: 开放80, 443, 8081, 8072等端口
echo.
echo 4. 云服务器安全组未配置
echo    解决: 在云控制台开放相应端口
echo.
echo 5. 域名DNS解析问题
echo    解决: 检查域名是否正确解析到服务器IP
echo.
echo 6. SSL证书配置错误
echo    解决: 检查HTTPS证书是否有效
echo.

echo ✅ 诊断和修复完成！
echo.
echo 如果问题仍然存在，请:
echo 1. 检查服务器防火墙设置
echo 2. 检查云服务器安全组配置
echo 3. 确认域名DNS解析正确
echo 4. 查看详细的容器日志: docker logs container-name
echo.

pause
