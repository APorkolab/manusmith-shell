#!/bin/bash
# Run script for ManuSmith Shell on macOS with JavaFX compatibility fixes

set -e

echo "🚀 Starting ManuSmith Shell with macOS JavaFX compatibility fixes..."

# Build the application
echo "📦 Building application..."
mvn clean compile

# Get the classpath
echo "🔍 Building classpath..."
CLASSPATH=$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):target/classes

# Try different rendering approaches
echo "🎨 Attempting to start with software rendering..."

# Method 1: Software rendering (most compatible)
echo "🖥️  Trying Method 1: Software rendering..."
java -Djava.awt.headless=false \
     -Dprism.order=sw \
     -Dprism.verbose=true \
     -Djavafx.animation.fullspeed=true \
     -Dprism.allowhidpi=false \
     --module-path "${CLASSPATH}" \
     --add-modules javafx.controls,javafx.fxml \
     --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
     org.manusmith.shell.MainApp 2>&1 && echo "✅ Success with Method 1!" && exit 0

echo "⚠️  Method 1 failed, trying Method 2..."

# Method 2: Force d3d (if available)
echo "🖥️  Trying Method 2: Direct3D rendering..."
java -Djava.awt.headless=false \
     -Dprism.order=d3d,sw \
     -Dprism.verbose=true \
     -Djavafx.animation.fullspeed=true \
     --module-path "${CLASSPATH}" \
     --add-modules javafx.controls,javafx.fxml \
     --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
     org.manusmith.shell.MainApp 2>&1 && echo "✅ Success with Method 2!" && exit 0

echo "⚠️  Method 2 failed, trying Method 3..."

# Method 3: OpenGL
echo "🖥️  Trying Method 3: OpenGL rendering..."
java -Djava.awt.headless=false \
     -Dprism.order=es2,sw \
     -Dprism.verbose=true \
     -Djavafx.animation.fullspeed=true \
     --module-path "${CLASSPATH}" \
     --add-modules javafx.controls,javafx.fxml \
     --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
     org.manusmith.shell.MainApp 2>&1 && echo "✅ Success with Method 3!" && exit 0

echo "⚠️  Method 3 failed, trying Method 4..."

# Method 4: JavaFX Maven Plugin with custom JVM args
echo "🖥️  Trying Method 4: JavaFX Maven Plugin..."
mvn javafx:run -Prun-javafx 2>&1 && echo "✅ Success with Method 4!" && exit 0

echo "❌ All methods failed. Please check your JavaFX installation and macOS compatibility."
echo "💡 Suggestions:"
echo "   1. Make sure you're using a compatible Java version (11+)"
echo "   2. Check if your macOS version supports JavaFX"
echo "   3. Try running from a packaged DMG instead"
echo "   4. Consider using a different Java distribution with better JavaFX support"

exit 1