# QMS-AI 目录结构优化脚本
Write-Host ""
Write-Host "========================================"
Write-Host "    QMS-AI 目录结构优化工具"
Write-Host "========================================"
Write-Host ""

# 创建备份
$backupDir = "directory-optimization-backup\$(Get-Date -Format 'yyyyMMdd_HHmmss')"
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
Write-Host "📦 创建备份目录: $backupDir" -ForegroundColor Green

# 统计变量
$movedFiles = 0
$deletedFiles = 0
$createdDirs = 0

# ==========================================
# 第一步：创建必要的目录结构
# ==========================================
Write-Host ""
Write-Host "[1/8] 📁 创建标准目录结构..." -ForegroundColor Cyan

$directories = @(
    "backend\tools",
    "backend\scripts", 
    "backend\config",
    "frontend\tools",
    "frontend\scripts",
    "tools",
    "tools\deployment",
    "tools\monitoring",
    "tools\database"
)

foreach ($dir in $directories) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "创建目录: $dir" -ForegroundColor Yellow
        $createdDirs++
    }
}

# ==========================================
# 第二步：移动部署相关文件
# ==========================================
Write-Host ""
Write-Host "[2/8] 🚀 整理部署相关文件..." -ForegroundColor Cyan

$deployFiles = @(
    "deploy-aliyun-windows.ps1",
    "deploy-optimizations.sh",
    "deploy-to-server.bat",
    "deploy-to-server.sh",
    "fix-npm-with-pnpm.sh"
)

foreach ($file in $deployFiles) {
    if (Test-Path $file) {
        Write-Host "移动部署文件: $file → tools/deployment/" -ForegroundColor Yellow
        Copy-Item $file $backupDir -Force
        Move-Item $file "tools\deployment\" -Force
        $movedFiles++
    }
}

# ==========================================
# 第三步：移动工具脚本
# ==========================================
Write-Host ""
Write-Host "[3/8] 🔧 整理工具脚本..." -ForegroundColor Cyan

$toolScripts = @(
    "优化实施脚本.sh",
    "性能测试脚本.js",
    "restart-services.ps1",
    "redis-persistent-manager.bat"
)

foreach ($script in $toolScripts) {
    if (Test-Path $script) {
        Write-Host "移动工具脚本: $script → tools/" -ForegroundColor Yellow
        Copy-Item $script $backupDir -Force
        Move-Item $script "tools\" -Force
        $movedFiles++
    }
}

# ==========================================
# 第四步：移动配置文件到config
# ==========================================
Write-Host ""
Write-Host "[4/8] ⚙️ 整理配置文件..." -ForegroundColor Cyan

$configFiles = @(
    "docker-compose.config-cluster.yml",
    "docker-compose.monitoring.yml", 
    "docker-compose.yml",
    "Dockerfile.config.fixed"
)

foreach ($config in $configFiles) {
    if (Test-Path $config) {
        Write-Host "移动配置文件: $config → config/" -ForegroundColor Yellow
        Copy-Item $config $backupDir -Force
        Move-Item $config "config\" -Force
        $movedFiles++
    }
}

# ==========================================
# 第五步：移动文档到docs
# ==========================================
Write-Host ""
Write-Host "[5/8] 📄 整理文档文件..." -ForegroundColor Cyan

$docFiles = @(
    "GitHub-Push-Complete-Report.md",
    "PROJECT-STRUCTURE-SUMMARY.md",
    "QMS-Cleanup-Complete-Report.md",
    "QMS-Final-Status-Report.md",
    "创建需求管理对象指南.md",
    "手动部署指南.md",
    "警告修复方案.md"
)

foreach ($doc in $docFiles) {
    if (Test-Path $doc) {
        Write-Host "移动文档: $doc → docs/" -ForegroundColor Yellow
        Copy-Item $doc $backupDir -Force
        Move-Item $doc "docs\" -Force
        $movedFiles++
    }
}

# ==========================================
# 第六步：删除重复的部署包和压缩文件
# ==========================================
Write-Host ""
Write-Host "[6/8] 🗑️ 清理重复的部署包..." -ForegroundColor Cyan

$duplicatePackages = @(
    "qms-aliyun-deploy.zip",
    "qms-deploy.zip", 
    "qms-pnpm-fix.zip",
    "qms-update-package.zip",
    "redis.zip"
)

foreach ($package in $duplicatePackages) {
    if (Test-Path $package) {
        Write-Host "删除重复包: $package" -ForegroundColor Yellow
        Copy-Item $package $backupDir -Force
        Remove-Item $package -Force
        $deletedFiles++
    }
}

# ==========================================
# 第七步：移动数据文件到data目录
# ==========================================
Write-Host ""
Write-Host "[7/8] 💾 整理数据文件..." -ForegroundColor Cyan

$dataFiles = @(
    "dump.rdb",
    "制造问题洗后版.xlsx",
    "来料问题洗后版.xlsx"
)

foreach ($dataFile in $dataFiles) {
    if (Test-Path $dataFile) {
        Write-Host "移动数据文件: $dataFile → data/" -ForegroundColor Yellow
        Copy-Item $dataFile $backupDir -Force
        Move-Item $dataFile "data\" -Force
        $movedFiles++
    }
}

# ==========================================
# 第八步：整理剩余的启动脚本
# ==========================================
Write-Host ""
Write-Host "[8/8] 🚀 整理启动脚本..." -ForegroundColor Cyan

# 移动专用启动脚本到tools
$specialScripts = @(
    "start-user-isolation-system.bat"
)

foreach ($script in $specialScripts) {
    if (Test-Path $script) {
        Write-Host "移动专用脚本: $script → tools/" -ForegroundColor Yellow
        Copy-Item $script $backupDir -Force
        Move-Item $script "tools\" -Force
        $movedFiles++
    }
}

Write-Host ""
Write-Host "✅ 目录优化完成！" -ForegroundColor Green
Write-Host ""
Write-Host "📊 优化统计:" -ForegroundColor Cyan
Write-Host "  • 移动文件: $movedFiles 个" -ForegroundColor White
Write-Host "  • 删除文件: $deletedFiles 个" -ForegroundColor White  
Write-Host "  • 创建目录: $createdDirs 个" -ForegroundColor White
Write-Host ""
Write-Host "📁 备份位置: $backupDir" -ForegroundColor Yellow
Write-Host ""
Write-Host "🎉 现在你的项目目录更加清洁整齐了！" -ForegroundColor Green
Write-Host ""

Read-Host "按回车键继续"
