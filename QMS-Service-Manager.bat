@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 颜色定义
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "MAGENTA=[95m"
set "CYAN=[96m"
set "WHITE=[97m"
set "RESET=[0m"

echo %CYAN%🚀 QMS-AI 全面服务管理器 v2.0%RESET%
echo %WHITE%========================================%RESET%
echo.

:menu
echo %YELLOW%请选择操作:%RESET%
echo %WHITE%1. 🔍 全面检查所有服务和端口%RESET%
echo %WHITE%2. 🚀 逐一启动所有服务%RESET%
echo %WHITE%3. 🛑 停止所有服务%RESET%
echo %WHITE%4. 📊 实时监控服务状态%RESET%
echo %WHITE%5. 🔄 重启所有服务%RESET%
echo %WHITE%6. 🧪 测试服务连接%RESET%
echo %WHITE%7. 📋 查看服务日志%RESET%
echo %WHITE%8. 🔧 服务故障诊断%RESET%
echo %WHITE%9. 退出%RESET%
echo.
set /p choice=%CYAN%请输入选择 (1-9): %RESET%

if "%choice%"=="1" goto check_all_services
if "%choice%"=="2" goto start_all_services
if "%choice%"=="3" goto stop_all_services
if "%choice%"=="4" goto monitor_services
if "%choice%"=="5" goto restart_all_services
if "%choice%"=="6" goto test_connections
if "%choice%"=="7" goto view_logs
if "%choice%"=="8" goto diagnose_services
if "%choice%"=="9" goto exit
echo %RED%❌ 无效选择，请重新输入%RESET%
goto menu

:check_all_services
echo.
echo %CYAN%🔍 全面检查所有服务和端口...%RESET%
echo %WHITE%========================================%RESET%
call :check_environment
call :check_dependencies
call :check_ports
call :check_processes
call :check_directories
echo.
echo %GREEN%✅ 检查完成！%RESET%
pause
goto menu

:start_all_services
echo.
echo %CYAN%🚀 逐一启动所有服务...%RESET%
echo %WHITE%========================================%RESET%
call :start_services_sequentially
echo.
echo %GREEN%✅ 所有服务启动完成！%RESET%
pause
goto menu

:stop_all_services
echo.
echo %CYAN%🛑 停止所有服务...%RESET%
echo %WHITE%========================================%RESET%
call QMS-Stop-All.bat
echo.
echo %GREEN%✅ 所有服务已停止！%RESET%
pause
goto menu

:monitor_services
echo.
echo %CYAN%📊 实时监控服务状态...%RESET%
echo %WHITE%========================================%RESET%
call :real_time_monitor
goto menu

:restart_all_services
echo.
echo %CYAN%🔄 重启所有服务...%RESET%
echo %WHITE%========================================%RESET%
call QMS-Stop-All.bat
echo %YELLOW%⏳ 等待5秒后重新启动...%RESET%
timeout /t 5 /nobreak >nul
call :start_services_sequentially
echo.
echo %GREEN%✅ 所有服务重启完成！%RESET%
pause
goto menu

:test_connections
echo.
echo %CYAN%🧪 测试服务连接...%RESET%
echo %WHITE%========================================%RESET%
call :test_all_connections
echo.
echo %GREEN%✅ 连接测试完成！%RESET%
pause
goto menu

:view_logs
echo.
echo %CYAN%📋 查看服务日志...%RESET%
echo %WHITE%========================================%RESET%
call :show_recent_logs
pause
goto menu

:diagnose_services
echo.
echo %CYAN%🔧 服务故障诊断...%RESET%
echo %WHITE%========================================%RESET%
call :diagnose_issues
echo.
echo %GREEN%✅ 诊断完成！%RESET%
pause
goto menu

:exit
echo.
echo %CYAN%👋 感谢使用 QMS-AI 服务管理器！%RESET%
pause
exit

REM ========================================
REM 检查函数定义
REM ========================================

:check_environment
echo %BLUE%🔍 检查运行环境...%RESET%
echo.

REM 检查Node.js
node --version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('node --version') do set node_version=%%i
    echo %GREEN%✅ Node.js: !node_version!%RESET%
) else (
    echo %RED%❌ Node.js: 未安装%RESET%
)

REM 检查npm
npm --version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('npm --version') do set npm_version=%%i
    echo %GREEN%✅ npm: !npm_version!%RESET%
) else (
    echo %RED%❌ npm: 未安装%RESET%
)

