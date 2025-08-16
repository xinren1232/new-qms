# QMS-AI 逐步启动服务脚本
Write-Host "QMS-AI 服务逐步启动" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
Write-Host ""

function Test-Port {
    param($Port)
    $listening = netstat -an | Select-String ":$Port " | Select-String "LISTENING"
    return $listening -ne $null
}

function Test-HttpEndpoint {
    param($Url, $TimeoutSec = 5)
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec $TimeoutSec -ErrorAction Stop
        return @{ Success = $true; StatusCode = $response.StatusCode }
    } catch {
        return @{ Success = $false; Message = $_.Exception.Message }
    }
}

function Start-ServiceStep {
    param($Name, $Port, $Directory, $Script, $HealthUrl)
    
    Write-Host "步骤: 启动 $Name" -ForegroundColor Yellow
    Write-Host "目录: $Directory" -ForegroundColor Gray
    Write-Host "脚本: $Script" -ForegroundColor Gray
    Write-Host "端口: $Port" -ForegroundColor Gray
    Write-Host ""
    
    # 检查端口是否已被占用
    if (Test-Port -Port $Port) {
        Write-Host "⚠️ 端口 $Port 已被占用，$Name 可能已在运行" -ForegroundColor Yellow
        
        # 测试健康检查
        if ($HealthUrl) {
            $health = Test-HttpEndpoint -Url $HealthUrl
            if ($health.Success) {
                Write-Host "✅ $Name 健康检查通过 (HTTP $($health.StatusCode))" -ForegroundColor Green
                return $true
            } else {
                Write-Host "❌ $Name 健康检查失败" -ForegroundColor Red
            }
        }
        return $true
    }
    
    # 检查目录是否存在
    $fullPath = Join-Path $PWD $Directory
    if (-not (Test-Path $fullPath)) {
        Write-Host "❌ 目录不存在: $fullPath" -ForegroundColor Red
        return $false
    }
    
    Write-Host "正在启动 $Name..." -ForegroundColor Green
    
    try {
        # 启动服务
        if ($Script.StartsWith("npm")) {
            $cmd = "cd /d `"$fullPath`" && $Script"
            Start-Process -FilePath "cmd" -ArgumentList "/c", $cmd -WindowStyle Normal
        } else {
            Start-Process -FilePath "node" -ArgumentList $Script -WorkingDirectory $fullPath -WindowStyle Normal
        }
        
        Write-Host "等待服务启动..." -ForegroundColor Yellow
        
        # 等待端口监听
        $maxWait = 30
        $waited = 0
        while ($waited -lt $maxWait) {
            Start-Sleep -Seconds 2
            $waited += 2
            
            if (Test-Port -Port $Port) {
                Write-Host "✅ $Name 启动成功！端口 $Port 正在监听" -ForegroundColor Green
                
                # 额外等待2秒让服务完全初始化
                Start-Sleep -Seconds 2
                
                # 测试健康检查
                if ($HealthUrl) {
                    $health = Test-HttpEndpoint -Url $HealthUrl
                    if ($health.Success) {
                        Write-Host "✅ $Name 健康检查通过 (HTTP $($health.StatusCode))" -ForegroundColor Green
                    } else {
                        Write-Host "⚠️ $Name 端口监听但健康检查失败: $($health.Message)" -ForegroundColor Yellow
                    }
                }
                
                return $true
            }
            
            Write-Host "等待中... ($waited/$maxWait 秒)" -ForegroundColor Gray
        }
        
        Write-Host "❌ $Name 启动超时，端口 $Port 未监听" -ForegroundColor Red
        return $false
        
    } catch {
        Write-Host "❌ 启动 $Name 失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

function Wait-UserConfirmation {
    param($Message = "按任意键继续下一步...")
    Write-Host ""
    Write-Host $Message -ForegroundColor Cyan
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Write-Host ""
}

# 开始逐步启动
Write-Host "开始逐步启动QMS-AI服务..." -ForegroundColor Green
Write-Host ""

# 第1步：配置中心服务
$success1 = Start-ServiceStep -Name "配置中心服务" -Port 3003 -Directory "backend\nodejs" -Script "lightweight-config-service.js" -HealthUrl "http://localhost:3003/health"
if (-not $success1) {
    Write-Host "❌ 配置中心服务启动失败，无法继续" -ForegroundColor Red
    exit 1
}
Wait-UserConfirmation "配置中心服务已启动，按任意键继续启动AI聊天服务..."

# 第2步：AI聊天服务
$success2 = Start-ServiceStep -Name "AI聊天服务" -Port 3004 -Directory "backend\nodejs" -Script "chat-service.js" -HealthUrl "http://localhost:3004/health"
if (-not $success2) {
    Write-Host "❌ AI聊天服务启动失败" -ForegroundColor Red
}
Wait-UserConfirmation "AI聊天服务已启动，按任意键继续启动认证服务..."

# 第3步：认证服务
$success3 = Start-ServiceStep -Name "认证服务" -Port 8084 -Directory "backend\nodejs" -Script "auth-service.js" -HealthUrl "http://localhost:8084/health"
if (-not $success3) {
    Write-Host "❌ 认证服务启动失败" -ForegroundColor Red
}
Wait-UserConfirmation "认证服务已启动，按任意键继续启动API网关..."

# 第4步：API网关
$success4 = Start-ServiceStep -Name "API网关" -Port 8085 -Directory "backend\nodejs" -Script "api-gateway.js" -HealthUrl "http://localhost:8085/health"
if (-not $success4) {
    Write-Host "❌ API网关启动失败" -ForegroundColor Red
}
Wait-UserConfirmation "API网关已启动，按任意键继续启动应用端前端..."

# 第5步：应用端前端
$success5 = Start-ServiceStep -Name "应用端前端" -Port 8081 -Directory "frontend\应用端" -Script "npm run dev" -HealthUrl "http://localhost:8081"
if (-not $success5) {
    Write-Host "❌ 应用端前端启动失败" -ForegroundColor Red
}
Wait-UserConfirmation "应用端前端已启动，按任意键继续启动配置端前端..."

# 第6步：配置端前端
$success6 = Start-ServiceStep -Name "配置端前端" -Port 8072 -Directory "frontend\配置端" -Script "npm run serve" -HealthUrl "http://localhost:8072"
if (-not $success6) {
    Write-Host "❌ 配置端前端启动失败" -ForegroundColor Red
}

# 最终状态检查
Write-Host ""
Write-Host "🎉 所有服务启动完成！" -ForegroundColor Green
Write-Host ""
Write-Host "最终状态检查:" -ForegroundColor Cyan
Write-Host "=============" -ForegroundColor Cyan

$services = @(
    @{Name="配置中心服务"; Port=3003; Url="http://localhost:3003"},
    @{Name="AI聊天服务"; Port=3004; Url="http://localhost:3004"},
    @{Name="认证服务"; Port=8084; Url="http://localhost:8084"},
    @{Name="API网关"; Port=8085; Url="http://localhost:8085"},
    @{Name="应用端前端"; Port=8081; Url="http://localhost:8081"},
    @{Name="配置端前端"; Port=8072; Url="http://localhost:8072"}
)

$runningCount = 0
foreach ($service in $services) {
    if (Test-Port -Port $service.Port) {
        Write-Host "✅ $($service.Name) (端口 $($service.Port)): 运行中" -ForegroundColor Green
        $runningCount++
    } else {
        Write-Host "❌ $($service.Name) (端口 $($service.Port)): 未运行" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "运行状态: $runningCount/6 服务正在运行" -ForegroundColor $(if ($runningCount -eq 6) { "Green" } else { "Yellow" })
Write-Host ""
Write-Host "访问地址:" -ForegroundColor Cyan
Write-Host "- 应用端: http://localhost:8081" -ForegroundColor White
Write-Host "- 配置端: http://localhost:8072" -ForegroundColor White  
Write-Host "- API网关: http://localhost:8085" -ForegroundColor White
Write-Host ""
