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
mvn compile package -DskipTests -q

REM Create obfuscated JAR using standalone ProGuard
echo 🔐 Starting ProGuard obfuscation...

REM Create ProGuard configuration
(
echo -injars target\manusmith-shell-1.0.0-SNAPSHOT-jar-with-dependencies.jar
echo -outjars target\manusmith-shell-2.0.0-obfuscated.jar
echo.
echo -libraryjars ^<java.home^>/jmods/java.base.jmod^(^^!**.jar;^^!module-info.class^)
echo -libraryjars ^<java.home^>/jmods/java.desktop.jmod^(^^!**.jar;^^!module-info.class^)
echo -libraryjars ^<java.home^>/jmods/javafx.controls.jmod^(^^!**.jar;^^!module-info.class^)
echo -libraryjars ^<java.home^>/jmods/javafx.fxml.jmod^(^^!**.jar;^^!module-info.class^)
echo.
echo -keep public class org.manusmith.shell.MainApp {
echo     public static void main^(java.lang.String[]^);
echo }
echo.
echo -keep class * extends javafx.application.Application { *; }
echo -keep class * extends javafx.fxml.Initializable { *; }
echo.
echo -keep class * {
echo     @javafx.fxml.FXML ^<fields^>;
echo     @javafx.fxml.FXML ^<methods^>;
echo }
echo.
echo -keepclassmembers class * {
echo     @javafx.fxml.FXML ^<fields^>;
echo     @javafx.fxml.FXML ^<methods^>;
echo }
echo.
echo -keep class javafx.** { *; }
echo -keep class com.sun.javafx.** { *; }
echo -keep class org.manusmith.shell.controller.** { *; }
echo -keep class org.manusmith.shell.service.** { *; }
echo.
echo -dontwarn java.desktop/**
echo -dontwarn javafx.**
echo -dontwarn com.sun.javafx.**
echo -dontwarn **
echo.
echo -dontshrink
echo -dontoptimize
echo -dontnote
echo -ignorewarnings
echo.
echo -keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
echo -adaptresourcefilenames **.fxml,**.css,**.properties
echo -adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF
) > proguard-config.pro

REM Download ProGuard if not exists
if not exist "proguard.jar" (
    echo 📥 Downloading ProGuard...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/Guardsquare/proguard/releases/download/v7.4.2/proguard-7.4.2.zip' -OutFile 'proguard.zip'"
    powershell -Command "Expand-Archive -Path 'proguard.zip' -DestinationPath '.'"
    copy proguard*\lib\proguard.jar .
    rmdir /s /q proguard*
    del proguard.zip
)

REM Run ProGuard obfuscation
java -jar proguard.jar @proguard-config.pro

if exist "target\manusmith-shell-2.0.0-obfuscated.jar" (
    echo ✅ ProGuard obfuscation completed successfully
) else (
    echo ❌ Obfuscated JAR not found. Build may have failed.
    exit /b 1
)

REM Create native Windows installer
echo 📦 Creating Windows MSI installer...
mvn -Pwindows-package install -DskipTests -q

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