REM 检查pnpm
pnpm --version >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=*" %%i in ('pnpm --version') do set pnpm_version=%%i
    echo %GREEN%✅ pnpm: !pnpm_version!%RESET%
) else (
    echo %YELLOW%⚠️ pnpm: 未安装 (推荐安装)%RESET%
)

echo.
goto :eof

:check_dependencies
echo %BLUE%🔍 检查项目依赖...%RESET%
echo.

REM 检查后端依赖
if exist "backend\nodejs\node_modules" (
    echo %GREEN%✅ 后端依赖: 已安装%RESET%
) else (
    echo %RED%❌ 后端依赖: 未安装%RESET%
    echo %YELLOW%   建议运行: cd backend\nodejs && npm install%RESET%
)

REM 检查应用端依赖
if exist "frontend\应用端\node_modules" (
    echo %GREEN%✅ 应用端依赖: 已安装%RESET%
) else (
    echo %RED%❌ 应用端依赖: 未安装%RESET%
    echo %YELLOW%   建议运行: cd frontend\应用端 && npm install%RESET%
)

REM 检查配置端依赖
if exist "frontend\配置端\node_modules" (
    echo %GREEN%✅ 配置端依赖: 已安装%RESET%
) else (
    echo %RED%❌ 配置端依赖: 未安装%RESET%
    echo %YELLOW%   建议运行: cd frontend\配置端 && npm install%RESET%
)

echo.
goto :eof

:check_ports
echo %BLUE%🔍 检查端口占用情况...%RESET%
echo.

REM 定义所有需要检查的端口
set ports=3003 3004 3005 3007 3008 3009 8072 8081 8084 8085 6379 6380

for %%p in (%ports%) do (
    netstat -ano | findstr ":%%p " >nul 2>&1
    if !errorlevel! equ 0 (
        echo %GREEN%✅ 端口%%p: 已占用%RESET%
    ) else (
        echo %YELLOW%⚠️ 端口%%p: 空闲%RESET%
    )
)

echo.
goto :eof

:check_processes
echo %BLUE%🔍 检查进程状态...%RESET%
echo.

REM 检查Node.js进程
tasklist /fi "imagename eq node.exe" 2>nul | findstr node.exe >nul
if %errorlevel% equ 0 (
    echo %GREEN%✅ Node.js进程: 运行中%RESET%
    for /f "tokens=1,2" %%a in ('tasklist /fi "imagename eq node.exe" ^| findstr node.exe') do (
        echo %WHITE%   进程: %%a PID: %%b%RESET%
    )
) else (
    echo %YELLOW%⚠️ Node.js进程: 未运行%RESET%
)

echo.
goto :eof

:check_directories
echo %BLUE%🔍 检查目录结构...%RESET%
echo.

REM 检查关键目录
set dirs=backend\nodejs frontend\应用端 frontend\配置端 logs config

for %%d in (%dirs%) do (
    if exist "%%d" (
        echo %GREEN%✅ %%d: 存在%RESET%
    ) else (
        echo %RED%❌ %%d: 不存在%RESET%
    )
)

echo.
goto :eof

:start_services_sequentially
echo %BLUE%🚀 按顺序启动所有服务...%RESET%
echo.

REM 首先检查并安装依赖
call :install_dependencies_if_needed

echo %YELLOW%📋 启动顺序:%RESET%
echo %WHITE%1. 配置中心服务 (端口3003)%RESET%
echo %WHITE%2. 聊天服务 (端口3004)%RESET%
echo %WHITE%3. 认证服务 (端口8084)%RESET%
echo %WHITE%4. 高级功能服务 (端口3009)%RESET%
echo %WHITE%5. 导出服务 (端口3008)%RESET%
echo %WHITE%6. Coze Studio服务 (端口3005)%RESET%
echo %WHITE%7. API网关 (端口8085)%RESET%
echo %WHITE%8. 应用端 (端口8081)%RESET%
echo %WHITE%9. 配置端 (端口8072)%RESET%
echo.

REM 1. 启动配置中心服务
echo %CYAN%⚙️ 启动配置中心服务 (端口3003)...%RESET%
start "QMS AI - 配置中心" cmd /k "cd backend\nodejs && node lightweight-config-service.js"
call :wait_for_service 3003 "配置中心"

REM 2. 启动聊天服务
echo %CYAN%💬 启动聊天服务 (端口3004)...%RESET%
start "QMS AI - 聊天服务" cmd /k "cd backend\nodejs && node chat-service.js"
call :wait_for_service 3004 "聊天服务"

