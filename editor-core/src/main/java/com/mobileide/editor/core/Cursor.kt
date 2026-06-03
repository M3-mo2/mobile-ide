package com.mobileide.editor.core

/**
 * Interface for cursor management.
 * The cursor system supports both single and multiple cursors.
 * All operations are immutable - they return new Cursor/CursorManager instances.
 */
interface Cursor {
    /**
     * The current position of this cursor.
     */
    val position: Position

    /**
     * The preferred column for vertical movement.
     * When moving up/down, the cursor tries to maintain this column.
     */
    val preferredColumn: Int

    /**
     * Moves the cursor in the given direction.
     * Returns a new Cursor at the new position.
     */
    fun move(direction: Direction, lineLengths: List<Int>): Cursor

    /**
     * Moves the cursor to the given position.
     * Returns a new Cursor at that position.
     */
    fun moveTo(newPosition: Position): Cursor

    /**
     * Creates a copy of this cursor with a new position.
     */
    fun withPosition(newPosition: Position): Cursor

    /**
     * Creates a copy of this cursor with a new preferred column.
     */
    fun withPreferredColumn(newColumn: Int): Cursor
}

/**
 * Manages multiple cursors.
 * Supports up to 10 cursors.
 */
interface CursorManager {
    /**
     * The primary (focused) cursor.
     */
    val primaryCursor: Cursor

    /**
     * Secondary cursors (up to 9 additional).
     */
    val secondaryCursors: List<Cursor>

    /**
     * All cursors including primary.
     */
    val allCursors: List<Cursor>
        get() = listOf(primaryCursor) + secondaryCursors

    /**
     * Adds a cursor at the given position.
     * Returns a new CursorManager with the added cursor.
     * If max cursors reached, returns this unchanged.
     */
    fun addCursor(position: Position): CursorManager

    /**
     * Removes a cursor at the given index (0 = primary).
     * Returns a new CursorManager without that cursor.
     */
    fun removeCursor(index: Int): CursorManager

    /**
     * Merges overlapping cursors.
     * Returns a new CursorManager with merged cursors.
     */
    fun mergeOverlapping(): CursorManager

    /**
     * Applies an operation to all cursors.
     * Returns a new CursorManager with updated cursors.
     */
    fun operateAll(operation: (Cursor) -> Cursor): CursorManager

    /**
     * Clears all secondary cursors, keeping only primary.
     */
    fun clearSecondary(): CursorManager
}

/**
 * Default implementation of Cursor.
 */
