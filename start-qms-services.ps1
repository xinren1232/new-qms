# QMS-AI Services Startup Script
Write-Host "Starting QMS-AI Services..." -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

# Check Node.js
Write-Host "[1/8] Checking Node.js..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "Node.js version: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "Error: Node.js not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Start Config Service
Write-Host "[2/8] Starting Config Service (port 3003)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'D:\QMS01\backend\nodejs'; node lightweight-config-service.js"
Start-Sleep 3

# Start Chat Service  
Write-Host "[3/8] Starting Chat Service (port 3004)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'D:\QMS01\backend\nodejs'; node chat-service.js"
Start-Sleep 3

# Start Auth Service
Write-Host "[4/8] Starting Auth Service (port 8084)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'D:\QMS01\backend\nodejs'; node auth-service.js"
Start-Sleep 3

# Start API Gateway
Write-Host "[5/8] Starting API Gateway (port 8085)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'D:\QMS01\backend\nodejs'; node api-gateway.js"
Start-Sleep 5

# Start Frontend App
Write-Host "[6/8] Starting Frontend App (port 8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'D:\QMS01\frontend\应用端'; npm run dev"
Start-Sleep 3

# Start Frontend Config
Write-Host "[7/8] Starting Frontend Config (port 8072)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'D:\QMS01\frontend\配置端'; npm run serve"
Start-Sleep 3

# Wait for services
Write-Host "[8/8] Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep 10

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "QMS-AI Services Started!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "Access URLs:" -ForegroundColor Cyan
Write-Host "- App: http://localhost:8081" -ForegroundColor White
Write-Host "- Config: http://localhost:8072" -ForegroundColor White
Write-Host "- Gateway: http://localhost:8085" -ForegroundColor White
Write-Host "- Config Service: http://localhost:3003" -ForegroundColor White
Write-Host "- Chat Service: http://localhost:3004" -ForegroundColor White
Write-Host "- Auth Service: http://localhost:8084" -ForegroundColor White
Write-Host ""

# Check service status
Write-Host "Checking service ports:" -ForegroundColor Yellow
$ports = @(3003, 3004, 8081, 8072, 8084, 8085)
foreach ($port in $ports) {
    $listening = netstat -an | Select-String ":$port "
    if ($listening) {
        Write-Host "Port $port : LISTENING" -ForegroundColor Green
    } else {
        Write-Host "Port $port : NOT LISTENING" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Services startup completed!" -ForegroundColor Green
Read-Host "Press Enter to exit"
