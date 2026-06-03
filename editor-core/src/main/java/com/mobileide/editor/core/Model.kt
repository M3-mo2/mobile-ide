package com.mobileide.editor.core

/**
 * Represents a position in the text buffer.
 * Line and column are 0-based.
 * Column is measured in UTF-16 code units.
 */
data class Position(val line: Int, val column: Int) : Comparable<Position> {
    init {
        require(line >= 0) { "Line must be non-negative" }
        require(column >= 0) { "Column must be non-negative" }
    }

    override fun compareTo(other: Position): Int {
        return if (line != other.line) {
            line.compareTo(other.line)
        } else {
            column.compareTo(other.column)
        }
    }

    /**
     * Returns true if this position is before the other position.
     */
    fun isBefore(other: Position): Boolean = this < other

    /**
     * Returns true if this position is after the other position.
     */
    fun isAfter(other: Position): Boolean = this > other

    /**
     * Creates a copy with a new line number.
     */
    fun withLine(newLine: Int): Position = copy(line = newLine)

    /**
     * Creates a copy with a new column number.
     */
    fun withColumn(newColumn: Int): Position = copy(column = newColumn)
}

/**
 * Represents a range of text from [start] to [end] (exclusive).
 * [start] is always <= [end].
 */
data class Range(val start: Position, val end: Position) {
    init {
        require(start <= end) { "Start position must be before or equal to end position" }
    }

    /**
     * Returns true if this range contains the given position.
     */
    fun contains(position: Position): Boolean {
        return position >= start && position < end
    }

    /**
     * Returns true if this range overlaps with the other range.
     */
    fun overlaps(other: Range): Boolean {
        return start < other.end && end > other.start
    }

    /**
     * Returns true if this range is empty (start == end).
     */
    fun isEmpty(): Boolean = start == end

    /**
     * Returns the length of this range in characters (approximate, for same-line ranges).
     * For multi-line ranges, this is not accurate.
     */
    fun length(): Int = if (start.line == end.line) {
        end.column - start.column
    } else {
        // Approximate for multi-line ranges
        -1
    }
}

/**
 * Represents a direction for cursor movement.
 */
enum class Direction {
    LEFT, RIGHT, UP, DOWN,
    WORD_LEFT, WORD_RIGHT,
    LINE_START, LINE_END,
    FILE_START, FILE_END,
    PAGE_UP, PAGE_DOWN
}

/**
 * Represents a line ending style.
 */
enum class LineEnding {
    LF, CRLF;

    companion object {
        fun detect(text: String): LineEnding {
            return if (text.contains("\r\n")) CRLF else LF
        }
    }

    fun string(): String = when (this) {
        LF -> "\n"
        CRLF -> "\r\n"
    }
}

/**
 * Represents a language for syntax highlighting.
 */
data class Language(
    val id: String,
    val name: String,
    val fileExtensions: List<String>
)
