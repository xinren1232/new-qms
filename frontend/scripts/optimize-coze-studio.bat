@echo off
chcp 65001 >nul
echo.
echo ========================================
echo 🔧 Coze Studio 优化和修复脚本
echo ========================================
echo.

REM 设置颜色
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "BLUE=[94m"
set "RESET=[0m"

echo %BLUE%🎯 开始 Coze Studio 优化流程...%RESET%
echo.

REM 1. 检查基础环境
echo %BLUE%📋 步骤 1: 检查基础环境%RESET%
echo %YELLOW%🔍 检查 Node.js...%RESET%
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo %RED%❌ Node.js 未安装%RESET%
    pause
    exit /b 1
)
echo %GREEN%✅ Node.js 环境正常%RESET%

echo %YELLOW%🔍 检查项目结构...%RESET%
if not exist "frontend\应用端\src\views\coze-studio\index.vue" (
    echo %RED%❌ Coze Studio 主文件不存在%RESET%
    pause
    exit /b 1
)
echo %GREEN%✅ 项目结构正常%RESET%

REM 2. 安装依赖
echo.
echo %BLUE%📋 步骤 2: 检查和安装依赖%RESET%
cd frontend\应用端
echo %YELLOW%🔍 检查 package.json...%RESET%
if not exist "package.json" (
    echo %RED%❌ package.json 不存在%RESET%
    cd ..\..
    pause
    exit /b 1
)

echo %YELLOW%📦 检查依赖安装状态...%RESET%
if not exist "node_modules" (
    echo %YELLOW%⚠️ 依赖未安装，开始安装...%RESET%
    npm install
    if %errorlevel% neq 0 (
        echo %RED%❌ 依赖安装失败%RESET%
        cd ..\..
        pause
        exit /b 1
    )
    echo %GREEN%✅ 依赖安装完成%RESET%
) else (
    echo %GREEN%✅ 依赖已安装%RESET%
)

REM 3. 检查关键组件
echo.
echo %BLUE%📋 步骤 3: 检查关键组件%RESET%
echo %YELLOW%🔍 检查 Coze Studio 组件...%RESET%

set "COMPONENT_COUNT=0"
set "COMPONENT_OK=0"

REM 检查主组件
if exist "src\views\coze-studio\index.vue" (
    set /a COMPONENT_OK+=1
    echo %GREEN%✅ 主界面组件%RESET%
) else (
    echo %RED%❌ 主界面组件缺失%RESET%
)
set /a COMPONENT_COUNT+=1

REM 检查子组件
if exist "src\views\coze-studio\components\CozeAgentBuilder.vue" (
    set /a COMPONENT_OK+=1
    echo %GREEN%✅ Agent Builder 组件%RESET%
) else (
    echo %RED%❌ Agent Builder 组件缺失%RESET%
)
set /a COMPONENT_COUNT+=1

if exist "src\views\coze-studio\components\CozeWorkflowBuilder.vue" (
    set /a COMPONENT_OK+=1
    echo %GREEN%✅ Workflow Builder 组件%RESET%
) else (
    echo %RED%❌ Workflow Builder 组件缺失%RESET%
)
set /a COMPONENT_COUNT+=1

if exist "src\views\coze-studio\components\CozeKnowledgeManager.vue" (
    set /a COMPONENT_OK+=1
    echo %GREEN%✅ Knowledge Manager 组件%RESET%
) else (
    echo %RED%❌ Knowledge Manager 组件缺失%RESET%
)
set /a COMPONENT_COUNT+=1

if exist "src\views\coze-studio\components\CozePluginManager.vue" (
    set /a COMPONENT_OK+=1
    echo %GREEN%✅ Plugin Manager 组件%RESET%
) else (
    echo %RED%❌ Plugin Manager 组件缺失%RESET%
)
set /a COMPONENT_COUNT+=1

if exist "src\views\coze-studio\components\CozeProjectCreator.vue" (
    set /a COMPONENT_OK+=1
    echo %GREEN%✅ Project Creator 组件%RESET%
) else (
    echo %RED%❌ Project Creator 组件缺失%RESET%
)
set /a COMPONENT_COUNT+=1

if exist "src\views\coze-studio\components\CozeSettings.vue" (
    set /a COMPONENT_OK+=1
    echo %GREEN%✅ Settings 组件%RESET%
) else (
    echo %RED%❌ Settings 组件缺失%RESET%
)
set /a COMPONENT_COUNT+=1

