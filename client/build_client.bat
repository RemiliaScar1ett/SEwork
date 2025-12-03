@echo off
setlocal enabledelayedexpansion

set SRC_DIR=src
set OUT_DIR=out
set LIB_DIR=lib

set JSON_JAR=%LIB_DIR%\json-20250517.jar

if not exist "%JSON_JAR%" (
  echo JSON jar not found: %JSON_JAR%
  exit /b 1
)

if not exist "%OUT_DIR%" (
  mkdir "%OUT_DIR%"
)

echo Collecting source files...
dir /S /B "%SRC_DIR%\*.java" > sources.txt

echo Compiling sources with --release 8 ...
javac --release 8 -cp "%JSON_JAR%" -d "%OUT_DIR%" @sources.txt
if errorlevel 1 (
  echo Compile failed.
  exit /b 1
)

echo Compile succeeded. Classes in %OUT_DIR%.
endlocal
