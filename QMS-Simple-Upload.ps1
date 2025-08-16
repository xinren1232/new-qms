# QMS-AI 简单上传脚本
# 使用PowerShell和WinSCP或手动指导

Write-Host ""
Write-Host "🚀 QMS-AI 简单上传脚本" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host "服务器信息:" -ForegroundColor Yellow
Write-Host "  IP: 47.108.152.16" -ForegroundColor White
Write-Host "  用户: root" -ForegroundColor White  
Write-Host "  密码: Zxylsy.99" -ForegroundColor White
Write-Host "  路径: /root/QMS01" -ForegroundColor White
Write-Host ""

# 检查必要的目录
$requiredDirs = @("backend", "frontend", "config")
$missingDirs = @()

foreach ($dir in $requiredDirs) {
    if (-not (Test-Path $dir)) {
        $missingDirs += $dir
    }
}

if ($missingDirs.Count -gt 0) {
    Write-Host "❌ 缺少必要目录: $($missingDirs -join ', ')" -ForegroundColor Red
    Write-Host "请确保在正确的QMS项目目录中运行此脚本" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "✅ 项目目录检查通过" -ForegroundColor Green
Write-Host ""

# 创建上传包
Write-Host "📦 创建上传包..." -ForegroundColor Yellow

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$packageName = "qms-update-$timestamp"
$tempDir = "temp_upload"

# 清理旧的临时目录
if (Test-Path $tempDir) {
    Remove-Item $tempDir -Recurse -Force
}

# 创建临时目录
New-Item -ItemType Directory -Path $tempDir | Out-Null

# 复制文件
$itemsToCopy = @(
    @{Source="backend"; Dest="$tempDir\backend"},
    @{Source="frontend"; Dest="$tempDir\frontend"},
    @{Source="config"; Dest="$tempDir\config"},
    @{Source="nginx"; Dest="$tempDir\nginx"},
    @{Source="deployment"; Dest="$tempDir\deployment"}
)

foreach ($item in $itemsToCopy) {
    if (Test-Path $item.Source) {
        Copy-Item $item.Source $item.Dest -Recurse -Force
        Write-Host "✅ 已复制 $($item.Source)" -ForegroundColor Green
    }
}

# 复制重要文件
$filesToCopy = @("docker-compose.yml", "QMS-AI-Complete-Deploy.sh", "package.json", "README.md")
foreach ($file in $filesToCopy) {
    if (Test-Path $file) {
        Copy-Item $file $tempDir -Force
        Write-Host "✅ 已复制 $file" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "📋 上传包已准备完成: $tempDir" -ForegroundColor Green
Write-Host ""

# 提供上传选项
Write-Host "🔧 上传方式选择:" -ForegroundColor Yellow
Write-Host "1. 使用WinSCP (推荐)" -ForegroundColor White
Write-Host "2. 使用其他SFTP工具" -ForegroundColor White
Write-Host "3. 手动压缩后上传" -ForegroundColor White
Write-Host ""

Write-Host "📋 WinSCP上传步骤:" -ForegroundColor Cyan
Write-Host "1. 下载并安装WinSCP: https://winscp.net/eng/download.php" -ForegroundColor White
Write-Host "2. 打开WinSCP，创建新连接:" -ForegroundColor White
Write-Host "   - 文件协议: SFTP" -ForegroundColor White
Write-Host "   - 主机名: 47.108.152.16" -ForegroundColor White
Write-Host "   - 用户名: root" -ForegroundColor White
Write-Host "   - 密码: Zxylsy.99" -ForegroundColor White
Write-Host "3. 连接成功后，导航到右侧的 /root/QMS01 目录" -ForegroundColor White
Write-Host "4. 将左侧的 $tempDir 目录中的所有文件拖拽到右侧" -ForegroundColor White
Write-Host "5. 上传完成后，在WinSCP中打开终端 (Ctrl+T)" -ForegroundColor White
Write-Host "6. 在终端中执行: chmod +x QMS-AI-Complete-Deploy.sh && ./QMS-AI-Complete-Deploy.sh" -ForegroundColor White
Write-Host ""

Write-Host "📋 其他工具上传:" -ForegroundColor Cyan
Write-Host "- FileZilla: 使用SFTP协议连接" -ForegroundColor White
Write-Host "- MobaXterm: 内置SFTP功能" -ForegroundColor White
Write-Host "- VS Code: 安装SFTP扩展" -ForegroundColor White
Write-Host ""

Write-Host "📋 手动压缩上传:" -ForegroundColor Cyan
Write-Host "1. 将 $tempDir 目录压缩为 zip 文件" -ForegroundColor White
Write-Host "2. 使用任何支持SFTP的工具上传zip文件" -ForegroundColor White
Write-Host "3. 在服务器上解压: unzip filename.zip" -ForegroundColor White
Write-Host ""

Write-Host "🎯 上传完成后的部署步骤:" -ForegroundColor Yellow
Write-Host "1. SSH连接到服务器: ssh root@47.108.152.16" -ForegroundColor White
Write-Host "2. 进入目录: cd /root/QMS01" -ForegroundColor White
Write-Host "3. 执行部署: chmod +x QMS-AI-Complete-Deploy.sh && ./QMS-AI-Complete-Deploy.sh" -ForegroundColor White
Write-Host ""

Write-Host "📊 部署完成后访问地址:" -ForegroundColor Green
Write-Host "🏠 主应用: http://47.108.152.16/" -ForegroundColor White
Write-Host "⚙️ 配置端: http://47.108.152.16/config/" -ForegroundColor White
Write-Host "🔧 API网关: http://47.108.152.16:8085/" -ForegroundColor White
Write-Host ""

# 询问是否要打开WinSCP下载页面
$openWinSCP = Read-Host "是否要打开WinSCP下载页面? (y/n)"
if ($openWinSCP -eq "y" -or $openWinSCP -eq "Y") {
    Start-Process "https://winscp.net/eng/download.php"
}

Write-Host ""
Write-Host "✅ 准备工作完成！请按照上述步骤进行上传和部署。" -ForegroundColor Green
Write-Host ""
Read-Host "按回车键退出"

# 可选：保持临时目录以供上传
# Remove-Item $tempDir -Recurse -Force
