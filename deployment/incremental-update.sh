#!/bin/bash

# QMS-AI 增量更新部署脚本
# 用于在现有阿里云服务器上部署最新功能

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置
PROJECT_DIR="/opt/qms"
BACKUP_DIR="/opt/qms-backup-$(date +%Y%m%d-%H%M%S)"
LOG_FILE="/var/log/qms-update.log"
GITHUB_REPO="https://github.com/xinren1232/qmsai.git"
BRANCH="clean-main"

# 日志函数
log() {
    echo -e "${CYAN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a $LOG_FILE
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a $LOG_FILE
    exit 1
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a $LOG_FILE
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a $LOG_FILE
}

# 显示横幅
show_banner() {
    echo -e "${PURPLE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                QMS-AI 增量更新部署工具 v2.0                  ║"
    echo "║              在现有服务器上部署最新功能更新                   ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# 检查环境
check_environment() {
    log "🔍 检查部署环境..."
    
    # 检查是否为root用户
    if [ "$EUID" -ne 0 ]; then
        error "请使用root用户运行此脚本: sudo $0"
    fi
    
    # 检查项目目录是否存在
    if [ ! -d "$PROJECT_DIR" ]; then
        error "项目目录不存在: $PROJECT_DIR，请先进行初始部署"
    fi
    
    # 检查Docker是否运行
    if ! docker info >/dev/null 2>&1; then
        error "Docker未运行，请启动Docker服务"
    fi
    
    # 检查Git是否安装
    if ! command -v git &> /dev/null; then
        log "安装Git..."
        if command -v yum &> /dev/null; then
            yum install -y git
        elif command -v apt &> /dev/null; then
            apt update && apt install -y git
        fi
    fi
    
    success "环境检查完成"
}

# 备份现有系统
backup_current_system() {
    log "💾 备份现有系统..."
    
    # 创建备份目录
    mkdir -p $BACKUP_DIR
    
    # 停止服务
    log "停止现有服务..."
    cd $PROJECT_DIR
    if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        docker-compose -f deployment/aliyun-deploy-optimized.yml down 2>/dev/null || true
    fi
    
    # 备份关键文件和目录
    log "备份文件..."
    cp -r $PROJECT_DIR/backend $BACKUP_DIR/ 2>/dev/null || true
    cp -r $PROJECT_DIR/frontend $BACKUP_DIR/ 2>/dev/null || true
    cp -r $PROJECT_DIR/deployment $BACKUP_DIR/ 2>/dev/null || true
    cp -r $PROJECT_DIR/config $BACKUP_DIR/ 2>/dev/null || true
    cp $PROJECT_DIR/.env $BACKUP_DIR/ 2>/dev/null || true
    
    # 备份数据库数据
    if [ -d "$PROJECT_DIR/data" ]; then
        cp -r $PROJECT_DIR/data $BACKUP_DIR/
    fi
    
    success "系统备份完成: $BACKUP_DIR"
}

# 拉取最新代码
pull_latest_code() {
    log "📥 拉取最新代码..."
    
    cd $PROJECT_DIR
    
    # 检查是否是Git仓库
    if [ ! -d ".git" ]; then
        log "初始化Git仓库..."
        git init
        git remote add origin $GITHUB_REPO
    fi
    
    # 保存本地更改
    git add . 2>/dev/null || true
    git stash 2>/dev/null || true
    
    # 拉取最新代码
    log "从GitHub拉取最新代码..."
    git fetch origin $BRANCH
    git checkout $BRANCH 2>/dev/null || git checkout -b $BRANCH origin/$BRANCH
    git pull origin $BRANCH
    
    success "代码更新完成"
}

# 恢复配置文件
restore_configurations() {
    log "⚙️ 恢复配置文件..."
    
    # 恢复环境变量文件
    if [ -f "$BACKUP_DIR/.env" ]; then
        cp $BACKUP_DIR/.env $PROJECT_DIR/
        log "恢复环境变量配置"
    fi
    
    # 恢复自定义配置
    if [ -d "$BACKUP_DIR/config" ]; then
        cp -r $BACKUP_DIR/config/* $PROJECT_DIR/config/ 2>/dev/null || true
        log "恢复自定义配置"
    fi
    
    success "配置文件恢复完成"
}

# 更新依赖
update_dependencies() {
    log "📦 更新项目依赖..."
    
    # 更新后端依赖
    if [ -f "$PROJECT_DIR/backend/nodejs/package.json" ]; then
        log "更新后端依赖..."
        cd $PROJECT_DIR/backend/nodejs
        npm install --production
    fi
    
    # 更新前端依赖（如果需要重新构建）
    if [ -f "$PROJECT_DIR/frontend/应用端/package.json" ]; then
        log "更新应用端依赖..."
        cd $PROJECT_DIR/frontend/应用端
        npm install
        npm run build
    fi
    
    if [ -f "$PROJECT_DIR/frontend/配置端/package.json" ]; then
        log "更新配置端依赖..."
        cd $PROJECT_DIR/frontend/配置端
        npm install
        npm run build
    fi
    
    success "依赖更新完成"
}

# 重新构建和启动服务
rebuild_and_start() {
    log "🚀 重新构建和启动服务..."
    
    cd $PROJECT_DIR
    
    # 清理旧镜像
    log "清理旧Docker镜像..."
    docker system prune -f
    
    # 重新构建镜像
    log "重新构建Docker镜像..."
    if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        docker-compose -f deployment/aliyun-deploy-optimized.yml build --no-cache
    fi
    
    # 启动服务
    log "启动所有服务..."
    if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        docker-compose -f deployment/aliyun-deploy-optimized.yml up -d
    fi
    
    # 等待服务启动
    log "等待服务启动..."
    sleep 30
    
    success "服务启动完成"
}

# 验证更新
verify_update() {
    log "🔍 验证更新结果..."
    
    cd $PROJECT_DIR
    
    # 检查服务状态
    if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        docker-compose -f deployment/aliyun-deploy-optimized.yml ps
    fi
    
    # 检查健康状态
    log "检查服务健康状态..."
    services=("3003" "3004" "8084" "8085")
    
    for port in "${services[@]}"; do
        if curl -f -s "http://localhost:$port/health" > /dev/null; then
            success "端口$port服务正常"
        else
            warning "端口$port服务可能未就绪"
        fi
    done
    
    success "更新验证完成"
}

# 显示更新结果
show_update_results() {
    echo ""
    echo -e "${GREEN}🎉 QMS-AI增量更新完成！${NC}"
    echo "========================================"
    echo ""
    echo -e "${BLUE}📋 更新内容:${NC}"
    echo "  ✅ 全面服务管理器 (QMS-Service-Manager.bat)"
    echo "  ✅ 快速检查和启动脚本"
    echo "  ✅ AI智能功能优化"
    echo "  ✅ 前端界面改进"
    echo "  ✅ 系统性能优化"
    echo ""
    echo -e "${BLUE}📊 新增功能:${NC}"
    echo "  🤖 AutoGPT执行器"
    echo "  🤝 CrewAI协调器"
    echo "  🧠 LangChain内存管理"
    echo "  🎯 智能模型选择器"
    echo "  🔧 插件生态系统"
    echo ""
    echo -e "${YELLOW}📁 备份位置:${NC}"
    echo "  💾 系统备份: $BACKUP_DIR"
    echo ""
    echo -e "${CYAN}🔧 管理命令:${NC}"
    echo "  查看状态: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml ps"
    echo "  查看日志: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml logs -f"
    echo "  重启服务: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml restart"
    echo ""
    echo -e "${GREEN}🌐 访问地址:${NC}"
    echo "  主应用: http://your-server-ip:8081"
    echo "  配置管理: http://your-server-ip:8072"
    echo "  API网关: http://your-server-ip:8085"
}

# 主函数
main() {
    show_banner
    
    log "开始QMS-AI增量更新..."
    
    check_environment
    backup_current_system
    pull_latest_code
    restore_configurations
    update_dependencies
    rebuild_and_start
    verify_update
    show_update_results
    
    success "增量更新完成！"
}

# 执行主函数
main "$@"
