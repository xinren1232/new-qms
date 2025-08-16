@echo off
chcp 65001 >nul
title QMS-AI Redis持久化管理器

:MAIN_MENU
cls
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                    QMS-AI Redis持久化管理器                    ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  🔧 Redis持久化服务管理                                        ║
echo ║                                                              ║
echo ║  [1] 🚀 启动Redis持久化服务                                    ║
echo ║  [2] 🛑 停止Redis持久化服务                                    ║
echo ║  [3] 🔄 重启Redis持久化服务                                    ║
echo ║  [4] 📊 查看Redis服务状态                                      ║
echo ║  [5] 🧪 测试Redis连接                                         ║
echo ║  [6] 💾 测试持久化功能                                         ║
echo ║  [7] 📁 查看持久化文件                                         ║
echo ║  [8] 🧹 清理Redis数据                                         ║
echo ║  [9] ⚙️ 查看Redis配置                                         ║
echo ║  [0] 🚪 退出                                                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
set /p choice=请选择操作 (0-9): 

if "%choice%"=="1" goto START_REDIS
if "%choice%"=="2" goto STOP_REDIS
if "%choice%"=="3" goto RESTART_REDIS
if "%choice%"=="4" goto STATUS_REDIS
if "%choice%"=="5" goto TEST_CONNECTION
if "%choice%"=="6" goto TEST_PERSISTENCE
if "%choice%"=="7" goto VIEW_FILES
if "%choice%"=="8" goto CLEAN_DATA
if "%choice%"=="9" goto VIEW_CONFIG
if "%choice%"=="0" goto EXIT
goto MAIN_MENU

:START_REDIS
echo.
echo 🚀 启动Redis持久化服务...
wsl -e bash -c "redis-server /mnt/d/QMS01/config/redis/redis-persistent.conf"
if errorlevel 1 (
    echo ❌ Redis启动失败
) else (
    echo ✅ Redis持久化服务启动成功
    echo 📍 服务地址: 172.17.202.149:6380
    echo 💾 持久化: RDB + AOF双重保护
)
pause
goto MAIN_MENU

:STOP_REDIS
echo.
echo 🛑 停止Redis持久化服务...
wsl -e bash -c "pkill -f 'redis-server.*6380'"
echo ✅ Redis服务停止命令已发送
pause
goto MAIN_MENU

:RESTART_REDIS
echo.
echo 🔄 重启Redis持久化服务...
echo 停止现有服务...
wsl -e bash -c "pkill -f 'redis-server.*6380'"
timeout /t 2 >nul
echo 启动新服务...
wsl -e bash -c "redis-server /mnt/d/QMS01/config/redis/redis-persistent.conf"
echo ✅ Redis服务重启完成
pause
goto MAIN_MENU

:STATUS_REDIS
echo.
echo 📊 Redis服务状态检查...
echo.
echo 🔍 检查Redis进程:
wsl -e bash -c "ps aux | grep redis"
echo.
echo 🔍 检查端口占用:
wsl -e bash -c "netstat -tlnp | grep 6380"
echo.
echo 🔍 检查持久化文件:
wsl -e bash -c "ls -la /tmp/redis-data/"
pause
goto MAIN_MENU

:TEST_CONNECTION
echo.
echo 🧪 测试Redis连接...
cd backend\nodejs
node test-redis.js
pause
goto MAIN_MENU

:TEST_PERSISTENCE
echo.
echo 💾 测试Redis持久化功能...
cd backend\nodejs
node test-redis-persistence.js
pause
goto MAIN_MENU

:VIEW_FILES
echo.
echo 📁 查看持久化文件详情...
echo.
echo 🗂️ 持久化文件列表:
wsl -e bash -c "ls -lah /tmp/redis-data/"
echo.
echo 📄 RDB文件信息:
wsl -e bash -c "file /tmp/redis-data/qms-ai-dump.rdb"
echo.
echo 📄 AOF文件大小:
wsl -e bash -c "wc -l /tmp/redis-data/qms-ai-appendonly.aof"
pause
goto MAIN_MENU

:CLEAN_DATA
echo.
echo ⚠️ 警告：此操作将清除所有Redis数据和持久化文件！
set /p confirm=确认清理吗？(y/N): 
if /i not "%confirm%"=="y" goto MAIN_MENU

echo.
echo 🧹 清理Redis数据...
echo 停止Redis服务...
wsl -e bash -c "pkill -f 'redis-server.*6380'"
timeout /t 2 >nul
echo 删除持久化文件...
wsl -e bash -c "rm -f /tmp/redis-data/*"
echo 重新启动Redis服务...
wsl -e bash -c "redis-server /mnt/d/QMS01/config/redis/redis-persistent.conf"
echo ✅ 数据清理完成
pause
goto MAIN_MENU

:VIEW_CONFIG
echo.
echo ⚙️ Redis持久化配置信息...
echo.
echo 📋 配置文件位置: config/redis/redis-persistent.conf
echo 📋 数据目录: /tmp/redis-data/
echo 📋 服务端口: 6380
echo 📋 绑定地址: 0.0.0.0
echo.
echo 💾 持久化配置:
echo   - RDB快照: 启用 (900s/1key, 300s/10keys, 60s/10000keys)
echo   - AOF日志: 启用 (每秒同步)
echo   - 内存限制: 512MB
echo   - 淘汰策略: allkeys-lru
echo.
echo 🔧 性能优化:
echo   - 多线程I/O: 4线程
echo   - 动态HZ: 启用
echo   - 延迟监控: 启用
pause
goto MAIN_MENU

:EXIT
echo.
echo 👋 感谢使用QMS-AI Redis持久化管理器！
echo.
pause
exit
