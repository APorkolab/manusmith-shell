#!/bin/bash
# ManuSmith Shell - macOS Launcher Script
# This script provides a working macOS launcher using system JavaFX

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🍎 ManuSmith Shell - macOS Launcher${NC}"
echo "================================================"

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Check for Java
echo -e "${YELLOW}☕ Checking Java installation...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed or not in PATH${NC}"
    echo "Please install Java 11 or higher from https://adoptopenjdk.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [[ "$JAVA_VERSION" -lt "11" ]]; then
    echo -e "${RED}❌ Java 11 or higher is required. Current version: $JAVA_VERSION${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Java version: $JAVA_VERSION${NC}"

# Check for JavaFX SDK
JAVAFX_SDK_PATH="$SCRIPT_DIR/javafx-sdk-22.0.2"
if [ ! -d "$JAVAFX_SDK_PATH" ]; then
    echo -e "${RED}❌ JavaFX SDK not found at: $JAVAFX_SDK_PATH${NC}"
    echo "Please run the build script first: ./build-macos.sh"
    exit 1
fi
echo -e "${GREEN}✅ JavaFX SDK found${NC}"

# Check for application JAR
APP_JAR="$SCRIPT_DIR/target/manusmith-shell-2.0.0-jar-with-dependencies.jar"
if [ ! -f "$APP_JAR" ]; then
    echo -e "${RED}❌ Application JAR not found: $APP_JAR${NC}"
    echo "Please build the application first: mvn clean package"
    exit 1
fi
echo -e "${GREEN}✅ Application JAR found${NC}"

# Launch the application
echo ""
echo -e "${BLUE}🚀 Launching ManuSmith Shell...${NC}"
echo "JavaFX Path: $JAVAFX_SDK_PATH/lib"
echo "Application: $APP_JAR"
echo ""

# Add --illegal-access=warn to suppress warnings if needed
java --illegal-access=warn \
     --module-path "$JAVAFX_SDK_PATH/lib" \
     --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics \
     -Djava.awt.headless=false \
     -Dprism.verbose=false \
     -Dprism.order=es2,sw \
     -Dapple.awt.UIElement=false \
     -Dapple.laf.useScreenMenuBar=true \
     -Xdock:name="ManuSmith Shell" \
     -Xmx2048m \
     -jar "$APP_JAR" "$@"

# Check exit code
EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ ManuSmith Shell closed successfully${NC}"
else
    echo -e "${YELLOW}⚠️  ManuSmith Shell exited with code: $EXIT_CODE${NC}"
    echo "This might be normal if you closed the application."
fi

exit $EXIT_CODE