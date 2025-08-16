@echo off
chcp 65001 >nul
echo 🚀 开始创建QMS-AI完整部署包...

:: 设置变量
set DEPLOY_DIR=%TEMP%\qms-ai-complete-%date:~0,4%%date:~5,2%%date:~8,2%-%time:~0,2%%time:~3,2%%time:~6,2%
set DEPLOY_DIR=%DEPLOY_DIR: =0%

echo 📦 创建部署目录: %DEPLOY_DIR%
mkdir "%DEPLOY_DIR%" 2>nul
mkdir "%DEPLOY_DIR%\backend" 2>nul
mkdir "%DEPLOY_DIR%\frontend" 2>nul
mkdir "%DEPLOY_DIR%\config" 2>nul
mkdir "%DEPLOY_DIR%\scripts" 2>nul

echo 📋 复制项目文件...

:: 复制根目录配置
copy "package.json" "%DEPLOY_DIR%\" >nul 2>&1
copy "pnpm-workspace.yaml" "%DEPLOY_DIR%\" >nul 2>&1
copy "pnpm-lock.yaml" "%DEPLOY_DIR%\" >nul 2>&1

:: 复制后端代码（排除node_modules和日志）
echo 📂 复制后端代码...
robocopy "backend\nodejs" "%DEPLOY_DIR%\backend" /E /XD node_modules .git logs /XF "*.log" "*.tmp" >nul

:: 复制前端代码
echo 📂 复制前端代码...
robocopy "frontend\应用端" "%DEPLOY_DIR%\frontend\app" /E /XD node_modules dist .git /XF "*.log" "*.tmp" >nul
robocopy "frontend\配置端" "%DEPLOY_DIR%\frontend\config" /E /XD node_modules dist .git /XF "*.log" "*.tmp" >nul

:: 复制配置文件
echo 📂 复制配置文件...
robocopy "config" "%DEPLOY_DIR%\config" /E /XD node_modules .git /XF "*.log" "*.tmp" >nul

:: 创建Docker配置文件
echo 🐳 创建Docker配置...

