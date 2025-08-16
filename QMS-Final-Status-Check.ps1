# QMS-AI 系统状态检查脚本
Write-Host ""
Write-Host "========================================"
Write-Host "    QMS-AI 系统状态检查报告"
Write-Host "========================================"
Write-Host ""

Write-Host "🔍 检查端口监听状态..." -ForegroundColor Cyan
Write-Host ""

$ports = @(
    @{Port=3003; Service="配置中心"},
    @{Port=3004; Service="聊天服务"},
    @{Port=3005; Service="Coze Studio"},
    @{Port=3008; Service="导出服务"},
    @{Port=3009; Service="高级功能"},
    @{Port=8072; Service="配置端"},
    @{Port=8081; Service="应用端"},
    @{Port=8084; Service="认证服务"},
    @{Port=8085; Service="API网关"}
)

foreach ($item in $ports) {
    $listening = netstat -ano | Select-String ":$($item.Port) " | Select-String "LISTENING"
    if ($listening) {
        Write-Host "  ✅ Port $($item.Port) ($($item.Service)): LISTENING" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Port $($item.Port) ($($item.Service)): NOT LISTENING" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🌐 服务访问地址:" -ForegroundColor Yellow
Write-Host "  • 配置中心:     http://localhost:3003"
Write-Host "  • 聊天服务:     http://localhost:3004"
Write-Host "  • Coze Studio:  http://localhost:3005"
Write-Host "  • 导出服务:     http://localhost:3008"
Write-Host "  • 高级功能:     http://localhost:3009"
Write-Host "  • 认证服务:     http://localhost:8084"
Write-Host "  • API网关:      http://localhost:8085"
Write-Host "  • 应用端:       http://localhost:8081"
Write-Host "  • 配置端:       http://localhost:8072"

Write-Host ""
Write-Host "🔧 健康检查:" -ForegroundColor Cyan
Write-Host ""

$healthUrls = @(3003, 3004, 3005, 3008, 3009, 8084, 8085)

foreach ($port in $healthUrls) {
    Write-Host "检查 http://localhost:$port/health..." -NoNewline
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:$port/health" -TimeoutSec 5 -ErrorAction Stop
        Write-Host " ✅ 健康检查通过" -ForegroundColor Green
    } catch {
        Write-Host " ⚠️ 健康检查失败或超时" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "📊 Redis 状态:" -ForegroundColor Cyan

$redis6379 = netstat -ano | Select-String ":6379 " | Select-String "LISTENING"
if ($redis6379) {
    Write-Host "  ✅ Redis (6379): LISTENING" -ForegroundColor Green
} else {
    Write-Host "  ❌ Redis (6379): NOT LISTENING" -ForegroundColor Red
}

$redis6380 = netstat -ano | Select-String ":6380 " | Select-String "LISTENING"
if ($redis6380) {
    Write-Host "  ✅ Redis (6380): LISTENING" -ForegroundColor Green
} else {
    Write-Host "  ❌ Redis (6380): NOT LISTENING" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================"
Write-Host "    检查完成！"
Write-Host "========================================"
Write-Host ""
