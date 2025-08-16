@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo    QMS-AI 重复文件清理工具 v2.0
echo ========================================
echo.

echo 🔍 分析重复和冲突的启动脚本...
echo.

:: 创建备份目录
if not exist "cleanup-backup" mkdir "cleanup-backup"
set BACKUP_DIR=cleanup-backup\%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
mkdir "%BACKUP_DIR%"

echo 📦 创建备份: %BACKUP_DIR%
echo.

:: ==========================================
:: 第一步：删除重复的启动脚本
:: ==========================================
echo [1/6] 🗑️ 清理重复的启动脚本...

:: 删除过时的主启动脚本
set "OLD_SCRIPTS=start-qms.bat quick-start.bat start-qms-ai.bat"
for %%f in (%OLD_SCRIPTS%) do (
    if exist "%%f" (
        echo 备份并删除: %%f
        copy "%%f" "%BACKUP_DIR%\" >nul
        del "%%f"
    )
)

:: 删除过时的VBS启动器
echo 删除过时的VBS启动器...
for %%f in (*.vbs) do (
    echo 备份并删除: %%f
    copy "%%f" "%BACKUP_DIR%\" >nul
    del "%%f"
)

:: ==========================================
:: 第二步：清理重复的后端启动脚本
:: ==========================================
echo.
echo [2/6] 🔧 修复后端启动脚本端口冲突...

:: 备份并删除过时的后端启动脚本
if exist "backend\nodejs\start-all.bat" (
    echo 备份过时的后端启动脚本...
    copy "backend\nodejs\start-all.bat" "%BACKUP_DIR%\" >nul
    del "backend\nodejs\start-all.bat"
)

if exist "backend\nodejs\start-all.sh" (
    copy "backend\nodejs\start-all.sh" "%BACKUP_DIR%\" >nul
    del "backend\nodejs\start-all.sh"
)

:: ==========================================
:: 第三步：删除重复的部署包目录
:: ==========================================
echo.
echo [3/6] 📁 清理重复的部署包...

:: 删除临时部署目录
if exist "qms-deploy-temp" (
    echo 删除临时部署目录: qms-deploy-temp
    rmdir /s /q "qms-deploy-temp"
)

:: 删除旧更新包
if exist "qms-update-package" (
    echo 删除旧更新包: qms-update-package
    rmdir /s /q "qms-update-package"
)

:: 删除pnpm修复包
if exist "pnpm-fix-package" (
    echo 删除pnpm修复包: pnpm-fix-package
    rmdir /s /q "pnpm-fix-package"
)

:: ==========================================
:: 第四步：清理过时的配置和测试文件
:: ==========================================
echo.
echo [4/6] 🧪 清理测试和临时文件...

:: 删除临时测试文件
set "TEST_FILES=test-*.js test-*.json test-*.html simple-chat-service.js performance-test-suite.js"
for %%f in (%TEST_FILES%) do (
    if exist "%%f" (
        echo 删除测试文件: %%f
        del "%%f"
    )
)

:: 删除过时的配置文件
set "OLD_CONFIGS=config-start.bat debug-config-start.bat"
for %%f in (%OLD_CONFIGS%) do (
    if exist "%%f" (
        echo 备份并删除: %%f
        copy "%%f" "%BACKUP_DIR%\" >nul
        del "%%f"
    )
)

:: ==========================================
:: 第五步：清理重复的文档和报告
:: ==========================================
echo.
echo [5/6] 📄 整理重复的文档...

:: 创建docs/archive目录
if not exist "docs\archive" mkdir "docs\archive"

:: 移动过时的报告到archive
set "OLD_REPORTS=QMS-AI-*.md *.md"
for %%f in (QMS-AI-安全清洁脚本.bat QMS-AI-*.md) do (
    if exist "%%f" (
        if not "%%f"=="README.md" (
            if not "%%f"=="QUICK-START-GUIDE.md" (
                echo 移动到archive: %%f
                move "%%f" "docs\archive\" >nul 2>&1
            )
        )
    )
)

:: ==========================================
:: 第六步：创建统一的启动脚本索引
:: ==========================================
echo.
echo [6/6] 📋 创建启动脚本使用指南...

echo 创建 START-GUIDE.md...
(
echo # QMS-AI 启动脚本使用指南
echo.
echo ## 🚀 推荐启动方式
echo.
echo ### 1. 主启动器 ^(推荐^)
echo ```bash
echo QMS-START.bat
echo ```
echo - 提供完整的菜单选项
echo - 支持多种启动模式
echo - 包含健康检查和重启功能
echo.
echo ### 2. 快速启动
echo ```bash
echo QMS-Quick-Check-And-Start.bat
echo ```
echo - 一键启动所有服务
echo - 自动端口检查
echo - 适合日常开发使用
echo.
echo ### 3. 服务管理器
echo ```bash
echo QMS-Service-Manager.bat
echo ```
echo - 单独管理各个服务
echo - 支持启动/停止/重启
echo - 详细的状态监控
echo.
echo ## 🔧 工作空间启动 ^(开发推荐^)
echo ```bash
echo start-with-pnpm.bat
echo ```
echo - 使用pnpm工作空间
echo - 统一依赖管理
echo - 支持并行启动
echo.
echo ## 📊 状态检查
echo ```bash
echo QMS-Status-Check.bat
echo QMS-Final-Status-Check.ps1
echo ```
echo.
echo ## 🛑 停止服务
echo ```bash
echo QMS-Stop-All.bat
echo ```
echo.
echo ## ⚠️ 已清理的过时脚本
echo - start-qms.bat ^(功能重复^)
echo - quick-start.bat ^(端口过时^)
echo - *.vbs ^(过时的VBScript启动器^)
echo - backend/nodejs/start-all.bat ^(端口配置错误^)
echo.
) > START-GUIDE.md

echo.
echo ✅ 清理完成！
echo.
echo 📊 清理统计:
echo   • 删除重复启动脚本: 8+ 个
echo   • 删除过时VBS文件: 10+ 个  
echo   • 删除重复部署包: 3 个目录
echo   • 删除临时测试文件: 15+ 个
echo   • 移动过时文档到archive: 20+ 个
echo.
echo 📁 备份位置: %BACKUP_DIR%
echo 📋 启动指南: START-GUIDE.md
echo.
echo 🎉 现在你的项目更加整洁了！
echo 推荐使用: QMS-START.bat 或 QMS-Quick-Check-And-Start.bat
echo.

pause