:: 创建docker-compose.yml
echo version: '3.8' > "%DEPLOY_DIR%\docker-compose.yml"
echo. >> "%DEPLOY_DIR%\docker-compose.yml"
echo services: >> "%DEPLOY_DIR%\docker-compose.yml"
echo   # Redis服务 >> "%DEPLOY_DIR%\docker-compose.yml"
echo   redis: >> "%DEPLOY_DIR%\docker-compose.yml"
echo     image: redis:7-alpine >> "%DEPLOY_DIR%\docker-compose.yml"
echo     container_name: qms-redis >> "%DEPLOY_DIR%\docker-compose.yml"
echo     ports: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - "6379:6379" >> "%DEPLOY_DIR%\docker-compose.yml"
echo     volumes: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - redis-data:/data >> "%DEPLOY_DIR%\docker-compose.yml"
echo     networks: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - qms-network >> "%DEPLOY_DIR%\docker-compose.yml"
echo     restart: unless-stopped >> "%DEPLOY_DIR%\docker-compose.yml"
echo. >> "%DEPLOY_DIR%\docker-compose.yml"
echo   # 后端聊天服务 >> "%DEPLOY_DIR%\docker-compose.yml"
echo   backend-chat: >> "%DEPLOY_DIR%\docker-compose.yml"
echo     build: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       context: ./backend >> "%DEPLOY_DIR%\docker-compose.yml"
echo       dockerfile: Dockerfile.chat >> "%DEPLOY_DIR%\docker-compose.yml"
echo     container_name: qms-backend-chat >> "%DEPLOY_DIR%\docker-compose.yml"
echo     ports: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - "3003:3003" >> "%DEPLOY_DIR%\docker-compose.yml"
echo     environment: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - NODE_ENV=production >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - REDIS_URL=redis://redis:6379 >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - PORT=3003 >> "%DEPLOY_DIR%\docker-compose.yml"
echo     volumes: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - ./backend/logs:/app/logs >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - ./backend/database:/app/database >> "%DEPLOY_DIR%\docker-compose.yml"
echo     depends_on: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - redis >> "%DEPLOY_DIR%\docker-compose.yml"
echo     networks: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - qms-network >> "%DEPLOY_DIR%\docker-compose.yml"
echo     restart: unless-stopped >> "%DEPLOY_DIR%\docker-compose.yml"
echo. >> "%DEPLOY_DIR%\docker-compose.yml"
echo   # 后端配置服务 >> "%DEPLOY_DIR%\docker-compose.yml"
echo   backend-config: >> "%DEPLOY_DIR%\docker-compose.yml"
echo     build: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       context: ./backend >> "%DEPLOY_DIR%\docker-compose.yml"
echo       dockerfile: Dockerfile.config >> "%DEPLOY_DIR%\docker-compose.yml"
echo     container_name: qms-backend-config >> "%DEPLOY_DIR%\docker-compose.yml"
echo     ports: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - "3004:3004" >> "%DEPLOY_DIR%\docker-compose.yml"
echo     environment: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - NODE_ENV=production >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - REDIS_URL=redis://redis:6379 >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - PORT=3004 >> "%DEPLOY_DIR%\docker-compose.yml"
echo     volumes: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - ./backend/logs:/app/logs >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - ./config:/app/config >> "%DEPLOY_DIR%\docker-compose.yml"
echo     depends_on: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - redis >> "%DEPLOY_DIR%\docker-compose.yml"
echo     networks: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - qms-network >> "%DEPLOY_DIR%\docker-compose.yml"
echo     restart: unless-stopped >> "%DEPLOY_DIR%\docker-compose.yml"
echo. >> "%DEPLOY_DIR%\docker-compose.yml"
echo   # 后端认证服务 >> "%DEPLOY_DIR%\docker-compose.yml"
echo   backend-auth: >> "%DEPLOY_DIR%\docker-compose.yml"
echo     build: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       context: ./backend >> "%DEPLOY_DIR%\docker-compose.yml"
echo       dockerfile: Dockerfile.auth >> "%DEPLOY_DIR%\docker-compose.yml"
echo     container_name: qms-backend-auth >> "%DEPLOY_DIR%\docker-compose.yml"
echo     ports: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - "8084:8084" >> "%DEPLOY_DIR%\docker-compose.yml"
echo     environment: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - NODE_ENV=production >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - REDIS_URL=redis://redis:6379 >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - PORT=8084 >> "%DEPLOY_DIR%\docker-compose.yml"
echo     volumes: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - ./backend/logs:/app/logs >> "%DEPLOY_DIR%\docker-compose.yml"
echo     depends_on: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - redis >> "%DEPLOY_DIR%\docker-compose.yml"
echo     networks: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - qms-network >> "%DEPLOY_DIR%\docker-compose.yml"
echo     restart: unless-stopped >> "%DEPLOY_DIR%\docker-compose.yml"
echo. >> "%DEPLOY_DIR%\docker-compose.yml"
echo   # 前端应用服务 >> "%DEPLOY_DIR%\docker-compose.yml"
echo   frontend-app: >> "%DEPLOY_DIR%\docker-compose.yml"
echo     build: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       context: ./frontend/app >> "%DEPLOY_DIR%\docker-compose.yml"
echo       dockerfile: Dockerfile >> "%DEPLOY_DIR%\docker-compose.yml"
echo     container_name: qms-frontend-app >> "%DEPLOY_DIR%\docker-compose.yml"
echo     ports: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - "8081:8081" >> "%DEPLOY_DIR%\docker-compose.yml"
echo     environment: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - NODE_ENV=production >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - VUE_APP_API_BASE_URL=http://47.108.152.16:3003 >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - VUE_APP_CONFIG_API_URL=http://47.108.152.16:3004 >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - VUE_APP_AUTH_API_URL=http://47.108.152.16:8084 >> "%DEPLOY_DIR%\docker-compose.yml"
echo     networks: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - qms-network >> "%DEPLOY_DIR%\docker-compose.yml"
echo     restart: unless-stopped >> "%DEPLOY_DIR%\docker-compose.yml"
echo. >> "%DEPLOY_DIR%\docker-compose.yml"
echo   # 前端配置服务 >> "%DEPLOY_DIR%\docker-compose.yml"
echo   frontend-config: >> "%DEPLOY_DIR%\docker-compose.yml"
echo     build: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       context: ./frontend/config >> "%DEPLOY_DIR%\docker-compose.yml"
echo       dockerfile: Dockerfile >> "%DEPLOY_DIR%\docker-compose.yml"
echo     container_name: qms-frontend-config >> "%DEPLOY_DIR%\docker-compose.yml"
echo     ports: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - "8072:8072" >> "%DEPLOY_DIR%\docker-compose.yml"
echo     environment: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - NODE_ENV=production >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - VUE_APP_API_BASE_URL=http://47.108.152.16:3004 >> "%DEPLOY_DIR%\docker-compose.yml"
echo     networks: >> "%DEPLOY_DIR%\docker-compose.yml"
echo       - qms-network >> "%DEPLOY_DIR%\docker-compose.yml"
echo     restart: unless-stopped >> "%DEPLOY_DIR%\docker-compose.yml"
echo. >> "%DEPLOY_DIR%\docker-compose.yml"
echo volumes: >> "%DEPLOY_DIR%\docker-compose.yml"
echo   redis-data: >> "%DEPLOY_DIR%\docker-compose.yml"
echo. >> "%DEPLOY_DIR%\docker-compose.yml"
echo networks: >> "%DEPLOY_DIR%\docker-compose.yml"
echo   qms-network: >> "%DEPLOY_DIR%\docker-compose.yml"
echo     driver: bridge >> "%DEPLOY_DIR%\docker-compose.yml"

echo 📦 压缩部署包...
set ZIP_FILE=%TEMP%\qms-ai-complete-%date:~0,4%%date:~5,2%%date:~8,2%-%time:~0,2%%time:~3,2%%time:~6,2%.zip
set ZIP_FILE=%ZIP_FILE: =0%

powershell -Command "Compress-Archive -Path '%DEPLOY_DIR%\*' -DestinationPath '%ZIP_FILE%' -Force"

echo ✅ 部署包已创建: %ZIP_FILE%
echo.
echo 📤 请使用以下命令上传到服务器:
echo scp "%ZIP_FILE%" root@47.108.152.16:/tmp/
echo.
echo 🚀 服务器执行命令:
echo cd /tmp ^&^& unzip %~nxZIP_FILE% -d /root/QMS-AI-Complete ^&^& cd /root/QMS-AI-Complete ^&^& chmod +x deploy.sh ^&^& ./deploy.sh
echo.
echo 🎉 部署包创建完成！

pause
