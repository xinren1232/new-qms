#!/bin/bash

echo "📤 QMS项目上传到阿里云服务器"
echo "============================="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 检查必要工具
check_tools() {
    if ! command -v rsync &> /dev/null; then
        echo -e "${RED}❌ rsync未安装，请先安装rsync${NC}"
        echo "Ubuntu/Debian: sudo apt install rsync"
        echo "CentOS/RHEL: sudo yum install rsync"
        echo "macOS: brew install rsync"
        exit 1
    fi
}

# 获取服务器信息
get_server_info() {
    echo -e "${YELLOW}请输入服务器信息:${NC}"
    
    read -p "服务器IP地址 (默认47.108.152.16): " SERVER_IP
    SERVER_IP=${SERVER_IP:-47.108.152.16}

    if [ -z "$SERVER_IP" ]; then
        echo -e "${RED}❌ 服务器IP不能为空${NC}"
        exit 1
    fi
    
    read -p "SSH用户名 (默认root): " SSH_USER
    SSH_USER=${SSH_USER:-root}
    
    read -p "SSH端口 (默认22): " SSH_PORT
    SSH_PORT=${SSH_PORT:-22}
    
    read -p "SSH密钥文件路径 (可选，回车使用密码): " SSH_KEY
    
    echo -e "${GREEN}✅ 服务器信息配置完成${NC}"
}

# 测试SSH连接
test_ssh_connection() {
    echo -e "${YELLOW}🔍 测试SSH连接...${NC}"
    
    if [ ! -z "$SSH_KEY" ]; then
        SSH_CMD="ssh -i $SSH_KEY -p $SSH_PORT $SSH_USER@$SERVER_IP"
    else
        SSH_CMD="ssh -p $SSH_PORT $SSH_USER@$SERVER_IP"
    fi
    
    if $SSH_CMD "echo 'SSH连接测试成功'" 2>/dev/null; then
        echo -e "${GREEN}✅ SSH连接正常${NC}"
    else
        echo -e "${RED}❌ SSH连接失败，请检查服务器信息${NC}"
        exit 1
    fi
}

# 准备上传文件
prepare_files() {
    echo -e "${YELLOW}📦 准备上传文件...${NC}"
    
    # 创建临时目录
    TEMP_DIR="/tmp/qms-upload"
    rm -rf $TEMP_DIR
    mkdir -p $TEMP_DIR
    
    # 复制项目文件
    echo "复制项目文件..."
    
    # 后端文件
    if [ -d "backend" ]; then
        cp -r backend $TEMP_DIR/
        echo "✅ 后端文件已复制"
    fi
    
    # 前端文件
    if [ -d "frontend" ]; then
        cp -r frontend $TEMP_DIR/
        echo "✅ 前端文件已复制"
    fi
    
    # Docker配置文件
    cp docker-compose.yml $TEMP_DIR/ 2>/dev/null || echo "⚠️ docker-compose.yml不存在"
    cp docker-compose.prod.yml $TEMP_DIR/ 2>/dev/null || echo "⚠️ docker-compose.prod.yml不存在"
    
    # 部署脚本
    if [ -d "deploy" ]; then
        cp -r deploy $TEMP_DIR/
        echo "✅ 部署脚本已复制"
    fi
    
    # 配置文件
    if [ -d "config" ]; then
        cp -r config $TEMP_DIR/
        echo "✅ 配置文件已复制"
    fi
    
    # 文档文件
    if [ -d "docs" ]; then
        cp -r docs $TEMP_DIR/
        echo "✅ 文档文件已复制"
    fi
    
    # 其他必要文件
    cp README.md $TEMP_DIR/ 2>/dev/null || true
    cp package.json $TEMP_DIR/ 2>/dev/null || true
    cp .gitignore $TEMP_DIR/ 2>/dev/null || true
    
    # 创建排除文件列表
    cat > $TEMP_DIR/.rsync-exclude << 'EOF'
node_modules/
.git/
.env
*.log
.DS_Store
Thumbs.db
.vscode/
.idea/
dist/
build/
coverage/
.nyc_output/
*.tmp
*.temp
EOF
    
    echo -e "${GREEN}✅ 文件准备完成${NC}"
}

