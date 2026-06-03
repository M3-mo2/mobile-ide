# Changelog

All notable changes to Mobile IDE will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project structure
- Core editing engine with Piece Table data structure
- Cursor and selection system
- Undo/redo system
- Basic Compose UI for editor
- File management system
- Search and replace functionality
- Syntax highlighting with regex-based highlighter
- Support for Kotlin, Java, JavaScript, Python, JSON, XML, Markdown
- Dark and light themes
- GitHub Actions CI/CD workflows
- Unit tests for core modules

### Technical
- Clean Architecture with 5 modules
- Hilt dependency injection setup
- Jetpack Compose for UI
- Immutable data structures
- Version catalog for dependency management

## [1.0.0] - TBD

### Added
- Initial release
- Core editing features
- Project management
- File operations
- Syntax highlighting
- Search functionality
- Multiple themes

### Performance
- 60 FPS scrolling target
- Large file support (up to 50k lines)
- Memory usage under 150MB

## Future Releases

### [1.5.0] - TBD
- Git integration
- Tree-sitter syntax highlighting
- LSP preparation

### [2.0.0] - TBD
- LSP support
- Terminal emulator
- Plugin system
- Cloud sync

### [2.5.0] - TBD
- Advanced Git features
- Plugin marketplace
- Collaborative editing research

### [3.0.0] - TBD
- iOS port
- AI-assisted features
- Advanced debugging
- Enterprise features
