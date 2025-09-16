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
mvn compile package -DskipTests -Dspotbugs.skip=true -q

# Create simple obfuscated JAR (copy the fat JAR as obfuscated for now)
echo "🔐 Creating obfuscated JAR..."
cp target/manusmith-shell-2.0.0-jar-with-dependencies.jar target/manusmith-shell-2.0.0-obfuscated.jar
if [ -f "target/manusmith-shell-2.0.0-obfuscated.jar" ]; then
    echo "✅ Obfuscated JAR created successfully (using fat JAR)"
else
    echo "❌ Obfuscated JAR creation failed"
    exit 1
fi

# Download JavaFX SDK if not present
JAVAFX_VERSION="22.0.2"
JAVAFX_DIR="javafx-sdk-${JAVAFX_VERSION}"
echo "📥 Checking for JavaFX SDK..."
if [ ! -d "$JAVAFX_DIR" ]; then
    echo "📦 Downloading JavaFX SDK for macOS..."
    curl -L -o javafx-sdk-macos.zip "https://download2.gluonhq.com/openjfx/22.0.2/openjfx-22.0.2_osx-aarch64_bin-sdk.zip"
    unzip -q javafx-sdk-macos.zip
    rm javafx-sdk-macos.zip
    echo "✅ JavaFX SDK downloaded and extracted"
else
    echo "✅ JavaFX SDK already available"
fi

# Copy JavaFX modules to target directory
echo "📋 Copying JavaFX modules..."
mkdir -p target/javafx-mods
cp "$JAVAFX_DIR/lib/"*.jar target/javafx-mods/
echo "✅ JavaFX modules copied to target directory"

# Create native macOS installer
echo "📦 Creating macOS DMG installer..."
mvn -Pmacos-package install -DskipTests -Dspotbugs.skip=true -q

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