@echo off

rem Gradle wrapper startup script for Windows.
rem See: https://docs.gradle.org/current/user/gradle_wrapper.html

setlocal enabledelayedexpansion
set "APP_HOME=%~dp0"
set "APP_HOME=%APP_HOME:~0,-1%"

set "JAVA_HOME=%JAVA_HOME%"
if not defined JAVA_HOME (
    where java >nul 2>&1
    if errorlevel 1 (
        echo Error: JAVA_HOME is not set and java was not found on PATH.
        exit /b 1
    )
    for /f "delims=" %%i in ('where java') do (
        set "JAVA_HOME=%%~dpi"
        goto :found_java
    )
    :found_java
    if "!JAVA_HOME:~0,-1!"=="\" set "JAVA_HOME=!JAVA_HOME:~0,-1!"
)

set "CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar"
"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" -Dgradle.wrapper.download.timeout=600 -Dgradle.wrapper.wait.time=120000 org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
