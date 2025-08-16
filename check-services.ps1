# QMS-AI Services Status Check
Write-Host "QMS-AI Services Status Check" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

$services = @(
    @{Name="Config Service"; Port=3003; Url="http://localhost:3003/health"},
    @{Name="Chat Service"; Port=3004; Url="http://localhost:3004/health"},
    @{Name="Auth Service"; Port=8084; Url="http://localhost:8084/health"},
    @{Name="API Gateway"; Port=8085; Url="http://localhost:8085/health"},
    @{Name="Frontend App"; Port=8081; Url="http://localhost:8081"},
    @{Name="Frontend Config"; Port=8072; Url="http://localhost:8072"}
)

Write-Host "Checking port status..." -ForegroundColor Yellow
foreach ($service in $services) {
    $port = $service.Port
    $name = $service.Name
    
    $listening = netstat -an | Select-String ":$port " | Select-String "LISTENING"
    if ($listening) {
        Write-Host "✅ $name (Port $port): RUNNING" -ForegroundColor Green
        
        # Test HTTP endpoint if available
        try {
            $response = Invoke-WebRequest -Uri $service.Url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                Write-Host "   HTTP Status: OK" -ForegroundColor Green
            }
        } catch {
            Write-Host "   HTTP Status: ERROR - $($_.Exception.Message)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ $name (Port $port): NOT RUNNING" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Access URLs:" -ForegroundColor Cyan
Write-Host "- Frontend App: http://localhost:8081" -ForegroundColor White
Write-Host "- Frontend Config: http://localhost:8072" -ForegroundColor White
Write-Host "- API Gateway: http://localhost:8085" -ForegroundColor White
Write-Host "- Config Service: http://localhost:3003" -ForegroundColor White
Write-Host "- Chat Service: http://localhost:3004" -ForegroundColor White
Write-Host "- Auth Service: http://localhost:8084" -ForegroundColor White

Write-Host ""
Write-Host "Service check completed!" -ForegroundColor Green
