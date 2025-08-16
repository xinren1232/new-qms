@echo off
chcp 65001 >nul
set JAVA_HOME=C:\Program Files\Java\jdk-21
set MAVEN_HOME=C:\apache-maven
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo 启动数据驱动服务...
echo 使用配置: simple
echo 端口: 8082

cd /d "%~dp0"

echo 正在启动Spring Boot应用...
mvn -s ..\..\settings.xml exec:java ^
  -Dexec.mainClass="com.transcend.plm.datadriven.Application" ^
  -Dexec.args="--spring.profiles.active=simple --server.port=8082"

pause
