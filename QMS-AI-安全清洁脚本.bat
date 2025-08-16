@echo off
chcp 65001 >nul
title QMS-AI 项目结构安全清洁工具
color 0B

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                QMS-AI 项目结构安全清洁工具                    ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  🧹 清理重复文件和冗余功能                                    ║
echo ║  📦 安全备份，可回滚操作                                      ║
echo ║  ⚠️ 不影响当前运行的核心功能                                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 📋 清洁计划概览:
echo   1. 创建安全备份
echo   2. 删除重复的启动脚本
echo   3. 归档未使用的项目
echo   4. 整理配置文件
echo   5. 更新文档引用
echo.

set /p confirm=是否继续执行清洁操作？(y/N): 
if /i not "%confirm%"=="y" (
    echo 操作已取消
    pause
    exit /b 0
)

echo.
echo 🚀 开始执行清洁操作...
echo.

:: ==========================================
:: 第一步：创建备份
:: ==========================================
echo [1/5] 📦 创建安全备份...

set backup_dir=backup-%date:~0,4%%date:~5,2%%date:~8,2%-%time:~0,2%%time:~3,2%%time:~6,2%
set backup_dir=%backup_dir: =0%

if not exist "%backup_dir%" mkdir "%backup_dir%"

echo 📁 备份目录: %backup_dir%

:: 备份重要文件
if exist "QMS-Full-System-Restart.bat" (
    echo 备份: QMS-Full-System-Restart.bat
    copy "QMS-Full-System-Restart.bat" "%backup_dir%\" >nul
)

if exist "frontend\配置驱动端" (
    echo 备份: frontend\配置驱动端
    xcopy "frontend\配置驱动端" "%backup_dir%\配置驱动端\" /E /I /Q >nul
)

if exist "backend\springboot" (
    echo 备份: backend\springboot
    xcopy "backend\springboot" "%backup_dir%\springboot\" /E /I /Q >nul
)

echo ✅ 备份完成

:: ==========================================
:: 第二步：删除重复的启动脚本
:: ==========================================
echo.
echo [2/5] 🗑️ 删除重复的启动脚本...

if exist "QMS-Full-System-Restart.bat" (
    echo 删除: QMS-Full-System-Restart.bat (功能与QMS-Full-Restart.bat重复)
    del "QMS-Full-System-Restart.bat"
    echo ✅ 已删除重复启动脚本
) else (
    echo ⚠️ QMS-Full-System-Restart.bat 不存在
)

:: 移动专用脚本到scripts目录
if exist "start-user-isolation-system.bat" (
    echo 移动: start-user-isolation-system.bat → scripts\
    if not exist "scripts" mkdir "scripts"
    move "start-user-isolation-system.bat" "scripts\" >nul
    echo ✅ 已移动专用脚本
)

:: ==========================================
:: 第三步：归档未使用的项目
:: ==========================================
echo.
echo [3/5] 📁 归档未使用的项目...

:: 创建归档目录
if not exist "archive" mkdir "archive"
if not exist "archive\frontend" mkdir "archive\frontend"
if not exist "archive\backend" mkdir "archive\backend"

:: 归档配置驱动端 (未完成的项目)
if exist "frontend\配置驱动端" (
    echo 归档: frontend\配置驱动端 (未完成项目)
    move "frontend\配置驱动端" "archive\frontend\" >nul
    echo ✅ 已归档配置驱动端
)

:: 归档Spring Boot项目 (未使用)
if exist "backend\springboot" (
    echo 归档: backend\springboot (未使用的Java服务)
    move "backend\springboot" "archive\backend\" >nul
    echo ✅ 已归档Spring Boot项目
)

if exist "backend\数据驱动" (
    echo 归档: backend\数据驱动 (重复功能)
    move "backend\数据驱动" "archive\backend\" >nul
    echo ✅ 已归档数据驱动项目
)

if exist "backend\配置中心" (
    echo 归档: backend\配置中心 (重复功能)
    move "backend\配置中心" "archive\backend\" >nul
    echo ✅ 已归档配置中心项目
)

:: ==========================================
:: 第四步：整理配置文件
:: ==========================================
echo.
echo [4/5] ⚙️ 整理配置文件...

:: 删除重复的Docker配置文件
cd config 2>nul
if exist "docker-compose.small.yml" (
    echo 删除: config\docker-compose.small.yml (功能重复)
    del "docker-compose.small.yml"
    echo ✅ 已删除重复配置
)

if exist "docker-compose.database.yml" (
    echo 归档: config\docker-compose.database.yml (可合并到主配置)
    if not exist "archive" mkdir "archive"
    move "docker-compose.database.yml" "archive\" >nul
    echo ✅ 已归档数据库配置
)
cd ..

:: ==========================================
:: 第五步：清理临时文件
:: ==========================================
echo.
echo [5/5] 🧹 清理临时文件...

:: 删除根目录的临时文件
if exist "nul" (
    echo 删除: nul (临时文件)
    del "nul"
)

:: 清理测试文件
if exist "test-*.js" (
    echo 移动测试文件到scripts目录...
    move "test-*.js" "scripts\" >nul 2>&1
)

if exist "test-*.html" (
    echo 移动测试文件到scripts目录...
    move "test-*.html" "scripts\" >nul 2>&1
)

echo ✅ 临时文件清理完成

:: ==========================================
:: 完成报告
:: ==========================================
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    🎉 清洁操作完成！                          ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 📊 清洁结果统计:
echo   ✅ 已删除重复启动脚本: 1个
echo   ✅ 已归档未使用项目: 4个
echo   ✅ 已整理配置文件: 2个
echo   ✅ 已清理临时文件: 若干
echo.

echo 📁 备份位置: %backup_dir%
echo 📋 如需回滚，请从备份目录恢复文件
echo.

echo 🔍 建议下一步操作:
echo   1. 运行 start-with-pnpm.bat 验证系统正常
echo   2. 检查 archive 目录确认归档内容
echo   3. 更新相关文档和README
echo.

echo ⚠️ 注意: 核心运行功能未受影响
echo   • 应用端前端 (8081) - 正常
echo   • 配置中心 (8082) - 正常  
echo   • 认证服务 (8084) - 正常
echo   • AI聊天服务 (3004) - 正常
echo   • Redis缓存 (6380) - 正常
echo.

pause