REM 3. 启动认证服务
echo %CYAN%🔐 启动认证服务 (端口8084)...%RESET%
start "QMS AI - 认证服务" cmd /k "cd backend\nodejs && node auth-service.js"
call :wait_for_service 8084 "认证服务"

REM 4. 启动高级功能服务
echo %CYAN%🧠 启动高级功能服务 (端口3009)...%RESET%
start "QMS AI - 高级功能" cmd /k "cd backend\nodejs && node advanced-features-service.js"
call :wait_for_service 3009 "高级功能服务"

REM 5. 启动导出服务
echo %CYAN%📤 启动导出服务 (端口3008)...%RESET%
start "QMS AI - 导出服务" cmd /k "cd backend\nodejs && node export-service-standalone.js"
call :wait_for_service 3008 "导出服务"

REM 6. 启动Coze Studio服务
echo %CYAN%🎨 启动Coze Studio服务 (端口3005)...%RESET%
start "QMS AI - Coze Studio" cmd /k "cd backend\nodejs && node coze-studio-service.js"
call :wait_for_service 3005 "Coze Studio服务"

REM 7. 启动API网关
echo %CYAN%🌐 启动API网关 (端口8085)...%RESET%
start "QMS AI - API网关" cmd /k "cd backend\nodejs && node api-gateway.js"
call :wait_for_service 8085 "API网关"

REM 8. 启动应用端
echo %CYAN%🖥️ 启动应用端 (端口8081)...%RESET%
start "QMS AI - 应用端" cmd /k "cd frontend\应用端 && npm run dev"
call :wait_for_service 8081 "应用端"

REM 9. 启动配置端
echo %CYAN%🔧 启动配置端 (端口8072)...%RESET%
start "QMS AI - 配置端" cmd /k "cd frontend\配置端 && npm run serve"
call :wait_for_service 8072 "配置端"

echo.
echo %GREEN%🎉 所有服务启动完成！%RESET%
echo.
call :show_service_urls
goto :eof

:install_dependencies_if_needed
echo %BLUE%📦 检查并安装依赖...%RESET%

REM 检查后端依赖
if not exist "backend\nodejs\node_modules" (
    echo %YELLOW%📦 安装后端依赖...%RESET%
    cd backend\nodejs
    npm install
    cd ..\..
)

REM 检查应用端依赖
if not exist "frontend\应用端\node_modules" (
    echo %YELLOW%📦 安装应用端依赖...%RESET%
    cd frontend\应用端
    npm install
    cd ..\..
)

REM 检查配置端依赖
if not exist "frontend\配置端\node_modules" (
    echo %YELLOW%📦 安装配置端依赖...%RESET%
    cd frontend\配置端
    npm install
    cd ..\..
)

goto :eof

:wait_for_service
set port=%1
set service_name=%2
set max_wait=30
set wait_count=0

echo %YELLOW%⏳ 等待%service_name%启动...%RESET%

:wait_loop
timeout /t 2 /nobreak >nul
set /a wait_count+=2

netstat -ano | findstr ":%port% " >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✅ %service_name%已启动 (端口%port%)%RESET%
    goto :eof
)

if %wait_count% geq %max_wait% (
    echo %RED%❌ %service_name%启动超时 (端口%port%)%RESET%
    goto :eof
)

goto wait_loop

:show_service_urls
echo %CYAN%🌐 服务访问地址:%RESET%
echo %WHITE%========================================%RESET%
echo %WHITE%• 配置中心:     http://localhost:3003%RESET%
echo %WHITE%• 聊天服务:     http://localhost:3004%RESET%
echo %WHITE%• 认证服务:     http://localhost:8084%RESET%
echo %WHITE%• 高级功能:     http://localhost:3009%RESET%
echo %WHITE%• 导出服务:     http://localhost:3008%RESET%
echo %WHITE%• Coze Studio:  http://localhost:3005%RESET%
echo %WHITE%• API网关:      http://localhost:8085%RESET%
echo %WHITE%• 应用端:       http://localhost:8081%RESET%
echo %WHITE%• 配置端:       http://localhost:8072%RESET%
echo %WHITE%========================================%RESET%
goto :eof

:real_time_monitor
echo %BLUE%📊 实时监控服务状态 (按Ctrl+C退出)...%RESET%
echo.

:monitor_loop
cls
echo %CYAN%🚀 QMS-AI 实时服务监控%RESET%
echo %WHITE%========================================%RESET%
echo %YELLOW%更新时间: %date% %time%%RESET%
echo.

