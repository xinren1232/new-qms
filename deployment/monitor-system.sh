#!/bin/bash

# QMS-AI系统监控脚本
# 监控内存使用、服务状态、性能指标

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

PROJECT_DIR="/opt/qms"

# 显示横幅
show_banner() {
    echo -e "${PURPLE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                    QMS-AI系统监控面板                        ║"
    echo "║                   实时性能 + 资源监控                        ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# 获取系统信息
get_system_info() {
    echo -e "${CYAN}📊 系统资源使用情况${NC}"
    echo "=================================="
    
    # CPU使用率
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
    echo -e "CPU使用率: ${GREEN}${CPU_USAGE}%${NC}"
    
    # 内存使用率
    MEMORY_INFO=$(free -m | awk 'NR==2{printf "%.1f/%.1fMB (%.1f%%)", $3,$2,$3*100/$2}')
    MEMORY_PERCENT=$(free | grep Mem | awk '{printf("%.1f", $3/$2 * 100.0)}')
    
    if (( $(echo "$MEMORY_PERCENT > 80" | bc -l) )); then
        echo -e "内存使用: ${RED}${MEMORY_INFO}${NC}"
    elif (( $(echo "$MEMORY_PERCENT > 60" | bc -l) )); then
        echo -e "内存使用: ${YELLOW}${MEMORY_INFO}${NC}"
    else
        echo -e "内存使用: ${GREEN}${MEMORY_INFO}${NC}"
    fi
    
    # 磁盘使用率
    DISK_USAGE=$(df -h / | tail -1 | awk '{print $5}' | cut -d'%' -f1)
    DISK_INFO=$(df -h / | tail -1 | awk '{printf "%s/%s (%s)", $3,$2,$5}')
    
    if [ $DISK_USAGE -gt 80 ]; then
        echo -e "磁盘使用: ${RED}${DISK_INFO}${NC}"
    elif [ $DISK_USAGE -gt 60 ]; then
        echo -e "磁盘使用: ${YELLOW}${DISK_INFO}${NC}"
    else
        echo -e "磁盘使用: ${GREEN}${DISK_INFO}${NC}"
    fi
    
    # 负载平均值
    LOAD_AVG=$(uptime | awk -F'load average:' '{print $2}')
    echo -e "负载平均: ${BLUE}${LOAD_AVG}${NC}"
    
    echo ""
}

# 检查Docker服务状态
check_docker_services() {
    echo -e "${CYAN}🐳 Docker服务状态${NC}"
    echo "=================================="
    
    if [ -d "$PROJECT_DIR" ]; then
        cd $PROJECT_DIR
        
        if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
            # 获取服务状态
            SERVICES=$(docker-compose -f deployment/aliyun-deploy-optimized.yml ps --format "table {{.Name}}\t{{.State}}\t{{.Ports}}" 2>/dev/null)
            
            if [ $? -eq 0 ]; then
                echo "$SERVICES"
            else
                echo -e "${RED}无法获取Docker服务状态${NC}"
            fi
        else
            echo -e "${YELLOW}未找到Docker Compose配置文件${NC}"
        fi
    else
        echo -e "${RED}项目目录不存在: $PROJECT_DIR${NC}"
    fi
    
    echo ""
}

# 检查容器资源使用
check_container_resources() {
    echo -e "${CYAN}📈 容器资源使用情况${NC}"
    echo "=================================="
    
    # 检查是否有运行的容器
    RUNNING_CONTAINERS=$(docker ps --format "table {{.Names}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>/dev/null | grep qms)
    
    if [ -n "$RUNNING_CONTAINERS" ]; then
        echo "容器名称                CPU使用率    内存使用"
        echo "------------------------------------------------"
        echo "$RUNNING_CONTAINERS"
    else
        echo -e "${YELLOW}没有运行中的QMS容器${NC}"
    fi
    
    echo ""
}

# 检查服务健康状态
check_service_health() {
    echo -e "${CYAN}🏥 服务健康检查${NC}"
    echo "=================================="
    
    # 定义服务列表
    declare -A services=(
        ["认证服务"]="8084"
        ["配置中心"]="8083"
        ["聊天服务"]="3004"
        ["前端应用"]="8081"
        ["监控面板"]="3000"
    )
    
    for service_name in "${!services[@]}"; do
        port=${services[$service_name]}
        
        # 检查端口是否开放
        if netstat -tuln 2>/dev/null | grep -q ":$port "; then
            # 尝试HTTP健康检查
            if curl -f -s "http://localhost:$port/health" > /dev/null 2>&1; then
                echo -e "${service_name}: ${GREEN}✅ 健康${NC}"
            elif curl -f -s "http://localhost:$port" > /dev/null 2>&1; then
                echo -e "${service_name}: ${GREEN}✅ 运行中${NC}"
            else
                echo -e "${service_name}: ${YELLOW}⚠️ 端口开放但服务异常${NC}"
            fi
        else
            echo -e "${service_name}: ${RED}❌ 离线${NC}"
        fi
    done
    
    echo ""
}

# 检查网络连接
check_network() {
    echo -e "${CYAN}🌐 网络连接状态${NC}"
    echo "=================================="
    
    # 检查关键端口
    echo "监听端口:"
    netstat -tuln 2>/dev/null | grep -E ':(80|443|3000|3004|6379|8081|8083|8084|9090)' | while read line; do
        port=$(echo $line | awk '{print $4}' | cut -d':' -f2)
        protocol=$(echo $line | awk '{print $1}')
        echo -e "  ${GREEN}$protocol $port${NC}"
    done
    
    # 检查外网连接
    echo ""
    echo "外网连接测试:"
    if ping -c 1 8.8.8.8 > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ 外网连接正常${NC}"
    else
        echo -e "  ${RED}❌ 外网连接异常${NC}"
    fi
    
    echo ""
}

# 显示最近日志
show_recent_logs() {
    echo -e "${CYAN}📝 最近系统日志${NC}"
    echo "=================================="
    
    # 显示系统日志
    echo "系统日志 (最近5条):"
    tail -n 5 /var/log/messages 2>/dev/null || tail -n 5 /var/log/syslog 2>/dev/null || echo "无法访问系统日志"
    
    echo ""
    
    # 显示Docker日志
    if [ -d "$PROJECT_DIR" ]; then
        echo "QMS应用日志 (最近5条):"
        cd $PROJECT_DIR
        if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
            docker-compose -f deployment/aliyun-deploy-optimized.yml logs --tail=5 2>/dev/null || echo "无法获取应用日志"
        fi
    fi
    
    echo ""
}

# 性能建议
show_performance_tips() {
    echo -e "${CYAN}💡 性能优化建议${NC}"
    echo "=================================="
    
    # 内存使用建议
    MEMORY_PERCENT=$(free | grep Mem | awk '{printf("%.1f", $3/$2 * 100.0)}')
    if (( $(echo "$MEMORY_PERCENT > 80" | bc -l) )); then
        echo -e "${RED}⚠️ 内存使用率过高 (${MEMORY_PERCENT}%)${NC}"
        echo "  建议: 重启高内存使用的容器或升级服务器配置"
    fi
    
    # 磁盘使用建议
    DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | cut -d'%' -f1)
    if [ $DISK_USAGE -gt 80 ]; then
        echo -e "${RED}⚠️ 磁盘使用率过高 (${DISK_USAGE}%)${NC}"
        echo "  建议: 清理日志文件或扩展磁盘空间"
    fi
    
    # CPU使用建议
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
    if (( $(echo "$CPU_USAGE > 80" | bc -l) )); then
        echo -e "${RED}⚠️ CPU使用率过高 (${CPU_USAGE}%)${NC}"
        echo "  建议: 检查是否有异常进程或考虑升级CPU"
    fi
    
    # 如果一切正常
    if (( $(echo "$MEMORY_PERCENT < 80" | bc -l) )) && [ $DISK_USAGE -lt 80 ] && (( $(echo "$CPU_USAGE < 80" | bc -l) )); then
        echo -e "${GREEN}✅ 系统性能良好，无需特别优化${NC}"
    fi
    
    echo ""
}

# 快捷操作菜单
show_quick_actions() {
    echo -e "${CYAN}🔧 快捷操作${NC}"
    echo "=================================="
    echo "1. 重启所有服务: docker-compose -f $PROJECT_DIR/deployment/aliyun-deploy-optimized.yml restart"
    echo "2. 查看实时日志: docker-compose -f $PROJECT_DIR/deployment/aliyun-deploy-optimized.yml logs -f"
    echo "3. 清理Docker: docker system prune -f"
    echo "4. 重新部署: cd $PROJECT_DIR && ./deployment/aliyun-deploy.sh"
    echo ""
}

# 主函数
main() {
    clear
    show_banner
    get_system_info
    check_docker_services
    check_container_resources
    check_service_health
    check_network
    show_recent_logs
    show_performance_tips
    show_quick_actions
    
    echo -e "${GREEN}监控完成！${NC}"
    echo -e "${YELLOW}提示: 运行 'watch -n 30 $0' 可以每30秒自动刷新监控信息${NC}"
}

# 检查bc命令是否存在
if ! command -v bc &> /dev/null; then
    echo "安装bc计算器..."
    if command -v yum &> /dev/null; then
        yum install -y bc
    elif command -v apt &> /dev/null; then
        apt install -y bc
    fi
fi

# 执行主函数
main "$@"
