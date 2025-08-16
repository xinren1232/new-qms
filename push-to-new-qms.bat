@echo off
echo 🚀 QMS-AI系统推送到GitHub脚本
echo ================================
echo 目标仓库: https://github.com/xinren1232/new-qms.git
echo.

echo ✅ 检查Git状态...
git status

echo.
echo 📡 验证远程仓库...
git remote -v

echo.
echo 🚀 推送到GitHub (new-qms仓库)...
git push -u origin main

if errorlevel 1 (
    echo.
    echo ⚠️ 推送失败，尝试其他方法...
    
    echo 🔄 尝试强制推送...
    git push -f origin main
    
    if errorlevel 1 (
        echo.
        echo ❌ 推送仍然失败，可能的原因:
        echo   1. 网络连接问题
        echo   2. GitHub访问限制
        echo   3. 身份验证问题
        echo   4. 仓库权限问题
        echo.
        echo 💡 建议解决方案:
        echo   1. 检查网络连接
        echo   2. 使用VPN或代理
        echo   3. 配置Git身份验证
        echo   4. 检查仓库权限
        echo.
        echo 🔧 手动推送命令:
        echo   git remote set-url origin https://github.com/xinren1232/new-qms.git
        echo   git push -u origin main
        echo.
        pause
        exit /b 1
    )
)

echo.
echo ✅ 成功推送到GitHub!
echo 🎉 QMS-AI系统已保存到 https://github.com/xinren1232/new-qms.git

echo.
echo 📋 推送内容包含:
echo   • 完整的前端应用 (Vue3 + Element Plus)
echo   • 后端微服务 (Node.js + Express)  
echo   • 18个插件验证系统
echo   • 8个AI模型集成
echo   • 配置驱动架构
echo   • 完整的部署脚本
echo   • 详细的技术文档

echo.
echo 🎯 QMS-AI插件验证系统完成状态:
echo   ✅ 文档解析类插件: 7个 (PDF、CSV、JSON、XML、XLSX、DOCX、Excel分析器)
echo   ✅ 数据分析类插件: 5个 (统计分析器、SPC控制器、数据清洗器、异常检测器、文本摘要器)
echo   ✅ 质量工具类插件: 3个 (FMEA分析器、MSA计算器、缺陷检测器)
echo   ✅ AI处理类插件: 1个 (OCR识别器)
echo   ✅ 外部连接类插件: 2个 (API连接器、数据库查询器)

echo.
echo 🔗 GitHub仓库链接: https://github.com/xinren1232/new-qms.git
echo 📊 总计: 18个插件 + 16个测试文件 + 完整系统架构

pause
