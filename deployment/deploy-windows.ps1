# QMS-AI阿里云一键部署脚本 (Windows版本)
# 目标服务器: 47.108.152.16

param(
    [string]$ServerIP = "47.108.152.16",
    [string]$SSHUser = "root",
    [string]$SSHPassword = "Zxylsy.99",
    [int]$SSHPort = 22
)

# 颜色定义
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    Cyan = "Cyan"
    Magenta = "Magenta"
}

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Show-Banner {
    Write-ColorOutput "╔══════════════════════════════════════════════════════════════╗" $Colors.Magenta
    Write-ColorOutput "║                QMS-AI阿里云一键部署工具                      ║" $Colors.Magenta
    Write-ColorOutput "║              目标服务器: 47.108.152.16                      ║" $Colors.Magenta
    Write-ColorOutput "╚══════════════════════════════════════════════════════════════╝" $Colors.Magenta
    Write-Host ""
}

function Test-Prerequisites {
    Write-ColorOutput "🔍 检查本地环境..." $Colors.Cyan
    
    # 检查必要工具
    $tools = @("git", "ssh", "scp")
    foreach ($tool in $tools) {
        if (!(Get-Command $tool -ErrorAction SilentlyContinue)) {
            Write-ColorOutput "❌ $tool 未安装或不在PATH中" $Colors.Red
            Write-ColorOutput "请安装Git for Windows或OpenSSH" $Colors.Yellow
            return $false
        }
    }
    
    # 检查部署文件
    $requiredFiles = @(
        "deployment/aliyun-deploy-optimized.yml",
        "deployment/.env.production",
        "deployment/mysql/init.sql"
    )
    
    foreach ($file in $requiredFiles) {
        if (!(Test-Path $file)) {
            Write-ColorOutput "❌ 缺少必要文件: $file" $Colors.Red
            return $false
        }
    }
    
    Write-ColorOutput "✅ 本地环境检查完成" $Colors.Green
    return $true
}

function Test-ServerConnection {
    Write-ColorOutput "🔍 测试服务器连接..." $Colors.Cyan

    # 优先使用 PuTTY 的 plink（支持 -pw 非交互登录），否则回退到 ssh
    if (Get-Command plink -ErrorAction SilentlyContinue) {
        try {
            $null = plink -ssh -P $SSHPort -pw $SSHPassword "$SSHUser@${ServerIP}" "echo 'SSH连接测试成功'" 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput "✅ 服务器连接正常 (plink)" $Colors.Green
                return $true
            }
        } catch {
            Write-ColorOutput "❌ 使用plink连接失败: $($_.Exception.Message)" $Colors.Red
        }
    }

    # 创建临时SSH配置并使用 openssh 的 ssh（将提示输入密码）
    $sshConfig = @"
Host qms-deploy
    HostName ${ServerIP}
    User $SSHUser
    Port $SSHPort
    StrictHostKeyChecking no
    UserKnownHostsFile /dev/null
"@
    $sshConfigPath = "$env:TEMP\ssh_config_qms"
    $sshConfig | Out-File -FilePath $sshConfigPath -Encoding UTF8

    try {
        $null = ssh -F $sshConfigPath -o ConnectTimeout=10 qms-deploy "echo 'SSH连接测试成功'" 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ 服务器连接正常 (ssh)" $Colors.Green
            return $true
        } else {
            Write-ColorOutput "❌ 无法连接到服务器 ${ServerIP}" $Colors.Red
            Write-ColorOutput "请检查:" $Colors.Yellow
            Write-ColorOutput "  1. 服务器IP是否正确" $Colors.Yellow
            Write-ColorOutput "  2. SSH服务是否启动" $Colors.Yellow
            Write-ColorOutput "  3. 防火墙是否开放22端口" $Colors.Yellow
            Write-ColorOutput "  4. 密码是否正确" $Colors.Yellow
            return $false
        }
    } catch {
        Write-ColorOutput "❌ SSH连接测试失败: $($_.Exception.Message)" $Colors.Red
        return $false
    } finally {
        Remove-Item $sshConfigPath -ErrorAction SilentlyContinue
    }
}

