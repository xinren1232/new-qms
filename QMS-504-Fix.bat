@echo off
title QMS-AI 504 Gateway Timeout Fix

echo.
echo QMS-AI 504 Gateway Timeout Diagnostic Tool
echo ==========================================
echo.

:: Check Docker status
echo [1/6] Checking Docker status...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker not installed or not running
    echo Please make sure Docker Desktop is running
    pause
    exit /b 1
) else (
    echo OK: Docker is running
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker Compose not installed
    pause
    exit /b 1
) else (
    echo OK: Docker Compose is available
)
echo.

:: Check container status
echo [2/6] Checking QMS containers...
docker ps --filter "name=qms" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

:: Check port status
echo [3/6] Checking port status...
set "ports=80 443 3003 3004 3005 3008 3009 6379 8072 8081 8084 8085"
for %%p in (%ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul 2>&1
    if errorlevel 1 (
        echo WARNING: Port %%p not listening
    ) else (
        echo OK: Port %%p is listening
    )
)
echo.

:: Check backend services health
echo [4/6] Checking backend services...
curl -s -f "http://localhost:3003/health" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Config service (3003) not healthy
) else (
    echo OK: Config service (3003) healthy
)

curl -s -f "http://localhost:3004/health" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Chat service (3004) not healthy
) else (
    echo OK: Chat service (3004) healthy
)

curl -s -f "http://localhost:8081/health" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Frontend app (8081) not healthy
) else (
    echo OK: Frontend app (8081) healthy
)

curl -s -f "http://localhost:8072/health" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Frontend config (8072) not healthy
) else (
    echo OK: Frontend config (8072) healthy
)

curl -s -f "http://localhost:8085/health" >nul 2>&1
if errorlevel 1 (
    echo ERROR: API Gateway (8085) not healthy
) else (
    echo OK: API Gateway (8085) healthy
)
echo.

:: Restart services
echo [5/6] Restarting services...
if exist "docker-compose.yml" (
    echo Using docker-compose.yml
    docker-compose restart
) else if exist "config\docker-compose.yml" (
    echo Using config\docker-compose.yml
    docker-compose -f config\docker-compose.yml restart
) else if exist "deployment\docker-compose.yml" (
    echo Using deployment\docker-compose.yml
    docker-compose -f deployment\docker-compose.yml restart
) else (
    echo ERROR: No docker-compose.yml found
    echo Please restart services manually
)

echo Waiting for services to start...
timeout /t 30 /nobreak >nul
echo.

:: Final verification
echo [6/6] Final verification...
curl -s -f "http://localhost/" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Main app not accessible
) else (
    echo OK: Main app accessible
)

curl -s -f "http://localhost/config/" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Config app not accessible
) else (
    echo OK: Config app accessible
)

curl -s -f "http://localhost/health" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Health endpoint not accessible
) else (
    echo OK: Health endpoint accessible
)
echo.

echo ==========================================
echo Fix completed!
echo.
echo Access URLs:
echo   Main App: http://localhost/
echo   Config App: http://localhost/config/
echo   API Gateway: http://localhost:8085/
echo.
echo If 504 error persists, check:
echo 1. Server firewall settings
echo 2. Cloud security group configuration
echo 3. Domain DNS resolution
echo 4. Container logs: docker logs container-name
echo.

pause
