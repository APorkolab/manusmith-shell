# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - 2025-09-15

### Added
- **Full ODT Support**: Complete integration with ManuSmith Engine v2.0.0 for native ODT file support
- **Round-trip Conversions**: ODT ↔ DOCX ↔ TXT conversion capabilities  
- **Format Preservation**: Maintains italic formatting during ODT conversions
- **CI/CD Pipeline**: Comprehensive GitHub Actions workflow with automated testing and releases
- **English Documentation**: Added English README for international users
- **Dependency Management**: Updated to Java 21 and cleaned up duplicate dependencies

### Changed
- **Engine Integration**: Updated from commented dependency to active ManuSmith Engine v2.0.0
- **Java Version**: Upgraded from Java 17 to Java 21 for consistency with engine
- **QuickConvert Status**: Updated from "Partially implemented" to "Fully implemented"
- **Documentation**: Enhanced README with ODT support information

### Improved  
- **Test Coverage**: All tests passing including ODT functionality tests
- **Performance**: Better stability through engine v2.0.0 integration
- **User Experience**: Complete document processing workflow with full format support

### Technical
- Added `-parameters` compiler flag for Spring Boot compatibility
- Removed duplicate document processing dependencies (now provided by engine)
- Fixed repository URLs in CI/CD configuration
- Consistent versioning across both projects

## [1.0.0] - Previous Release

### Features
- **MVP (Shunn formatting)**: Complete implementation with italic → underline conversion
- **Cover Letter Generator**: Automatic cover letter generation from templates
- **TypoKit**: Basic typographic fixes for HU/DE/EN and scene break normalization  
- **CrossClip Lite**: System tray integration with theme switcher and clipboard cleaning
- **JavaFX GUI**: Complete graphical user interface for all features

### Core Functionality
- File selection with drag & drop support
- Metadata input forms
- Document processing with real-time status updates
- Multi-language support (Hungarian/English/German)
- Theme switching (light/dark mode)
- Configuration management and persistence