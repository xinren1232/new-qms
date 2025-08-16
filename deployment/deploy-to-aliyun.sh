#!/bin/bash

# QMS-AI阿里云一键部署脚本
# 目标服务器: 47.108.152.16

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 服务器配置
SERVER_IP="47.108.152.16"
SSH_USER="root"
SSH_PORT="22"
PROJECT_DIR="/opt/qms"

# 显示横幅
show_banner() {
    echo -e "${PURPLE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                QMS-AI阿里云一键部署工具                      ║"
    echo "║              目标服务器: 47.108.152.16                      ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# 检查本地环境
check_local_environment() {
    echo -e "${CYAN}🔍 检查本地环境...${NC}"
    
    # 检查Git
    if ! command -v git &> /dev/null; then
        echo -e "${RED}❌ Git未安装${NC}"
        exit 1
    fi
    
    # 检查SSH
    if ! command -v ssh &> /dev/null; then
        echo -e "${RED}❌ SSH未安装${NC}"
        exit 1
    fi
    
    # 检查项目文件
    if [ ! -f "deployment/aliyun-deploy-optimized.yml" ]; then
        echo -e "${RED}❌ 部署配置文件不存在${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ 本地环境检查完成${NC}"
}

# 测试服务器连接
test_server_connection() {
    echo -e "${CYAN}🔍 测试服务器连接...${NC}"
    
    if ssh -o ConnectTimeout=10 -p $SSH_PORT $SSH_USER@$SERVER_IP "echo 'SSH连接测试成功'" 2>/dev/null; then
        echo -e "${GREEN}✅ 服务器连接正常${NC}"
    else
        echo -e "${RED}❌ 无法连接到服务器 $SERVER_IP${NC}"
        echo -e "${YELLOW}请检查:${NC}"
        echo "  1. 服务器IP是否正确"
        echo "  2. SSH服务是否启动"
        echo "  3. 防火墙是否开放22端口"
        echo "  4. 网络连接是否正常"
        exit 1
    fi
}

# 准备部署文件
prepare_deployment_files() {
    echo -e "${CYAN}📦 准备部署文件...${NC}"
    
    # 创建临时目录
    TEMP_DIR=$(mktemp -d)
    echo "临时目录: $TEMP_DIR"
    
    # 复制项目文件
    cp -r . $TEMP_DIR/
    cd $TEMP_DIR
    
    # 清理不需要的文件
    rm -rf .git node_modules .venv __pycache__ *.log
    find . -name "*.tmp" -delete
    find . -name ".DS_Store" -delete
    
    # 使用生产环境配置
    cp deployment/.env.production .env
    
    echo -e "${GREEN}✅ 部署文件准备完成${NC}"
}

# 上传文件到服务器
upload_files() {
    echo -e "${CYAN}📤 上传文件到服务器...${NC}"
    
    # 创建服务器目录
    ssh -p $SSH_PORT $SSH_USER@$SERVER_IP "mkdir -p /tmp/qms-deploy"
    
    # 上传文件
    if command -v rsync &> /dev/null; then
        rsync -avz --progress --delete -e "ssh -p $SSH_PORT" . $SSH_USER@$SERVER_IP:/tmp/qms-deploy/
    else
        scp -r -P $SSH_PORT . $SSH_USER@$SERVER_IP:/tmp/qms-deploy/
    fi
    
    echo -e "${GREEN}✅ 文件上传完成${NC}"
}

# 在服务器上执行部署
deploy_on_server() {
    echo -e "${CYAN}🚀 在服务器上执行部署...${NC}"
    
    ssh -p $SSH_PORT $SSH_USER@$SERVER_IP << 'EOF'
set -e

echo "🔧 开始服务器端部署..."

# 创建项目目录
sudo mkdir -p /opt/qms
sudo mkdir -p /opt/qms-backup

# 备份现有部署（如果存在）
if [ -d "/opt/qms" ] && [ "$(ls -A /opt/qms)" ]; then
    echo "📦 备份现有部署..."
    sudo cp -r /opt/qms /opt/qms-backup/backup-$(date +%Y%m%d-%H%M%S)
fi

# 复制新文件
sudo cp -r /tmp/qms-deploy/* /opt/qms/
sudo chown -R root:root /opt/qms

# 进入项目目录
cd /opt/qms

# 给脚本执行权限
chmod +x deployment/*.sh

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "🐳 安装Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    systemctl start docker
    systemctl enable docker
    
    # 配置Docker镜像加速器
    mkdir -p /etc/docker
    cat > /etc/docker/daemon.json << 'DOCKER_EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "3"
  }
}
DOCKER_EOF
    systemctl restart docker
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "🔧 安装Docker Compose..."
    DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d'"' -f4)
    curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
fi

# 创建必要目录
mkdir -p {logs,data,config,ssl,backup,nginx/conf.d,prometheus,grafana/provisioning}
chmod 755 logs data config ssl backup

# 停止现有服务
if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
    echo "🛑 停止现有服务..."
    docker-compose -f deployment/aliyun-deploy-optimized.yml down 2>/dev/null || true
fi

# 清理旧镜像
echo "🧹 清理旧镜像..."
docker system prune -f

# 构建和启动服务
echo "🔨 构建和启动服务..."
docker-compose -f deployment/aliyun-deploy-optimized.yml build --no-cache
docker-compose -f deployment/aliyun-deploy-optimized.yml up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 60

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose -f deployment/aliyun-deploy-optimized.yml ps

echo "✅ 服务器端部署完成！"
EOF
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 服务器端部署成功${NC}"
    else
        echo -e "${RED}❌ 服务器端部署失败${NC}"
        exit 1
    fi
}

# 检查部署结果
check_deployment() {
    echo -e "${CYAN}🔍 检查部署结果...${NC}"
    
    # 检查服务状态
    echo "检查服务状态..."
    ssh -p $SSH_PORT $SSH_USER@$SERVER_IP "cd /opt/qms && docker-compose -f deployment/aliyun-deploy-optimized.yml ps"
    
    # 检查服务健康状态
    echo ""
    echo "检查服务健康状态..."
    
    services=("8084" "8083" "3004")
    service_names=("认证服务" "配置中心" "聊天服务")
    
    for i in "${!services[@]}"; do
        port=${services[$i]}
        name=${service_names[$i]}
        
        if ssh -p $SSH_PORT $SSH_USER@$SERVER_IP "curl -f -s http://localhost:$port/health" > /dev/null 2>&1; then
            echo -e "  ${name}: ${GREEN}✅ 健康${NC}"
        else
            echo -e "  ${name}: ${YELLOW}⚠️ 启动中或异常${NC}"
        fi
    done
}

# 显示部署结果
show_results() {
    echo ""
    echo -e "${GREEN}🎉 QMS-AI阿里云部署完成！${NC}"
    echo "========================================"
    echo ""
    echo -e "${BLUE}📋 访问信息:${NC}"
    echo "  🌐 主应用: http://$SERVER_IP:8081"
    echo "  💬 AI聊天: http://$SERVER_IP:8081/ai-management/chat"
    echo "  🤖 模型管理: http://$SERVER_IP:8081/ai-management/models"
    echo "  📊 监控面板: http://$SERVER_IP:3000"
    echo ""
    echo -e "${BLUE}🔧 服务端口:${NC}"
    echo "  🔐 认证服务: $SERVER_IP:8084"
    echo "  ⚙️ 配置中心: $SERVER_IP:8083"
    echo "  💬 聊天服务: $SERVER_IP:3004"
    echo "  🌐 前端应用: $SERVER_IP:8081"
    echo ""
    echo -e "${YELLOW}📝 管理命令:${NC}"
    echo "  登录服务器: ssh -p $SSH_PORT $SSH_USER@$SERVER_IP"
    echo "  查看服务: cd /opt/qms && docker-compose -f deployment/aliyun-deploy-optimized.yml ps"
    echo "  查看日志: cd /opt/qms && docker-compose -f deployment/aliyun-deploy-optimized.yml logs -f"
    echo "  重启服务: cd /opt/qms && docker-compose -f deployment/aliyun-deploy-optimized.yml restart"
    echo ""
    echo -e "${PURPLE}💡 下一步操作:${NC}"
    echo "  1. 如果有域名，配置域名解析指向 $SERVER_IP"
    echo "  2. 配置防火墙开放端口: 80, 443, 3000, 3004, 8081, 8083, 8084"
    echo "  3. 如果需要HTTPS，申请SSL证书"
    echo "  4. 配置阿里云RDS和Redis（如果使用）"
}

# 清理临时文件
cleanup() {
    if [ -n "$TEMP_DIR" ] && [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
        echo -e "${GREEN}✅ 清理临时文件完成${NC}"
    fi
}

# 主函数
main() {
    show_banner
    
    echo -e "${CYAN}开始QMS-AI阿里云一键部署...${NC}"
    echo ""
    
    check_local_environment
    test_server_connection
    prepare_deployment_files
    upload_files
    deploy_on_server
    check_deployment
    show_results
    cleanup
    
    echo -e "${GREEN}🎉 部署流程完成！${NC}"
}

# 捕获退出信号，确保清理
trap cleanup EXIT

# 执行主函数
main "$@"