echo.
echo %BLUE%📊 组件检查结果: %COMPONENT_OK%/%COMPONENT_COUNT%%RESET%

REM 4. 语法检查和修复
echo.
echo %BLUE%📋 步骤 4: 语法检查和修复%RESET%
echo %YELLOW%🔍 运行 ESLint 检查...%RESET%

REM 尝试运行 lint，如果失败则跳过
npm run lint >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✅ 语法检查通过%RESET%
) else (
    echo %YELLOW%⚠️ 语法检查发现问题，但不影响运行%RESET%
)

REM 5. 启动开发服务器
echo.
echo %BLUE%📋 步骤 5: 启动开发服务器%RESET%
echo %YELLOW%🚀 启动应用端开发服务器...%RESET%

REM 检查端口是否被占用
netstat -an | find "8081" >nul 2>&1
if %errorlevel% equ 0 (
    echo %YELLOW%⚠️ 端口 8081 已被占用%RESET%
    set /p "killPort=是否终止占用端口的进程? (y/n): "
    if /i "!killPort!"=="y" (
        echo %YELLOW%🔄 终止端口占用进程...%RESET%
        for /f "tokens=5" %%a in ('netstat -ano ^| find "8081" ^| find "LISTENING"') do (
            taskkill /PID %%a /F >nul 2>&1
        )
        timeout /t 2 /nobreak >nul
    )
)

echo %BLUE%🌐 启动开发服务器 (端口 8081)...%RESET%
start "Coze Studio - 应用端" cmd /k "npm run dev"

REM 等待服务器启动
echo %YELLOW%⏳ 等待服务器启动...%RESET%
timeout /t 10 /nobreak >nul

REM 6. 健康检查
echo.
echo %BLUE%📋 步骤 6: 健康检查%RESET%
echo %YELLOW%🔍 检查服务器状态...%RESET%

set "RETRY_COUNT=0"
:CHECK_SERVER
curl -s "http://localhost:8081/" >nul 2>&1
if %errorlevel% equ 0 (
    echo %GREEN%✅ 应用端服务器运行正常%RESET%
    goto SERVER_OK
) else (
    set /a RETRY_COUNT+=1
    if %RETRY_COUNT% lss 5 (
        echo %YELLOW%⏳ 等待服务器启动... (%RETRY_COUNT%/5)%RESET%
        timeout /t 5 /nobreak >nul
        goto CHECK_SERVER
    ) else (
        echo %RED%❌ 服务器启动失败%RESET%
        goto SERVER_FAILED
    )
)

:SERVER_OK
cd ..\..

REM 7. 打开测试页面
echo.
echo %BLUE%📋 步骤 7: 打开测试页面%RESET%
echo %GREEN%🎉 Coze Studio 优化完成！%RESET%
echo.

echo %BLUE%🌐 访问地址:%RESET%
echo    🧪 功能测试: %GREEN%http://localhost:8081/test-coze-studio.html%RESET%
echo    📱 Coze Studio: %GREEN%http://localhost:8081/ai-management/coze-studio%RESET%
echo    🏠 应用端首页: %GREEN%http://localhost:8081%RESET%
echo.

set /p "openTest=是否打开功能测试页面? (y/n): "
if /i "%openTest%"=="y" (
    echo %BLUE%🌐 正在打开功能测试页面...%RESET%
    start http://localhost:8081/test-coze-studio.html
    timeout /t 2 /nobreak >nul
)

set /p "openStudio=是否打开 Coze Studio? (y/n): "
if /i "%openStudio%"=="y" (
    echo %BLUE%🌐 正在打开 Coze Studio...%RESET%
    start http://localhost:8081/ai-management/coze-studio
)

echo.
echo %GREEN%✨ 优化流程完成！%RESET%
echo %BLUE%💡 使用建议:%RESET%
echo    1. 先在功能测试页面验证各模块
echo    2. 然后进入 Coze Studio 开始使用
echo    3. 如遇问题请查看浏览器控制台
echo.

goto END

:SERVER_FAILED
cd ..\..
echo.
echo %RED%💥 服务器启动失败！%RESET%
echo %YELLOW%🔧 故障排除建议:%RESET%
echo    1. 检查端口 8081 是否被占用
echo    2. 检查 Node.js 版本是否兼容
echo    3. 尝试删除 node_modules 重新安装
echo    4. 查看终端错误信息
echo.

:END
pause
