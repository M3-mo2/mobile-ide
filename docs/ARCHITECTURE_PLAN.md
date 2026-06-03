# Mobile IDE: Architecture & Development Plan

**Version:** 1.0.0
**Date:** 2026-06-03
**Status:** Draft - Ready for Review
**Classification:** Internal Engineering Document

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Product Scope](#2-product-scope)
3. [Architecture Overview](#3-architecture-overview)
4. [Module Structure](#4-module-structure)
5. [Editor Core Design](#5-editor-core-design)
6. [Rendering Strategy](#6-rendering-strategy)
7. [File Management Design](#7-file-management-design)
8. [Syntax Highlighting Plan](#8-syntax-highlighting-plan)
9. [Large File Strategy](#9-large-file-strategy)
10. [Performance Budget](#10-performance-budget)
11. [Technical Roadmap](#11-technical-roadmap)
12. [Development Workflow](#12-development-workflow)
13. [Risks & Mitigations](#13-risks--mitigations)
14. [Final Recommendations](#14-final-recommendations)

---

## 1. Executive Summary

### 1.1 Project Description

Mobile IDE is a professional code workspace and editor for Android devices. It is designed to provide a desktop-class editing experience on mobile platforms, focusing on code writing, project management, and file operations without code execution capabilities.

The project targets developers who need to write, review, and manage code on mobile devices, including:
- Professional developers working remotely
- Students learning programming
- Open-source contributors reviewing code
- Developers managing configuration files

### 1.2 Core Objectives

1. **Performance First**: Handle files up to 100,000 lines smoothly on mid-range Android devices
2. **Native Experience**: Fully native Android implementation using Kotlin and Jetpack Compose
3. **Professional Editing**: Desktop-class editing capabilities including multiple cursors, advanced search, and code intelligence
4. **Project Management**: Efficient handling of multi-file projects with thousands of files
5. **Extensibility**: Architecture designed for future additions: Git integration, LSP support, terminal, and plugins

### 1.3 Target Audience

| Segment | Needs | Priority |
|---------|-------|----------|
| Professional Developers | Code review, quick edits, remote work | High |
| Students | Learning, practice, small projects | Medium |
| OSS Maintainers | Review PRs, manage issues, edit configs | High |
| Sysadmins | Edit configuration files, scripts | Medium |

### 1.4 Long-term Vision (3-5 Years)

- **Year 1**: Establish core editing experience with project management
- **Year 2**: Add Git integration, LSP support, and cloud sync
- **Year 3**: Plugin ecosystem, collaborative editing, iOS port
- **Year 4-5**: AI-assisted coding, advanced debugging, enterprise features

### 1.5 Key Differentiators

1. **Native Performance**: No WebView, no JavaScript bridge, pure Kotlin implementation
2. **Mobile-Optimized**: Touch-friendly interface with gesture support
3. **Offline-First**: All operations work without internet connection
4. **Lightweight**: Sub-100MB memory footprint for typical usage
5. **Extensible**: Plugin architecture for community contributions

---

## 2. Product Scope

### 2.1 MVP Features (Version 1.0)

#### Core Editing
- [ ] Text editing with standard operations (insert, delete, copy, paste)
- [ ] Multiple cursor support (up to 10 cursors)
- [ ] Undo/redo with history (up to 10,000 operations)
- [ ] Find and replace (single file)
- [ ] Go to line
- [ ] Line numbers display
- [ ] Basic syntax highlighting (top 20 languages)
- [ ] Auto-indentation
- [ ] Bracket matching
- [ ] Code folding (basic)

#### File Management
- [ ] Project creation and opening
- [ ] File explorer with tree view
- [ ] File operations (create, rename, delete, move)
- [ ] Multiple tabs (up to 20 open files)
- [ ] Recent projects list
- [ ] File search within project

#### UI/UX
- [ ] Dark and light themes
- [ ] Customizable font size and family
- [ ] Split view (2 files side by side)
- [ ] Minimap (simplified code overview)
- [ ] Settings persistence

#### Performance
- [ ] Smooth scrolling (60 FPS target)
- [ ] Files up to 50,000 lines
- [ ] Projects up to 5,000 files
- [ ] Memory usage under 150MB

### 2.2 Deferred Features (Post-MVP)

| Feature | Target Version | Reason |
|---------|---------------|--------|
| Git Integration | V1.5 | Requires external libraries and UI |
| LSP Support | V2.0 | Complex protocol implementation |
| Terminal | V2.0 | Requires native process management |
| Plugin System | V2.5 | Needs stable API first |
| Cloud Sync | V2.0 | Requires backend infrastructure |
| Collaborative Editing | V3.0 | Complex CRDT implementation |
| Debugging | V3.0 | Requires language-specific adapters |
| iOS Port | V3.0 | Major platform expansion |

### 2.3 Features Priority Matrix

```
                    High Impact
                         |
    +--------------------+--------------------+
    |                    |                    |
    |  DO FIRST          |  DO NEXT           |
    |  - Core Editor     |  - Git Integration |
    |  - File Explorer   |  - LSP Support     |
    |  - Syntax Highlight|  - Terminal        |
    |  - Search          |  - Cloud Sync      |
    |                    |                    |
Low |--------------------+--------------------| High
Effort|  AVOID             |  CONSIDER          |
    |  - Custom Language |  - Plugin API      |
    |  - AI Completion   |  - Collaboration   |
    |  - Video Tutorials |  - Marketplace     |
    |                    |                    |
    +--------------------+--------------------+
                         |
                    Low Impact
```

---

## 3. Architecture Overview

### 3.1 Architectural Principles

1. **Separation of Concerns**: Each module has a single, well-defined responsibility
2. **Dependency Inversion**: High-level modules depend on abstractions, not implementations
3. **Immutability**: Core data structures are immutable to prevent side effects
4. **Lazy Evaluation**: Compute only what is needed, when it is needed
5. **Offline-First**: All data stored locally, sync is optional

### 3.2 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐ │
│  │   Editor    │  │   Sidebar   │  │   Tab Bar   │  │  Toolbar  │ │
│  │   Screen    │  │   (Files)   │  │             │  │           │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └─────┬─────┘ │
│         │                │                │              │         │
│         └────────────────┴────────────────┴──────────────┘         │
│                              │                                    │
│                              ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │                    EDITOR UI MODULE                       │    │
│  │  (Compose Screens, ViewModels, State Management)         │    │
│  └──────────────────────────┬───────────────────────────────┘    │
└─────────────────────────────┼─────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         DOMAIN LAYER                                 │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                     EDITOR CORE MODULE                        │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │  │
│  │  │  Text    │  │  Cursor  │  │ Selection│  │    Undo/     │ │  │
│  │  │  Buffer  │  │  System  │  │  System  │  │    Redo      │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────────┘ │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │  │
│  │  │ Document │  │  Search  │  │  Syntax  │  │   Folding    │ │  │
│  │  │ Lifecycle│  │  Engine  │  │ Highlight│  │   Engine     │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────────┘ │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                      │
│                              ▼                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                     EDITOR FILES MODULE                         │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │  │
│  │  │ Workspace│  │  File    │  │  Project │  │   Autosave   │ │  │
│  │  │ Manager  │  │  System  │  │  Index   │  │   Engine     │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────────┘ │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                      │
│                              ▼                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                     EDITOR SEARCH MODULE                        │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐                   │  │
│  │  │  Find in │  │  Replace │  │  Project │                   │  │
│  │  │  File    │  │  Engine  │  │  Search  │                   │  │
│  │  └──────────┘  └──────────┘  └──────────┘                   │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                              │                                      │
│                              ▼                                      │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                     EDITOR HIGHLIGHT MODULE                     │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐                   │  │
│  │  │  Regex   │  │  Theme   │  │  Token   │                   │  │
│  │  │  Engine  │  │  Manager │  │  Cache   │                   │  │
│  │  └──────────┘  └──────────┘  └──────────┘                   │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │
│  │   Local     │  │   File      │  │ Preferences │  │   Cache   │  │
│  │   Database  │  │   System    │  │   (Settings)│  │   (LRU)   │  │
│  │   (Room)    │  │   (Direct)  │  │   (DataStore)│  │           │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └───────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.3 Layer Responsibilities

#### Presentation Layer
- **Responsibility**: Render UI and handle user interactions
- **Components**: Compose screens, ViewModels, state holders
- **Rules**: 
  - No direct access to file system
  - All operations through ViewModel
  - State is immutable and observable

#### Domain Layer (Editor Modules)
- **Responsibility**: Core editing logic and business rules
- **Components**: Editor core, file management, search, highlighting
- **Rules**:
  - Pure Kotlin, no Android dependencies
  - All operations are testable
  - State changes are explicit and logged

#### Data Layer
- **Responsibility**: Persistence and external data access
- **Components**: Room database, file system access, preferences
- **Rules**:
  - All I/O on background threads
  - Transactions for consistency
  - Caching for performance

### 3.4 Data Flow

```
User Action
     │
     ▼
┌─────────────┐
│  UI Event   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  ViewModel  │
│  (Process)  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Use Case   │
│  (Business  │
│   Logic)    │
└──────┬──────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│  Repository │────▶│  In-Memory │
│  (Abstract)  │     │   Cache    │
└──────┬──────┘     └─────────────┘
       │
       ▼
┌─────────────┐
│  Data Source│
│  (File/DB)  │
└─────────────┘
       │
       ▼
┌─────────────┐
│  Result     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  UI State   │
│  Update     │
└─────────────┘
```

---

## 4. Module Structure

### 4.1 Core Modules (V1.0)

#### editor-core
**Purpose**: Heart of the editing engine
**Responsibilities**:
- Text buffer management (Piece Table)
- Cursor and selection logic
- Undo/redo system
- Document lifecycle
- Edit operations

**Public API**:
```
TextBuffer (interface)
  - insert(offset: Int, text: String): TextBuffer
  - delete(offset: Int, length: Int): TextBuffer
  - getText(): String
  - getLine(lineNumber: Int): String
  - getLineCount(): Int

Cursor (interface)
  - move(direction: Direction): Cursor
  - moveTo(position: Position): Cursor
  - getPosition(): Position

Selection (interface)
  - select(range: Range): Selection
  - extend(direction: Direction): Selection
  - getRanges(): List<Range>

UndoManager (interface)
  - record(operation: EditOperation): UndoManager
  - undo(): EditOperation?
  - redo(): EditOperation?
  - canUndo(): Boolean
  - canRedo(): Boolean
```

**Dependencies**: None (pure Kotlin)

#### editor-ui
**Purpose**: User interface components
**Responsibilities**:
- Editor screen layout
- Compose canvas rendering
- Touch and gesture handling
- Theme application
- Toolbar and menus

**Public API**:
```
EditorScreen (Composable)
  - state: EditorState
  - onEdit: (EditOperation) -> Unit
  - onCursorMove: (Position) -> Unit

EditorCanvas (Composable)
  - textLines: List<RenderedLine>
  - viewport: Viewport
  - onScroll: (Float) -> Unit

Gutter (Composable)
  - lineNumbers: List<Int>
  - cursorLine: Int
  - breakpoints: List<Int>
```

**Dependencies**: editor-core, editor-highlight

#### editor-files
**Purpose**: File and project management
**Responsibilities**:
- Workspace structure
- File operations
- Project indexing
- Tab management
- Recent files tracking

**Public API**:
```
Workspace (interface)
  - openProject(path: String): Project
  - closeProject(): Unit
  - getCurrentProject(): Project?

FileManager (interface)
  - createFile(path: String): Boolean
  - deleteFile(path: String): Boolean
  - renameFile(oldPath: String, newPath: String): Boolean
  - readFile(path: String): String
  - writeFile(path: String, content: String): Boolean

TabManager (interface)
  - openFile(path: String): Tab
  - closeFile(tabId: String): Boolean
  - switchTab(tabId: String): Tab
  - getOpenTabs(): List<Tab>
```

**Dependencies**: editor-core

#### editor-search
**Purpose**: Search and replace functionality
**Responsibilities**:
- In-file search
- Project-wide search
- Regex support
- Replace operations
- Search history

**Public API**:
```
SearchEngine (interface)
  - search(query: String, options: SearchOptions): SearchResult
  - findNext(): Match?
  - findPrevious(): Match?
  - replace(match: Match, replacement: String): EditOperation
  - replaceAll(query: String, replacement: String): Int

ProjectSearch (interface)
  - searchInProject(query: String, scope: SearchScope): Flow<SearchResult>
  - indexProject(project: Project): Flow<IndexingProgress>
```

**Dependencies**: editor-core, editor-files

#### editor-highlight
**Purpose**: Syntax highlighting
**Responsibilities**:
- Language detection
- Tokenization
- Theme management
- Style application

**Public API**:
```
Highlighter (interface)
  - highlight(text: String, language: Language): List<Token>
  - getLanguages(): List<Language>
  - detectLanguage(fileName: String): Language?

ThemeManager (interface)
  - getTheme(id: String): Theme
  - setTheme(id: String): Unit
  - getAvailableThemes(): List<Theme>
```

**Dependencies**: editor-core

### 4.2 Future Modules (Post-V1)

#### editor-lsp
**Purpose**: Language Server Protocol support
**Integration Point**: Extends editor-core with LSP client
**Architecture**:
```
LSPClient (interface)
  - connect(server: LanguageServer): Connection
  - initialize(params: InitializeParams): InitializeResult
  - completion(params: CompletionParams): CompletionList
  - hover(params: HoverParams): Hover
  - definition(params: DefinitionParams): List<Location>
  - diagnostics(params: DocumentDiagnosticParams): DiagnosticResult
```

**Dependencies**: editor-core, editor-highlight

#### editor-git
**Purpose**: Git version control
**Integration Point**: Extends editor-files with version control
**Architecture**:
```
GitManager (interface)
  - init(path: String): Repository
  - clone(url: String, path: String): Repository
  - commit(message: String): Commit
  - push(remote: String, branch: String): Boolean
  - pull(remote: String, branch: String): Boolean
  - status(): Status
  - log(): List<Commit>
```

**Dependencies**: editor-files

#### editor-terminal
**Purpose**: Terminal emulator
**Integration Point**: New UI component in editor-ui
**Architecture**:
```
Terminal (interface)
  - createSession(shell: String): Session
  - execute(command: String): Output
  - resize(rows: Int, cols: Int): Unit
```

**Dependencies**: editor-ui

#### editor-plugins
**Purpose**: Plugin system
**Integration Point**: Extension points in all modules
**Architecture**:
```
PluginManager (interface)
  - loadPlugin(path: String): Plugin
  - unloadPlugin(id: String): Boolean
  - getLoadedPlugins(): List<Plugin>
  - registerExtensionPoint(point: ExtensionPoint): Unit
```

**Dependencies**: All modules

### 4.3 Module Dependency Graph

```
                    editor-ui
                       │
           ┌───────────┼───────────┐
           │           │           │
           ▼           ▼           ▼
      editor-core  editor-files editor-highlight
           │           │           │
           └───────────┼───────────┘
                       │
                       ▼
                  editor-search
                       │
           ┌───────────┼───────────┐
           │           │           │
           ▼           ▼           ▼
      editor-lsp   editor-git  editor-terminal
           │           │           │
           └───────────┴───────────┘
                       │
                       ▼
                  editor-plugins
```

### 4.4 Future Module Integration Strategy

To add future modules without restructuring:

1. **Extension Points**: Define interfaces in core modules that future modules implement
2. **Event Bus**: Use events for loose coupling between modules
3. **Dependency Injection**: Hilt modules per feature for easy addition
4. **Feature Flags**: Toggle features without code changes

---

## 5. Editor Core Design

### 5.1 Text Buffer Strategy

#### Data Structure: Piece Table

**Rationale**:
- Immutable operations enable undo/redo
- Efficient for large files (O(log n) edits)
- Memory efficient (stores edits, not copies)
- Proven in VS Code, Zed, Helix

**Design**:
```
Piece Table Structure:
  Original Buffer: String (immutable)
  Add Buffer: StringBuilder (append-only)
  Pieces: List<Piece> (sorted)

Piece:
  source: ORIGINAL | ADD
  start: Int
  length: Int
  lineStarts: List<Int>
```

**Operations**:
- **Insert**: O(log n) - find piece, split, insert new piece
- **Delete**: O(log n) - find range, split, mark deleted
- **Get Text**: O(m) where m = number of pieces intersecting range
- **Get Line**: O(1) with line cache

**Memory Model**:
```
For file with N lines, M edits:
  Original text: N * avg_line_length bytes
  Add buffer: sum(edit_lengths) bytes
  Pieces: M * 32 bytes overhead
  Line cache: N * 4 bytes

Example: 10,000 lines, 1,000 edits
  Original: ~500KB
  Add buffer: ~50KB
  Pieces: ~32KB
  Line cache: ~40KB
  Total: ~622KB (vs 500KB for plain text)
```

**Line Cache Strategy**:
- Cache line start offsets in array
- Updated incrementally on edits
- Enables O(1) line access
- Invalidated on major structural changes

### 5.2 Cursor System

#### Design

**Cursor State**:
```
Cursor:
  position: Position (line, column)
  preferredColumn: Int (for vertical movement)
  isRTL: Boolean

Position:
  line: Int (0-based)
  column: Int (0-based, UTF-16 code units)
```

**Movement Operations**:
- **Horizontal**: left, right, wordLeft, wordRight, lineStart, lineEnd
- **Vertical**: up, down, pageUp, pageDown
- **Document**: fileStart, fileEnd
- **Smart**: matchingBracket, declaration

**Vertical Movement Logic**:
```
When moving up/down:
  1. Calculate target line
  2. Try to maintain preferredColumn
  3. Clamp to line length
  4. Update preferredColumn if explicit horizontal move
```

**Multiple Cursors**:
```
CursorManager:
  primaryCursor: Cursor
  secondaryCursors: List<Cursor> (max 10)

Operations:
  - addCursor(position): Add cursor at position
  - removeCursor(index): Remove specific cursor
  - mergeOverlapping(): Merge cursors that overlap
  - operateAll(operation): Apply edit to all cursors
```

### 5.3 Selection System

#### Design

**Selection State**:
```
Selection:
  anchor: Position (fixed end)
  head: Position (active end)

Derived:
  start: Position (min(anchor, head))
  end: Position (max(anchor, head))
  isEmpty: Boolean (start == end)
  isReversed: Boolean (head < anchor)
```

**Selection Types**:
1. **Normal**: Click and drag
2. **Word**: Double-click (select word)
3. **Line**: Triple-click (select line)
4. **Block**: Alt+drag (rectangular selection)

**Selection Operations**:
- **Extend**: Shift+direction extends selection
- **Shrink**: Contract from active end
- **Invert**: Swap anchor and head
- **Clear**: Remove selection, keep cursor

**Multiple Selections**:
- Each cursor can have its own selection
- Operations apply to all selections simultaneously
- Overlapping selections are merged

### 5.4 Undo/Redo

#### Design: Version Tree

**Rationale**: Simple stack is insufficient for multiple cursors and complex operations. Version tree allows branching history.

**Structure**:
```
UndoManager:
  history: List<EditOperation>
  currentIndex: Int
  maxHistory: Int (default 10,000)

EditOperation:
  id: UUID
  type: INSERT | DELETE | REPLACE | COMPOUND
  range: Range
  oldText: String
  newText: String
  cursorBefore: Cursor
  cursorAfter: Cursor
  timestamp: Long
  isCompound: Boolean

CompoundOperation:
  operations: List<EditOperation>
  (groups multiple edits into single undoable unit)
```

**Operations**:
- **Record**: Add operation, clear redo history
- **Undo**: Decrement index, return inverse operation
- **Redo**: Increment index, return operation
- **Group**: Mark start/end of group for compound operations

**Grouping Strategy**:
```
Auto-group edits when:
  - Same operation type
  - Adjacent positions
  - Within time threshold (500ms)
  - From same source (typing vs paste)

Examples:
  - Typing "hello" = 1 group (5 inserts)
  - Paste = 1 group (1 insert)
  - Multi-cursor edit = 1 compound operation
```

### 5.5 Document Lifecycle

#### States

```
Document Lifecycle:

  [CREATED] ──open()──▶ [OPEN]
                            │
                            │ edit()
                            ▼
                        [MODIFIED]
                            │
                   ┌────────┴────────┐
                   │                 │
                save()           close()
                   │                 │
                   ▼                 ▼
               [SAVED]          [CLOSED]
                   │                 │
                edit()            reopen()
                   │                 │
                   └────────┬────────┘
                            ▼
                        [MODIFIED]
```

**State Transitions**:
- **CREATED -> OPEN**: File opened, content loaded
- **OPEN -> MODIFIED**: User makes edit
- **MODIFIED -> SAVED**: Auto-save or manual save
- **MODIFIED -> CLOSED**: Close without saving (prompt user)
- **CLOSED -> OPEN**: Reopen file

**Dirty Tracking**:
```
Document:
  contentHash: String (SHA-256 of content)
  savedHash: String (hash at last save)
  
  isDirty: Boolean = contentHash != savedHash
  
  onEdit():
    update contentHash
    mark dirty
    start auto-save timer
```

### 5.6 State Management

#### Architecture: Unidirectional Data Flow

```
┌─────────────┐
│   Action    │ (User intent)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Reducer    │ (Pure function: State + Action -> New State)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  New State  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Observer   │ (UI updates)
└─────────────┘
```

**State Structure**:
```
EditorState:
  document: DocumentState
  cursor: CursorState
  selection: SelectionState
  viewport: ViewportState
  search: SearchState
  settings: EditorSettings

DocumentState:
  filePath: String?
  content: TextBuffer
  isDirty: Boolean
  isReadOnly: Boolean
  language: Language
  encoding: String
  lineEnding: LineEnding

CursorState:
  primary: Cursor
  secondary: List<Cursor>
  isBlinking: Boolean

ViewportState:
  topLine: Int
  leftColumn: Int
  width: Float
  height: Float
  lineHeight: Float
```

**State Immutability**:
- All state objects are immutable data classes
- Changes create new instances
- Enables time-travel debugging
- Thread-safe by design

---

## 6. Rendering Strategy

### 6.1 Compose Canvas

#### Rationale

**Why Compose Canvas over Custom View:**
1. **Future-proof**: Jetpack Compose is the future of Android UI
2. **Declarative**: UI is function of state, easier to reason about
3. **Performance**: Skia rendering, GPU accelerated
4. **Integration**: Seamless with rest of app UI
5. **Testing**: Easier to test Compose components

**Why not TextView/EditText:**
- Limited customization
- Poor performance with large files
- No multi-cursor support
- Hard to extend

#### Architecture

```
EditorCanvas (Composable)
  ├── Gutter (Composable)
  │     └── LineNumbers
  ├── TextArea (Composable)
  │     └── Lines (LazyColumn)
  │           └── Line (Composable)
  │                 └── Tokens
  ├── Minimap (Composable)
  └── Scrollbar (Composable)
```

### 6.2 Virtual Scrolling

#### Design

**Concept**: Render only visible lines + small buffer

```
Viewport:
  topLine: Int = 0
  visibleLines: Int = 50
  bufferLines: Int = 10 (above and below)
  
Total rendered: visibleLines + 2 * bufferLines = 70 lines

For 100,000 line file:
  Rendered: 70 lines (0.07%)
  Memory: ~70 * avg_line_length * overhead
```

**Implementation**:
```
LazyColumn:
  itemsIndexed(lines) { index, line ->
    if (index in visibleRange) {
      LineComposable(line)
    } else {
      Spacer(modifier = Modifier.height(lineHeight))
    }
  }
```

**Scroll Handling**:
- **Smooth scroll**: Animate offset within viewport
- **Jump scroll**: Recalculate visible range
- **Momentum**: Continue scrolling after gesture ends
- **Edge resistance**: Slow down at file boundaries

### 6.3 Line Cache

#### Design

**Purpose**: Avoid re-rendering unchanged lines

**Cache Structure**:
```
LineCache:
  maxSize: Int = 200 (configurable)
  cache: LinkedHashMap<Int, RenderedLine>

RenderedLine:
  lineNumber: Int
  text: String
  tokens: List<Token>
  layout: LineLayout
  width: Float
  height: Float
  timestamp: Long
  version: Int
```

**Cache Policy**:
- **LRU Eviction**: Remove least recently used lines
- **Version Check**: Increment version on edit, invalidate if version mismatch
- **Memory Pressure**: Reduce cache size on low memory warnings

**Invalidation Strategy**:
```
On edit at line N:
  - Invalidate line N
  - Invalidate lines N-1 to N+1 (for context)
  - Keep other lines

On theme change:
  - Invalidate all lines
  - Re-render with new theme

On font change:
  - Invalidate all lines
  - Recalculate layouts
```

### 6.4 Dirty Rendering

#### Design

**Purpose**: Only redraw changed regions

**Dirty Regions**:
```
DirtyRegion:
  lineStart: Int
  lineEnd: Int
  columnStart: Int (optional)
  columnEnd: Int (optional)
```

**Rendering Pipeline**:
```
1. Calculate dirty regions from edits
2. For each dirty region:
   a. Re-tokenize lines
   b. Re-layout lines
   c. Update cache
3. Compose recomposes only changed lines
4. Canvas draws only changed regions
```

**Optimization**:
- Batch multiple edits into single recomposition
- Debounce rapid edits (typing)
- Use `remember` for expensive calculations
- `derivedStateOf` for computed values

### 6.5 Memory Management

#### Strategy

**Memory Budget**: 150MB total for editor

**Allocation**:
```
Text Buffer: 50MB max
  - Original text: 30MB
  - Add buffer: 10MB
  - Pieces overhead: 5MB
  - Line cache: 5MB

Rendered Lines: 30MB max
  - Line cache: 200 lines * 150KB = 30MB

UI State: 20MB
  - Compose nodes: 10MB
  - ViewModels: 5MB
  - Other: 5MB

File Cache: 30MB
  - Recently opened files
  - Project index
  - Search index

Other: 20MB
  - Syntax highlighter state
  - Undo history
  - Settings
```

**Memory Pressure Handling**:
```
On low memory warning:
  1. Reduce line cache to 100 lines
  2. Clear file cache (keep metadata)
  3. Trim undo history to 100 operations
  4. Save all dirty files
  5. Release syntax parser caches

On critical memory:
  1. Close non-active tabs
  2. Unload project index
  3. Keep only current file in memory
```

---

## 7. File Management Design

### 7.1 Workspace Structure

#### Directory Layout

```
Workspace Root (user-selected directory)
├── .mobile-ide/              (IDE metadata, hidden)
│   ├── settings.json         (Workspace-specific settings)
│   ├── session.json          (Open files, cursor positions)
│   ├── index/                (Search index)
│   │   └── files.db
│   └── cache/                (File cache)
│       └── ...
│
├── src/                      (Source files)
├── lib/                      (Libraries)
├── assets/                   (Assets)
└── ...                       (User files)
```

**Metadata Files**:
```
settings.json:
{
  "version": 1,
  "excludedPaths": [".git", "node_modules", "build"],
  "fileAssociations": {
    "*.kt": "kotlin",
    "*.java": "java"
  },
  "encoding": "UTF-8",
  "lineEnding": "lf"
}

session.json:
{
  "version": 1,
  "openFiles": [
    {
      "path": "src/main.kt",
      "cursor": {"line": 10, "column": 5},
      "selection": null
    }
  ],
  "activeFile": "src/main.kt",
  "sidebarState": {
    "expandedFolders": ["src"]
  }
}
```

### 7.2 Open Files

#### Document Model

```
Document:
  id: UUID
  filePath: String
  content: TextBuffer
  cursor: Cursor
  selection: Selection
  undoManager: UndoManager
  isDirty: Boolean
  lastModified: Long
  encoding: String
  language: Language
```

**Lifecycle**:
```
1. User opens file
2. Load content from disk
3. Create TextBuffer
4. Restore cursor/selection from session
5. Add to open documents list
6. Start auto-save timer

1. User closes file
2. Check if dirty
3. If dirty: prompt save/discard/cancel
4. Save session state
5. Remove from open documents
6. Release memory (keep in cache briefly)
```

### 7.3 Tabs

#### Design

```
Tab:
  id: String (file path or UUID)
  documentId: UUID
  title: String (file name)
  isDirty: Boolean
  isPinned: Boolean
  order: Int

TabManager:
  tabs: List<Tab> (maintains order)
  activeTabId: String?
  maxTabs: Int = 20

Operations:
  - open(filePath): Tab
  - close(tabId): Boolean
  - activate(tabId): Tab
  - reorder(fromIndex, toIndex): List<Tab>
  - pin(tabId): Tab
  - unpin(tabId): Tab
```

**Tab Behavior**:
- **Open**: Add to end, activate
- **Close**: Remove, activate previous (or next if no previous)
- **Reorder**: Drag and drop
- **Pin**: Prevent auto-close, show pin icon
- **Overflow**: Show "More" dropdown if tabs exceed screen width

**Auto-close Strategy**:
```
When opening new tab and at max:
  1. Close oldest unpinned tab
  2. If all pinned, prompt user
  3. Save state before closing
```

### 7.4 Recent Projects

#### Design

```
RecentProjects:
  maxEntries: Int = 10
  entries: List<RecentProject>

RecentProject:
  path: String
  name: String
  lastOpened: Long
  isFavorite: Boolean
```

**Management**:
- Add on project open
- Update timestamp on re-open
- Remove on project deletion
- Pin favorites to top
- Store in DataStore (encrypted)

### 7.5 Autosave Strategy

#### Design

**Trigger Conditions**:
```
1. Time-based: 30 seconds after last edit
2. Action-based: App background, low memory, tab close
3. Manual: User presses save button
```

**Implementation**:
```
AutoSaveEngine:
  dirtyDocuments: Set<Document>
  timer: Timer
  interval: Long = 30000ms

On edit:
  1. Mark document dirty
  2. Add to dirty set
  3. Reset timer

On timer expiry:
  1. For each dirty document:
     a. Write to disk
     b. Update saved hash
     c. Remove from dirty set
  2. Show brief "Saved" indicator

On app background:
  1. Force save all dirty documents
  2. Save session state
  3. Persist to disk
```

**Conflict Resolution**:
```
On save, if file changed externally:
  1. Detect hash mismatch
  2. Show conflict dialog
  3. Options:
     - Overwrite (use our version)
     - Reload (use disk version)
     - Merge (if possible)
     - Save As (new file)
```

---

## 8. Syntax Highlighting Plan

### 8.1 V1 Implementation: Regex-Based Highlighter

#### Rationale

**Why Regex for V1:**
1. **Fast to implement**: 1-2 weeks vs 1-2 months for Tree-sitter
2. **Sufficient for basic highlighting**: Keywords, strings, comments
3. **Low memory**: No parser state
4. **Easy to extend**: Add new languages with JSON files

**Limitations (Known and Accepted):**
- No nested structure awareness
- Limited context sensitivity
- Heuristic-based, not 100% accurate
- Slower for very large files

#### Architecture

```
RegexHighlighter:
  languages: Map<String, LanguagePattern>
  
LanguagePattern:
  name: String
  fileExtensions: List<String>
  patterns: List<Pattern>
  
Pattern:
  regex: Regex
  tokenType: TokenType
  priority: Int

TokenType:
  KEYWORD, STRING, NUMBER, COMMENT,
  OPERATOR, IDENTIFIER, TYPE, FUNCTION,
  VARIABLE, PUNCTUATION, TAG, ATTRIBUTE
```

**Language Definitions (JSON)**:
```json
{
  "name": "kotlin",
  "extensions": [".kt", "kts"],
  "patterns": [
    {"regex": "\\b(fun|val|var|class|interface|object)\\b", "token": "KEYWORD", "priority": 100},
    {"regex": "\".*?\"", "token": "STRING", "priority": 90},
    {"regex": "//.*$", "token": "COMMENT", "priority": 80},
    {"regex": "\\b\\d+\\b", "token": "NUMBER", "priority": 70}
  ]
}
```

**Processing Pipeline**:
```
1. Detect language from file extension
2. Load language patterns
3. For each line:
   a. Apply patterns in priority order
   b. Resolve overlaps (higher priority wins)
   c. Create tokens
4. Cache tokenized lines
5. Invalidate on edit
```

### 8.2 V2 Transition: Tree-sitter Integration

#### Timeline: V1.5 (3-4 months after V1)

**Why Tree-sitter:**
1. **Accurate**: Real parser, not regex
2. **Fast**: Incremental parsing
3. **Rich**: AST for folding, navigation, LSP
4. **Universal**: One parser, many languages

**Migration Strategy**:
```
Phase 1: Add Tree-sitter dependency
  - Add tree-sitter-kotlin binding
  - Test with small files

Phase 2: Dual-mode highlighting
  - Use Tree-sitter when available
  - Fallback to Regex when not
  - Compare performance

Phase 3: Full migration
  - Remove Regex for supported languages
  - Keep Regex for unsupported languages
  - Update language definitions
```

**Architecture**:
```
TreeSitterHighlighter:
  parser: Parser
  tree: Tree
  
  parse(text: String): Tree
  edit(edit: InputEdit): Tree
  highlight(tree: Tree): List<Token>
  
InputEdit:
  startByte: Int
  oldEndByte: Int
  newEndByte: Int
  startPosition: Position
  oldEndPosition: Position
  newEndPosition: Position
```

### 8.3 Theme System

#### Design

```
Theme:
  id: String
  name: String
  isDark: Boolean
  colors: EditorColors
  tokens: Map<TokenType, TokenStyle>

EditorColors:
  background: Color
  foreground: Color
  gutterBackground: Color
  gutterForeground: Color
  lineHighlight: Color
  selection: Color
  cursor: Color

TokenStyle:
  foreground: Color?
  background: Color?
  isBold: Boolean
  isItalic: Boolean
  isUnderline: Boolean
```

**Built-in Themes**:
1. **Light**: Default light theme
2. **Dark**: Default dark theme
3. **High Contrast**: Accessibility theme
4. **Custom**: User-defined

**Theme Loading**:
```
1. Built-in themes compiled into app
2. Custom themes loaded from files
3. Theme switching without restart
4. Per-language overrides possible
```

---

## 9. Large File Strategy

### 9.1 File Size Thresholds

#### Defined Limits

| Size | Lines | Strategy | Features Disabled |
|------|-------|----------|-------------------|
| Small | < 10k | Full features | None |
| Medium | 10k - 50k | Optimized | Minimap simplified |
| Large | 50k - 100k | Streaming | Syntax highlight delayed |
| Very Large | 100k - 500k | Chunked | No syntax highlight, no undo |
| Huge | > 500k | Read-only | Editing disabled |

### 9.2 Strategies by Level

#### Small Files (< 10k lines)

**Strategy**: Load entirely into memory

```
Operations:
  - Full Piece Table in memory
  - Complete syntax highlighting
  - Full undo history
  - Minimap enabled
  - All features active

Memory: ~50MB max
```

#### Medium Files (10k - 50k lines)

**Strategy**: Load with optimizations

```
Optimizations:
  - Simplified minimap (every 10th line)
  - Lazy syntax highlighting (visible lines first)
  - Reduced undo history (1,000 operations)
  - Aggressive line cache eviction

Memory: ~80MB max
```

#### Large Files (50k - 100k lines)

**Strategy**: Streaming with background processing

```
Optimizations:
  - Delayed syntax highlighting (background thread)
  - No minimap
  - Limited undo (100 operations)
  - Chunked loading (1,000 lines at a time)
  - Search uses file directly, not memory

Memory: ~100MB max
```

#### Very Large Files (100k - 500k lines)

**Strategy**: Read-only or limited editing

```
Optimizations:
  - No syntax highlighting
  - No undo/redo
  - Read-only by default
  - Allow edit with warning
  - Save creates new file (no in-place edit)

Memory: ~120MB max
```

#### Huge Files (> 500k lines)

**Strategy**: View only

```
Operations:
  - Read-only mode
  - Line-by-line streaming
  - Search with external tool
  - No editing capabilities
  - Suggest splitting file

Memory: ~50MB (streaming buffer)
```

### 9.3 Implementation Details

#### Chunked Loading

```
FileLoader:
  chunkSize: Int = 1000
  
loadFile(path: String):
  1. Open file stream
  2. Read first chunk (1,000 lines)
  3. Display immediately
  4. Continue reading in background
  5. Notify when complete

scrollTo(line: Int):
  1. Check if line loaded
  2. If not, load surrounding chunk
  3. Display loading indicator briefly
```

#### Memory-Mapped Files (Future)

```
For files > 100MB:
  - Use MemoryMappedFile API (Android 12+)
  - Map file to virtual memory
  - Access without loading to RAM
  - Limited by address space, not RAM
```

---

## 10. Performance Budget

### 10.1 Target Metrics

#### Startup Performance

| Metric | Target | Measurement |
|--------|--------|-------------|
| Cold start | < 2 seconds | App launch to interactive |
| Warm start | < 1 second | Resume from background |
| Project open | < 3 seconds | Select project to editable |
| File open | < 500ms | Click file to visible |

#### Runtime Performance

| Metric | Target | Measurement |
|--------|--------|-------------|
| Scroll FPS | 60 FPS | GPU profiling |
| Typing latency | < 16ms | Key press to character visible |
| Search response | < 100ms | Query to first results |
| File switch | < 200ms | Tab switch to interactive |
| Auto-save | < 50ms | Trigger to completion |

#### Memory Usage

| Scenario | Target | Measurement |
|----------|--------|-------------|
| Idle | < 80MB | After startup, no files open |
| Single file | < 120MB | One 10k line file open |
| Multiple files | < 150MB | 5 files open |
| Large project | < 200MB | 100k line file open |

#### Battery & Resources

| Metric | Target | Measurement |
|--------|--------|-------------|
| Background CPU | < 1% | When idle |
| Active CPU | < 10% | During typing |
| Battery drain | < 5%/hour | Continuous use |

### 10.2 Measurement Strategy

#### Profiling Tools

1. **Android Profiler**: Memory, CPU, Network
2. **Systrace**: UI thread performance
3. **Macrobenchmark**: Startup and scrolling
4. **Custom telemetry**: User-perceived metrics

#### Telemetry Events

```
Performance Events:
- editor_startup_time: Long
- file_open_time: Long (file_path, line_count)
- scroll_jank_frames: Int (session_duration)
- typing_latency: Long (average, max)
- memory_usage: Long (scenario)
- search_time: Long (query_length, result_count)
```

### 10.3 Optimization Checklist

#### Before Release

- [ ] Startup time < 2 seconds on mid-range device
- [ ] Scroll at 60 FPS on large files
- [ ] Memory usage < 150MB with 5 files open
- [ ] No ANR (Application Not Responding) events
- [ ] No OOM (Out Of Memory) crashes
- [ ] Battery usage acceptable (< 5%/hour)
- [ ] Search completes < 100ms for 10k line file

---

## 11. Technical Roadmap

### 11.1 Phase 1: Foundation (Weeks 1-6)

**Goal**: Core editing engine with basic UI

**Deliverables**:
- [ ] editor-core module (TextBuffer, Cursor, Selection, Undo)
- [ ] Basic editor-ui (Compose Canvas, single file editing)
- [ ] editor-files (File operations, single project)
- [ ] Simple syntax highlighting (5 languages)
- [ ] Basic search (find in file)

**Milestone**: Can open, edit, and save a single file

**Duration**: 6 weeks

### 11.2 Phase 2: Project Support (Weeks 7-12)

**Goal**: Multi-file project management

**Deliverables**:
- [ ] Project workspace support
- [ ] File explorer with tree view
- [ ] Multiple tabs
- [ ] Recent projects
- [ ] Project-wide search
- [ ] Settings persistence
- [ ] Dark/light themes
- [ ] Syntax highlighting (20 languages)

**Milestone**: Can manage a multi-file project

**Duration**: 6 weeks

### 11.3 Phase 3: Polish & Performance (Weeks 13-18)

**Goal**: Production-ready performance and UX

**Deliverables**:
- [ ] Performance optimization (60 FPS scrolling)
- [ ] Large file support (up to 100k lines)
- [ ] Advanced editing (multiple cursors, block selection)
- [ ] Code folding
- [ ] Minimap
- [ ] Bracket matching
- [ ] Auto-indentation
- [ ] Comprehensive testing

**Milestone**: Beta release quality

**Duration**: 6 weeks

### 11.4 Phase 4: Release Preparation (Weeks 19-22)

**Goal**: Play Store release

**Deliverables**:
- [ ] Beta testing with users
- [ ] Bug fixes and polish
- [ ] Documentation
- [ ] Play Store assets
- [ ] Analytics integration
- [ ] Crash reporting
- [ ] Privacy policy
- [ ] App signing

**Milestone**: V1.0 on Play Store

**Duration**: 4 weeks

### 11.5 Post-V1 Roadmap

#### V1.5 (Months 6-8)
- Git integration (basic)
- Tree-sitter syntax highlighting
- LSP preparation

#### V2.0 (Months 9-12)
- LSP support (language servers)
- Terminal emulator
- Plugin system (basic)
- Cloud sync

#### V2.5 (Months 13-18)
- Advanced Git (diff, blame, history)
- Plugin marketplace
- Collaborative editing (research)

#### V3.0 (Year 2+)
- iOS port (if viable)
- AI-assisted features
- Advanced debugging
- Enterprise features

---

## 12. Development Workflow

### 12.1 Branch Strategy

#### Git Flow Simplified

```
main (production)
  │
  ├── release/v1.0 (release preparation)
  │     └── bug fixes only
  │
  ├── develop (integration)
  │     ├── feature/editor-core
  │     ├── feature/file-explorer
  │     ├── feature/syntax-highlight
  │     └── ...
  │
  └── hotfix/* (urgent fixes to main)
```

**Branch Rules**:
- `main`: Production code, tagged releases only
- `develop`: Integration branch, feature branches merge here
- `feature/*`: Individual features, delete after merge
- `release/*`: Release preparation, no new features
- `hotfix/*`: Emergency fixes, merge to both main and develop

### 12.2 CI/CD Structure

#### GitHub Actions Workflows

**1. CI Workflow** (`ci.yml`)
```yaml
Triggers: push to develop, PR to develop/main
Jobs:
  - lint: ktlint, detekt
  - unit-tests: Run unit tests
  - integration-tests: Run integration tests
  - build: Build debug APK
  - coverage: Generate coverage report
```

**2. Release Workflow** (`release.yml`)
```yaml
Triggers: push to release/*, manual
Jobs:
  - build-release: Build release APK
  - sign: Sign with release key
  - upload: Upload to Play Store Internal Testing
```

**3. Nightly Workflow** (`nightly.yml`)
```yaml
Triggers: schedule (daily at 2 AM)
Jobs:
  - build-nightly: Build debug APK
  - upload: Upload to Firebase App Distribution
```

### 12.3 Testing Strategy

#### Test Pyramid

```
                    /\
                   /  \
                  / E2E \     10% (UI tests)
                 /--------\
                /  Integration \  20% (Feature tests)
               /----------------\
              /    Unit Tests     \  70% (Logic tests)
             /----------------------\
```

**Unit Tests**:
- TextBuffer operations
- Cursor movement
- Undo/redo
- Search algorithms
- File operations

**Integration Tests**:
- Open and edit file
- Project operations
- Search across files
- Settings persistence

**E2E Tests**:
- Complete user flows
- Performance benchmarks
- Memory leak detection

#### Test Coverage Targets

| Module | Target Coverage |
|--------|----------------|
| editor-core | 90% |
| editor-ui | 70% |
| editor-files | 80% |
| editor-search | 85% |
| editor-highlight | 75% |

### 12.4 Release Strategy

#### Versioning

**Semantic Versioning**: `MAJOR.MINOR.PATCH`

```
MAJOR: Breaking changes, major features
MINOR: New features, backwards compatible
PATCH: Bug fixes, performance improvements

Examples:
  1.0.0 - Initial release
  1.1.0 - New feature (Git support)
  1.1.1 - Bug fix
  2.0.0 - Breaking change (new API)
```

#### Release Channels

1. **Internal Testing**: Team only, daily builds
2. **Closed Testing**: Selected users, weekly builds
3. **Open Testing**: Public beta, bi-weekly builds
4. **Production**: Play Store, monthly releases

#### Release Checklist

- [ ] All tests passing
- [ ] Performance benchmarks met
- [ ] No critical bugs
- [ ] Documentation updated
- [ ] Changelog prepared
- [ ] Play Store assets ready
- [ ] Privacy policy current
- [ ] App signed with release key

### 12.5 Versioning Strategy

#### Code Versioning

```
Version Code: Integer, increments with each release
Version Name: Semantic version (1.0.0)

Build Types:
  debug: versionCode + 0
  beta: versionCode + 1000
  release: versionCode

Example:
  Release 1.0.0: versionCode=100, versionName="1.0.0"
  Beta 1.1.0: versionCode=1100, versionName="1.1.0-beta"
```

#### API Versioning

```
Internal API versions for modules:
  editor-core: v1
  editor-files: v1
  
Future modules declare their API version:
  editor-lsp: v1 (when released)
```

---

## 13. Risks & Mitigations

### 13.1 Technical Risks

#### Risk 1: Performance Not Meeting Targets

**Impact**: High | **Probability**: Medium

**Description**: Editor not achieving 60 FPS on mid-range devices

**Mitigation**:
- Early performance testing (Week 3)
- Profile on low-end device (2GB RAM)
- Implement virtual scrolling from start
- Use LazyColumn for line rendering
- Cache aggressively
- Have fallback to simpler rendering

**Contingency**:
- If 60 FPS not achievable: Target 30 FPS with smooth animations
- If memory too high: Reduce features (disable minimap, reduce undo)

#### Risk 2: Large File Handling Failure

**Impact**: High | **Probability**: Medium

**Description**: App crashes or becomes unresponsive with files > 50k lines

**Mitigation**:
- Implement chunked loading from start
- Test with progressively larger files
- Set realistic limits and communicate to user
- Implement read-only mode for huge files

**Contingency**:
- If Piece Table too slow: Use gap buffer for large files
- If memory insufficient: Stream from disk

#### Risk 3: Tree-sitter Integration Complexity

**Impact**: Medium | **Probability**: Medium

**Description**: Tree-sitter bindings don't work well on Android

**Mitigation**:
- Research bindings early (Week 2)
- Have Regex fallback ready
- Test with simple language first (JSON)
- Consider alternative parsers (ANTLR)

**Contingency**:
- If Tree-sitter fails: Use ANTLR or custom parsers
- If too slow: Keep Regex for most languages

### 13.2 Performance Risks

#### Risk 4: Memory Leaks

**Impact**: High | **Probability**: Medium

**Description**: Memory usage grows over time, leading to OOM crashes

**Mitigation**:
- Use LeakCanary in debug builds
- Profile memory usage regularly
- Implement cache size limits
- Clear caches on low memory
- Use WeakReferences where appropriate

**Contingency**:
- If leaks found: Fix immediately, don't accumulate

#### Risk 5: Battery Drain

**Impact**: Medium | **Probability**: Low

**Description**: App drains battery quickly during use

**Mitigation**:
- Minimize background work
- Use WorkManager for periodic tasks
- Reduce polling frequency
- Optimize animations

**Contingency**:
- If drain high: Add battery saver mode (reduce features)

### 13.3 Scalability Risks

#### Risk 6: Architecture Doesn't Scale

**Impact**: High | **Probability**: Low

**Description**: Current architecture can't support future features (LSP, Git, etc.)

**Mitigation**:
- Design extension points from start
- Use dependency injection (Hilt)
- Define clear module boundaries
- Document architecture decisions
- Regular architecture reviews

**Contingency**:
- If architecture breaks: Refactor early, don't patch

#### Risk 7: Plugin System Too Complex

**Impact**: Medium | **Probability**: Medium

**Description**: Plugin API becomes bottleneck for community contributions

**Mitigation**:
- Design simple API from start
- Use existing standards (LSP for language support)
- Provide examples and templates
- Start with built-in features, extract to plugins later

**Contingency**:
- If too complex: Delay plugin system, focus on built-in features

### 13.4 Maintenance Risks

#### Risk 8: AI Agent Can't Maintain Code

**Impact**: High | **Probability**: Low

**Description**: Code becomes too complex for AI to modify safely

**Mitigation**:
- Write clear, self-documenting code
- Follow Kotlin conventions strictly
- Keep functions small and focused
- Use explicit types over inference
- Document all public APIs
- Avoid clever tricks, prefer clarity

**Contingency**:
- If AI struggles: Simplify architecture, reduce abstraction

#### Risk 9: Dependency Updates Break Build

**Impact**: Medium | **Probability**: Medium

**Description**: Compose or Kotlin updates cause breaking changes

**Mitigation**:
- Pin dependency versions
- Update dependencies regularly (monthly)
- Test updates in separate branch
- Have rollback plan
- Minimize external dependencies

**Contingency**:
- If update breaks: Stay on working version until fix

### 13.5 Risk Summary Matrix

| Risk | Impact | Probability | Priority | Mitigation Status |
|------|--------|-------------|----------|-------------------|
| Performance | High | Medium | 1 | Early testing |
| Large files | High | Medium | 2 | Chunked loading |
| Tree-sitter | Medium | Medium | 3 | Fallback ready |
| Memory leaks | High | Medium | 4 | LeakCanary |
| Architecture | High | Low | 5 | Extension points |
| AI maintenance | High | Low | 6 | Clear code |
| Dependencies | Medium | Medium | 7 | Version pinning |
| Battery | Medium | Low | 8 | Background limits |
| Plugins | Medium | Medium | 9 | Simple API |

---

## 14. Final Recommendations

### 14.1 Before Writing First Line of Code

1. **Set up Repository**
   - Create GitHub repository
   - Set up branch protection rules
   - Configure GitHub Actions
   - Add CODEOWNERS file

2. **Define Coding Standards**
   - Kotlin style guide (Android Kotlin Style Guide)
   - Commit message conventions (Conventional Commits)
   - PR template and review checklist
   - Documentation requirements

3. **Set up Development Environment**
   - Android Studio version and plugins
   - JDK version (17 recommended)
   - Gradle configuration
   - Emulator/device requirements

4. **Create Project Structure**
   - Module structure as defined in Section 4
   - Gradle configuration with version catalog
   - Dependency injection setup (Hilt)
   - Basic navigation structure

5. **Implement Core First**
   - Start with editor-core module
   - Write comprehensive unit tests
   - Benchmark performance early
   - Don't build UI until core is solid

### 14.2 Architecture Principles to Follow

1. **Immutability First**: All data classes immutable, state changes explicit
2. **Testability**: Every module testable in isolation, no Android dependencies in domain
3. **Lazy Evaluation**: Don't compute until needed, cache results
4. **Fail Fast**: Validate inputs, throw meaningful exceptions
5. **Logging**: Log all state changes, errors, and performance metrics
6. **Documentation**: Document every public API, architecture decisions, and complex logic

### 14.3 Anti-Patterns to Avoid

1. **Don't**: Use global state or singletons for mutable data
2. **Don't**: Block main thread with I/O operations
3. **Don't**: Load entire file into memory without limits
4. **Don't**: Use reflection or dynamic code execution
5. **Don't**: Depend on specific Android versions without fallbacks
6. **Don't**: Ignore memory warnings or low memory conditions
7. **Don't**: Use deprecated APIs without migration plan
8. **Don't**: Build features without tests

### 14.4 Success Criteria

**V1.0 Success**:
- [ ] Can edit files up to 50k lines smoothly
- [ ] Memory usage under 150MB
- [ ] 60 FPS scrolling on mid-range device
- [ ] No crashes in beta testing (100+ users, 2 weeks)
- [ ] Positive user feedback on core editing experience
- [ ] Stable enough for daily use

**Long-term Success**:
- [ ] 10,000+ active users
- [ ] Community plugin ecosystem
- [ ] Positive reviews (4.5+ stars)
- [ ] Sustainable development (revenue or funding)
- [ ] Recognition as best mobile code editor

### 14.5 Final Checklist

Before starting development:

- [ ] This document reviewed and approved
- [ ] Repository created and configured
- [ ] Development environment set up
- [ ] Team (or solo developer) understands architecture
- [ ] First milestone defined (Week 1-2 tasks)
- [ ] Testing strategy agreed upon
- [ ] Performance targets documented
- [ ] Risk mitigations planned
- [ ] Backup and recovery plan in place
- [ ] Legal review (license, privacy policy)

---

## Appendices

### A. Glossary

| Term | Definition |
|------|------------|
| Piece Table | Data structure for efficient text editing |
| LSP | Language Server Protocol |
| AST | Abstract Syntax Tree |
| LRU | Least Recently Used (cache eviction) |
| ANR | Application Not Responding |
| OOM | Out Of Memory |
| FPS | Frames Per Second |
| MVP | Minimum Viable Product |
| CI/CD | Continuous Integration/Deployment |
| DSL | Domain Specific Language |

### B. References

1. **VS Code Architecture**: https://code.visualstudio.com/docs/editor/editingevolved
2. **Piece Table Paper**: "The Piece Table" by Peter Reiser
3. **Tree-sitter Documentation**: https://tree-sitter.github.io/tree-sitter/
4. **Jetpack Compose Performance**: https://developer.android.com/jetpack/compose/performance
5. **Android Memory Management**: https://developer.android.com/topic/performance/memory
6. **LSP Specification**: https://microsoft.github.io/language-server-protocol/
7. **Kotlin Coding Conventions**: https://kotlinlang.org/docs/coding-conventions.html

### C. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2026-06-03 | AI Architect | Initial document |

---

**End of Document**

*This document is the authoritative reference for the Mobile IDE project architecture and development plan. All development decisions should align with the principles and specifications outlined herein.*
