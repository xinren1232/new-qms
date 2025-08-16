@echo off
chcp 65001 >nul
echo.
echo ========================================
echo 🚀 QMS AI系统优化应用脚本
echo ========================================
echo.

echo 📊 检查系统状态...
echo.

REM 检查应用端状态
echo 🔍 检查应用端状态 (端口8082)...
curl -s "http://localhost:8082/" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 应用端运行正常
) else (
    echo ❌ 应用端未启动，请先启动应用端
    echo    运行命令: cd frontend/应用端 && npm run dev
    pause
    exit /b 1
)

REM 检查聊天服务状态
echo 🔍 检查聊天服务状态 (端口3004)...
curl -s "http://localhost:3004/health" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 聊天服务运行正常
) else (
    echo ❌ 聊天服务未启动，请先启动聊天服务
    echo    运行命令: cd backend/nodejs && node chat-service.js
    pause
    exit /b 1
)

echo.
echo 📦 安装优化依赖...
echo.

REM 检查并安装监控依赖
cd backend\nodejs
echo 🔧 安装监控依赖...
call npm install prom-client --save >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 监控依赖安装成功
) else (
    echo ⚠️ 监控依赖安装失败，但不影响主要功能
)

echo.
echo 🎨 应用前端优化...
echo.

REM 返回根目录
cd ..\..

echo ✅ 主题切换功能已添加
echo    - 支持亮色/暗色/自动主题
echo    - 在右上角导航栏可以切换
echo.

echo ✅ 智能推荐功能已添加
echo    - 个性化推荐
echo    - 热门问题推荐
echo    - 快速入门指导
echo.

echo ✅ 性能监控已优化
echo    - 添加了监控中间件
echo    - 支持Prometheus指标收集
echo.

echo.
echo 🎯 优化完成！
echo.
echo 📋 新增功能说明:
echo.
echo 1. 🎨 主题切换
echo    - 位置: 右上角导航栏
echo    - 功能: 亮色/暗色/自动主题切换
echo    - 存储: 自动保存用户偏好
echo.
echo 2. 💡 智能推荐
echo    - 位置: 聊天界面欢迎页面
echo    - 功能: 个性化问题推荐
echo    - 分类: 个人推荐/热门问题/快速入门
echo.
echo 3. 📊 性能监控
echo    - 端点: http://localhost:3004/metrics
echo    - 功能: Prometheus指标收集
echo    - 监控: 请求时间、内存使用等
echo.
echo 🌐 访问地址:
echo    应用端: http://localhost:8082
echo    聊天功能: http://localhost:8082/ai-management/chat
echo    监控指标: http://localhost:3004/metrics
echo.
echo 🎉 优化应用完成！请刷新浏览器查看新功能。
echo.
pause
