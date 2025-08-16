# QMS-AI 本地开发环境服务启动脚本
# 创建日期: 2025-01-23
# 用途: 一键启动本地开发环境所需的所有服务

Write-Host "========================================" -ForegroundColor Green
Write-Host "QMS-AI 本地开发环境服务启动脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 检查管理员权限
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "❌ 需要管理员权限来管理Windows服务" -ForegroundColor Red
    Write-Host "请以管理员身份运行PowerShell" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "`n🔍 检查服务状态..." -ForegroundColor Cyan

# 1. 检查并启动Redis (WSL2)
Write-Host "`n📦 检查Redis服务 (WSL2)..." -ForegroundColor Yellow
try {
    $redisStatus = wsl redis-cli ping 2>$null
    if ($redisStatus -eq "PONG") {
        Write-Host "✅ Redis已运行" -ForegroundColor Green
    } else {
        Write-Host "🔄 启动Redis服务..." -ForegroundColor Yellow
        wsl sudo systemctl start redis-server
        Start-Sleep -Seconds 3
        $redisStatus = wsl redis-cli ping 2>$null
        if ($redisStatus -eq "PONG") {
            Write-Host "✅ Redis启动成功" -ForegroundColor Green
        } else {
            Write-Host "❌ Redis启动失败" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "❌ WSL2或Redis未正确安装" -ForegroundColor Red
}

# 2. 检查并启动RabbitMQ (Windows)
Write-Host "`n📦 检查RabbitMQ服务 (Windows)..." -ForegroundColor Yellow
try {
    $rabbitmqService = Get-Service -Name "RabbitMQ" -ErrorAction SilentlyContinue
    if ($rabbitmqService) {
        if ($rabbitmqService.Status -eq "Running") {
            Write-Host "✅ RabbitMQ已运行" -ForegroundColor Green
        } else {
            Write-Host "🔄 启动RabbitMQ服务..." -ForegroundColor Yellow
            Start-Service -Name "RabbitMQ"
            Start-Sleep -Seconds 5
            $rabbitmqService = Get-Service -Name "RabbitMQ"
            if ($rabbitmqService.Status -eq "Running") {
                Write-Host "✅ RabbitMQ启动成功" -ForegroundColor Green
            } else {
                Write-Host "❌ RabbitMQ启动失败" -ForegroundColor Red
            }
        }
    } else {
        Write-Host "❌ RabbitMQ服务未找到" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ RabbitMQ服务管理失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 服务连接测试
Write-Host "`n🧪 服务连接测试..." -ForegroundColor Cyan

# Redis连接测试
Write-Host "测试Redis连接..." -ForegroundColor Yellow
try {
    $redisTest = wsl redis-cli ping 2>$null
    if ($redisTest -eq "PONG") {
        Write-Host "✅ Redis连接正常 (localhost:6379)" -ForegroundColor Green
    } else {
        Write-Host "❌ Redis连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Redis连接测试失败" -ForegroundColor Red
}

# RabbitMQ端口测试
Write-Host "测试RabbitMQ端口..." -ForegroundColor Yellow
$rabbitmqPorts = @(5672, 15672)
foreach ($port in $rabbitmqPorts) {
    $portTest = netstat -an | Select-String ":$port.*LISTENING"
    if ($portTest) {
        Write-Host "✅ RabbitMQ端口 $port 正常监听" -ForegroundColor Green
    } else {
        Write-Host "❌ RabbitMQ端口 $port 未监听" -ForegroundColor Red
    }
}

# 4. 显示服务信息
Write-Host "`n📋 服务信息摘要:" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "Redis (WSL2):" -ForegroundColor White
Write-Host "  地址: localhost:6379" -ForegroundColor Gray
Write-Host "  测试: wsl redis-cli ping" -ForegroundColor Gray
Write-Host ""
Write-Host "RabbitMQ (Windows):" -ForegroundColor White
Write-Host "  AMQP: localhost:5672" -ForegroundColor Gray
Write-Host "  管理界面: http://localhost:15672" -ForegroundColor Gray
Write-Host "  用户: qms_ai_user / QmsAi@2025!" -ForegroundColor Gray
Write-Host "  虚拟主机: /qms-ai-loc" -ForegroundColor Gray
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray

Write-Host "`n🎉 服务启动脚本执行完成!" -ForegroundColor Green
Write-Host "现在可以启动Spring Boot应用进行测试" -ForegroundColor Yellow

# 询问是否打开RabbitMQ管理界面
$openWeb = Read-Host "`n是否打开RabbitMQ管理界面? (y/n)"
if ($openWeb -eq "y" -or $openWeb -eq "Y") {
    Start-Process "http://localhost:15672"
    Write-Host "✅ 已打开RabbitMQ管理界面" -ForegroundColor Green
}

Write-Host "`n按任意键退出..." -ForegroundColor Gray
pause
