# Install Redis on Windows for QMS-AI
# This script downloads and sets up Redis for local development

param(
    [string]$RedisVersion = "3.0.504",
    [string]$InstallPath = "C:\Redis"
)

Write-Host "Installing Redis $RedisVersion for QMS-AI..." -ForegroundColor Cyan

# Check if already installed
if (Test-Path "$InstallPath\redis-server.exe") {
    Write-Host "Redis already installed at $InstallPath" -ForegroundColor Green
    Write-Host "Starting Redis server..." -ForegroundColor Yellow
    Start-Process -FilePath "$InstallPath\redis-server.exe" -ArgumentList "$InstallPath\redis.windows.conf" -WindowStyle Minimized
    Start-Sleep 3
    Write-Host "Redis server started. Testing connection..." -ForegroundColor Green
    try {
        $test = Test-NetConnection -ComputerName localhost -Port 6379 -InformationLevel Quiet
        if ($test) {
            Write-Host "✅ Redis is running on port 6379" -ForegroundColor Green
        } else {
            Write-Host "❌ Redis failed to start" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Cannot test Redis connection" -ForegroundColor Red
    }
    exit 0
}

# Create install directory
New-Item -ItemType Directory -Path $InstallPath -Force | Out-Null

# Download Redis for Windows
$downloadUrl = "https://github.com/microsoftarchive/redis/releases/download/win-$RedisVersion/Redis-x64-$RedisVersion.zip"
$zipPath = "$env:TEMP\Redis-x64-$RedisVersion.zip"

Write-Host "Downloading Redis from GitHub..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath -UseBasicParsing
    Write-Host "✅ Download completed" -ForegroundColor Green
} catch {
    Write-Host "❌ Download failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Extract Redis
Write-Host "Extracting Redis to $InstallPath..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $zipPath -DestinationPath $InstallPath -Force
    Write-Host "✅ Extraction completed" -ForegroundColor Green
} catch {
    Write-Host "❌ Extraction failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Clean up
Remove-Item $zipPath -Force

# Add to PATH (optional)
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
if ($currentPath -notlike "*$InstallPath*") {
    Write-Host "Adding Redis to PATH..." -ForegroundColor Yellow
    [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$InstallPath", "User")
    Write-Host "✅ Added to PATH (restart terminal to take effect)" -ForegroundColor Green
}

# Start Redis server
Write-Host "Starting Redis server..." -ForegroundColor Yellow
Start-Process -FilePath "$InstallPath\redis-server.exe" -ArgumentList "$InstallPath\redis.windows.conf" -WindowStyle Minimized

# Wait and test
Start-Sleep 3
Write-Host "Testing Redis connection..." -ForegroundColor Green
try {
    $test = Test-NetConnection -ComputerName localhost -Port 6379 -InformationLevel Quiet
    if ($test) {
        Write-Host "✅ Redis installation and startup successful!" -ForegroundColor Green
        Write-Host "Redis is running on localhost:6379" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Redis installed but failed to start" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Cannot test Redis connection" -ForegroundColor Red
}

Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Restart your terminal to use 'redis-server' command" -ForegroundColor White
Write-Host "2. Run your QMS-AI services with Redis support" -ForegroundColor White
Write-Host "3. Redis will auto-start with the config file" -ForegroundColor White
