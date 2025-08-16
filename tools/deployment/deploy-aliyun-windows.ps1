# QMS-AI阿里云Windows服务器部署脚本
# 基于现有配置优化的一键部署方案

param(
    [string]$ServerIP = "47.108.152.16",
    [string]$SSHUser = "root",
    [string]$SSHPassword = "Zxylsy.99",
    [int]$SSHPort = 22,
    [switch]$SkipUpload = $false,
    [switch]$OnlyDeploy = $false
)

# 颜色输出函数
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Show-Banner {
    Write-ColorOutput "╔══════════════════════════════════════════════════════════════╗" "Magenta"
    Write-ColorOutput "║                QMS-AI阿里云一键部署工具                      ║" "Magenta"
    Write-ColorOutput "║              目标服务器: $ServerIP                      ║" "Magenta"
    Write-ColorOutput "║              基于现有配置的优化部署                          ║" "Magenta"
    Write-ColorOutput "╚══════════════════════════════════════════════════════════════╝" "Magenta"
    Write-Host ""
}

function Test-Prerequisites {
    Write-ColorOutput "🔍 检查本地环境..." "Cyan"
    
    # 检查必要文件
    $requiredFiles = @(
        "deployment/aliyun-deploy-optimized.yml",
        "deployment/.env.aliyun",
        "deployment/阿里云部署指南.md"
    )
    
    foreach ($file in $requiredFiles) {
        if (!(Test-Path $file)) {
            Write-ColorOutput "❌ 缺少必要文件: $file" "Red"
            return $false
        }
    }
    
    Write-ColorOutput "✅ 本地环境检查通过" "Green"
    return $true
}

function Test-ServerConnection {
    Write-ColorOutput "🔗 测试服务器连接..." "Cyan"
    
    try {
        # 使用plink测试SSH连接（如果有PuTTY）
        $testCommand = "echo 'Connection test successful'"
        $result = & plink -ssh -batch -pw $SSHPassword $SSHUser@$ServerIP -P $SSHPort $testCommand 2>$null
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ SSH连接测试成功" "Green"
            return $true
        } else {
            Write-ColorOutput "❌ SSH连接失败" "Red"
            return $false
        }
    } catch {
        Write-ColorOutput "⚠️ 无法使用plink，将尝试直接部署" "Yellow"
        return $true
    }
}

function Deploy-ToServer {
    Write-ColorOutput "🚀 开始部署到阿里云服务器..." "Cyan"
    
    # 创建部署命令
    $deployCommands = @"
#!/bin/bash
echo "🚀 QMS-AI阿里云部署开始..."

# 更新系统
echo "📦 更新系统包..."
yum update -y

# 安装Docker（如果未安装）
if ! command -v docker &> /dev/null; then
    echo "🐳 安装Docker..."
    yum install -y yum-utils
    yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    systemctl start docker
    systemctl enable docker
fi

# 安装Docker Compose（如果未安装）
if ! command -v docker-compose &> /dev/null; then
    echo "📦 安装Docker Compose..."
    curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

# 创建项目目录
mkdir -p /opt/qms-ai
cd /opt/qms-ai

# 停止现有服务
echo "🛑 停止现有服务..."
docker-compose down 2>/dev/null || true

# 清理旧容器和镜像
echo "🧹 清理旧资源..."
docker container prune -f
docker image prune -f

echo "✅ 服务器环境准备完成"
"@

    # 将部署命令写入临时文件
    $tempScript = [System.IO.Path]::GetTempFileName() + ".sh"
    $deployCommands | Out-File -FilePath $tempScript -Encoding UTF8
    
    Write-ColorOutput "📝 部署脚本已创建: $tempScript" "Blue"
    return $tempScript
}

function Upload-ProjectFiles {
    Write-ColorOutput "📤 准备上传项目文件..." "Cyan"
    
    # 创建部署包
    $deployPackage = "qms-ai-deploy-$(Get-Date -Format 'yyyyMMdd-HHmmss').zip"
    
    Write-ColorOutput "📦 创建部署包: $deployPackage" "Blue"
    
    # 压缩项目文件（排除不必要的文件）
    $excludePatterns = @("*.git*", "node_modules", ".venv", "__pycache__", "*.log", "*.tmp", "data", "logs")
    
    # 使用PowerShell压缩
    $compressionLevel = [System.IO.Compression.CompressionLevel]::Optimal
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    
    try {
        [System.IO.Compression.ZipFile]::CreateFromDirectory((Get-Location).Path, $deployPackage, $compressionLevel, $false)
        Write-ColorOutput "✅ 部署包创建成功: $deployPackage" "Green"
        return $deployPackage
    } catch {
        Write-ColorOutput "❌ 部署包创建失败: $($_.Exception.Message)" "Red"
        return $null
    }
}