# 上传文件到服务器
upload_files() {
    echo -e "${YELLOW}📤 上传文件到服务器...${NC}"
    
    # 构建rsync命令
    if [ ! -z "$SSH_KEY" ]; then
        RSYNC_CMD="rsync -avz --progress --delete --exclude-from=$TEMP_DIR/.rsync-exclude -e 'ssh -i $SSH_KEY -p $SSH_PORT' $TEMP_DIR/ $SSH_USER@$SERVER_IP:/tmp/qms-source/"
    else
        RSYNC_CMD="rsync -avz --progress --delete --exclude-from=$TEMP_DIR/.rsync-exclude -e 'ssh -p $SSH_PORT' $TEMP_DIR/ $SSH_USER@$SERVER_IP:/tmp/qms-source/"
    fi
    
    # 执行上传
    if $RSYNC_CMD; then
        echo -e "${GREEN}✅ 文件上传成功${NC}"
    else
        echo -e "${RED}❌ 文件上传失败${NC}"
        exit 1
    fi
    
    # 清理临时目录
    rm -rf $TEMP_DIR
}

# 在服务器上执行部署
deploy_on_server() {
    echo -e "${YELLOW}🚀 在服务器上执行部署...${NC}"
    
    # 构建SSH命令
    if [ ! -z "$SSH_KEY" ]; then
        SSH_CMD="ssh -i $SSH_KEY -p $SSH_PORT $SSH_USER@$SERVER_IP"
    else
        SSH_CMD="ssh -p $SSH_PORT $SSH_USER@$SERVER_IP"
    fi
    
    # 执行部署命令
    $SSH_CMD << 'EOF'
echo "🔧 开始服务器端部署..."

# 创建项目目录
sudo mkdir -p /opt/qms
sudo chown -R $(whoami):$(whoami) /opt/qms

# 复制文件到项目目录
cp -r /tmp/qms-source/* /opt/qms/

# 进入项目目录
cd /opt/qms

# 给脚本执行权限
chmod +x deploy/*.sh

echo "✅ 文件部署完成"
echo "📋 下一步操作："
echo "1. 运行环境配置: sudo ./deploy/aliyun-setup.sh"
echo "2. 一键部署: sudo ./deploy/one-click-deploy.sh"
echo "3. 或者手动配置: 编辑.env文件后运行 docker-compose up -d"
EOF
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 服务器端部署完成${NC}"
    else
        echo -e "${RED}❌ 服务器端部署失败${NC}"
        exit 1
    fi
}

# 显示后续操作指南
show_next_steps() {
    echo ""
    echo -e "${GREEN}🎉 项目上传完成！${NC}"
    echo "============================="
    echo "📋 后续操作步骤："
    echo ""
    echo "1. 登录服务器:"
    if [ ! -z "$SSH_KEY" ]; then
        echo "   ssh -i $SSH_KEY -p $SSH_PORT $SSH_USER@$SERVER_IP"
    else
        echo "   ssh -p $SSH_PORT $SSH_USER@$SERVER_IP"
    fi
    echo ""
    echo "2. 进入项目目录:"
    echo "   cd /opt/qms"
    echo ""
    echo "3. 选择部署方式:"
    echo "   方式一 - 一键部署 (推荐):"
    echo "   sudo ./deploy/one-click-deploy.sh"
    echo ""
    echo "   方式二 - 分步部署:"
    echo "   sudo ./deploy/aliyun-setup.sh"
    echo "   cp .env.template .env && vim .env"
    echo "   docker-compose -f docker-compose.prod.yml up -d"
    echo ""
    echo "4. 配置域名解析:"
    echo "   将域名 A 记录指向服务器IP: $SERVER_IP"
    echo ""
    echo "5. 配置飞书应用:"
    echo "   回调URL: https://your-domain.com/auth/feishu/callback"
    echo ""
    echo -e "${YELLOW}⚠️ 重要提醒:${NC}"
    echo "- 确保服务器防火墙开放了必要端口 (80, 443, 22)"
    echo "- 及时修改默认密码"
    echo "- 配置SSL证书"
    echo "- 定期备份数据"
}

# 主函数
main() {
    echo -e "${GREEN}开始QMS项目上传部署流程...${NC}"
    
    check_tools
    get_server_info
    test_ssh_connection
    prepare_files
    upload_files
    deploy_on_server
    show_next_steps
    
    echo -e "${GREEN}✅ 上传部署流程完成！${NC}"
}

# 执行主函数
main "$@"
