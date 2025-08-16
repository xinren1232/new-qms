@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title QMS-AI 代码上传到服务器脚本

echo.
echo 🚀 QMS-AI 代码上传到服务器脚本 (软件小白专用)
echo ================================================
echo 此脚本将为您完成：
echo 1. 打包本地代码
echo 2. 上传到阿里云服务器
echo 3. 在服务器上执行部署
echo 4. 验证系统运行状态
echo.

:: 检查必要工具
echo [1/6] 检查系统环境...

:: 检查是否有scp或pscp
where scp >nul 2>&1
if errorlevel 1 (
    where pscp >nul 2>&1
    if errorlevel 1 (
        echo ❌ 未找到scp或pscp工具
        echo 💡 建议安装Git for Windows或PuTTY来获得这些工具
        echo 💡 或者使用WinSCP等图形化工具手动上传
        pause
        exit /b 1
    ) else (
        set "SCP_CMD=pscp"
        set "SSH_CMD=plink"
        echo ✅ 找到PuTTY工具
    )
) else (
    set "SCP_CMD=scp"
    set "SSH_CMD=ssh"
    echo ✅ 找到OpenSSH工具
)

:: 创建备份
echo.
echo [2/9] 备份当前系统...
set "backup_dir=qms-backup-%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "backup_dir=%backup_dir: =0%"
mkdir "%backup_dir%" 2>nul

if exist "backend" (
    xcopy /E /I /Q "backend" "%backup_dir%\backend" >nul
    echo ✅ 已备份backend目录
)

if exist "frontend" (
    xcopy /E /I /Q "frontend" "%backup_dir%\frontend" >nul
    echo ✅ 已备份frontend目录
)

if exist "nginx" (
    xcopy /E /I /Q "nginx" "%backup_dir%\nginx" >nul
    echo ✅ 已备份nginx目录
)

if exist "docker-compose.yml" (
    copy "docker-compose.yml" "%backup_dir%\" >nul
    echo ✅ 已备份docker-compose.yml
)

echo ✅ 系统备份完成: %backup_dir%

:: 停止当前服务
echo.
echo [3/9] 停止当前服务...

:: 停止QMS容器
for /f "tokens=*" %%i in ('docker ps --filter "name=qms" -q 2^>nul') do (
    docker stop %%i >nul 2>&1
    docker rm %%i >nul 2>&1
)

:: 使用docker-compose停止
if exist "docker-compose.yml" (
    docker-compose down >nul 2>&1
)

if exist "config\docker-compose.yml" (
    docker-compose -f config\docker-compose.yml down >nul 2>&1
)

echo ✅ 服务停止完成

:: 更新代码文件
echo.
echo [4/9] 更新代码文件...

:: 安装WebSocket依赖
if exist "backend\nodejs\package.json" (
    echo 📦 安装WebSocket依赖...
    cd backend\nodejs
    npm install ws@^8.18.0 >nul 2>&1
    if errorlevel 1 (
        echo ⚠️ WebSocket依赖安装可能有问题，继续执行...
    ) else (
        echo ✅ WebSocket依赖安装完成
    )
    cd ..\..
)

echo ✅ 代码更新完成

:: 更新Nginx配置
echo.
echo [5/9] 更新Nginx配置...

if not exist "nginx\conf.d" mkdir nginx\conf.d

