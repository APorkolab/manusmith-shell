#!/bin/bash
# ManuSmith Shell - macOS Build Script
# This script builds a native macOS DMG installer with obfuscated code

set -e

echo "🍎 Building ManuSmith Shell for macOS..."

# Check if we're on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "❌ This script must be run on macOS"
    exit 1
fi

# Check Java version
echo "☕ Checking Java version..."
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [[ "$JAVA_VERSION" -lt "21" ]]; then
    echo "❌ Java 21 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi
echo "✅ Java version: $JAVA_VERSION"

# Build manusmith-engine first (if not already installed)
# Skip this check in CI environment as engine is pre-installed
if [ -z "${GITHUB_ACTIONS:-}" ]; then
    echo "🔧 Checking manusmith-engine dependency..."
    if ! mvn dependency:resolve | grep -q "manusmith-engine-plugin"; then
        echo "📦 Building manusmith-engine..."
        if [ -d "../manusmith-engine" ]; then
            cd ../manusmith-engine
            mvn clean install -DskipTests -Dspotbugs.skip=true -Ddependency-check.skip=true -Djpackage.skip=true -q
            cd ../manusmith-shell
            echo "✅ manusmith-engine built and installed"
        else
            echo "❌ manusmith-engine not found. Please clone it to ../manusmith-engine"
            exit 1
        fi
    else
        echo "✅ manusmith-engine already available"
    fi
else
    echo "🎦 Running in CI environment - assuming engine is pre-installed"
fi

# Clean and build the shell project
echo "🧹 Cleaning previous build..."
mvn clean

echo "🔨 Building project (without ProGuard in Maven)..."
mvn compile package -DskipTests -q

# Create obfuscated JAR using standalone ProGuard
echo "🔐 Starting ProGuard obfuscation..."

# Create ProGuard configuration
cat > proguard-config.pro << 'EOF'
-injars target/manusmith-shell-2.0.0-jar-with-dependencies.jar
-outjars target/manusmith-shell-2.0.0-obfuscated.jar

-libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/jmods/java.desktop.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/jmods/javafx.controls.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/jmods/javafx.fxml.jmod(!**.jar;!module-info.class)

-keep public class org.manusmith.shell.MainApp {
    public static void main(java.lang.String[]);
}

-keep class * extends javafx.application.Application { *; }
-keep class * extends javafx.fxml.Initializable { *; }

-keep class * {
    @javafx.fxml.FXML <fields>;
    @javafx.fxml.FXML <methods>;
}

-keepclassmembers class * {
    @javafx.fxml.FXML <fields>;
    @javafx.fxml.FXML <methods>;
}

-keep class javafx.** { *; }
-keep class com.sun.javafx.** { *; }
-keep class org.manusmith.shell.controller.** { *; }
-keep class org.manusmith.shell.service.** { *; }

-dontwarn java.desktop/**
-dontwarn javafx.**
-dontwarn com.sun.javafx.**
-dontwarn **

-dontshrink
-dontoptimize
-dontnote
-ignorewarnings

-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
-adaptresourcefilenames **.fxml,**.css,**.properties
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF
EOF

# Download ProGuard if not exists
if [ ! -f "proguard.jar" ]; then
    echo "📥 Downloading ProGuard..."
    curl -L -o proguard.zip "https://github.com/Guardsquare/proguard/releases/download/v7.4.2/proguard-7.4.2.zip"
    unzip -q proguard.zip
    cp proguard*/lib/proguard.jar .
    rm -rf proguard* proguard.zip
fi

# Run ProGuard obfuscation
java -jar proguard.jar @proguard-config.pro

if [ -f "target/manusmith-shell-2.0.0-obfuscated.jar" ]; then
    echo "✅ ProGuard obfuscation completed successfully"
else
    echo "❌ Obfuscated JAR not found. Build may have failed."
    exit 1
fi

# Create native macOS installer
echo "📦 Creating macOS DMG installer..."
mvn -Pmacos-package install -DskipTests -q

# Check if DMG was created
DMG_PATH="target/dist/ManuSmith Shell-2.0.0.dmg"
if [ -f "$DMG_PATH" ]; then
    echo "🎉 macOS DMG created successfully: $DMG_PATH"
    echo "📊 DMG size: $(du -h "$DMG_PATH" | cut -f1)"
    echo ""
    echo "📋 Installation instructions:"
    echo "1. Double-click the DMG file to mount it"
    echo "2. Drag 'ManuSmith Shell' to your Applications folder" 
    echo "3. Launch from Applications or Spotlight"
    echo ""
    echo "🔒 Note: The application code is obfuscated for protection"
else
    echo "❌ DMG creation failed"
    exit 1
fi

echo "✅ Build completed successfully!"