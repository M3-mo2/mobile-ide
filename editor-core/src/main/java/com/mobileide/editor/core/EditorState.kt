package com.mobileide.editor.core

/**
 * Represents the state of a document.
 * This is an immutable data class that holds all document-related state.
 */
data class DocumentState(
    val filePath: String? = null,
    val content: TextBuffer = TextBufferFactory.createEmpty(),
    val isDirty: Boolean = false,
    val isReadOnly: Boolean = false,
    val language: Language? = null,
    val encoding: String = "UTF-8",
    val lineEnding: LineEnding = LineEnding.LF
) {
    /**
     * Creates a copy with new content and marks as dirty.
     */
    fun withContent(newContent: TextBuffer): DocumentState {
        return copy(
            content = newContent,
            isDirty = true
        )
    }

    /**
     * Creates a copy marked as saved (not dirty).
     */
    fun saved(): DocumentState {
        return copy(isDirty = false)
    }

    /**
     * Creates a copy with read-only flag.
     */
    fun readOnly(): DocumentState {
        return copy(isReadOnly = true)
    }

    /**
     * Creates a copy with a new file path.
     */
    fun withFilePath(path: String): DocumentState {
        return copy(filePath = path)
    }

    /**
     * Creates a copy with a new language.
     */
    fun withLanguage(newLanguage: Language): DocumentState {
        return copy(language = newLanguage)
    }
}

/**
 * Represents the state of the cursor system.
 */
data class CursorState(
    val primary: Cursor = CursorImpl(Position(0, 0)),
    val secondary: List<Cursor> = emptyList(),
    val isBlinking: Boolean = true
) {
    /**
     * All cursors including primary.
     */
    val allCursors: List<Cursor>
        get() = listOf(primary) + secondary
    /**
     * Creates a copy with a new primary cursor.
     */
    fun withPrimary(newPrimary: Cursor): CursorState {
        return copy(primary = newPrimary)
    }

    /**
     * Creates a copy with secondary cursors.
     */
    fun withSecondary(newSecondary: List<Cursor>): CursorState {
        return copy(secondary = newSecondary)
    }

    /**
     * Returns a CursorManager from this state.
     */
    fun toCursorManager(): CursorManager {
        return CursorManagerImpl(primary, secondary)
    }

    /**
     * Creates a CursorState from a CursorManager.
     */
    companion object {
        fun from(manager: CursorManager): CursorState {
            return CursorState(
                primary = manager.primaryCursor,
                secondary = manager.secondaryCursors
            )
        }
    }
}

/**
 * Represents the state of the selection system.
 */
data class SelectionState(
    val primary: Selection = SelectionImpl(Position(0, 0), Position(0, 0)),
    val secondary: List<Selection> = emptyList()
) {
    /**
     * Creates a copy with a new primary selection.
     */
    fun withPrimary(newPrimary: Selection): SelectionState {
        return copy(primary = newPrimary)
    }

    /**
     * Creates a copy with secondary selections.
     */
    fun withSecondary(newSecondary: List<Selection>): SelectionState {
        return copy(secondary = newSecondary)
    }

    /**
     * Returns true if any selection is non-empty.
     */
    fun hasSelection(): Boolean {
        return !primary.isEmpty || secondary.any { !it.isEmpty }
    }

    /**
     * Returns a SelectionManager from this state.
     */
    fun toSelectionManager(): SelectionManager {
        return SelectionManagerImpl(primary, secondary)
    }

    /**
     * Creates a SelectionState from a SelectionManager.
     */
    companion object {
        fun from(manager: SelectionManager): SelectionState {
            return SelectionState(
                primary = manager.primarySelection,
                secondary = manager.secondarySelections
            )
        }
    }
}

/**
 * Represents the viewport state for rendering.
 */
