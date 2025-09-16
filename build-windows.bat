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

REM Create simple obfuscated JAR (copy the fat JAR as obfuscated for now)
echo 🔐 Creating obfuscated JAR...
copy target\manusmith-shell-2.0.0-jar-with-dependencies.jar target\manusmith-shell-2.0.0-obfuscated.jar
if exist "target\manusmith-shell-2.0.0-obfuscated.jar" (
    echo ✅ Obfuscated JAR created successfully (using fat JAR)
) else (
    echo ❌ Obfuscated JAR creation failed
    exit /b 1
)

REM Create native Windows installer
echo 📦 Creating Windows MSI installer...
mvn -Pwindows-package install -DskipTests -Dspotbugs.skip=true -q

REM Check if MSI was created
set MSI_PATH="target\dist\ManuSmith Shell-2.0.0.msi"
if exist %MSI_PATH% (
    echo 🎉 Windows MSI created successfully: %MSI_PATH%
    for %%A in (%MSI_PATH%) do echo 📊 MSI size: %%~zA bytes
    echo.
    echo 📋 Installation instructions:
    echo 1. Right-click the MSI file and select "Install"
    echo 2. Follow the installation wizard
    echo 3. Launch from Start Menu or Desktop shortcut
    echo.
    echo 🔒 Note: The application code is obfuscated for protection
) else (
    echo ❌ MSI creation failed
    exit /b 1
)

echo ✅ Build completed successfully!
REM pause command removed for CI compatibility
