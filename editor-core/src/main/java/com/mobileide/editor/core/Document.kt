package com.mobileide.editor.core

/**
 * Represents a document with its full lifecycle.
 * Manages content, cursor, selection, and undo state.
 */
class Document(
    val filePath: String? = null,
    val content: TextBuffer = TextBufferFactory.createEmpty(),
    val cursor: Cursor = CursorImpl(Position(0, 0)),
    val selection: Selection = SelectionImpl(Position(0, 0), Position(0, 0)),
    val undoManager: UndoManager = UndoManagerImpl(),
    val encoding: String = "UTF-8",
    val language: Language? = null
) {
    /**
     * Returns true if the document has unsaved changes.
     */
    val isDirty: Boolean
        get() = contentHash != savedHash

    private var contentHash: String = computeHash()
    private var savedHash: String = contentHash

    /**
     * Creates a new document with updated content.
     */
    fun withContent(newContent: TextBuffer): Document {
        return Document(
            filePath = filePath,
            content = newContent,
            cursor = cursor,
            selection = selection,
            undoManager = undoManager,
            encoding = encoding,
            language = language
        ).apply {
            savedHash = this@Document.savedHash
        }
    }

    /**
     * Creates a new document with updated cursor.
     */
    fun withCursor(newCursor: Cursor): Document {
        return Document(
            filePath = filePath,
            content = content,
            cursor = newCursor,
            selection = selection,
            undoManager = undoManager,
            encoding = encoding,
            language = language
        ).apply {
            savedHash = this@Document.savedHash
        }
    }

    /**
     * Creates a new document with updated selection.
     */
    fun withSelection(newSelection: Selection): Document {
        return Document(
            filePath = filePath,
            content = content,
            cursor = cursor,
            selection = newSelection,
            undoManager = undoManager,
            encoding = encoding,
            language = language
        ).apply {
            savedHash = this@Document.savedHash
        }
    }

    /**
     * Creates a new document with updated undo manager.
     */
    fun withUndoManager(newUndoManager: UndoManager): Document {
        return Document(
            filePath = filePath,
            content = content,
            cursor = cursor,
            selection = selection,
            undoManager = newUndoManager,
            encoding = encoding,
            language = language
        ).apply {
            savedHash = this@Document.savedHash
        }
    }

    /**
     * Marks the document as saved.
     */
    fun markSaved(): Document {
        return Document(
            filePath = filePath,
            content = content,
            cursor = cursor,
            selection = selection,
            undoManager = undoManager,
            encoding = encoding,
            language = language
        ).apply {
            savedHash = contentHash
        }
    }

    private fun computeHash(): String {
        // Simple hash based on content length and first/last characters
        val text = content.getText()
        return if (text.isEmpty()) {
            "empty"
        } else {
            "${text.length}_${text.first()}_${text.last()}"
        }
    }
}