data class CursorImpl(
    override val position: Position,
    override val preferredColumn: Int = position.column
) : Cursor {

    override fun move(direction: Direction, lineLengths: List<Int>): Cursor {
        val maxLine = lineLengths.size - 1

        val newPosition = when (direction) {
            Direction.LEFT -> {
                if (position.column > 0) {
                    position.withColumn(position.column - 1)
                } else if (position.line > 0) {
                    val prevLineLength = lineLengths[position.line - 1]
                    Position(position.line - 1, prevLineLength)
                } else {
                    position
                }
            }
            Direction.RIGHT -> {
                val currentLineLength = lineLengths.getOrElse(position.line) { 0 }
                if (position.column < currentLineLength) {
                    position.withColumn(position.column + 1)
                } else if (position.line < maxLine) {
                    Position(position.line + 1, 0)
                } else {
                    position
                }
            }
            Direction.UP -> {
                if (position.line > 0) {
                    val targetLine = position.line - 1
                    val targetLineLength = lineLengths[targetLine]
                    val targetColumn = preferredColumn.coerceAtMost(targetLineLength)
                    Position(targetLine, targetColumn)
                } else {
                    position
                }
            }
            Direction.DOWN -> {
                if (position.line < maxLine) {
                    val targetLine = position.line + 1
                    val targetLineLength = lineLengths[targetLine]
                    val targetColumn = preferredColumn.coerceAtMost(targetLineLength)
                    Position(targetLine, targetColumn)
                } else {
                    position
                }
            }
            Direction.WORD_LEFT -> {
                // Simple implementation: move to start of word or previous word
                val lineText = "" // Would need actual text
                position // Placeholder
            }
            Direction.WORD_RIGHT -> {
                // Simple implementation: move to end of word or next word
                position // Placeholder
            }
            Direction.LINE_START -> {
                position.withColumn(0)
            }
            Direction.LINE_END -> {
                val lineLength = lineLengths.getOrElse(position.line) { 0 }
                position.withColumn(lineLength)
            }
            Direction.FILE_START -> {
                Position(0, 0)
            }
            Direction.FILE_END -> {
                Position(maxLine, lineLengths.getOrElse(maxLine) { 0 })
            }
            Direction.PAGE_UP -> {
                val targetLine = (position.line - 20).coerceAtLeast(0)
                val targetLineLength = lineLengths[targetLine]
                val targetColumn = preferredColumn.coerceAtMost(targetLineLength)
                Position(targetLine, targetColumn)
            }
            Direction.PAGE_DOWN -> {
                val targetLine = (position.line + 20).coerceAtMost(maxLine)
                val targetLineLength = lineLengths[targetLine]
                val targetColumn = preferredColumn.coerceAtMost(targetLineLength)
                Position(targetLine, targetColumn)
            }
        }

        val newPreferredColumn = if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            newPosition.column
        } else {
            preferredColumn
        }

        return CursorImpl(newPosition, newPreferredColumn)
    }

    override fun moveTo(newPosition: Position): Cursor {
        return CursorImpl(newPosition, newPosition.column)
    }

    override fun withPosition(newPosition: Position): Cursor {
        return copy(position = newPosition)
    }

    override fun withPreferredColumn(newColumn: Int): Cursor {
        return copy(preferredColumn = newColumn)
    }
}

/**
 * Default implementation of CursorManager.
 */
data class CursorManagerImpl(
    override val primaryCursor: Cursor,
    override val secondaryCursors: List<Cursor> = emptyList()
) : CursorManager {

    companion object {
        const val MAX_CURSORS = 10
    }

    init {
        require(secondaryCursors.size < MAX_CURSORS) {
            "Cannot have more than ${MAX_CURSORS - 1} secondary cursors"
        }
    }

    override fun addCursor(position: Position): CursorManager {
        if (allCursors.size >= MAX_CURSORS) return this

        val newCursor = CursorImpl(position)
        return copy(secondaryCursors = secondaryCursors + newCursor)
    }

    override fun removeCursor(index: Int): CursorManager {
        return when {
            index < 0 || index >= allCursors.size -> this
            index == 0 -> {
                // Remove primary, promote first secondary
                val newSecondary = secondaryCursors.drop(1)
                if (newSecondary.isNotEmpty()) {
                    copy(
                        primaryCursor = newSecondary.first(),
                        secondaryCursors = newSecondary.drop(1)
                    )
                } else {
                    // No cursors left, keep primary at current position
                    this
                }
            }
            else -> {
                val adjustedIndex = index - 1 // Account for primary
                copy(secondaryCursors = secondaryCursors.filterIndexed { i, _ -> i != adjustedIndex })
            }
        }
    }

    override fun mergeOverlapping(): CursorManager {
        val all = allCursors
        val merged = mutableListOf<Cursor>()

        for (cursor in all) {
            val overlapping = merged.any { it.position == cursor.position }
            if (!overlapping) {
                merged.add(cursor)
            }
        }

        return if (merged.isEmpty()) {
            this
        } else {
            CursorManagerImpl(
                primaryCursor = merged.first(),
                secondaryCursors = merged.drop(1)
            )
        }
    }

    override fun operateAll(operation: (Cursor) -> Cursor): CursorManager {
        val newPrimary = operation(primaryCursor)
        val newSecondary = secondaryCursors.map { operation(it) }
        return CursorManagerImpl(newPrimary, newSecondary).mergeOverlapping()
    }

    override fun clearSecondary(): CursorManager {
        return CursorManagerImpl(primaryCursor)
    }
}
