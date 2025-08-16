@echo off
echo 🧹 QMS-AI项目清理工具
echo ================================

echo 🔍 清理重复和过时的文件...

REM 删除重复的启动脚本
if exist "start-qms.bat" (
    echo 删除过时的启动脚本: start-qms.bat
    del "start-qms.bat"
)

if exist "start-qms-simple.bat" (
    echo 删除重复的启动脚本: start-qms-simple.bat
    del "start-qms-simple.bat"
)

if exist "start-qms-unified.bat" (
    echo 删除重复的启动脚本: start-qms-unified.bat
    del "start-qms-unified.bat"
)

REM 删除重复的测试文件
if exist "test-node.js" (
    echo 删除临时测试文件: test-node.js
    del "test-node.js"
)

if exist "test-unified-ports.bat" (
    echo 删除过时的测试文件: test-unified-ports.bat
    del "test-unified-ports.bat"
)

REM 删除重复的Docker相关文件（因为不使用Docker）
if exist "check-docker-ready.bat" (
    echo 删除Docker相关文件: check-docker-ready.bat
    del "check-docker-ready.bat"
)

if exist "first-time-docker-start.bat" (
    echo 删除Docker相关文件: first-time-docker-start.bat
    del "first-time-docker-start.bat"
)

if exist "start-docker-local.bat" (
    echo 删除Docker相关文件: start-docker-local.bat
    del "start-docker-local.bat"
)

if exist "docker-manage.bat" (
    echo 删除Docker相关文件: docker-manage.bat
    del "docker-manage.bat"
)

REM 删除重复的系统优化文件
if exist "system-optimization-executor.bat" (
    echo 删除重复的优化文件: system-optimization-executor.bat
    del "system-optimization-executor.bat"
)

if exist "quick-fix.js" (
    echo 删除临时修复文件: quick-fix.js
    del "quick-fix.js"
)

REM 删除重复的重启脚本
if exist "restart-ai-service.bat" (
    echo 删除重复的重启脚本: restart-ai-service.bat
    del "restart-ai-service.bat"
)

REM 删除过时的修复脚本
if exist "fix-frontend-issues.bat" (
    echo 删除过时的修复脚本: fix-frontend-issues.bat
    del "fix-frontend-issues.bat"
)

REM 删除重复的升级脚本
if exist "system-upgrade.js" (
    echo 删除重复的升级脚本: system-upgrade.js
    del "system-upgrade.js"
)

if exist "frontend-upgrade-executor.js" (
    echo 删除重复的前端升级脚本: frontend-upgrade-executor.js
    del "frontend-upgrade-executor.js"
)

REM 删除重复的健康检查脚本
if exist "system-health-check.js" (
    echo 删除重复的健康检查脚本: system-health-check.js
    del "system-health-check.js"
)

if exist "system-full-startup-optimizer.js" (
    echo 删除重复的启动优化脚本: system-full-startup-optimizer.js
    del "system-full-startup-optimizer.js"
)

echo.
echo ✅ 文件清理完成！
echo.
echo 📋 保留的核心文件:
echo   🚀 QMS-Quick-Start.bat - 主启动脚本
echo   🛑 QMS-Stop-All.bat - 停止脚本
echo   🔧 qms-service-manager.js - 服务管理器
echo   🎛️ qms-manager.bat - 图形化管理
echo.
pause
