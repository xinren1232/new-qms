@echo off
echo ========================================
echo QMS-AI阿里云简化部署脚本
echo 目标服务器: 47.108.152.16
echo ========================================
echo.

echo 步骤1: 创建部署包...
if exist qms-deploy.zip del qms-deploy.zip
powershell -Command "Compress-Archive -Path backend,frontend,deployment,config,package.json,.env.production -DestinationPath qms-deploy.zip -Force"
if exist qms-deploy.zip (
    echo ✅ 部署包创建成功
) else (
    echo ❌ 部署包创建失败
    pause
    exit /b 1
)

echo.
echo 步骤2: 上传到服务器...
echo 请在提示时输入服务器密码: Zxylsy.99
scp qms-deploy.zip root@47.108.152.16:/tmp/

echo.
echo 步骤3: 登录服务器执行部署...
echo 请在提示时输入服务器密码: Zxylsy.99
echo 登录后请执行以下命令:
echo.
echo cd /tmp
echo unzip -o qms-deploy.zip
echo mkdir -p /opt/qms
echo cp -r * /opt/qms/
echo cd /opt/qms
echo.
echo # 安装Docker (如果未安装)
echo curl -fsSL https://get.docker.com -o get-docker.sh
echo sh get-docker.sh
echo systemctl start docker
echo systemctl enable docker
echo.
echo # 安装Docker Compose
echo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
echo chmod +x /usr/local/bin/docker-compose
echo.
echo # 启动服务
echo docker-compose -f deployment/aliyun-deploy-optimized.yml up -d
echo.
echo 按任意键连接到服务器...
pause

ssh root@47.108.152.16

echo.
echo 部署完成后访问地址:
echo 主应用: http://47.108.152.16:8081
echo 聊天服务: http://47.108.152.16:3004/health
echo 配置中心: http://47.108.152.16:8083/health
echo 认证服务: http://47.108.152.16:8084/health
echo.
pause
