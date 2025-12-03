@echo off
setlocal

rem ====== change JAVA_HOME to your real JDK path ======
set "JAVA_HOME=C:\Program Files\Java\jdk-21"

if not exist "%JAVA_HOME%\bin\jar.exe" (
  echo jar.exe not found in %JAVA_HOME%\bin
  exit /b 1
)

set OUT_DIR=out
set LIB_DIR=lib
set BUILD_TMP=build_tmp
set JAR_NAME=PurchaseClient.jar

rem change this file name to your real json jar
set JSON_JAR=%LIB_DIR%\json-20250517.jar

if not exist "%OUT_DIR%" (
  echo Output directory %OUT_DIR% not found. Run build_client.bat first.
  exit /b 1
)

if not exist "%JSON_JAR%" (
  echo JSON jar not found: %JSON_JAR%
  exit /b 1
)

if exist "%JAR_NAME%" del "%JAR_NAME%"

if exist "%BUILD_TMP%" rd /S /Q "%BUILD_TMP%"
mkdir "%BUILD_TMP%"

echo Copying compiled classes to temp folder...
xcopy "%OUT_DIR%\*" "%BUILD_TMP%\" /E /I /Y >nul

echo Unpacking json jar to temp folder...
pushd "%BUILD_TMP%"
"%JAVA_HOME%\bin\jar" xf "..\%JSON_JAR%"
popd

echo Building fat jar: %JAR_NAME% ...
rem Main is the entry class (no package)
"%JAVA_HOME%\bin\jar" cfe "%JAR_NAME%" Main -C "%BUILD_TMP%" .

echo Cleaning temp folder...
rd /S /Q "%BUILD_TMP%"

echo Done. Fat jar created: %JAR_NAME%
endlocal
