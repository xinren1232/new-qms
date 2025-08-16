# QMS-AI VS Code 部署脚本
# 使用 VS Code SFTP 扩展部署到服务器

Write-Host ""
Write-Host "🚀 QMS-AI VS Code 部署脚本" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""

# 检查 VS Code 是否安装
$vscodePath = Get-Command code -ErrorAction SilentlyContinue
if (-not $vscodePath) {
    Write-Host "❌ VS Code 未找到，请确保 VS Code 已安装并添加到 PATH" -ForegroundColor Red
    Write-Host "   下载地址: https://code.visualstudio.com/" -ForegroundColor Yellow
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "✅ VS Code 已找到" -ForegroundColor Green

# 检查项目文件
if (-not (Test-Path "QMS-AI-System-v2.0.zip")) {
    Write-Host "❌ 未找到 QMS-AI-System-v2.0.zip 文件" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "✅ 找到部署包: QMS-AI-System-v2.0.zip" -ForegroundColor Green

# 检查 SFTP 配置
if (-not (Test-Path ".vscode/sftp.json")) {
    Write-Host "❌ 未找到 SFTP 配置文件" -ForegroundColor Red
    Write-Host "   请先安装 SFTP 扩展并配置连接" -ForegroundColor Yellow
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "✅ SFTP 配置文件已存在" -ForegroundColor Green
Write-Host ""

# 显示操作步骤
Write-Host "📋 VS Code 部署步骤:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1️⃣ 安装 SFTP 扩展:" -ForegroundColor Cyan
Write-Host "   - 在 VS Code 中按 Ctrl+Shift+X 打开扩展面板" -ForegroundColor White
Write-Host "   - 搜索 'SFTP' 并安装 'SFTP' 扩展 (by liximomo)" -ForegroundColor White
Write-Host ""

Write-Host "2️⃣ 打开项目:" -ForegroundColor Cyan
Write-Host "   - 在 VS Code 中打开当前目录 (File -> Open Folder)" -ForegroundColor White
Write-Host "   - 或者在终端执行: code ." -ForegroundColor White
Write-Host ""

Write-Host "3️⃣ 连接服务器:" -ForegroundColor Cyan
Write-Host "   - 按 Ctrl+Shift+P 打开命令面板" -ForegroundColor White
Write-Host "   - 输入 'SFTP: Config' 并选择" -ForegroundColor White
Write-Host "   - 配置文件已自动创建在 .vscode/sftp.json" -ForegroundColor White
Write-Host ""

Write-Host "4️⃣ 上传文件:" -ForegroundColor Cyan
Write-Host "   - 右键点击 'QMS-AI-System-v2.0.zip' 文件" -ForegroundColor White
Write-Host "   - 选择 'Upload' 或 'SFTP: Upload'" -ForegroundColor White
Write-Host "   - 文件将上传到服务器 /root/QMS01/ 目录" -ForegroundColor White
Write-Host ""

Write-Host "5️⃣ 执行部署:" -ForegroundColor Cyan
Write-Host "   - 按 Ctrl+Shift+P 打开命令面板" -ForegroundColor White
Write-Host "   - 输入 'Terminal: Create New Terminal' 或按 Ctrl+`" -ForegroundColor White
Write-Host "   - 在终端中执行 SSH 连接和部署命令" -ForegroundColor White
Write-Host ""

Write-Host "📋 SSH 部署命令:" -ForegroundColor Yellow
Write-Host "ssh root@47.108.152.16" -ForegroundColor Gray
Write-Host "cd /root/QMS01" -ForegroundColor Gray
Write-Host "unzip -o QMS-AI-System-v2.0.zip" -ForegroundColor Gray
Write-Host "chmod +x QMS-AI-Complete-Deploy.sh" -ForegroundColor Gray
Write-Host "./QMS-AI-Complete-Deploy.sh" -ForegroundColor Gray
Write-Host ""

Write-Host "🔧 服务器连接信息:" -ForegroundColor Yellow
Write-Host "主机: 47.108.152.16" -ForegroundColor White
Write-Host "用户: root" -ForegroundColor White
Write-Host "密码: Zxylsy.99" -ForegroundColor White
Write-Host "路径: /root/QMS01" -ForegroundColor White
Write-Host ""

Write-Host "📊 部署完成后访问地址:" -ForegroundColor Green
Write-Host "🏠 主应用: http://47.108.152.16/" -ForegroundColor White
Write-Host "⚙️ 配置端: http://47.108.152.16/config/" -ForegroundColor White
Write-Host "🔧 API网关: http://47.108.152.16:8085/" -ForegroundColor White
Write-Host ""

# 询问是否打开 VS Code
$openVSCode = Read-Host "是否要打开 VS Code 开始部署? (y/n)"
if ($openVSCode -eq "y" -or $openVSCode -eq "Y") {
    Write-Host "正在打开 VS Code..." -ForegroundColor Green
    Start-Process "code" -ArgumentList "."
    Write-Host "✅ VS Code 已打开，请按照上述步骤进行部署" -ForegroundColor Green
}

Write-Host ""
Write-Host "💡 提示: 如果是第一次使用，请先安装 SFTP 扩展" -ForegroundColor Yellow
Write-Host "💡 扩展地址: https://marketplace.visualstudio.com/items?itemName=liximomo.sftp" -ForegroundColor Yellow
Write-Host ""
Read-Host "按回车键退出"
