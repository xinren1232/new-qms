@echo off
:: QMS-AI Local Stop (Windows)
wmic process where "commandline like '%%lightweight-config-service.js%%'" call terminate >nul 2>nul
wmic process where "commandline like '%%chat-service.js%%'" call terminate >nul 2>nul
wmic process where "commandline like '%%auth-service.js%%'" call terminate >nul 2>nul
wmic process where "commandline like '%%api-gateway.js%%'" call terminate >nul 2>nul
wmic process where "commandline like '%%vite%%'" call terminate >nul 2>nul

echo Stopped local QMS-AI processes (if any).

