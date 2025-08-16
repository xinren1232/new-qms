# QMS-AI 增量更新包创建器
Write-Host "Creating QMS-AI Update Package..." -ForegroundColor Green

$DEPLOY_DIR = "qms-update-package"
$ARCHIVE_NAME = "qms-update-package.zip"

# 清理旧目录
if (Test-Path $DEPLOY_DIR) {
    Remove-Item -Recurse -Force $DEPLOY_DIR
}

# 创建目录结构
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\backend\nodejs" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\frontend\app" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\frontend\config" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\deployment" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\nginx" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\config" | Out-Null

# 复制后端文件
Write-Host "Copying backend files..." -ForegroundColor Cyan
if (Test-Path "backend\nodejs") {
    Copy-Item -Recurse -Force "backend\nodejs\*" "$DEPLOY_DIR\backend\nodejs\"
}

# 复制前端文件
Write-Host "Copying frontend files..." -ForegroundColor Cyan
$frontendDirs = Get-ChildItem -Path "frontend" -Directory
foreach ($dir in $frontendDirs) {
    if ($dir.Name -match "应用" -or $dir.Name -eq "app") {
        Copy-Item -Recurse -Force "$($dir.FullName)\*" "$DEPLOY_DIR\frontend\app\"
    }
    if ($dir.Name -match "配置" -or $dir.Name -eq "config") {
        Copy-Item -Recurse -Force "$($dir.FullName)\*" "$DEPLOY_DIR\frontend\config\"
    }
}

# 复制部署文件
Write-Host "Copying deployment files..." -ForegroundColor Cyan
if (Test-Path "deployment") {
    Get-ChildItem "deployment\*.sh" | Copy-Item -Destination "$DEPLOY_DIR\deployment\" -Force
    Get-ChildItem "deployment\*.yml" | Copy-Item -Destination "$DEPLOY_DIR\deployment\" -Force
    Get-ChildItem "deployment\*.md" | Copy-Item -Destination "$DEPLOY_DIR\deployment\" -Force
}

# 复制配置文件
Write-Host "Copying config files..." -ForegroundColor Cyan
if (Test-Path "docker-compose.yml") {
    Copy-Item -Force "docker-compose.yml" "$DEPLOY_DIR\"
}
if (Test-Path "nginx") {
    Copy-Item -Recurse -Force "nginx\*" "$DEPLOY_DIR\nginx\"
}

# 创建部署说明
$deployGuide = @"
# QMS-AI Update Package

## Deployment Steps

1. Upload to server:
   scp qms-update-package.zip root@47.108.152.16:/opt/

2. Login and extract:
   ssh root@47.108.152.16
   cd /opt
   unzip -o qms-update-package.zip
   cd qms-update-package

3. Run update:
   chmod +x deployment/quick-update.sh
   ./deployment/quick-update.sh

## Access URLs
- Main App: http://47.108.152.16:8081
- Config: http://47.108.152.16:8072
- API Gateway: http://47.108.152.16:8085
"@

$deployGuide | Out-File -FilePath "$DEPLOY_DIR\README.txt" -Encoding UTF8

# 创建压缩包
Write-Host "Creating archive..." -ForegroundColor Cyan
if (Test-Path $ARCHIVE_NAME) {
    Remove-Item -Force $ARCHIVE_NAME
}

Compress-Archive -Path "$DEPLOY_DIR\*" -DestinationPath $ARCHIVE_NAME -Force

Write-Host "Package created successfully!" -ForegroundColor Green
Write-Host "File: $ARCHIVE_NAME" -ForegroundColor Yellow
Write-Host "Size: $([math]::Round((Get-Item $ARCHIVE_NAME).Length / 1MB, 2)) MB" -ForegroundColor Yellow

Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Upload: scp $ARCHIVE_NAME root@47.108.152.16:/opt/" -ForegroundColor White
Write-Host "2. Deploy: ssh root@47.108.152.16 'cd /opt && unzip -o $ARCHIVE_NAME && cd qms-update-package && chmod +x deployment/quick-update.sh && ./deployment/quick-update.sh'" -ForegroundColor White