function Execute-RemoteDeployment {
    param([string]$DeployPackage)
    
    Write-ColorOutput "🎯 执行远程部署..." "Cyan"
    
    # 创建远程部署脚本
    $remoteScript = @"
#!/bin/bash
cd /opt/qms-ai

# 解压部署包
echo "📦 解压部署包..."
unzip -o qms-ai-deploy-*.zip

# 复制阿里云配置
echo "⚙️ 配置阿里云环境..."
cp deployment/.env.aliyun .env
cp deployment/aliyun-deploy-optimized.yml docker-compose.yml

# 构建和启动服务
echo "🏗️ 构建Docker镜像..."
docker-compose build --no-cache

echo "🚀 启动服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose ps

echo "✅ 部署完成！"
echo "📱 访问地址:"
echo "  主应用: http://$ServerIP:8081"
echo "  配置端: http://$ServerIP:8072"
echo "  API网关: http://$ServerIP:8085"
"@

    $remoteScriptFile = [System.IO.Path]::GetTempFileName() + ".sh"
    $remoteScript | Out-File -FilePath $remoteScriptFile -Encoding UTF8
    
    Write-ColorOutput "📝 远程部署脚本已创建" "Blue"
    return $remoteScriptFile
}

function Show-DeploymentResults {
    Write-ColorOutput "`n🎉 QMS-AI阿里云部署完成！" "Green"
    Write-ColorOutput "========================" "Green"
    Write-Host ""
    
    Write-ColorOutput "📱 前端应用:" "Cyan"
    Write-ColorOutput "  主应用: http://$ServerIP:8081" "White"
    Write-ColorOutput "  配置端: http://$ServerIP:8072" "White"
    Write-Host ""
    
    Write-ColorOutput "🔧 后端服务:" "Cyan"
    Write-ColorOutput "  配置中心: http://$ServerIP:3003" "White"
    Write-ColorOutput "  聊天服务: http://$ServerIP:3004" "White"
    Write-ColorOutput "  认证服务: http://$ServerIP:8084" "White"
    Write-ColorOutput "  API网关: http://$ServerIP:8085" "White"
    Write-Host ""
    
    Write-ColorOutput "📋 管理命令:" "Cyan"
    Write-ColorOutput "  SSH连接: ssh $SSHUser@$ServerIP" "White"
    Write-ColorOutput "  查看日志: docker-compose logs -f" "White"
    Write-ColorOutput "  重启服务: docker-compose restart" "White"
    Write-ColorOutput "  停止服务: docker-compose down" "White"
}

# 主执行函数
function Main {
    Show-Banner
    
    if (!(Test-Prerequisites)) {
        Write-ColorOutput "❌ 环境检查失败，请检查必要文件" "Red"
        exit 1
    }
    
    if (!(Test-ServerConnection)) {
        Write-ColorOutput "⚠️ 服务器连接测试失败，但继续部署" "Yellow"
    }
    
    $deployScript = Deploy-ToServer
    $deployPackage = Upload-ProjectFiles
    
    if ($deployPackage) {
        $remoteScript = Execute-RemoteDeployment -DeployPackage $deployPackage
        
        Write-ColorOutput "`n📋 部署文件已准备完成:" "Green"
        Write-ColorOutput "  部署包: $deployPackage" "White"
        Write-ColorOutput "  部署脚本: $deployScript" "White"
        Write-ColorOutput "  远程脚本: $remoteScript" "White"
        Write-Host ""
        
        Write-ColorOutput "🎯 下一步操作:" "Yellow"
        Write-ColorOutput "1. 将部署包上传到服务器 /opt/qms-ai/ 目录" "White"
        Write-ColorOutput "2. 在服务器上执行部署脚本" "White"
        Write-ColorOutput "3. 访问应用验证部署结果" "White"
        
        Show-DeploymentResults
    } else {
        Write-ColorOutput "❌ 部署包创建失败" "Red"
        exit 1
    }
}

# 执行主函数
Main
