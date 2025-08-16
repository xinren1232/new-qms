#!/bin/bash

# QMS-AI 快速更新脚本
# 在服务器上直接运行此脚本进行增量更新

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                QMS-AI 快速增量更新 v2.0                      ║"
echo "║              包含最新AI功能和界面优化                         ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# 配置
PROJECT_DIR="/opt/qms"
BACKUP_DIR="/opt/qms-backup-$(date +%Y%m%d-%H%M%S)"
GITHUB_REPO="https://github.com/xinren1232/qmsai.git"
BRANCH="clean-main"

# 日志函数
log() {
    echo -e "${CYAN}[$(date +'%H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

# 检查环境
check_environment() {
    log "检查部署环境..."
    
    if [ "$EUID" -ne 0 ]; then
        error "请使用root用户运行此脚本"
    fi
    
    if [ ! -d "$PROJECT_DIR" ]; then
        error "项目目录不存在: $PROJECT_DIR"
    fi
    
    if ! docker info >/dev/null 2>&1; then
        error "Docker未运行"
    fi
    
    success "环境检查通过"
}

# 备份系统
backup_system() {
    log "备份现有系统..."
    
    mkdir -p $BACKUP_DIR
    cd $PROJECT_DIR
    
    # 停止服务
    log "停止现有服务..."
    if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        docker-compose -f deployment/aliyun-deploy-optimized.yml down 2>/dev/null || true
    fi
    
    # 备份文件
    log "备份关键文件..."
    cp -r backend $BACKUP_DIR/ 2>/dev/null || true
    cp -r frontend $BACKUP_DIR/ 2>/dev/null || true
    cp -r deployment $BACKUP_DIR/ 2>/dev/null || true
    cp .env $BACKUP_DIR/ 2>/dev/null || true
    
    success "系统备份完成: $BACKUP_DIR"
}

# 更新代码
update_code() {
    log "更新代码..."
    
    cd $PROJECT_DIR
    
    # 检查Git仓库
    if [ ! -d ".git" ]; then
        log "初始化Git仓库..."
        git init
        git remote add origin $GITHUB_REPO
    fi
    
    # 保存本地更改
    git add . 2>/dev/null || true
    git stash 2>/dev/null || true
    
    # 拉取最新代码
    log "拉取最新代码..."
    git fetch origin $BRANCH
    git checkout $BRANCH 2>/dev/null || git checkout -b $BRANCH origin/$BRANCH
    git pull origin $BRANCH
    
    success "代码更新完成"
}

# 恢复配置
restore_config() {
    log "恢复配置文件..."
    
    if [ -f "$BACKUP_DIR/.env" ]; then
        cp $BACKUP_DIR/.env $PROJECT_DIR/
        success "环境变量恢复完成"
    fi
    
    if [ -d "$BACKUP_DIR/config" ]; then
        cp -r $BACKUP_DIR/config/* $PROJECT_DIR/config/ 2>/dev/null || true
        success "自定义配置恢复完成"
    fi
}

# 更新依赖
update_dependencies() {
    log "更新项目依赖..."
    
    # 更新后端依赖
    if [ -f "$PROJECT_DIR/backend/nodejs/package.json" ]; then
        log "更新后端依赖..."
        cd $PROJECT_DIR/backend/nodejs
        npm install --production --silent
        cd $PROJECT_DIR
        success "后端依赖更新完成"
    fi
}

# 重新部署
redeploy_services() {
    log "重新部署服务..."
    
    cd $PROJECT_DIR
    
    # 清理旧镜像
    log "清理Docker镜像..."
    docker system prune -f >/dev/null 2>&1
    
    # 重新构建
    log "重新构建镜像..."
    if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        docker-compose -f deployment/aliyun-deploy-optimized.yml build --no-cache >/dev/null 2>&1
    fi
    
    # 启动服务
    log "启动所有服务..."
    if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        docker-compose -f deployment/aliyun-deploy-optimized.yml up -d
    fi
    
    success "服务部署完成"
}

# 等待服务启动
wait_for_services() {
    log "等待服务启动..."
    
    sleep 30
    
    # 检查服务状态
    cd $PROJECT_DIR
    if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        docker-compose -f deployment/aliyun-deploy-optimized.yml ps
    fi
    
    success "服务启动完成"
}

# 验证部署
verify_deployment() {
    log "验证部署结果..."
    
    # 检查端口
    services=("3003" "3004" "8084" "8085")
    
    for port in "${services[@]}"; do
        if curl -f -s "http://localhost:$port/health" >/dev/null 2>&1; then
            success "端口$port服务正常"
        else
            warning "端口$port服务可能未就绪"
        fi
    done
}

# 显示结果
show_results() {
    echo ""
    echo -e "${GREEN}🎉 QMS-AI增量更新完成！${NC}"
    echo "========================================"
    echo ""
    echo -e "${BLUE}📋 更新内容:${NC}"
    echo "  ✅ 全面服务管理器"
    echo "  ✅ AI智能功能优化"
    echo "  ✅ 前端界面改进"
    echo "  ✅ 系统性能优化"
    echo ""
    echo -e "${BLUE}🆕 新增功能:${NC}"
    echo "  🤖 AutoGPT执行器"
    echo "  🤝 CrewAI协调器"
    echo "  🧠 LangChain内存管理"
    echo "  🎯 智能模型选择器"
    echo "  🔧 插件生态系统"
    echo ""
    echo -e "${YELLOW}💾 备份位置:${NC}"
    echo "  $BACKUP_DIR"
    echo ""
    echo -e "${CYAN}🌐 访问地址:${NC}"
    echo "  主应用: http://$(curl -s ifconfig.me 2>/dev/null || echo 'your-server-ip'):8081"
    echo "  配置管理: http://$(curl -s ifconfig.me 2>/dev/null || echo 'your-server-ip'):8072"
    echo "  API网关: http://$(curl -s ifconfig.me 2>/dev/null || echo 'your-server-ip'):8085"
    echo ""
    echo -e "${CYAN}🔧 管理命令:${NC}"
    echo "  查看状态: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml ps"
    echo "  查看日志: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml logs -f"
    echo "  重启服务: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml restart"
    echo ""
}

# 主函数
main() {
    check_environment
    backup_system
    update_code
    restore_config
    update_dependencies
    redeploy_services
    wait_for_services
    verify_deployment
    show_results
    
    success "增量更新流程完成！"
}

# 执行主函数
main "$@"