data class ViewportState(
    val topLine: Int = 0,
    val leftColumn: Int = 0,
    val width: Float = 0f,
    val height: Float = 0f,
    val lineHeight: Float = 20f
) {
    /**
     * Returns the number of visible lines based on height.
     */
    fun visibleLines(): Int {
        return if (lineHeight > 0) {
            (height / lineHeight).toInt()
        } else {
            0
        }
    }

    /**
     * Returns the bottom line number (exclusive).
     */
    fun bottomLine(): Int {
        return topLine + visibleLines()
    }

    /**
     * Creates a copy scrolled to a new top line.
     */
    fun scrollTo(newTopLine: Int): ViewportState {
        return copy(topLine = newTopLine.coerceAtLeast(0))
    }

    /**
     * Creates a copy scrolled horizontally to a new left column.
     */
    fun scrollHorizontal(newLeftColumn: Int): ViewportState {
        return copy(leftColumn = newLeftColumn.coerceAtLeast(0))
    }
}

/**
 * Represents search state.
 */
data class SearchState(
    val query: String = "",
    val isRegex: Boolean = false,
    val isCaseSensitive: Boolean = false,
    val isWholeWord: Boolean = false,
    val currentMatch: Int = 0,
    val totalMatches: Int = 0
) {
    /**
     * Returns true if there is an active search query.
     */
    fun hasQuery(): Boolean = query.isNotEmpty()

    /**
     * Creates a copy with a new query.
     */
    fun withQuery(newQuery: String): SearchState {
        return copy(query = newQuery, currentMatch = 0, totalMatches = 0)
    }

    /**
     * Creates a copy with match counts.
     */
    fun withMatches(current: Int, total: Int): SearchState {
        return copy(currentMatch = current, totalMatches = total)
    }
}

/**
 * Represents editor settings.
 */
data class EditorSettings(
    val fontSize: Float = 14f,
    val fontFamily: String = "monospace",
    val tabSize: Int = 4,
    val useSpaces: Boolean = true,
    val wordWrap: Boolean = false,
    val showLineNumbers: Boolean = true,
    val showMinimap: Boolean = true,
    val theme: String = "dark"
) {
    /**
     * Returns the indentation string (tabs or spaces).
     */
    fun indentation(): String {
        return if (useSpaces) {
            " ".repeat(tabSize)
        } else {
            "\t"
        }
    }
}

/**
 * The main editor state that combines all sub-states.
 * This is the single source of truth for the editor.
 */
data class EditorState(
    val document: DocumentState = DocumentState(),
    val cursor: CursorState = CursorState(),
    val selection: SelectionState = SelectionState(),
    val viewport: ViewportState = ViewportState(),
    val search: SearchState = SearchState(),
    val settings: EditorSettings = EditorSettings(),
    val undoManager: UndoManager = UndoManagerImpl()
) {
    /**
     * Creates a copy with a new document state.
     */
    fun withDocument(newDocument: DocumentState): EditorState {
        return copy(document = newDocument)
    }

    /**
     * Creates a copy with a new cursor state.
     */
    fun withCursor(newCursor: CursorState): EditorState {
        return copy(cursor = newCursor)
    }

    /**
     * Creates a copy with a new selection state.
     */
    fun withSelection(newSelection: SelectionState): EditorState {
        return copy(selection = newSelection)
    }

    /**
     * Creates a copy with a new viewport state.
     */
    fun withViewport(newViewport: ViewportState): EditorState {
        return copy(viewport = newViewport)
    }

    /**
     * Creates a copy with a new search state.
     */
    fun withSearch(newSearch: SearchState): EditorState {
        return copy(search = newSearch)
    }

    /**
     * Creates a copy with new settings.
     */
    fun withSettings(newSettings: EditorSettings): EditorState {
        return copy(settings = newSettings)
    }

    /**
     * Creates a copy with a new undo manager.
     */
    fun withUndoManager(newUndoManager: UndoManager): EditorState {
        return copy(undoManager = newUndoManager)
    }

    /**
     * Returns the line lengths for cursor movement calculations.
     */
    fun lineLengths(): List<Int> {
        val lineCount = document.content.getLineCount()
        return (0 until lineCount).map { line ->
            document.content.getLine(line).length
        }
    }
}
