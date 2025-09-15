# ManuSmith Shell - Installation Guide

This guide covers building native installers and deploying ManuSmith Shell on different platforms.

## 🚀 Quick Build

### Prerequisites

- **Java 21+** (required)
- **Maven 3.9+** (required)
- **Git** (for cloning repositories)

### Automated Build

```bash
# Make script executable
chmod +x build.sh

# Build for current platform
./build.sh
```

## 🍎 macOS Installation

### Building DMG Installer

1. **Clone both repositories:**
   ```bash
   git clone <manusmith-engine-repo> manusmith-engine
   git clone <manusmith-shell-repo> manusmith-shell
   ```

2. **Build the DMG:**
   ```bash
   cd manusmith-shell
   ./build-macos.sh
   ```

3. **Generated files:**
   - `target/dist/ManuSmith Shell-2.0.0.dmg` - Main installer
   - `target/manusmith-shell-1.0.0-SNAPSHOT-obfuscated.jar` - Obfuscated JAR

### Installing on macOS

1. **Mount the DMG:**
   - Double-click `ManuSmith Shell-2.0.0.dmg`

2. **Install:**
   - Drag "ManuSmith Shell" to Applications folder
   - Allow installation from unidentified developer (if prompted)

3. **Launch:**
   - From Applications folder
   - From Spotlight: Search "ManuSmith Shell"

## 🪟 Windows Installation

### Building MSI Installer

1. **Install WiX Toolset** (recommended):
   - Download from: https://wixtoolset.org/
   - Add to system PATH

2. **Clone repositories:**
   ```cmd
   git clone <manusmith-engine-repo> manusmith-engine
   git clone <manusmith-shell-repo> manusmith-shell
   ```

3. **Build the MSI:**
   ```cmd
   cd manusmith-shell
   build-windows.bat
   ```

4. **Generated files:**
   - `target\dist\ManuSmith Shell-2.0.0.msi` - Main installer
   - `target\manusmith-shell-1.0.0-SNAPSHOT-obfuscated.jar` - Obfuscated JAR

### Installing on Windows

1. **Run installer:**
   - Right-click `ManuSmith Shell-2.0.0.msi`
   - Select "Install" or "Run as administrator"

2. **Follow wizard:**
   - Accept license terms
   - Choose installation directory
   - Create shortcuts (recommended)

3. **Launch:**
   - From Start Menu → ManuSmith
   - From Desktop shortcut
   - Search "ManuSmith Shell"

## 🔧 Manual Build Process

### 1. Build Engine Dependency

```bash
cd manusmith-engine
mvn clean install -DskipTests -Dspotbugs.skip=true -Ddependency-check.skip=true -Djpackage.skip=true
```

### 2. Build Shell with Obfuscation

```bash
cd manusmith-shell
mvn clean compile package -DskipTests
```

### 3. Create Native Installer

**macOS:**
```bash
mvn -Pmacos-package install -DskipTests
```

**Windows:**
```bash
mvn -Pwindows-package install -DskipTests
```

## 🔒 Security Features

### Code Obfuscation
- **ProGuard integration** - Bytecode obfuscation
- **Class name obfuscation** - Prevents reverse engineering
- **Method renaming** - Obscures functionality
- **String encryption** - Protects sensitive data

### Protected Elements
- ✅ Main application classes
- ✅ Service layer components  
- ✅ Controller logic
- ✅ Business logic
- ✅ Configuration handling

### Excluded from Obfuscation
- JavaFX framework classes
- FXML-annotated methods
- Serialization interfaces
- Resource files (icons, FXML, CSS)

## 📂 Output Structure

```
target/
├── dist/                                    # Native installers
│   ├── ManuSmith Shell-2.0.0.dmg          # macOS installer
│   └── ManuSmith Shell-2.0.0.msi          # Windows installer
├── manusmith-shell-1.0.0-SNAPSHOT.jar     # Original JAR
├── manusmith-shell-1.0.0-SNAPSHOT-jar-with-dependencies.jar
└── manusmith-shell-1.0.0-SNAPSHOT-obfuscated.jar  # Protected JAR
```

## ⚙️ Build Configuration

### ProGuard Configuration
- Configured in `pom.xml` under ProGuard plugin
- Obfuscates classes while preserving JavaFX functionality
- Protects intellectual property

### JPackage Settings
- **macOS**: Creates DMG with proper app bundle structure
- **Windows**: Creates MSI with installation wizard
- **Memory**: Pre-configured with 2GB heap space
- **JavaFX**: Includes required runtime modules

## 🐛 Troubleshooting

### Common Issues

**"Java 21 required" error:**
- Install Java 21+ from https://adoptium.net/
- Update JAVA_HOME environment variable

**"WiX Toolset not found" (Windows):**
- Install WiX v3.x from https://wixtoolset.org/
- Add WiX bin directory to system PATH

**"manusmith-engine not found":**
- Clone manusmith-engine to parent directory
- Run build script from correct location

**DMG/MSI creation fails:**
- Check available disk space (>500MB recommended)
- Ensure no antivirus interference
- Run with administrator privileges (Windows)

### Verification

**Test obfuscated JAR:**
```bash
java -jar target/manusmith-shell-1.0.0-SNAPSHOT-obfuscated.jar
```

**Verify native installer:**
- Install on clean system
- Test all major features
- Confirm ODT support works

## 📞 Support

For build issues or questions:
1. Check this documentation first
2. Verify system requirements
3. Review error messages carefully
4. Test with minimal example

---

**🔐 Note**: All distributed versions include code obfuscation to protect intellectual property. Source code is not included in final installers.