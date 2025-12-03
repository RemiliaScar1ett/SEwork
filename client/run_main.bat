@echo off
setlocal

set OUT_DIR=out
set LIB_DIR=lib
set JSON_JAR=%LIB_DIR%\json-20250517.jar

if not exist "%OUT_DIR%" (
    echo No such files: %OUT_DIR% , build_client.bat first.
    exit /b 1
)

echo Starting Main...
java -cp "%OUT_DIR%;%JSON_JAR%" Main

endlocal
