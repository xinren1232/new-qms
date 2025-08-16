@echo off
chcp 65001 >nul
title QMS-AI 统一启动管理器
color 0B

cls
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    QMS-AI 统一启动管理器                      ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  🚀 一键启动QMS-AI系统的所有组件                              ║
echo ║  📊 智能选择最适合的启动方式                                  ║
echo ║  🔧 支持开发和生产环境                                        ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:menu
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                        启动选项菜单                          ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 🚀 快速启动选项:
echo   1. 完整启动 (推荐) - 使用pnpm启动所有服务
echo   2. 服务管理模式 - 交互式管理各个服务
echo   3. 全面重启 - 清理并重新启动所有服务
echo.
echo 🔧 专用功能:
echo   4. 用户隔离系统 - 启动完整的用户认证和数据隔离
echo   5. 健康检查 - 检查系统状态和服务健康
echo   6. 停止所有服务 - 安全停止所有运行的服务
echo.
echo 📖 帮助选项:
echo   7. 查看启动指南
echo   0. 退出
echo.

set /p choice=请选择启动方式 (0-7): 

if "%choice%"=="1" goto full_start
if "%choice%"=="2" goto service_manager
if "%choice%"=="3" goto full_restart
if "%choice%"=="4" goto user_isolation
if "%choice%"=="5" goto health_check
if "%choice%"=="6" goto stop_all
if "%choice%"=="7" goto show_guide
if "%choice%"=="0" goto exit
goto invalid_choice

:full_start
echo.
echo 🚀 启动完整QMS-AI系统...
echo 使用pnpm工作空间管理，包含所有前后端服务
echo.
call start-with-pnpm.bat
goto menu

:service_manager
echo.
echo 🔧 启动服务管理器...
echo 可以单独管理各个服务的启动和停止
echo.
call qms-manager.bat
goto menu

:full_restart
echo.
echo 🔄 执行全面重启...
echo 将清理所有进程并重新启动系统
echo.
call QMS-Full-Restart.bat
goto menu

:user_isolation
echo.
echo 🔐 启动用户隔离系统...
echo 包含完整的用户认证和数据隔离功能
echo.
call start-user-isolation-system.bat
goto menu

:health_check
echo.
echo 🏥 执行系统健康检查...
echo.
call QMS-Health-Check.bat
pause
goto menu

:stop_all
echo.
echo 🛑 停止所有服务...
echo.
call QMS-Stop-All.bat
pause
goto menu

:show_guide
cls
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                      QMS-AI 启动指南                         ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo 📋 启动方式说明:
echo.
echo 🎯 【完整启动】- 推荐新用户使用
echo   • 自动安装pnpm和所有依赖
echo   • 启动Redis、后端服务、前端应用
echo   • 适合：首次使用、完整开发环境
echo.
echo 🔧 【服务管理模式】- 推荐开发者使用  
echo   • 可单独启动/停止各个服务
echo   • 实时查看服务状态
echo   • 适合：日常开发、调试特定服务
echo.
echo 🔄 【全面重启】- 推荐故障恢复使用
echo   • 清理所有端口和进程
echo   • 重新检查环境和依赖
echo   • 适合：系统异常、端口冲突
echo.
echo 🔐 【用户隔离系统】- 推荐生产环境使用
echo   • 完整的用户认证和权限管理
echo   • 数据完全隔离
echo   • 适合：多用户生产环境
echo.
echo 💡 首次使用建议: 选择 "1. 完整启动"
echo.
pause
goto menu

:invalid_choice
echo.
echo ❌ 无效选择，请输入 0-7 之间的数字
echo.
pause
goto menu

:exit
echo.
echo 👋 感谢使用 QMS-AI 系统！
echo.
exit /b 0
