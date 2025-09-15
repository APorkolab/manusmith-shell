#!/bin/bash
# ManuSmith Shell - Cross-Platform Build Script
# This script builds native installers for the current platform

set -e

echo "🚀 ManuSmith Shell - Native Installer Builder"
echo "============================================="

# Detect the operating system
case "$OSTYPE" in
    darwin*)  
        echo "🍎 Detected: macOS"
        echo "Building DMG installer..."
        chmod +x build-macos.sh
        ./build-macos.sh
        ;;
    linux*)   
        echo "🐧 Detected: Linux"
        echo "Linux native packaging not yet supported."
        echo "Use: mvn clean package to create JAR file"
        exit 1
        ;;
    msys*|cygwin*|win32*) 
        echo "🪟 Detected: Windows"
        echo "Building MSI installer..."
        ./build-windows.bat
        ;;
    *)        
        echo "❌ Unsupported operating system: $OSTYPE"
        echo "Supported platforms:"
        echo "  - macOS (creates DMG)"
        echo "  - Windows (creates MSI)"
        exit 1
        ;;
esac

echo ""
echo "🎉 Build process completed!"
echo ""
echo "📁 Output files are located in:"
echo "   target/dist/"
echo ""
echo "🔒 Security Features:"
echo "   ✅ Code obfuscation enabled"
echo "   ✅ Bytecode protection active"
echo "   ✅ Source code not included"
echo ""
echo "📦 Distribution ready!"