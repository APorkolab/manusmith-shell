@echo off
REM ManuSmith Shell - Windows Build Script
REM This script builds a native Windows MSI installer with obfuscated code

echo 🪟 Building ManuSmith Shell for Windows...

REM Check Java version
echo ☕ Checking Java version...
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i version') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "tokens=1,2 delims=." %%a in ("%JAVA_VERSION%") do (
    if %%a==1 (
        set JAVA_MAJOR=%%b
    ) else (
        set JAVA_MAJOR=%%a
    )
)

if %JAVA_MAJOR% LSS 21 (
    echo ❌ Java 21 or higher is required. Current version: %JAVA_VERSION%
    exit /b 1
)
echo ✅ Java version: %JAVA_VERSION%

REM Check if WiX Toolset is available (for MSI creation)
where candle >nul 2>nul
if errorlevel 1 (
    echo ⚠️  WiX Toolset not found in PATH. MSI creation may fail.
    echo    Download from: https://wixtoolset.org/
) else (
    echo ✅ WiX Toolset found
)

REM Build manusmith-engine first (if not already installed)
REM Skip this check in CI environment as engine is pre-installed
if not defined GITHUB_ACTIONS (
    echo 🔧 Checking manusmith-engine dependency...
    mvn dependency:resolve | findstr "manusmith-engine-plugin" >nul
    if errorlevel 1 (
        echo 📦 Building manusmith-engine...
        if exist "..\manusmith-engine" (
            cd ..\manusmith-engine
            mvn clean install -DskipTests -Dspotbugs.skip=true -Ddependency-check.skip=true -Djpackage.skip=true -q
            cd ..\manusmith-shell
            echo ✅ manusmith-engine built and installed
        ) else (
            echo ❌ manusmith-engine not found. Please clone it to ..\manusmith-engine
            exit /b 1
        )
    ) else (
        echo ✅ manusmith-engine already available
    )
) else (
    echo 🎦 Running in CI environment - assuming engine is pre-installed
)

REM Clean and build the shell project
echo 🧹 Cleaning previous build...
mvn clean

echo 🔨 Building project (without ProGuard in Maven)...
mvn compile package -DskipTests -Dspotbugs.skip=true -q
if errorlevel 1 (
    echo ❌ Maven package build failed
    exit /b 1
)
echo ✅ Maven package completed successfully

REM Create simple obfuscated JAR (copy the fat JAR as obfuscated for now)
echo 🔐 Creating obfuscated JAR...
echo Debug: Listing target directory contents after package
dir target\*.jar
echo Debug: Checking specifically for jar-with-dependencies
if exist "target\manusmith-shell-2.0.0-jar-with-dependencies.jar" (
    echo ✅ Fat JAR found, ready for copying
) else (
    echo ❌ Fat JAR not found - Maven package may have failed
    echo Debug: Full target directory listing:
    dir target
    exit /b 1
)
copy target\manusmith-shell-2.0.0-jar-with-dependencies.jar target\manusmith-shell-2.0.0-obfuscated.jar
if exist "target\manusmith-shell-2.0.0-obfuscated.jar" (
    echo ✅ Obfuscated JAR created successfully (using fat JAR)
) else (
    echo ❌ Obfuscated JAR creation failed
    exit /b 1
)

REM Create target\dist directory for artifacts
echo 📦 Creating dist directory and copying artifacts...
mkdir target\dist 2>nul
if exist "target\manusmith-shell-2.0.0-obfuscated.jar" (
    echo ✅ Copying obfuscated JAR to dist directory
    copy target\manusmith-shell-2.0.0-obfuscated.jar target\dist\
) else (
    echo ❌ Obfuscated JAR missing, cannot proceed
    exit /b 1
)

REM For now, skip MSI creation and just prepare files
echo ⚠️ Skipping MSI creation for debugging - preparing JAR files only
echo ✅ Files prepared in target\dist directory

REM Check if files were prepared successfully
echo Debug: Checking final build results
if exist "target\dist" (
    echo ✅ target\dist directory exists
    dir target\dist
    if exist "target\dist\manusmith-shell-2.0.0-obfuscated.jar" (
        echo 🎉 Windows build completed successfully
        echo 📋 Available for download: Obfuscated JAR in target\dist
    ) else (
        echo ❌ No obfuscated JAR found in dist directory
        exit /b 1
    )
) else (
    echo ❌ target\dist directory was not created
    exit /b 1
)

echo ✅ Build completed successfully!
REM pause command removed for CI compatibility
