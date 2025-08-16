#!/bin/bash

# QMS-AI优先优化实施脚本
# 版本: 1.0
# 作者: QMS-AI团队
# 日期: 2025-08-06

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# 检查依赖
check_dependencies() {
    log_info "检查系统依赖..."
    
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
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装，请先安装Node.js"
        exit 1
    fi
    
    log_success "依赖检查完成"
}

# 备份当前配置
backup_current_config() {
    log_info "备份当前配置..."
    
    BACKUP_DIR="backup-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$BACKUP_DIR"
    
    # 备份重要配置文件
    if [ -f "docker-compose.yml" ]; then
        cp docker-compose.yml "$BACKUP_DIR/"
    fi
    
    if [ -f ".env" ]; then
        cp .env "$BACKUP_DIR/"
    fi
    
    if [ -d "backend/nodejs/config" ]; then
        cp -r backend/nodejs/config "$BACKUP_DIR/"
    fi
    
    log_success "配置已备份到 $BACKUP_DIR"
}

# 1. 部署配置中心集群
deploy_config_cluster() {
    log_info "部署配置中心集群..."
    
    # 创建配置中心集群配置
    cat > docker-compose.config-cluster.yml << 'EOF'
version: '3.8'
services:
  config-primary:
    build: ./backend/配置端
    ports:
      - "8081:8081"
    environment:
      - NODE_ROLE=primary
      - CLUSTER_NODES=config-secondary:8081,config-tertiary:8081
      - DB_HOST=postgres
      - REDIS_HOST=redis
    volumes:
      - config-data-1:/app/data
    depends_on:
      - postgres
      - redis
    networks:
      - qms-network

  config-secondary:
    build: ./backend/配置端
    ports:
      - "8082:8081"
    environment:
      - NODE_ROLE=secondary
      - CLUSTER_NODES=config-primary:8081,config-tertiary:8081
      - DB_HOST=postgres
      - REDIS_HOST=redis
    volumes:
      - config-data-2:/app/data
    depends_on:
      - postgres
      - redis
    networks:
      - qms-network

  config-tertiary:
    build: ./backend/配置端
    ports:
      - "8083:8081"
    environment:
      - NODE_ROLE=tertiary
      - CLUSTER_NODES=config-primary:8081,config-secondary:8081
      - DB_HOST=postgres
      - REDIS_HOST=redis
    volumes:
      - config-data-3:/app/data
    depends_on:
      - postgres
      - redis
    networks:
      - qms-network

  # 配置中心负载均衡
  config-lb:
    image: nginx:alpine
    ports:
      - "8080:80"
    volumes:
      - ./config/nginx-config-lb.conf:/etc/nginx/nginx.conf
    depends_on:
      - config-primary
      - config-secondary
      - config-tertiary
    networks:
      - qms-network

volumes:
  config-data-1:
  config-data-2:
  config-data-3:

networks:
  qms-network:
    external: true
EOF

    # 创建Nginx负载均衡配置
    mkdir -p config
    cat > config/nginx-config-lb.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    upstream config_cluster {
        server config-primary:8081 weight=3;
        server config-secondary:8081 weight=2;
        server config-tertiary:8081 weight=1;
    }

    server {
        listen 80;
        
        location / {
            proxy_pass http://config_cluster;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            
            # 健康检查
            proxy_next_upstream error timeout invalid_header http_500 http_502 http_503;
            proxy_connect_timeout 2s;
            proxy_send_timeout 2s;
            proxy_read_timeout 2s;
        }
        
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
EOF

    # 启动配置中心集群
    docker-compose -f docker-compose.config-cluster.yml up -d
    
    log_success "配置中心集群部署完成"
}

# 2. 优化数据库配置
optimize_database() {
    log_info "优化数据库配置..."
    
    # 创建优化的PostgreSQL配置
    cat > config/postgresql.conf << 'EOF'
# 连接设置
max_connections = 200
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
maintenance_work_mem = 64MB

# 性能优化
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100

# 日志设置
log_statement = 'mod'
log_min_duration_statement = 1000
log_checkpoints = on
log_connections = on
log_disconnections = on

# 自动清理
autovacuum = on
autovacuum_max_workers = 3
autovacuum_naptime = 1min
EOF

    # 更新数据库Docker配置
    cat >> docker-compose.yml << 'EOF'

  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: qms
      POSTGRES_USER: qms_app
      POSTGRES_PASSWORD: qms123
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./config/postgresql.conf:/etc/postgresql/postgresql.conf
    command: postgres -c config_file=/etc/postgresql/postgresql.conf
    ports:
      - "5432:5432"
    networks:
      - qms-network
EOF

    log_success "数据库配置优化完成"
}

# 3. 部署Redis集群
deploy_redis_cluster() {
    log_info "部署Redis集群..."
    
    # 创建Redis配置
    mkdir -p config/redis
    
    # 主Redis配置
    cat > config/redis/redis-master.conf << 'EOF'
port 6379
bind 0.0.0.0
protected-mode no
save 900 1
save 300 10
save 60 10000
rdbcompression yes
dbfilename dump.rdb
dir /data
maxmemory 512mb
maxmemory-policy allkeys-lru
EOF

    # 从Redis配置
    cat > config/redis/redis-slave.conf << 'EOF'
port 6379
bind 0.0.0.0
protected-mode no
replicaof redis-master 6379
save 900 1
save 300 10
save 60 10000
rdbcompression yes
dbfilename dump.rdb
dir /data
maxmemory 256mb
maxmemory-policy allkeys-lru
EOF

    # 添加Redis集群到docker-compose
    cat >> docker-compose.yml << 'EOF'

  redis-master:
    image: redis:6-alpine
    volumes:
      - ./config/redis/redis-master.conf:/usr/local/etc/redis/redis.conf
      - redis_master_data:/data
    command: redis-server /usr/local/etc/redis/redis.conf
    ports:
      - "6379:6379"
    networks:
      - qms-network

  redis-slave:
    image: redis:6-alpine
    volumes:
      - ./config/redis/redis-slave.conf:/usr/local/etc/redis/redis.conf
      - redis_slave_data:/data
    command: redis-server /usr/local/etc/redis/redis.conf
    depends_on:
      - redis-master
    ports:
      - "6380:6379"
    networks:
      - qms-network

  redis-sentinel:
    image: redis:6-alpine
    command: redis-sentinel /usr/local/etc/redis/sentinel.conf
    volumes:
      - ./config/redis/sentinel.conf:/usr/local/etc/redis/sentinel.conf
    depends_on:
      - redis-master
      - redis-slave
    ports:
      - "26379:26379"
    networks:
      - qms-network
EOF

    # 创建Sentinel配置
    cat > config/redis/sentinel.conf << 'EOF'
port 26379
sentinel monitor mymaster redis-master 6379 1
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 10000
sentinel parallel-syncs mymaster 1
EOF

    log_success "Redis集群配置完成"
}

# 4. 部署监控系统
deploy_monitoring() {
    log_info "部署监控系统..."
    
    # 创建Prometheus配置
    mkdir -p config/prometheus
    cat > config/prometheus/prometheus.yml << 'EOF'
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'qms-api'
    static_configs:
      - targets: ['nodejs-api:3000']
  
  - job_name: 'config-center'
    static_configs:
      - targets: ['config-lb:80']
  
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres:5432']
  
  - job_name: 'redis'
    static_configs:
      - targets: ['redis-master:6379']
EOF

    # 添加监控服务到docker-compose
    cat >> docker-compose.yml << 'EOF'

  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./config/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
    networks:
      - qms-network

  grafana:
    image: grafana/grafana
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    volumes:
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - qms-network
EOF

    log_success "监控系统配置完成"
}

# 5. 更新应用配置
update_app_config() {
    log_info "更新应用配置..."
    
    # 更新Node.js应用的环境变量
    cat > .env.optimized << 'EOF'
# 数据库配置 (读写分离)
DB_READ_HOST=postgres
DB_WRITE_HOST=postgres
DB_NAME=qms
DB_USER=qms_app
DB_PASSWORD=qms123

# Redis配置 (主从)
REDIS_MASTER_HOST=redis-master
REDIS_SLAVE_HOST=redis-slave
REDIS_SENTINEL_HOST=redis-sentinel
REDIS_SENTINEL_PORT=26379

# 配置中心集群
CONFIG_CENTER_URLS=http://config-lb:80

# 缓存配置
CACHE_ENABLED=true
CACHE_TTL=3600
CACHE_MAX_SIZE=1000

# 性能配置
DB_POOL_MAX=20
DB_POOL_MIN=5
API_RATE_LIMIT=100

# 监控配置
PROMETHEUS_ENABLED=true
METRICS_PORT=9091
EOF

    # 备份原配置并应用新配置
    if [ -f ".env" ]; then
        cp .env .env.backup
    fi
    cp .env.optimized .env
    
    log_success "应用配置更新完成"
}

# 6. 启动优化后的系统
start_optimized_system() {
    log_info "启动优化后的系统..."
    
    # 创建网络
    docker network create qms-network 2>/dev/null || true
    
    # 停止现有服务
    docker-compose down 2>/dev/null || true
    
    # 启动所有服务
    docker-compose up -d
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30
    
    # 检查服务状态
    log_info "检查服务状态..."
    docker-compose ps
    
    log_success "优化后的系统启动完成"
}

# 7. 验证优化效果
verify_optimization() {
    log_info "验证优化效果..."
    
    # 检查配置中心集群
    echo "检查配置中心集群..."
    curl -s http://localhost:8080/health && echo " ✓ 配置中心负载均衡正常" || echo " ✗ 配置中心负载均衡异常"
    
    # 检查数据库连接
    echo "检查数据库连接..."
    docker exec -it $(docker-compose ps -q postgres) pg_isready -U qms_app -d qms && echo " ✓ 数据库连接正常" || echo " ✗ 数据库连接异常"
    
    # 检查Redis集群
    echo "检查Redis集群..."
    docker exec -it $(docker-compose ps -q redis-master) redis-cli ping && echo " ✓ Redis主节点正常" || echo " ✗ Redis主节点异常"
    
    # 检查监控系统
    echo "检查监控系统..."
    curl -s http://localhost:9090/-/healthy && echo " ✓ Prometheus正常" || echo " ✗ Prometheus异常"
    curl -s http://localhost:3001/api/health && echo " ✓ Grafana正常" || echo " ✗ Grafana异常"
    
    log_success "优化验证完成"
}

# 主函数
main() {
    echo "=========================================="
    echo "🚀 QMS-AI优先优化实施脚本"
    echo "=========================================="
    
    check_dependencies
    backup_current_config
    
    echo ""
    echo "开始实施优化..."
    echo ""
    
    deploy_config_cluster
    optimize_database
    deploy_redis_cluster
    deploy_monitoring
    update_app_config
    start_optimized_system
    verify_optimization
    
    echo ""
    echo "=========================================="
    echo "🎉 优化实施完成！"
    echo "=========================================="
    echo ""
    echo "📊 访问地址："
    echo "  - 配置中心: http://localhost:8080"
    echo "  - 监控面板: http://localhost:9090"
    echo "  - Grafana: http://localhost:3001 (admin/admin123)"
    echo "  - 应用端: http://localhost:8081"
    echo ""
    echo "📋 下一步："
    echo "  1. 检查所有服务运行状态"
    echo "  2. 配置Grafana监控面板"
    echo "  3. 进行性能测试验证"
    echo "  4. 更新应用代码以使用新的优化配置"
    echo ""
}

# 执行主函数
main "$@"
