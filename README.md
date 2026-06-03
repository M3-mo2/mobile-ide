# Mobile IDE

A professional code workspace and editor for Android devices.

## Overview

Mobile IDE is a native Android code editor built with Kotlin and Jetpack Compose. It provides a desktop-class editing experience on mobile platforms, focusing on code writing, project management, and file operations.

## Architecture

The project follows Clean Architecture with the following modules:

- **app**: Main application module with UI and DI setup
- **editor-core**: Core editing engine (TextBuffer, Cursor, Selection, Undo)
- **editor-ui**: Compose UI components for the editor
- **editor-files**: File and project management
- **editor-search**: Search and replace functionality
- **editor-highlight**: Syntax highlighting

## Features

### Core Editing
- Text editing with standard operations
- Multiple cursor support
- Undo/redo with history
- Find and replace
- Go to line
- Line numbers display

### File Management
- Project creation and opening
- File explorer with tree view
- File operations (create, rename, delete, move)
- Multiple tabs
- Recent projects list

### UI/UX
- Dark and light themes
- Customizable font size
- Split view
- Minimap
- Settings persistence

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture / MVVM
- **DI**: Hilt
- **Async**: Kotlin Coroutines
- **Build System**: Gradle with Version Catalog

## Project Structure

```
mobile-ide/
├── app/                          # Main application
│   ├── src/main/java/com/mobileide/
│   │   ├── MainActivity.kt       # Main activity
│   │   ├── MainViewModel.kt      # Main ViewModel
│   │   ├── MobileIdeApplication.kt # Application class
│   │   └── ui/theme/             # Compose themes
│   └── src/test/                 # App tests
├── editor-core/                  # Core editing engine
│   └── src/main/java/com/mobileide/editor/core/
│       ├── TextBuffer.kt         # TextBuffer interface
│       ├── PieceTableTextBuffer.kt # Piece Table implementation
│       ├── Cursor.kt             # Cursor system
│       ├── Selection.kt          # Selection system
│       ├── UndoManager.kt        # Undo/Redo system
│       ├── EditorState.kt        # Editor state management
│       ├── Document.kt           # Document lifecycle
│       └── Model.kt              # Data models
├── editor-ui/                    # Editor UI components
│   └── src/main/java/com/mobileide/editor/ui/
│       ├── EditorScreen.kt       # Main editor screen
│       ├── EditorCanvas.kt       # Editor canvas
│       ├── IdeLayout.kt          # IDE layout
│       ├── FileExplorer.kt       # File explorer sidebar
│       ├── SearchPanel.kt        # Search panel
│       ├── SettingsPanel.kt      # Settings panel
│       └── ComingSoonScreen.kt   # Placeholder screen
├── editor-files/                 # File management
│   └── src/main/java/com/mobileide/editor/files/
│       └── FileManager.kt        # File operations
├── editor-search/                # Search functionality
│   └── src/main/java/com/mobileide/editor/search/
│       └── SearchEngine.kt       # Search engine
├── editor-highlight/             # Syntax highlighting
│   └── src/main/java/com/mobileide/editor/highlight/
│       └── Highlighter.kt        # Highlighter
└── .github/workflows/            # CI/CD workflows
    ├── ci.yml                    # CI workflow
    ├── release.yml               # Release workflow
    └── nightly.yml               # Nightly workflow
```

## Architecture Overview

```
Presentation Layer (Compose UI)
    │
    ▼
Domain Layer (Editor Modules)
    ├── editor-core (TextBuffer, Cursor, Selection, Undo, Document)
    ├── editor-files (Workspace, FileManager, TabManager)
    ├── editor-search (SearchEngine, ProjectSearch)
    └── editor-highlight (Highlighter, ThemeManager)
    │
    ▼
Data Layer (Room, File System, Preferences)
```

## Performance Targets

- Cold start: < 2 seconds
- Warm start: < 1 second
- File open: < 500ms
- Scroll FPS: 60 FPS
- Typing latency: < 16ms
- Memory usage: < 150MB with 5 files open

## Development

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Minimum SDK: 26

### Building

```bash
# Build debug APK
./build.sh --build

# Run tests
./build.sh --test

# Run lint
./build.sh --lint

# Build and install debug APK
./build.sh --debug
```

### Testing

The project uses a test pyramid strategy:

- **Unit Tests**: 70% (Logic tests)
- **Integration Tests**: 20% (Feature tests)
- **E2E Tests**: 10% (UI tests)

```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :editor-core:test
```

## CI/CD

The project uses GitHub Actions for CI/CD:

- **CI Workflow**: Runs on push to develop/main branches
  - Lint (ktlint, detekt)
  - Unit tests
  - Build debug APK
  - Coverage report

- **Release Workflow**: Builds and signs release APK
- **Nightly Workflow**: Daily builds for testing

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Architecture inspired by VS Code and Zed editors
- Piece Table data structure from "The Piece Table" by Peter Reiser
- Jetpack Compose for modern Android UI
