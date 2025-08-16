# QMS-AI 简单部署包创建器
# 用于创建增量更新部署包

Write-Host "🚀 创建QMS-AI增量更新部署包..." -ForegroundColor Green

$DEPLOY_DIR = "qms-update-package"
$ARCHIVE_NAME = "qms-update-package.zip"

# 清理旧目录
if (Test-Path $DEPLOY_DIR) {
    Write-Host "清理旧部署目录..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force $DEPLOY_DIR
}

# 创建目录结构
Write-Host "创建目录结构..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\backend\nodejs" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\frontend\app" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\frontend\config" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\deployment" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\nginx" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\config" | Out-Null

# 复制后端文件
Write-Host "复制后端文件..." -ForegroundColor Cyan
if (Test-Path "backend\nodejs") {
    Copy-Item -Recurse -Force "backend\nodejs\*" "$DEPLOY_DIR\backend\nodejs\"
    Write-Host "✅ 后端文件复制完成" -ForegroundColor Green
}

# 复制前端文件
Write-Host "复制前端文件..." -ForegroundColor Cyan
$frontendAppPath = Get-ChildItem -Path "frontend" -Directory | Where-Object { $_.Name -like "*应用*" -or $_.Name -eq "app" } | Select-Object -First 1
if ($frontendAppPath) {
    Copy-Item -Recurse -Force "$($frontendAppPath.FullName)\*" "$DEPLOY_DIR\frontend\app\"
    Write-Host "✅ 应用端文件复制完成" -ForegroundColor Green
}

$frontendConfigPath = Get-ChildItem -Path "frontend" -Directory | Where-Object { $_.Name -like "*配置*" -or $_.Name -eq "config" } | Select-Object -First 1
if ($frontendConfigPath) {
    Copy-Item -Recurse -Force "$($frontendConfigPath.FullName)\*" "$DEPLOY_DIR\frontend\config\"
    Write-Host "✅ 配置端文件复制完成" -ForegroundColor Green
}

# 复制部署文件
Write-Host "复制部署文件..." -ForegroundColor Cyan
if (Test-Path "deployment") {
    Copy-Item -Force "deployment\*.sh" "$DEPLOY_DIR\deployment\" -ErrorAction SilentlyContinue
    Copy-Item -Force "deployment\*.yml" "$DEPLOY_DIR\deployment\" -ErrorAction SilentlyContinue
    Copy-Item -Force "deployment\*.md" "$DEPLOY_DIR\deployment\" -ErrorAction SilentlyContinue
    Write-Host "✅ 部署文件复制完成" -ForegroundColor Green
}

# 复制配置文件
Write-Host "复制配置文件..." -ForegroundColor Cyan
if (Test-Path "docker-compose.yml") {
    Copy-Item -Force "docker-compose.yml" "$DEPLOY_DIR\"
}
if (Test-Path "config") {
    Copy-Item -Recurse -Force "config\*" "$DEPLOY_DIR\config\" -ErrorAction SilentlyContinue
}
if (Test-Path "nginx") {
    Copy-Item -Recurse -Force "nginx\*" "$DEPLOY_DIR\nginx\" -ErrorAction SilentlyContinue
}

# 创建部署说明文件
Write-Host "创建部署说明..." -ForegroundColor Cyan
$deployGuide = @"
# QMS-AI 增量更新部署包

## 部署步骤

1. 上传到服务器:
   scp qms-update-package.zip root@47.108.152.16:/opt/

2. 登录服务器并解压:
   ssh root@47.108.152.16
   cd /opt
   unzip -o qms-update-package.zip
   cd qms-update-package

3. 执行更新:
   chmod +x deployment/quick-update.sh
   ./deployment/quick-update.sh

## 访问地址
- 主应用: http://47.108.152.16:8081
- 配置管理: http://47.108.152.16:8072
- API网关: http://47.108.152.16:8085

## 管理命令
- 查看状态: docker-compose ps
- 查看日志: docker-compose logs -f
- 重启服务: docker-compose restart
"@

$deployGuide | Out-File -FilePath "$DEPLOY_DIR\部署说明.txt" -Encoding UTF8

# 创建压缩包
Write-Host "创建压缩包..." -ForegroundColor Cyan
if (Test-Path $ARCHIVE_NAME) {
    Remove-Item -Force $ARCHIVE_NAME
}

Compress-Archive -Path "$DEPLOY_DIR\*" -DestinationPath $ARCHIVE_NAME -Force

Write-Host "🎉 部署包创建完成!" -ForegroundColor Green
Write-Host "文件名: $ARCHIVE_NAME" -ForegroundColor Yellow
Write-Host "大小: $((Get-Item $ARCHIVE_NAME).Length / 1MB) MB" -ForegroundColor Yellow

# 显示下一步操作
Write-Host "`n📋 下一步操作:" -ForegroundColor Cyan
Write-Host "1. 上传到服务器: scp $ARCHIVE_NAME root@47.108.152.16:/opt/" -ForegroundColor White
Write-Host "2. 登录服务器: ssh root@47.108.152.16" -ForegroundColor White
Write-Host "3. 解压并部署: cd /opt && unzip -o $ARCHIVE_NAME && cd qms-update-package && chmod +x deployment/quick-update.sh && ./deployment/quick-update.sh" -ForegroundColor White

Write-Host "`n✅ 准备就绪，可以开始部署了!" -ForegroundColor Green
