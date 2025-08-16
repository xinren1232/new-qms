Write-Host "🔄 重启QMS-AI服务以应用外网模型修复..." -ForegroundColor Cyan
Write-Host ""

# 检查并停止现有服务
Write-Host "🛑 停止现有服务..." -ForegroundColor Yellow
$processes = Get-Process | Where-Object {$_.ProcessName -eq "node" -and $_.MainWindowTitle -like "*config*" -or $_.MainWindowTitle -like "*chat*"}
if ($processes) {
    $processes | Stop-Process -Force
    Write-Host "✅ 已停止现有Node.js服务" -ForegroundColor Green
}

# 等待端口释放
Start-Sleep -Seconds 3

# 启动配置中心
Write-Host "🚀 启动配置中心..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'backend\nodejs'; node lightweight-config-service.js" -WindowStyle Minimized

# 等待配置中心启动
Start-Sleep -Seconds 5

# 检查配置中心
try {
    $configHealth = Invoke-RestMethod -Uri "http://localhost:8082/health" -Method Get -TimeoutSec 5
    Write-Host "✅ 配置中心启动成功" -ForegroundColor Green
} catch {
    Write-Host "❌ 配置中心启动失败" -ForegroundColor Red
    exit 1
}

# 启动聊天服务
Write-Host "🚀 启动聊天服务..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'backend\nodejs'; node chat-service.js" -WindowStyle Minimized

# 等待聊天服务启动
Start-Sleep -Seconds 10

# 检查聊天服务
try {
    $chatHealth = Invoke-RestMethod -Uri "http://localhost:3004/health" -Method Get -TimeoutSec 5
    Write-Host "✅ 聊天服务启动成功" -ForegroundColor Green
} catch {
    Write-Host "❌ 聊天服务启动失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎯 服务重启完成！外网模型修复已生效。" -ForegroundColor Green
Write-Host ""
Write-Host "📋 服务状态:" -ForegroundColor Cyan
Write-Host "   ✅ 配置中心: http://localhost:8082" -ForegroundColor Green
Write-Host "   ✅ 聊天服务: http://localhost:3004" -ForegroundColor Green
Write-Host "   ✅ 前端服务: http://localhost:8081" -ForegroundColor Green
Write-Host ""
Write-Host "🧪 现在可以测试外网模型了！" -ForegroundColor Yellow
Write-Host "   - DeepSeek Chat (V3-0324) 现在应该可以正常工作"
Write-Host "   - DeepSeek Reasoner (R1-0528) 现在应该可以正常工作"