REM 检查各服务状态
call :check_service_status 3003 "配置中心"
call :check_service_status 3004 "聊天服务"
call :check_service_status 8084 "认证服务"
call :check_service_status 3009 "高级功能"
call :check_service_status 3008 "导出服务"
call :check_service_status 3005 "Coze Studio"
call :check_service_status 8085 "API网关"
call :check_service_status 8081 "应用端"
call :check_service_status 8072 "配置端"

echo.
echo %WHITE%按任意键刷新，Ctrl+C退出...%RESET%
timeout /t 5 /nobreak >nul
goto monitor_loop

:check_service_status
set port=%1
set service_name=%2

netstat -ano | findstr ":%port% " >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✅ %service_name% (端口%port%): 运行中%RESET%
) else (
    echo %RED%❌ %service_name% (端口%port%): 未运行%RESET%
)
goto :eof

:test_all_connections
echo %BLUE%🧪 测试所有服务连接...%RESET%
echo.

REM 测试HTTP服务
call :test_http_service "http://localhost:3003/health" "配置中心"
call :test_http_service "http://localhost:3004/health" "聊天服务"
call :test_http_service "http://localhost:8084/health" "认证服务"
call :test_http_service "http://localhost:3009/health" "高级功能"
call :test_http_service "http://localhost:3008/health" "导出服务"
call :test_http_service "http://localhost:3005/health" "Coze Studio"
call :test_http_service "http://localhost:8085/health" "API网关"
call :test_http_service "http://localhost:8081" "应用端"
call :test_http_service "http://localhost:8072" "配置端"

goto :eof

:test_http_service
set url=%1
set service_name=%2

echo %YELLOW%🔍 测试%service_name%...%RESET%
curl -s --connect-timeout 5 %url% >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✅ %service_name%连接正常%RESET%
) else (
    echo %RED%❌ %service_name%连接失败%RESET%
)
goto :eof

:show_recent_logs
echo %BLUE%📋 显示最近的服务日志...%RESET%
echo.

if exist "backend\nodejs\logs" (
    echo %YELLOW%📄 后端服务日志:%RESET%
    if exist "backend\nodejs\logs\combined.log" (
        echo %WHITE%--- 最近10行 combined.log ---%RESET%
        powershell "Get-Content 'backend\nodejs\logs\combined.log' -Tail 10"
        echo.
    )
    if exist "backend\nodejs\logs\error.log" (
        echo %WHITE%--- 最近5行 error.log ---%RESET%
        powershell "Get-Content 'backend\nodejs\logs\error.log' -Tail 5"
        echo.
    )
) else (
    echo %YELLOW%⚠️ 未找到后端日志目录%RESET%
)

echo %YELLOW%📄 系统进程信息:%RESET%
tasklist /fi "imagename eq node.exe" 2>nul | findstr node.exe
echo.

goto :eof

:diagnose_issues
echo %BLUE%🔧 诊断常见问题...%RESET%
echo.

REM 检查端口冲突
echo %YELLOW%🔍 检查端口冲突...%RESET%
set conflict_found=0
for %%p in (3003 3004 3005 3008 3009 8072 8081 8084 8085) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p "') do (
        if not "%%a"=="" (
            echo %RED%⚠️ 端口%%p被进程%%a占用%RESET%
            set conflict_found=1
        )
    )
)
if %conflict_found%==0 (
    echo %GREEN%✅ 无端口冲突%RESET%
)
echo.

REM 检查依赖问题
echo %YELLOW%🔍 检查依赖问题...%RESET%
if not exist "backend\nodejs\node_modules" (
    echo %RED%❌ 后端依赖未安装%RESET%
    echo %WHITE%   解决方案: cd backend\nodejs && npm install%RESET%
)
if not exist "frontend\应用端\node_modules" (
    echo %RED%❌ 应用端依赖未安装%RESET%
    echo %WHITE%   解决方案: cd frontend\应用端 && npm install%RESET%
)
if not exist "frontend\配置端\node_modules" (
    echo %RED%❌ 配置端依赖未安装%RESET%
    echo %WHITE%   解决方案: cd frontend\配置端 && npm install%RESET%
)

REM 检查关键文件
echo %YELLOW%🔍 检查关键文件...%RESET%
set files=backend\nodejs\chat-service.js backend\nodejs\lightweight-config-service.js frontend\应用端\package.json frontend\配置端\package.json
for %%f in (%files%) do (
    if exist "%%f" (
        echo %GREEN%✅ %%f: 存在%RESET%
    ) else (
        echo %RED%❌ %%f: 缺失%RESET%
    )
)

goto :eof
