@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ========================================
echo    QMS-AI 服务状态检查
echo ========================================
echo.

echo 🔍 检查端口监听状态...
echo ----------------------------------------

set "services=3003:配置中心 3004:聊天服务 3005:Coze-Studio 8081:应用端 8082:配置中心服务 8084:认证服务 8085:API网关 8072:配置端"

for %%s in (%services%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        netstat -ano | findstr ":%%a " | findstr LISTENING >nul
        if !errorlevel! equ 0 (
            for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%%a " ^| findstr LISTENING') do (
                echo ✅ 端口%%a ^(%%b^): 正常运行 ^(PID: %%p^)
            )
        ) else (
            echo ❌ 端口%%a ^(%%b^): 未运行
        )
    )
)

echo.
echo 🔍 检查服务健康状态...
echo ----------------------------------------

echo 测试配置中心...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:3003/health' -UseBasicParsing -TimeoutSec 5; if($r.StatusCode -eq 200) { Write-Host '✅ 配置中心: 健康' } else { Write-Host '⚠️ 配置中心: 响应异常' } } catch { Write-Host '❌ 配置中心: 连接失败' }"

echo 测试聊天服务...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:3004/health' -UseBasicParsing -TimeoutSec 5; if($r.StatusCode -eq 200) { Write-Host '✅ 聊天服务: 健康' } else { Write-Host '⚠️ 聊天服务: 响应异常' } } catch { Write-Host '❌ 聊天服务: 连接失败' }"

echo 测试Coze Studio...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:3005/health' -UseBasicParsing -TimeoutSec 5; if($r.StatusCode -eq 200) { Write-Host '✅ Coze Studio: 健康' } else { Write-Host '⚠️ Coze Studio: 响应异常' } } catch { Write-Host '❌ Coze Studio: 连接失败' }"

echo 测试认证服务...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8084/health' -UseBasicParsing -TimeoutSec 5; if($r.StatusCode -eq 200) { Write-Host '✅ 认证服务: 健康' } else { Write-Host '⚠️ 认证服务: 响应异常' } } catch { Write-Host '❌ 认证服务: 连接失败' }"

echo 测试API网关...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8085/health' -UseBasicParsing -TimeoutSec 5; if($r.StatusCode -eq 200) { Write-Host '✅ API网关: 健康' } else { Write-Host '⚠️ API网关: 响应异常' } } catch { Write-Host '❌ API网关: 连接失败' }"

echo 测试应用端前端...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8081' -UseBasicParsing -TimeoutSec 5; if($r.StatusCode -eq 200) { Write-Host '✅ 应用端前端: 正常' } else { Write-Host '⚠️ 应用端前端: 响应异常' } } catch { Write-Host '❌ 应用端前端: 连接失败' }"

echo 测试配置端前端...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8072' -UseBasicParsing -TimeoutSec 5; if($r.StatusCode -eq 200) { Write-Host '✅ 配置端前端: 正常' } else { Write-Host '⚠️ 配置端前端: 响应异常' } } catch { Write-Host '❌ 配置端前端: 连接失败' }"

echo.
echo 🔍 检查错误端口（应该为空）...
echo ----------------------------------------

set "error_ports=1117 1684"
for %%p in (%error_ports%) do (
    netstat -ano | findstr ":%%p " | findstr LISTENING >nul
    if !errorlevel! equ 0 (
        echo ⚠️ 端口%%p: 异常监听（需要检查）
    ) else (
        echo ✅ 端口%%p: 正常（未监听）
    )
)

echo.
echo ========================================
echo    检查完成！
echo ========================================
echo.
echo 📍 访问地址:
echo   - 应用端: http://localhost:8081
echo   - 配置端: http://localhost:8072
echo   - API网关: http://localhost:8085
echo   - 配置中心: http://localhost:3003
echo   - 聊天服务: http://localhost:3004
echo   - 认证服务: http://localhost:8084
echo.

pause
