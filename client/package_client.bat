@echo off
setlocal

rem === 配置 ===
set OUT_DIR=out
set LIB_DIR=lib

rem 这里改成你 lib 目录下 org.json 的真实文件名
set JSON_JAR_NAME=json-20250517.jar

set JSON_JAR=%LIB_DIR%\%JSON_JAR_NAME%
set JAR_NAME=PurchaseClient.jar
set MANIFEST_FILE=manifest.mf

if not exist "%OUT_DIR%" (
    echo Destination %OUT_DIR% not found, Please build_client.bat first.
    exit /b 1
)

if not exist "%JSON_JAR%" (
    echo JSON not found %JSON_JAR% .
    exit /b 1
)

echo spawning Manifest files %MANIFEST_FILE% ...

rem 注意 echo 后面有空格，不要漏掉
> "%MANIFEST_FILE%" echo Main-Class: Main
>> "%MANIFEST_FILE%" echo Class-Path: %JSON_JAR%
>> "%MANIFEST_FILE%" echo

echo packing JAR as %JAR_NAME% ...
if exist "%JAR_NAME%" del "%JAR_NAME%"

jar cfm "%JAR_NAME%" "%MANIFEST_FILE%" -C "%OUT_DIR%" .

if errorlevel 1 (
    echo packing failed。
    exit /b 1
)

echo successfully packed: %JAR_NAME%
echo run using: java -jar %JAR_NAME%

endlocal
