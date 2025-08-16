@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title QMS-AI 代码上传到服务器脚本

echo.
echo 🚀 QMS-AI 代码上传到服务器脚本 (软件小白专用)
echo ================================================
echo 此脚本将为您完成：
echo 1. 打包本地代码
echo 2. 上传到阿里云服务器 (47.108.152.16)
echo 3. 在服务器上执行部署
echo 4. 验证系统运行状态
echo.

:: 设置服务器信息
set "SERVER_IP=47.108.152.16"
set "SERVER_USER=root"
set "SERVER_PATH=/root/QMS01"
set "SERVER_PASSWORD=Zxylsy.99"

echo [1/5] 服务器信息确认...
echo 🖥️  服务器: %SERVER_USER%@%SERVER_IP%
echo 📁 路径: %SERVER_PATH%
echo.

:: 检查必要工具
echo [2/5] 检查上传工具...

:: 检查是否有scp
where scp >nul 2>&1
if errorlevel 1 (
    echo ❌ 未找到scp工具
    echo.
    echo 💡 解决方案：
    echo    1. 安装Git for Windows (推荐)
    echo       下载地址: https://git-scm.com/download/win
    echo    2. 或使用WinSCP等图形化工具手动上传
    echo    3. 或安装Windows Subsystem for Linux (WSL)
    echo.
    echo ⚠️  请安装上述工具后重新运行此脚本
    pause
    exit /b 1
) else (
    echo ✅ 找到scp工具
)

:: 创建部署包
echo.
echo [3/5] 创建部署包...
set "timestamp=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "timestamp=%timestamp: =0%"
set "package_name=qms-update-%timestamp%"

echo 📦 正在打包代码文件...

:: 创建临时目录
if exist "temp_upload" rmdir /s /q "temp_upload"
mkdir "temp_upload"

:: 复制需要的文件和目录
if exist "backend" (
    xcopy /E /I /Q "backend" "temp_upload\backend" >nul
    echo ✅ 已打包 backend 目录
)

if exist "frontend" (
    xcopy /E /I /Q "frontend" "temp_upload\frontend" >nul
    echo ✅ 已打包 frontend 目录
)

if exist "nginx" (
    xcopy /E /I /Q "nginx" "temp_upload\nginx" >nul
    echo ✅ 已打包 nginx 目录
)

if exist "config" (
    xcopy /E /I /Q "config" "temp_upload\config" >nul
    echo ✅ 已打包 config 目录
)

if exist "deployment" (
    xcopy /E /I /Q "deployment" "temp_upload\deployment" >nul
    echo ✅ 已打包 deployment 目录
)

:: 复制重要文件
if exist "docker-compose.yml" (
    copy "docker-compose.yml" "temp_upload\" >nul
    echo ✅ 已打包 docker-compose.yml
)

if exist "QMS-AI-Complete-Deploy.sh" (
    copy "QMS-AI-Complete-Deploy.sh" "temp_upload\" >nul
    echo ✅ 已打包 QMS-AI-Complete-Deploy.sh
)

if exist "package.json" (
    copy "package.json" "temp_upload\" >nul
    echo ✅ 已打包 package.json
)

echo ✅ 代码打包完成

:: 上传到服务器
echo.
echo [4/5] 上传到服务器...
echo 🚀 正在上传文件到服务器...
echo ⚠️  可能需要输入服务器密码: %SERVER_PASSWORD%
echo.

:: 使用scp上传整个目录
scp -r -o StrictHostKeyChecking=no temp_upload/* %SERVER_USER%@%SERVER_IP%:%SERVER_PATH%/

if errorlevel 1 (
    echo ❌ 上传失败
    echo.
    echo 💡 可能的原因：
    echo    1. 网络连接问题
    echo    2. 服务器密码错误
    echo    3. 服务器路径不存在
    echo.
    echo 🔧 手动上传方法：
    echo    1. 使用WinSCP连接到 %SERVER_IP%
    echo    2. 用户名: %SERVER_USER%，密码: %SERVER_PASSWORD%
    echo    3. 将 temp_upload 目录中的所有文件上传到 %SERVER_PATH%
    echo.
    pause
    exit /b 1
) else (
    echo ✅ 文件上传成功
)

:: 在服务器上执行部署
echo.
echo [5/5] 在服务器上执行部署...
echo 🔧 正在服务器上执行部署脚本...
echo.

:: 连接服务器并执行部署脚本
ssh -o StrictHostKeyChecking=no %SERVER_USER%@%SERVER_IP% "cd %SERVER_PATH% && chmod +x QMS-AI-Complete-Deploy.sh && ./QMS-AI-Complete-Deploy.sh"

if errorlevel 1 (
    echo ⚠️ 自动部署可能有问题
    echo.
    echo 🔧 手动部署方法：
    echo    1. 使用SSH连接到服务器: ssh %SERVER_USER%@%SERVER_IP%
    echo    2. 进入目录: cd %SERVER_PATH%
    echo    3. 执行部署: chmod +x QMS-AI-Complete-Deploy.sh && ./QMS-AI-Complete-Deploy.sh
    echo.
) else (
    echo ✅ 服务器部署完成
)

:: 清理临时文件
echo.
echo 🧹 清理临时文件...
if exist "temp_upload" rmdir /s /q "temp_upload"
echo ✅ 临时文件清理完成

:: 显示访问信息
echo.
echo 🎉 QMS-AI系统更新完成！
echo.
echo 📋 访问地址:
echo   🏠 主应用: http://%SERVER_IP%/
echo   ⚙️ 配置端: http://%SERVER_IP%/config/
echo   🔧 API网关: http://%SERVER_IP%:8085/
echo.
echo 📊 如果服务异常，请等待几分钟让服务完全启动
echo.

pause
