@echo off
chcp 65001 >nul
echo.
echo ========================================
echo 🎯 Coze Studio 最终部署验证
echo ========================================
echo.

REM 设置颜色
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "BLUE=[94m"
set "CYAN=[96m"
set "RESET=[0m"

echo %CYAN%🚀 开始 Coze Studio 最终部署验证...%RESET%
echo.

REM 初始化计数器
set "TOTAL_CHECKS=0"
set "PASSED_CHECKS=0"

REM 1. 环境检查
echo %BLUE%📋 步骤 1: 环境检查%RESET%
echo %YELLOW%🔍 检查 Node.js 环境...%RESET%
set /a TOTAL_CHECKS+=1
node --version >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✅ Node.js 环境正常%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ Node.js 环境异常%RESET%
)

echo %YELLOW%🔍 检查项目根目录...%RESET%
set /a TOTAL_CHECKS+=1
if exist "frontend\应用端\package.json" (
    echo %GREEN%✅ 项目结构正常%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 项目结构异常%RESET%
)

REM 2. 核心文件检查
echo.
echo %BLUE%📋 步骤 2: 核心文件检查%RESET%

REM 主界面文件
echo %YELLOW%🔍 检查主界面文件...%RESET%
set /a TOTAL_CHECKS+=1
if exist "frontend\应用端\src\views\coze-studio\index.vue" (
    echo %GREEN%✅ 主界面文件存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 主界面文件缺失%RESET%
)

REM 组件文件检查
set "COMPONENTS=CozeAgentBuilder CozeWorkflowBuilder CozeKnowledgeManager CozePluginManager CozeProjectCreator CozeSettings"
for %%c in (%COMPONENTS%) do (
    echo %YELLOW%🔍 检查 %%c 组件...%RESET%
    set /a TOTAL_CHECKS+=1
    if exist "frontend\应用端\src\views\coze-studio\components\%%c.vue" (
        echo %GREEN%✅ %%c 组件存在%RESET%
        set /a PASSED_CHECKS+=1
    ) else (
        echo %RED%❌ %%c 组件缺失%RESET%
    )
)

REM 工具文件检查
echo %YELLOW%🔍 检查工具文件...%RESET%
set /a TOTAL_CHECKS+=1
if exist "frontend\应用端\src\views\coze-studio\utils\storage.js" (
    echo %GREEN%✅ 存储工具存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 存储工具缺失%RESET%
)

set /a TOTAL_CHECKS+=1
if exist "frontend\应用端\src\views\coze-studio\utils\errorHandler.js" (
    echo %GREEN%✅ 错误处理工具存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 错误处理工具缺失%RESET%
)

set /a TOTAL_CHECKS+=1
if exist "frontend\应用端\src\views\coze-studio\utils\performance.js" (
    echo %GREEN%✅ 性能监控工具存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 性能监控工具缺失%RESET%
)

REM 3. 文档和脚本检查
echo.
echo %BLUE%📋 步骤 3: 文档和脚本检查%RESET%

echo %YELLOW%🔍 检查启动脚本...%RESET%
set /a TOTAL_CHECKS+=1
if exist "scripts\start-coze-studio.bat" (
    echo %GREEN%✅ 启动脚本存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 启动脚本缺失%RESET%
)

echo %YELLOW%🔍 检查优化脚本...%RESET%
set /a TOTAL_CHECKS+=1
if exist "scripts\optimize-coze-studio.bat" (
    echo %GREEN%✅ 优化脚本存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 优化脚本缺失%RESET%
)

echo %YELLOW%🔍 检查测试页面...%RESET%
set /a TOTAL_CHECKS+=1
if exist "frontend\应用端\public\test-coze-studio.html" (
    echo %GREEN%✅ 测试页面存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 测试页面缺失%RESET%
)

echo %YELLOW%🔍 检查用户指南...%RESET%
set /a TOTAL_CHECKS+=1
if exist "docs\Coze-Studio-User-Guide.md" (
    echo %GREEN%✅ 用户指南存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 用户指南缺失%RESET%
)

echo %YELLOW%🔍 检查部署报告...%RESET%
set /a TOTAL_CHECKS+=1
if exist "docs\Coze-Studio-Deployment-Report.md" (
    echo %GREEN%✅ 部署报告存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 部署报告缺失%RESET%
)

REM 4. 依赖检查
echo.
echo %BLUE%📋 步骤 4: 依赖检查%RESET%
cd frontend\应用端

echo %YELLOW%🔍 检查 package.json...%RESET%
set /a TOTAL_CHECKS+=1
if exist "package.json" (
    echo %GREEN%✅ package.json 存在%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ package.json 缺失%RESET%
)

