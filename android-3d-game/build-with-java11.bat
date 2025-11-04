@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java: %JAVA_HOME%
java -version
echo.
echo Starting build...
call "%~dp0gradlew.bat" assembleDebug
