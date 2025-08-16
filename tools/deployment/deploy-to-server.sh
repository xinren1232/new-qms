#!/bin/bash

echo "🚀 QMS-AI系统服务器部署脚本"
echo "================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查系统环境
check_system() {
    log_info "检查系统环境..."
    
    # 检查操作系统
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        log_success "操作系统: Linux"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        log_success "操作系统: macOS"
    else
        log_error "不支持的操作系统: $OSTYPE"
        exit 1
    fi
    
    # 检查内存
    MEMORY=$(free -m | awk 'NR==2{printf "%.1f", $2/1024}' 2>/dev/null || echo "unknown")
    log_info "系统内存: ${MEMORY}GB"
    
    # 检查磁盘空间
    DISK=$(df -h / | awk 'NR==2 {print $4}')
    log_info "可用磁盘空间: $DISK"
}

# 安装Docker
install_docker() {
    log_info "检查Docker安装状态..."
    
    if command -v docker &> /dev/null; then
        log_success "Docker已安装: $(docker --version)"
    else
        log_warning "Docker未安装，正在安装..."
        
        # 更新包管理器
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y docker.io docker-compose
        elif command -v yum &> /dev/null; then
            sudo yum install -y docker docker-compose
        else
            log_error "不支持的包管理器"
            exit 1
        fi
        
        # 启动Docker服务
        sudo systemctl start docker
        sudo systemctl enable docker
        
        # 添加用户到docker组
        sudo usermod -aG docker $USER
        
        log_success "Docker安装完成"
    fi
    
    # 检查Docker Compose
    if command -v docker-compose &> /dev/null; then
        log_success "Docker Compose已安装: $(docker-compose --version)"
    else
        log_error "Docker Compose未安装"
        exit 1
    fi
}

# 准备部署环境
prepare_environment() {
    log_info "准备部署环境..."
    
    # 创建必要目录
    mkdir -p {logs,data/redis,ssl,backup}
    
    # 设置权限
    chmod -R 755 logs data ssl backup
    
    # 检查端口占用
    PORTS=(80 443 3003 3004 6379 8081 8072 8084 8085)
    for port in "${PORTS[@]}"; do
        if netstat -tuln | grep ":$port " > /dev/null; then
            log_warning "端口 $port 已被占用"
        else
            log_success "端口 $port 可用"
        fi
    done
}

# 构建和部署服务
deploy_services() {
    log_info "开始构建和部署服务..."
    
    # 停止现有服务
    log_info "停止现有服务..."
    docker-compose down 2>/dev/null || true
    
    # 清理旧容器和镜像
    log_info "清理旧容器..."
    docker container prune -f
    docker image prune -f
    
    # 构建镜像
    log_info "构建Docker镜像..."
    docker-compose build --no-cache
    
    # 启动服务
    log_info "启动服务..."
    docker-compose up -d
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30
}

# 验证部署
verify_deployment() {
    log_info "验证部署状态..."
    
    # 检查容器状态
    echo "📊 容器状态:"
    docker-compose ps
    
    # 检查服务健康状态
    SERVICES=(
        "http://localhost:3003/health:配置中心"
        "http://localhost:3004/health:聊天服务"
        "http://localhost:8084/health:认证服务"
        "http://localhost:8085/health:API网关"
    )
    
    echo ""
    echo "🔍 服务健康检查:"
    for service in "${SERVICES[@]}"; do
        url=$(echo $service | cut -d: -f1-2)
        name=$(echo $service | cut -d: -f3)
        
        if curl -s -f "$url" > /dev/null; then
            log_success "$name: 健康"
        else
            log_error "$name: 异常"
        fi
    done
}

# 显示部署结果
show_results() {
    echo ""
    echo "🎉 QMS-AI系统部署完成！"
    echo "========================"
    echo ""
    echo "📱 前端应用:"
    echo "  主应用: http://localhost:8081"
    echo "  配置端: http://localhost:8072"
    echo ""
    echo "🔧 后端服务:"
    echo "  配置中心: http://localhost:3003"
    echo "  聊天服务: http://localhost:3004"
    echo "  认证服务: http://localhost:8084"
    echo "  API网关: http://localhost:8085"
    echo ""
    echo "💾 数据服务:"
    echo "  Redis缓存: localhost:6379"
    echo ""
    echo "📋 管理命令:"
    echo "  查看日志: docker-compose logs -f [service_name]"
    echo "  重启服务: docker-compose restart [service_name]"
    echo "  停止服务: docker-compose down"
    echo "  更新服务: docker-compose pull && docker-compose up -d"
    echo ""
    echo "📁 重要目录:"
    echo "  日志目录: ./logs"
    echo "  数据目录: ./data"
    echo "  备份目录: ./backup"
}

# 主函数
main() {
    log_info "开始QMS-AI系统部署..."
    
    check_system
    install_docker
    prepare_environment
    deploy_services
    verify_deployment
    show_results
    
    log_success "部署完成！"
}

# 执行主函数
main "$@"