function Prepare-DeploymentFiles {
    Write-ColorOutput "📦 准备部署文件..." $Colors.Cyan
    
    # 创建临时目录
    $tempDir = New-TemporaryFile | ForEach-Object { Remove-Item $_; New-Item -ItemType Directory -Path $_ }
    Write-ColorOutput "临时目录: $($tempDir.FullName)" $Colors.Blue
    
    # 复制项目文件
    $excludePatterns = @("*.git*", "node_modules", ".venv", "__pycache__", "*.log", "*.tmp")
    
    Get-ChildItem -Path . -Recurse | Where-Object {
        $item = $_
        $shouldExclude = $false
        foreach ($pattern in $excludePatterns) {
            if ($item.Name -like $pattern -or $item.FullName -like "*\$pattern\*") {
                $shouldExclude = $true
                break
            }
        }
        return !$shouldExclude
    } | Copy-Item -Destination { Join-Path $tempDir.FullName $_.FullName.Substring((Get-Location).Path.Length + 1) } -Force
    
    # 复制生产环境配置
    Copy-Item "deployment\.env.production" "$($tempDir.FullName)\.env" -Force
    
    Write-ColorOutput "✅ 部署文件准备完成" $Colors.Green
    return $tempDir.FullName
}

function Upload-Files {
    param([string]$TempDir)

    Write-ColorOutput "📤 上传文件到服务器..." $Colors.Cyan

    try {
        # 创建服务器目录
        ssh -p $SSHPort $SSHUser@$ServerIP "mkdir -p /tmp/qms-deploy"

        # 目标地址字符串（避免 PowerShell 变量解析问题）
        $dest = "${SSHUser}@${ServerIP}:/tmp/qms-deploy/"

        # 优先使用 PuTTY 的 pscp（支持 -pw 非交互登录），否则回退到 scp
        if (Get-Command pscp -ErrorAction SilentlyContinue) {
            pscp -r -P $SSHPort -pw $SSHPassword "$TempDir\*" $dest
        } else {
            scp -r -P $SSHPort "$TempDir\*" $dest
        }

        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ 文件上传完成" $Colors.Green
            return $true
        } else {
            Write-ColorOutput "❌ 文件上传失败" $Colors.Red
            return $false
        }
    } catch {
        Write-ColorOutput "❌ 上传过程中出错: $($_.Exception.Message)" $Colors.Red
        return $false
    }
}

