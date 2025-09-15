# Icons Directory

This directory should contain application icons for native packaging:

## Required Files

### macOS
- `manusmith.icns` - Application icon for macOS DMG packaging
- Recommended size: 512x512px or higher
- Should include multiple resolutions (16, 32, 64, 128, 256, 512, 1024px)

### Windows  
- `manusmith.ico` - Application icon for Windows MSI packaging
- Recommended sizes: 16, 24, 32, 48, 64, 96, 128, 256px

## Icon Creation Tools

### macOS
- **Icon Composer** (Xcode Tools)
- **iconutil** (command line)
- **Third-party**: LookUp, Image2icon

### Windows
- **IcoFX** 
- **GIMP** with ICO plugin
- **Online converters**: ConvertICO, ICO Convert

## Build Behavior

If icon files are missing, the build will:
1. Show warnings but continue
2. Use default Java/JavaFX icons
3. Create functional but unbranded installers

To add proper icons:
1. Create or convert your logo to required formats
2. Place files in this directory
3. Rebuild with `./build.sh`

---

**Note**: Icons are not obfuscated and will be visible in the final installer packages.