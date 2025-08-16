# QMS-AI 本地开发环境服务状态检查脚本
# 创建日期: 2025-01-23
# 用途: 检查所有服务的运行状态和配置

Write-Host "========================================" -ForegroundColor Green
Write-Host "QMS-AI 服务状态检查" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 1. Redis状态检查
Write-Host "`n📦 Redis 状态检查 (WSL2)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

try {
    # 检查WSL2状态
    $wslStatus = wsl --list --verbose | Select-String "Ubuntu.*Running"
    if ($wslStatus) {
        Write-Host "✅ WSL2 Ubuntu 运行中" -ForegroundColor Green
    } else {
        Write-Host "❌ WSL2 Ubuntu 未运行" -ForegroundColor Red
    }

    # 检查Redis连接
    $redisTest = wsl redis-cli ping 2>$null
    if ($redisTest -eq "PONG") {
        Write-Host "✅ Redis 服务正常" -ForegroundColor Green
        
        # 获取Redis详细信息
        $redisInfo = wsl redis-cli info server | Select-String "redis_version|tcp_port|uptime_in_seconds"
        Write-Host "Redis 详细信息:" -ForegroundColor Yellow
        $redisInfo | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
    } else {
        Write-Host "❌ Redis 连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Redis 检查失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. RabbitMQ状态检查
Write-Host "`n📦 RabbitMQ 状态检查 (Windows)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

try {
    # 检查Windows服务
    $rabbitmqService = Get-Service -Name "RabbitMQ" -ErrorAction SilentlyContinue
    if ($rabbitmqService) {
        if ($rabbitmqService.Status -eq "Running") {
            Write-Host "✅ RabbitMQ Windows服务运行中" -ForegroundColor Green
        } else {
            Write-Host "❌ RabbitMQ Windows服务未运行 (状态: $($rabbitmqService.Status))" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ RabbitMQ Windows服务未找到" -ForegroundColor Red
    }

    # 检查端口监听
    $amqpPort = netstat -an | Select-String ":5672.*LISTENING"
    $webPort = netstat -an | Select-String ":15672.*LISTENING"
    
    if ($amqpPort) {
        Write-Host "✅ AMQP端口 5672 正常监听" -ForegroundColor Green
    } else {
        Write-Host "❌ AMQP端口 5672 未监听" -ForegroundColor Red
    }
    
    if ($webPort) {
        Write-Host "✅ Web管理端口 15672 正常监听" -ForegroundColor Green
    } else {
        Write-Host "❌ Web管理端口 15672 未监听" -ForegroundColor Red
    }

} catch {
    Write-Host "❌ RabbitMQ 检查失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 配置文件检查
Write-Host "`n📋 配置文件检查" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

$configFile = "backend\配置中心\transcend-plm-configcenter-provider\src\main\resources\config\application-loc.properties"
if (Test-Path $configFile) {
    Write-Host "✅ 配置文件存在: $configFile" -ForegroundColor Green
    
    # 检查关键配置项
    $configContent = Get-Content $configFile
    $redisHost = $configContent | Select-String "spring.redis.host"
    $rabbitmqHost = $configContent | Select-String "spring.rabbitmq.first.host"
    $rabbitmqUser = $configContent | Select-String "spring.rabbitmq.first.username"
    $rabbitmqVhost = $configContent | Select-String "spring.rabbitmq.first.virtual-host"
    
    Write-Host "关键配置项:" -ForegroundColor Yellow
    if ($redisHost) { Write-Host "  $redisHost" -ForegroundColor Gray }
    if ($rabbitmqHost) { Write-Host "  $rabbitmqHost" -ForegroundColor Gray }
    if ($rabbitmqUser) { Write-Host "  $rabbitmqUser" -ForegroundColor Gray }
    if ($rabbitmqVhost) { Write-Host "  $rabbitmqVhost" -ForegroundColor Gray }
} else {
    Write-Host "❌ 配置文件不存在: $configFile" -ForegroundColor Red
}

# 4. 网络连通性测试
Write-Host "`n🌐 网络连通性测试" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

# 测试Redis连接
Write-Host "测试Redis连接 (localhost:6379)..." -ForegroundColor Yellow
try {
    $redisTest = wsl redis-cli -h localhost -p 6379 ping 2>$null
    if ($redisTest -eq "PONG") {
        Write-Host "✅ Redis连接测试通过" -ForegroundColor Green
    } else {
        Write-Host "❌ Redis连接测试失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Redis连接测试异常" -ForegroundColor Red
}

# 测试RabbitMQ Web界面
Write-Host "测试RabbitMQ Web界面 (localhost:15672)..." -ForegroundColor Yellow
try {
    $webTest = Test-NetConnection -ComputerName localhost -Port 15672 -WarningAction SilentlyContinue
    if ($webTest.TcpTestSucceeded) {
        Write-Host "✅ RabbitMQ Web界面连接正常" -ForegroundColor Green
    } else {
        Write-Host "❌ RabbitMQ Web界面连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ RabbitMQ Web界面测试异常" -ForegroundColor Red
}

# 5. 总结报告
Write-Host "`n📊 状态总结" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

$currentTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Write-Host "检查时间: $currentTime" -ForegroundColor Gray
Write-Host ""
Write-Host "服务状态:" -ForegroundColor White
Write-Host "  Redis (WSL2):     $(if ($redisTest -eq 'PONG') { '🟢 正常' } else { '🔴 异常' })" -ForegroundColor Gray
Write-Host "  RabbitMQ (Win):   $(if ($rabbitmqService -and $rabbitmqService.Status -eq 'Running') { '🟢 正常' } else { '🔴 异常' })" -ForegroundColor Gray
Write-Host ""
Write-Host "网络连接:" -ForegroundColor White
Write-Host "  Redis:6379        $(if ($redisTest -eq 'PONG') { '🟢 可达' } else { '🔴 不可达' })" -ForegroundColor Gray
Write-Host "  RabbitMQ:5672     $(if ($amqpPort) { '🟢 监听' } else { '🔴 未监听' })" -ForegroundColor Gray
Write-Host "  RabbitMQ:15672    $(if ($webPort) { '🟢 监听' } else { '🔴 未监听' })" -ForegroundColor Gray

Write-Host "`n🎯 下一步建议:" -ForegroundColor Yellow
if ($redisTest -ne "PONG") {
    Write-Host "  • 启动Redis: wsl sudo systemctl start redis-server" -ForegroundColor Gray
}
if (-not ($rabbitmqService -and $rabbitmqService.Status -eq "Running")) {
    Write-Host "  • 启动RabbitMQ: Start-Service RabbitMQ" -ForegroundColor Gray
}
if ($redisTest -eq "PONG" -and $rabbitmqService -and $rabbitmqService.Status -eq "Running") {
    Write-Host "  • 所有服务正常，可以启动Spring Boot应用" -ForegroundColor Green
}

Write-Host "`n按任意键退出..." -ForegroundColor Gray
pause
