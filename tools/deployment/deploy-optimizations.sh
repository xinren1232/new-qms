#!/bin/bash

# QMS-AI系统优化部署脚本
# 版本: 2.0
# 作者: QMS-AI团队
# 日期: 2025-08-06

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# 显示横幅
show_banner() {
    echo -e "${CYAN}"
    echo "=========================================="
    echo "🚀 QMS-AI系统优化部署脚本 v2.0"
    echo "=========================================="
    echo "优化内容："
    echo "  ✅ 配置中心高可用集群"
    echo "  ✅ 数据库连接池优化"
    echo "  ✅ Redis多级缓存架构"
    echo "  ✅ API统一错误处理"
    echo "  ✅ 监控告警系统"
    echo "=========================================="
    echo -e "${NC}"
}

# 检查依赖
check_dependencies() {
    log_step "检查系统依赖..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    # 检查Docker服务状态
    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker服务"
        exit 1
    fi
    
    log_success "依赖检查完成"
}

# 创建必要的目录
create_directories() {
    log_step "创建必要的目录结构..."
    
    # 创建配置目录
    mkdir -p config/{nginx,redis,prometheus/{rules},grafana/{provisioning,dashboards},alertmanager,loki,promtail}
    mkdir -p logs
    mkdir -p data/{postgres,redis,prometheus,grafana,loki}
    
    # 设置权限
    chmod 755 config logs data
    chmod -R 644 config/*
    
    log_success "目录结构创建完成"
}

# 备份现有配置
backup_existing_config() {
    log_step "备份现有配置..."
    
    BACKUP_DIR="backup-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$BACKUP_DIR"
    
    # 备份重要配置文件
    if [ -f "docker-compose.yml" ]; then
        cp docker-compose.yml "$BACKUP_DIR/"
        log_info "已备份 docker-compose.yml"
    fi
    
    if [ -f ".env" ]; then
        cp .env "$BACKUP_DIR/"
        log_info "已备份 .env"
    fi
    
    if [ -d "config" ]; then
        cp -r config "$BACKUP_DIR/"
        log_info "已备份 config 目录"
    fi
    
    log_success "配置已备份到 $BACKUP_DIR"
}

# 创建网络
create_network() {
    log_step "创建Docker网络..."
    
    # 创建自定义网络
    if ! docker network ls | grep -q qms-network; then
        docker network create qms-network --driver bridge --subnet=172.20.0.0/16
        log_success "Docker网络 qms-network 创建成功"
    else
        log_info "Docker网络 qms-network 已存在"
    fi
}

# 停止现有服务
stop_existing_services() {
    log_step "停止现有服务..."
    
    # 停止可能存在的服务
    docker-compose down 2>/dev/null || true
    docker-compose -f docker-compose.config-cluster.yml down 2>/dev/null || true
    docker-compose -f docker-compose.monitoring.yml down 2>/dev/null || true
    
    log_success "现有服务已停止"
}

# 部署配置中心集群
deploy_config_cluster() {
    log_step "部署配置中心集群..."
    
    # 启动配置中心集群
    docker-compose -f docker-compose.config-cluster.yml up -d
    
    # 等待服务启动
    log_info "等待配置中心集群启动..."
    sleep 30
    
    # 检查服务状态
    if curl -s http://localhost:8080/health > /dev/null; then
        log_success "配置中心集群部署成功"
    else
        log_warning "配置中心集群可能未完全启动，请稍后检查"
    fi
}

# 部署监控系统
deploy_monitoring() {
    log_step "部署监控告警系统..."
    
    # 启动监控服务
    docker-compose -f docker-compose.monitoring.yml up -d
    
    # 等待服务启动
    log_info "等待监控系统启动..."
    sleep 45
    
    # 检查Prometheus
    if curl -s http://localhost:9090/-/healthy > /dev/null; then
        log_success "Prometheus监控服务启动成功"
    else
        log_warning "Prometheus可能未完全启动"
    fi
    
    # 检查Grafana
    if curl -s http://localhost:3001/api/health > /dev/null; then
        log_success "Grafana可视化服务启动成功"
    else
        log_warning "Grafana可能未完全启动"
    fi
}

# 更新应用配置
update_app_config() {
    log_step "更新应用配置..."
    
    # 创建优化后的环境变量配置
    cat > .env.optimized << 'EOF'
# QMS-AI优化配置
NODE_ENV=production

# 数据库配置 (读写分离)
DB_TYPE=postgresql
DB_HOST=postgres
DB_READ_HOST=postgres
DB_WRITE_HOST=postgres
DB_NAME=qms_config
DB_USER=qms_app
DB_PASSWORD=qms123
DB_PORT=5432

# Redis配置 (主从)
REDIS_ENABLED=true
REDIS_MASTER_HOST=redis-master
REDIS_SLAVE_HOST=redis-slave
REDIS_SENTINEL_HOST=redis-sentinel
REDIS_SENTINEL_PORT=26379
REDIS_PORT=6379

# 配置中心集群
CONFIG_CENTER_URLS=http://config-lb:80
CONFIG_CENTER_ENABLED=true

# 缓存配置
CACHE_ENABLED=true
CACHE_TTL=3600
CACHE_MAX_SIZE=1000
CACHE_LEVELS=3

# 性能配置
DB_POOL_MAX_READ=15
DB_POOL_MAX_WRITE=10
DB_POOL_MIN=5
API_RATE_LIMIT=100
API_TIMEOUT=30000

# 监控配置
PROMETHEUS_ENABLED=true
METRICS_PORT=9091
JAEGER_ENABLED=true
JAEGER_ENDPOINT=http://jaeger:14268/api/traces

# 日志配置
LOG_LEVEL=info
LOG_FORMAT=json
LOKI_ENABLED=true
LOKI_URL=http://loki:3100

# 安全配置
JWT_SECRET=qms_ai_jwt_secret_2025
API_KEY_ENCRYPTION=true
RATE_LIMIT_ENABLED=true
EOF

    # 备份原配置并应用新配置
    if [ -f ".env" ]; then
        cp .env .env.backup.$(date +%Y%m%d-%H%M%S)
        log_info "原配置已备份"
    fi
    
    cp .env.optimized .env
    log_success "应用配置更新完成"
}

# 验证部署
verify_deployment() {
    log_step "验证部署状态..."
    
    echo ""
    echo "🔍 服务状态检查："
    echo "===================="
    
    # 检查配置中心集群
    if curl -s http://localhost:8080/health > /dev/null; then
        echo -e "✅ 配置中心负载均衡: ${GREEN}正常${NC}"
    else
        echo -e "❌ 配置中心负载均衡: ${RED}异常${NC}"
    fi
    
    # 检查各个配置节点
    for port in 8081 8082 8083; do
        if curl -s http://localhost:$port/health > /dev/null; then
            echo -e "✅ 配置节点:$port: ${GREEN}正常${NC}"
        else
            echo -e "❌ 配置节点:$port: ${RED}异常${NC}"
        fi
    done
    
    # 检查数据库
    if docker exec -it qms-postgres pg_isready -U qms_app -d qms_config > /dev/null 2>&1; then
        echo -e "✅ PostgreSQL数据库: ${GREEN}正常${NC}"
    else
        echo -e "❌ PostgreSQL数据库: ${RED}异常${NC}"
    fi
    
    # 检查Redis
    if docker exec -it qms-redis-master redis-cli ping > /dev/null 2>&1; then
        echo -e "✅ Redis主节点: ${GREEN}正常${NC}"
    else
        echo -e "❌ Redis主节点: ${RED}异常${NC}"
    fi
    
    if docker exec -it qms-redis-slave redis-cli ping > /dev/null 2>&1; then
        echo -e "✅ Redis从节点: ${GREEN}正常${NC}"
    else
        echo -e "❌ Redis从节点: ${RED}异常${NC}"
    fi
    
    # 检查监控系统
    if curl -s http://localhost:9090/-/healthy > /dev/null; then
        echo -e "✅ Prometheus监控: ${GREEN}正常${NC}"
    else
        echo -e "❌ Prometheus监控: ${RED}异常${NC}"
    fi
    
    if curl -s http://localhost:3001/api/health > /dev/null; then
        echo -e "✅ Grafana面板: ${GREEN}正常${NC}"
    else
        echo -e "❌ Grafana面板: ${RED}异常${NC}"
    fi
    
    echo ""
}

# 显示访问信息
show_access_info() {
    log_step "部署完成！访问信息："
    
    echo ""
    echo -e "${CYAN}📊 服务访问地址：${NC}"
    echo "===================="
    echo -e "🔧 配置中心集群: ${YELLOW}http://localhost:8080${NC}"
    echo -e "📈 Prometheus监控: ${YELLOW}http://localhost:9090${NC}"
    echo -e "📊 Grafana面板: ${YELLOW}http://localhost:3001${NC} (admin/admin123)"
    echo -e "🚨 AlertManager: ${YELLOW}http://localhost:9093${NC}"
    echo -e "🔍 Jaeger链路追踪: ${YELLOW}http://localhost:16686${NC}"
    echo -e "📋 cAdvisor容器监控: ${YELLOW}http://localhost:8080${NC}"
    echo ""
    echo -e "${CYAN}🔧 配置节点直接访问：${NC}"
    echo "===================="
    echo -e "主节点: ${YELLOW}http://localhost:8081${NC}"
    echo -e "从节点1: ${YELLOW}http://localhost:8082${NC}"
    echo -e "从节点2: ${YELLOW}http://localhost:8083${NC}"
    echo ""
    echo -e "${CYAN}📋 下一步操作：${NC}"
    echo "===================="
    echo "1. 检查所有服务运行状态"
    echo "2. 配置Grafana监控面板"
    echo "3. 设置AlertManager告警通知"
    echo "4. 更新应用代码以使用新的优化配置"
    echo "5. 运行性能测试验证优化效果"
    echo ""
}

# 主函数
main() {
    show_banner
    
    # 检查是否以root权限运行
    if [[ $EUID -eq 0 ]]; then
        log_warning "建议不要以root权限运行此脚本"
    fi
    
    # 执行部署步骤
    check_dependencies
    create_directories
    backup_existing_config
    create_network
    stop_existing_services
    
    log_info "开始部署优化组件..."
    
    deploy_config_cluster
    deploy_monitoring
    update_app_config
    
    # 等待所有服务完全启动
    log_info "等待所有服务完全启动..."
    sleep 60
    
    verify_deployment
    show_access_info
    
    echo ""
    echo -e "${GREEN}🎉 QMS-AI系统优化部署完成！${NC}"
    echo ""
}

# 执行主函数
main "$@"
