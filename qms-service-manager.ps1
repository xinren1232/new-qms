# QMS-AI 综合服务状态检查和管理脚本
param(
    [switch]$StartServices,
    [switch]$StopServices,
    [switch]$RestartServices,
    [switch]$Detailed,
    [switch]$Help
)

# 服务配置
$services = @(
    @{ 
        Name = "配置中心服务"; 
        Port = 3003; 
        Url = "http://localhost:3003/health"; 
        Script = "lightweight-config-service.js";
        Directory = "backend\nodejs";
        Priority = 1
    },
    @{ 
        Name = "AI聊天服务"; 
        Port = 3004; 
        Url = "http://localhost:3004/health"; 
        Script = "chat-service.js";
        Directory = "backend\nodejs";
        Priority = 2
    },
    @{ 
        Name = "认证服务"; 
        Port = 8084; 
        Url = "http://localhost:8084/health"; 
        Script = "auth-service.js";
        Directory = "backend\nodejs";
        Priority = 3
    },
    @{ 
        Name = "Coze Studio服务"; 
        Port = 3005; 
        Url = "http://localhost:3005/health"; 
        Script = "coze-studio-service.js";
        Directory = "backend\nodejs";
        Priority = 4
    },
    @{ 
        Name = "高级功能服务"; 
        Port = 3009; 
        Url = "http://localhost:3009/health"; 
        Script = "advanced-features-service.js";
        Directory = "backend\nodejs";
        Priority = 5
    },
    @{ 
        Name = "导出服务"; 
        Port = 3008; 
        Url = "http://localhost:3008/health"; 
        Script = "export-service-standalone.js";
        Directory = "backend\nodejs";
        Priority = 6
    },
    @{ 
        Name = "API网关"; 
        Port = 8085; 
        Url = "http://localhost:8085/health"; 
        Script = "api-gateway.js";
        Directory = "backend\nodejs";
        Priority = 7
    },
    @{
        Name = "应用端前端";
        Port = 8081;
        Url = "http://localhost:8081";
        Script = "npm run dev";
        Directory = "frontend\应用端";
        Priority = 8
    },
    @{
        Name = "配置端前端";
        Port = 8072;
        Url = "http://localhost:8072";
        Script = "npm run serve";
        Directory = "frontend\配置端";
        Priority = 9
    }
)

function Show-Help {
    Write-Host "QMS-AI 服务管理脚本" -ForegroundColor Cyan
    Write-Host "===================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "用法:" -ForegroundColor Yellow
    Write-Host "  .\qms-service-manager.ps1                    # 检查服务状态"
    Write-Host "  .\qms-service-manager.ps1 -StartServices     # 启动所有服务"
    Write-Host "  .\qms-service-manager.ps1 -StopServices      # 停止所有服务"
    Write-Host "  .\qms-service-manager.ps1 -RestartServices   # 重启所有服务"
    Write-Host "  .\qms-service-manager.ps1 -Detailed          # 详细状态检查"
    Write-Host "  .\qms-service-manager.ps1 -Help              # 显示帮助"
    Write-Host ""
}

function Test-Port {
    param($Port)
    $listening = netstat -an | Select-String ":$Port " | Select-String "LISTENING"
    return $listening -ne $null
}

function Test-HttpEndpoint {
    param($Url, $TimeoutSec = 5)
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec $TimeoutSec -ErrorAction Stop
        return @{ Success = $true; StatusCode = $response.StatusCode; Message = "OK" }
    } catch {
        return @{ Success = $false; StatusCode = 0; Message = $_.Exception.Message }
    }
}

function Get-ProcessByPort {
    param($Port)
    try {
        $netstatOutput = netstat -ano | Select-String ":$Port "
        if ($netstatOutput) {
            $processId = ($netstatOutput -split '\s+')[-1]
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            return $process
        }
    } catch {
        return $null
    }
    return $null
}

function Stop-ServiceByPort {
    param($Port, $ServiceName)
    $process = Get-ProcessByPort -Port $Port
    if ($process) {
        Write-Host "正在停止 $ServiceName (PID: $($process.Id))..." -ForegroundColor Yellow
        try {
            Stop-Process -Id $process.Id -Force
            Start-Sleep -Seconds 2
            Write-Host "✅ $ServiceName 已停止" -ForegroundColor Green
            return $true
        } catch {
            Write-Host "❌ 停止 $ServiceName 失败: $($_.Exception.Message)" -ForegroundColor Red
            return $false
        }
    } else {
        Write-Host "⚠️ $ServiceName 未运行" -ForegroundColor Yellow
        return $true
    }
}

