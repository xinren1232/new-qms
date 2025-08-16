@echo off
setlocal enableextensions enabledelayedexpansion

:: QMS-AI Local Start (Windows)
:: Starts config (3003), chat (3004), auth (8084), gateway (8085), and frontend dev (8081)

set ROOT=%~dp0..
set NODE_DIR=%ROOT%\backend\nodejs
set FRONT_DIR=%ROOT%\frontend\应用端

:: Check Node
where node >nul 2>nul || (
  echo [ERROR] Node.js not found in PATH. Please install Node 18+.
  goto :end
)

:: Start services in new windows
start "QMS Config 3003" cmd /k "cd /d %NODE_DIR% && set NODE_ENV=development && node lightweight-config-service.js"
call :delay 4
start "QMS Chat 3004" cmd /k "cd /d %NODE_DIR% && set NODE_ENV=development && node chat-service.js"
call :delay 4
start "QMS Auth 8084" cmd /k "cd /d %NODE_DIR% && set NODE_ENV=development && node auth-service.js"
call :delay 4
start "QMS Gateway 8085" cmd /k "cd /d %NODE_DIR% && set NODE_ENV=development && node api-gateway.js"
call :delay 3

:: Start frontend dev server
start "QMS Frontend 8081" cmd /k "cd /d %FRONT_DIR% && npm run dev"

:: Open browser (optional)
start http://localhost:8081

echo Done. All windows can be closed separately.

:end
exit /b 0

:delay
set /a s=%1
if "%1"=="" set s=3
ping -n %s% 127.0.0.1 >nul
exit /b 0