function Deploy-OnServer {
    Write-ColorOutput "🚀 在服务器上执行部署..." $Colors.Cyan
    
    $deployScript = @'
set -e

echo "🔧 开始服务器端部署..."

# 创建项目目录
sudo mkdir -p /opt/qms
sudo mkdir -p /opt/qms-backup

# 备份现有部署（如果存在）
if [ -d "/opt/qms" ] && [ "$(ls -A /opt/qms)" ]; then
    echo "📦 备份现有部署..."
    sudo cp -r /opt/qms /opt/qms-backup/backup-$(date +%Y%m%d-%H%M%S)
fi

# 复制新文件
sudo cp -r /tmp/qms-deploy/* /opt/qms/
sudo chown -R root:root /opt/qms

# 进入项目目录
cd /opt/qms

# 给脚本执行权限
chmod +x deployment/*.sh

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "🐳 安装Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    systemctl start docker
    systemctl enable docker
    
    # 配置Docker镜像加速器
    mkdir -p /etc/docker
    cat > /etc/docker/daemon.json << 'DOCKER_EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "3"
  }
}
DOCKER_EOF
    systemctl restart docker
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "🔧 安装Docker Compose..."
    DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d'"' -f4)
    curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
fi

# 创建必要目录
mkdir -p {logs,data,config,ssl,backup,nginx/conf.d,prometheus,grafana/provisioning,mysql}
chmod 755 logs data config ssl backup mysql

# 停止现有服务
if [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
    echo "🛑 停止现有服务..."
    docker-compose -f deployment/aliyun-deploy-optimized.yml down 2>/dev/null || true
fi

# 清理旧镜像
echo "🧹 清理旧镜像..."
docker system prune -f

# 构建和启动服务
echo "🔨 构建和启动服务..."
docker-compose -f deployment/aliyun-deploy-optimized.yml build --no-cache
docker-compose -f deployment/aliyun-deploy-optimized.yml up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 60

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose -f deployment/aliyun-deploy-optimized.yml ps

echo "✅ 服务器端部署完成！"
'@
    
    try {
        $deployScript | ssh -p $SSHPort $SSHUser@$ServerIP "bash"
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ 服务器端部署成功" $Colors.Green
            return $true
        } else {
            Write-ColorOutput "❌ 服务器端部署失败" $Colors.Red
            return $false
        }
    } catch {
        Write-ColorOutput "❌ 部署过程中出错: $($_.Exception.Message)" $Colors.Red
        return $false
    }
}

function Test-Deployment {
    Write-ColorOutput "🔍 检查部署结果..." $Colors.Cyan
    
    # 检查服务状态
    Write-ColorOutput "检查服务状态..." $Colors.Blue
    ssh -p $SSHPort $SSHUser@$ServerIP "cd /opt/qms && docker-compose -f deployment/aliyun-deploy-optimized.yml ps"
    
    # 检查服务健康状态
    Write-Host ""
    Write-ColorOutput "检查服务健康状态..." $Colors.Blue
    
    $services = @(
        @{Port=8084; Name="认证服务"},
        @{Port=8083; Name="配置中心"},
        @{Port=3004; Name="聊天服务"}
    )
    
    foreach ($service in $services) {
        try {
            $result = ssh -p $SSHPort $SSHUser@$ServerIP "curl -f -s http://localhost:$($service.Port)/health" 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput "  $($service.Name): ✅ 健康" $Colors.Green
            } else {
                Write-ColorOutput "  $($service.Name): ⚠️ 启动中或异常" $Colors.Yellow
            }
        } catch {
            Write-ColorOutput "  $($service.Name): ❌ 异常" $Colors.Red
        }
    }
}

function Show-Results {
    Write-Host ""
    Write-ColorOutput "🎉 QMS-AI阿里云部署完成！" $Colors.Green
    Write-ColorOutput "========================================" $Colors.Green
    Write-Host ""
    Write-ColorOutput "📋 访问信息:" $Colors.Blue
    Write-ColorOutput "  🌐 主应用: http://$ServerIP:8081" $Colors.White
    Write-ColorOutput "  💬 AI聊天: http://$ServerIP:8081/ai-management/chat" $Colors.White
    Write-ColorOutput "  🤖 模型管理: http://$ServerIP:8081/ai-management/models" $Colors.White
    Write-ColorOutput "  📊 监控面板: http://$ServerIP:3000" $Colors.White
    Write-Host ""
    Write-ColorOutput "🔧 服务端口:" $Colors.Blue
    Write-ColorOutput "  🔐 认证服务: $ServerIP:8084" $Colors.White
    Write-ColorOutput "  ⚙️ 配置中心: $ServerIP:8083" $Colors.White
    Write-ColorOutput "  💬 聊天服务: $ServerIP:3004" $Colors.White
    Write-ColorOutput "  🌐 前端应用: $ServerIP:8081" $Colors.White
    Write-ColorOutput "  🗄️ MySQL数据库: $ServerIP:3306" $Colors.White
    Write-ColorOutput "  💾 Redis缓存: $ServerIP:6379" $Colors.White
    Write-Host ""
    Write-ColorOutput "📝 管理命令:" $Colors.Yellow
    Write-ColorOutput "  登录服务器: ssh -p $SSHPort $SSHUser@$ServerIP" $Colors.White
    Write-ColorOutput "  查看服务: cd /opt/qms && docker-compose -f deployment/aliyun-deploy-optimized.yml ps" $Colors.White
    Write-ColorOutput "  查看日志: cd /opt/qms && docker-compose -f deployment/aliyun-deploy-optimized.yml logs -f" $Colors.White
    Write-ColorOutput "  重启服务: cd /opt/qms && docker-compose -f deployment/aliyun-deploy-optimized.yml restart" $Colors.White
}

function Cleanup {
    param([string]$TempDir)
    
    if ($TempDir -and (Test-Path $TempDir)) {
        Remove-Item $TempDir -Recurse -Force
        Write-ColorOutput "✅ 清理临时文件完成" $Colors.Green
    }
}

# 主函数
function Main {
    Show-Banner
    
    Write-ColorOutput "开始QMS-AI阿里云一键部署..." $Colors.Cyan
    Write-Host ""
    
    if (!(Test-Prerequisites)) {
        exit 1
    }
    
    if (!(Test-ServerConnection)) {
        exit 1
    }
    
    $tempDir = Prepare-DeploymentFiles
    
    try {
        if (!(Upload-Files $tempDir)) {
            exit 1
        }
        
        if (!(Deploy-OnServer)) {
            exit 1
        }
        
        Test-Deployment
        Show-Results
        
        Write-ColorOutput "🎉 部署流程完成！" $Colors.Green
    } finally {
        Cleanup $tempDir
    }
}

# 执行主函数
Main