function Start-Service {
    param($Service)
    Write-Host "正在启动 $($Service.Name)..." -ForegroundColor Yellow
    
    $workingDir = Join-Path $PWD $Service.Directory
    if (-not (Test-Path $workingDir)) {
        Write-Host "❌ 目录不存在: $workingDir" -ForegroundColor Red
        return $false
    }
    
    try {
        if ($Service.Script.StartsWith("npm")) {
            $processInfo = Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$workingDir`" && $($Service.Script)" -WindowStyle Minimized -PassThru
        } else {
            $processInfo = Start-Process -FilePath "node" -ArgumentList $Service.Script -WorkingDirectory $workingDir -WindowStyle Minimized -PassThru
        }
        
        Start-Sleep -Seconds 3
        
        if (Test-Port -Port $Service.Port) {
            Write-Host "✅ $($Service.Name) 启动成功 (PID: $($processInfo.Id))" -ForegroundColor Green
            return $true
        } else {
            Write-Host "❌ $($Service.Name) 启动失败 - 端口未监听" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "❌ 启动 $($Service.Name) 失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

function Show-ServiceStatus {
    param($Detailed = $false)
    
    Write-Host "QMS-AI 服务状态检查" -ForegroundColor Cyan
    Write-Host "===================" -ForegroundColor Cyan
    Write-Host ""
    
    $runningCount = 0
    $totalCount = $services.Count
    
    foreach ($service in $services) {
        $isRunning = Test-Port -Port $service.Port
        
        if ($isRunning) {
            Write-Host "✅ $($service.Name) (端口 $($service.Port)): 运行中" -ForegroundColor Green
            $runningCount++
            
            if ($Detailed) {
                $process = Get-ProcessByPort -Port $service.Port
                if ($process) {
                    Write-Host "   进程ID: $($process.Id), 进程名: $($process.ProcessName)" -ForegroundColor Gray
                }
                
                $httpTest = Test-HttpEndpoint -Url $service.Url
                if ($httpTest.Success) {
                    Write-Host "   HTTP状态: ✅ $($httpTest.StatusCode) $($httpTest.Message)" -ForegroundColor Green
                } else {
                    Write-Host "   HTTP状态: ❌ $($httpTest.Message)" -ForegroundColor Yellow
                }
            }
        } else {
            Write-Host "❌ $($service.Name) (端口 $($service.Port)): 未运行" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "状态总结: $runningCount/$totalCount 服务运行中" -ForegroundColor $(if ($runningCount -eq $totalCount) { "Green" } else { "Yellow" })
    
    if ($runningCount -eq $totalCount) {
        Write-Host "🎉 所有服务运行正常！" -ForegroundColor Green
    } elseif ($runningCount -eq 0) {
        Write-Host "⚠️ 所有服务都未运行" -ForegroundColor Red
    } else {
        Write-Host "⚠️ 部分服务未运行，请检查" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "访问地址:" -ForegroundColor Cyan
    Write-Host "- 应用端前端: http://localhost:8081" -ForegroundColor White
    Write-Host "- 配置端前端: http://localhost:8072" -ForegroundColor White
    Write-Host "- API网关: http://localhost:8085" -ForegroundColor White
    Write-Host "- 配置中心: http://localhost:3003" -ForegroundColor White
    Write-Host "- AI聊天服务: http://localhost:3004" -ForegroundColor White
    Write-Host "- 认证服务: http://localhost:8084" -ForegroundColor White
}

# 主逻辑
if ($Help) {
    Show-Help
    exit
}

if ($StopServices) {
    Write-Host "正在停止所有QMS-AI服务..." -ForegroundColor Yellow
    Write-Host ""
    
    # 按优先级倒序停止服务
    $sortedServices = $services | Sort-Object Priority -Descending
    foreach ($service in $sortedServices) {
        Stop-ServiceByPort -Port $service.Port -ServiceName $service.Name
    }
    
    Write-Host ""
    Write-Host "所有服务停止操作完成" -ForegroundColor Green
    exit
}

if ($StartServices) {
    Write-Host "正在启动所有QMS-AI服务..." -ForegroundColor Yellow
    Write-Host ""
    
    # 按优先级顺序启动服务
    $sortedServices = $services | Sort-Object Priority
    foreach ($service in $sortedServices) {
        if (-not (Test-Port -Port $service.Port)) {
            Start-Service -Service $service
            Start-Sleep -Seconds 2
        } else {
            Write-Host "⚠️ $($service.Name) 已在运行中" -ForegroundColor Yellow
        }
    }
    
    Write-Host ""
    Write-Host "等待服务完全启动..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    Write-Host ""
    Show-ServiceStatus -Detailed $false
    exit
}

if ($RestartServices) {
    Write-Host "正在重启所有QMS-AI服务..." -ForegroundColor Yellow
    Write-Host ""
    
    # 先停止所有服务
    $sortedServices = $services | Sort-Object Priority -Descending
    foreach ($service in $sortedServices) {
        Stop-ServiceByPort -Port $service.Port -ServiceName $service.Name
    }
    
    Write-Host ""
    Write-Host "等待服务完全停止..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    
    # 再启动所有服务
    $sortedServices = $services | Sort-Object Priority
    foreach ($service in $sortedServices) {
        Start-Service -Service $service
        Start-Sleep -Seconds 2
    }
    
    Write-Host ""
    Write-Host "等待服务完全启动..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    
    Write-Host ""
    Show-ServiceStatus -Detailed $false
    exit
}

# 默认：显示服务状态
Show-ServiceStatus -Detailed $Detailed