echo %YELLOW%🔍 检查依赖安装...%RESET%
set /a TOTAL_CHECKS+=1
if exist "node_modules" (
    echo %GREEN%✅ 依赖已安装%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %YELLOW%⚠️ 依赖未安装，正在安装...%RESET%
    npm install >nul 2>&1
    if %errorlevel% equ 0 (
        echo %GREEN%✅ 依赖安装成功%RESET%
        set /a PASSED_CHECKS+=1
    ) else (
        echo %RED%❌ 依赖安装失败%RESET%
    )
)

REM 5. 路由检查
echo.
echo %BLUE%📋 步骤 5: 路由配置检查%RESET%
echo %YELLOW%🔍 检查路由配置...%RESET%
set /a TOTAL_CHECKS+=1
findstr /C:"coze-studio" src\router\index.js >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✅ 路由配置正常%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %RED%❌ 路由配置异常%RESET%
)

REM 6. 启动测试
echo.
echo %BLUE%📋 步骤 6: 启动测试%RESET%
echo %YELLOW%🔍 检查端口占用...%RESET%
set /a TOTAL_CHECKS+=1
netstat -an | find "8081" >nul 2>&1
if %errorlevel% equ 0 (
    echo %YELLOW%⚠️ 端口 8081 已被占用%RESET%
    echo %CYAN%ℹ️ 这可能意味着服务已在运行%RESET%
    set /a PASSED_CHECKS+=1
) else (
    echo %GREEN%✅ 端口 8081 可用%RESET%
    set /a PASSED_CHECKS+=1
)

REM 返回根目录
cd ..\..

REM 7. 计算结果
echo.
echo %BLUE%📊 验证结果统计%RESET%
echo ========================================

set /a SUCCESS_RATE=(%PASSED_CHECKS% * 100) / %TOTAL_CHECKS%

echo %CYAN%总检查项: %TOTAL_CHECKS%%RESET%
echo %GREEN%通过检查: %PASSED_CHECKS%%RESET%
echo %RED%失败检查: %TOTAL_CHECKS% - %PASSED_CHECKS% = %TOTAL_CHECKS%-%PASSED_CHECKS%%RESET%
set /a FAILED_CHECKS=%TOTAL_CHECKS%-%PASSED_CHECKS%
echo %RED%失败检查: %FAILED_CHECKS%%RESET%

if %SUCCESS_RATE% geq 90 (
    echo %GREEN%成功率: %SUCCESS_RATE%%% - 优秀%RESET%
    echo.
    echo %GREEN%🎉 Coze Studio 部署验证通过！%RESET%
    echo %GREEN%✨ 系统已准备就绪，可以正常使用%RESET%
    
    echo.
    echo %BLUE%🚀 快速启动指南:%RESET%
    echo %CYAN%1. 运行启动脚本:%RESET% scripts\start-coze-studio.bat
    echo %CYAN%2. 或运行优化脚本:%RESET% scripts\optimize-coze-studio.bat
    echo %CYAN%3. 访问测试页面:%RESET% http://localhost:8081/test-coze-studio.html
    echo %CYAN%4. 访问主界面:%RESET% http://localhost:8081/ai-management/coze-studio
    
) else if %SUCCESS_RATE% geq 70 (
    echo %YELLOW%成功率: %SUCCESS_RATE%%% - 良好%RESET%
    echo.
    echo %YELLOW%⚠️ Coze Studio 基本功能正常，但存在一些问题%RESET%
    echo %YELLOW%🔧 建议修复失败的检查项以获得最佳体验%RESET%
    
) else (
    echo %RED%成功率: %SUCCESS_RATE%%% - 需要修复%RESET%
    echo.
    echo %RED%❌ Coze Studio 存在严重问题%RESET%
    echo %RED%🚨 请修复失败的检查项后再使用%RESET%
)

echo.
echo %BLUE%📚 相关文档:%RESET%
echo %CYAN%• 用户指南:%RESET% docs\Coze-Studio-User-Guide.md
echo %CYAN%• 部署报告:%RESET% docs\Coze-Studio-Deployment-Report.md
echo %CYAN%• 功能测试:%RESET% frontend\应用端\public\test-coze-studio.html

echo.
echo %BLUE%🛠️ 故障排除:%RESET%
echo %CYAN%• 如果依赖安装失败，请检查网络连接%RESET%
echo %CYAN%• 如果端口被占用，请关闭其他服务或更改端口%RESET%
echo %CYAN%• 如果组件缺失，请重新运行部署脚本%RESET%
echo %CYAN%• 如果路由异常，请检查 router/index.js 文件%RESET%

echo.
echo %GREEN%✨ 验证完成！%RESET%
echo %BLUE%💡 建议先运行测试页面验证功能，然后开始使用 Coze Studio%RESET%
echo.

pause
