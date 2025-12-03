@echo off
setlocal enabledelayedexpansion

rem === 配置路径 ===
set SRC_DIR=src
set OUT_DIR=out
set LIB_DIR=lib

rem 修改成你实际的 org.json 文件名
set JSON_JAR=%LIB_DIR%\json-20250517.jar

rem === 创建输出目录 ===
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

rem === 收集所有 .java 源文件 ===
echo Collecting Java sourcefiles ...
dir /S /B "%SRC_DIR%\*.java" > sources.txt

rem === 编译 ===
echo Compiling...
javac -cp "%JSON_JAR%" -d "%OUT_DIR%" @sources.txt
if errorlevel 1 (
    echo Compile failed.
    exit /b 1
)

echo Compiled successfully, under "%OUT_DIR%" .
endlocal
