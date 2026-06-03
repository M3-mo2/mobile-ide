package com.mobileide.editor.core

/**
 * Interface for the text buffer data structure.
 * All operations are immutable - they return a new TextBuffer instance.
 * This enables efficient undo/redo and prevents side effects.
 */
interface TextBuffer {
    /**
     * Inserts text at the given offset.
     * Returns a new TextBuffer with the insertion applied.
     */
    fun insert(offset: Int, text: String): TextBuffer

    /**
     * Deletes text starting at [offset] for [length] characters.
     * Returns a new TextBuffer with the deletion applied.
     */
    fun delete(offset: Int, length: Int): TextBuffer

    /**
     * Replaces text starting at [offset] for [length] with [text].
     * Returns a new TextBuffer with the replacement applied.
     */
    fun replace(offset: Int, length: Int, text: String): TextBuffer

    /**
     * Returns the full text content.
     */
    fun getText(): String

    /**
     * Returns the text at the given line number (0-based).
     */
    fun getLine(lineNumber: Int): String

    /**
     * Returns the total number of lines.
     */
    fun getLineCount(): Int

    /**
     * Returns the length of the text in characters.
     */
    fun length(): Int

    /**
     * Returns the character at the given offset.
     */
    fun charAt(offset: Int): Char

    /**
     * Converts a line/column position to a character offset.
     */
    fun positionToOffset(position: Position): Int

    /**
     * Converts a character offset to a line/column position.
     */
    fun offsetToPosition(offset: Int): Position

    /**
     * Returns the line ending style of the document.
     */
    fun getLineEnding(): LineEnding

    /**
     * Returns a substring from [startOffset] to [endOffset] (exclusive).
     */
    fun substring(startOffset: Int, endOffset: Int): String
}

/**
 * Factory for creating TextBuffer instances.
 */
object TextBufferFactory {
    /**
     * Creates a new empty TextBuffer.
     */
    fun createEmpty(): TextBuffer = PieceTableTextBuffer("")

    /**
     * Creates a TextBuffer from the given text.
     */
    fun fromText(text: String): TextBuffer = PieceTableTextBuffer(text)
}