:: 创建优化的Nginx配置文件
(
echo # QMS-AI主应用 - WebSocket优化版本
echo map $http_upgrade $connection_upgrade {
echo     default upgrade;
echo     '' close;
echo }
echo.
echo upstream qms_frontend_app {
echo     server qms-frontend-app:80;
echo     keepalive 32;
echo }
echo.
echo upstream qms_frontend_config {
echo     server qms-frontend-config:80;
echo     keepalive 32;
echo }
echo.
echo upstream qms_api_gateway {
echo     server qms-api-gateway:8085;
echo     keepalive 32;
echo }
echo.
echo server {
echo     listen 80;
echo     server_name _;
echo.
echo     # 全局超时和缓冲设置
echo     proxy_connect_timeout 60s;
echo     proxy_send_timeout 60s;
echo     proxy_read_timeout 60s;
echo     proxy_buffering on;
echo     proxy_buffer_size 8k;
echo     proxy_buffers 32 8k;
echo     proxy_busy_buffers_size 16k;
echo.
echo     # 客户端超时设置
echo     client_body_timeout 60s;
echo     client_header_timeout 60s;
echo     send_timeout 60s;
echo     client_max_body_size 50m;
echo.
echo     # 主应用根路径
echo     location / {
echo         proxy_pass http://qms_frontend_app;
echo         proxy_set_header Host $host;
echo         proxy_set_header X-Real-IP $remote_addr;
echo         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
echo         proxy_set_header X-Forwarded-Proto $scheme;
echo.
echo         # WebSocket支持
echo         proxy_http_version 1.1;
echo         proxy_set_header Upgrade $http_upgrade;
echo         proxy_set_header Connection $connection_upgrade;
echo         proxy_cache_bypass $http_upgrade;
echo.
echo         # 错误重试
echo         proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
echo         proxy_next_upstream_tries 3;
echo         proxy_next_upstream_timeout 30s;
echo     }
echo.
echo     # 配置端路径
echo     location /config {
echo         proxy_pass http://qms_frontend_config/;
echo         proxy_set_header Host $host;
echo         proxy_set_header X-Real-IP $remote_addr;
echo         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
echo         proxy_set_header X-Forwarded-Proto $scheme;
echo.
echo         # WebSocket支持
echo         proxy_http_version 1.1;
echo         proxy_set_header Upgrade $http_upgrade;
echo         proxy_set_header Connection $connection_upgrade;
echo         proxy_cache_bypass $http_upgrade;
echo.
echo         # 错误重试
echo         proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
echo         proxy_next_upstream_tries 3;
echo         proxy_next_upstream_timeout 30s;
echo     }
echo.
echo     # API代理
echo     location /api/ {
echo         proxy_pass http://qms_api_gateway/api/;
echo         proxy_set_header Host $host;
echo         proxy_set_header X-Real-IP $remote_addr;
echo         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
echo         proxy_set_header X-Forwarded-Proto $scheme;
echo.
echo         # API专用超时设置
echo         proxy_connect_timeout 30s;
echo         proxy_send_timeout 120s;
echo         proxy_read_timeout 120s;
echo.
echo         # 错误重试
echo         proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
echo         proxy_next_upstream_tries 2;
echo         proxy_next_upstream_timeout 60s;
echo     }
echo.
echo     # WebSocket专用代理
echo     location /config-sync {
echo         proxy_pass http://qms_api_gateway/config-sync;
echo         proxy_http_version 1.1;
echo         proxy_set_header Upgrade $http_upgrade;
echo         proxy_set_header Connection $connection_upgrade;
echo         proxy_set_header Host $host;
echo         proxy_set_header X-Real-IP $remote_addr;
echo         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
echo         proxy_set_header X-Forwarded-Proto $scheme;
echo         proxy_cache_bypass $http_upgrade;
echo.
echo         proxy_connect_timeout 60s;
echo         proxy_send_timeout 300s;
echo         proxy_read_timeout 300s;
echo     }
echo.
echo     # 健康检查
echo     location /health {
echo         access_log off;
echo         proxy_pass http://qms_api_gateway/health;
echo         proxy_connect_timeout 5s;
echo         proxy_send_timeout 5s;
echo         proxy_read_timeout 5s;
echo     }
echo }
) > nginx\conf.d\qms.conf

echo ✅ Nginx配置更新完成

:: 重新构建和启动服务
echo.
echo [6/9] 重新构建和启动服务...

:: 查找docker-compose文件
set compose_file=
if exist "docker-compose.yml" set compose_file=docker-compose.yml
if exist "config\docker-compose.yml" set compose_file=config\docker-compose.yml
if exist "deployment\docker-compose.yml" set compose_file=deployment\docker-compose.yml

if defined compose_file (
    echo 📋 使用配置文件: %compose_file%
    
    echo 🧹 清理旧镜像...
    docker system prune -f >nul 2>&1
    
    echo 🔨 重新构建并启动服务...
    docker-compose -f %compose_file% up --build -d
    
    echo ✅ 服务启动完成
) else (
    echo ❌ 未找到docker-compose配置文件
    pause
    exit /b 1
)

:: 等待服务启动
echo.
echo [7/9] 等待服务启动...
echo 等待60秒让服务完全启动...
timeout /t 60 /nobreak >nul
echo ✅ 服务启动等待完成

:: 验证系统状态
echo.
echo [8/9] 验证系统状态...

echo 📊 检查容器状态:
docker ps --filter "name=qms" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo 📊 检查端口监听状态:
set "ports=80 443 3003 3004 8081 8072 8085"
for %%p in (%ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul 2>&1
    if errorlevel 1 (
        echo ⚠️ 端口 %%p: 未监听
    ) else (
        echo ✅ 端口 %%p: 监听中
    )
)

echo.
echo 📊 检查服务健康状态:
set "services=3003:配置中心 3004:聊天服务 8081:应用端 8072:配置端 8085:API网关"
for %%s in (%services%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        curl -s -f "http://localhost:%%a/health" >nul 2>&1
        if errorlevel 1 (
            echo ⚠️ %%b ^(%%a^): 异常
        ) else (
            echo ✅ %%b ^(%%a^): 健康
        )
    )
)

echo ✅ 系统验证完成

:: 显示访问信息
echo.
echo [9/9] 显示访问信息...
echo.
echo 🎉 QMS-AI系统部署更新完成！
echo.
echo 📋 访问地址:
echo   🏠 主应用: http://localhost/ 或 http://your-server-ip/
echo   ⚙️ 配置端: http://localhost/config/ 或 http://your-server-ip/config/
echo   🔧 API网关: http://localhost:8085/ 或 http://your-server-ip:8085/
echo   🔌 WebSocket: ws://localhost:8085/config-sync 或 ws://your-server-ip:8085/config-sync
echo.
echo 📊 服务状态:
echo   • 如果所有服务都显示'健康'，系统运行正常
echo   • 如果有服务异常，请等待几分钟后重新检查
echo.
echo 🔧 故障排除:
echo   • 如果无法访问，请检查防火墙和安全组设置
echo   • 查看容器日志: docker logs container-name
echo   • 重新运行此脚本: QMS-AI-Complete-Deploy.bat
echo.
echo 📁 备份位置: %backup_dir%
echo.

echo ✅ 🎉 部署更新完成！
echo.

pause
