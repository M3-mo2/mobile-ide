package com.mobileide.editor.core

import java.util.UUID

/**
 * Represents a single edit operation.
 * This is the unit of undo/redo.
 */
data class EditOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: EditType,
    val range: Range,
    val oldText: String,
    val newText: String,
    val cursorBefore: Position,
    val cursorAfter: Position,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompound: Boolean = false
) {
    /**
     * Returns the inverse operation for undo.
     */
    fun inverse(): EditOperation {
        return copy(
            type = when (type) {
                EditType.INSERT -> EditType.DELETE
                EditType.DELETE -> EditType.INSERT
                EditType.REPLACE -> EditType.REPLACE
                EditType.COMPOUND -> EditType.COMPOUND
            },
            oldText = newText,
            newText = oldText,
            cursorBefore = cursorAfter,
            cursorAfter = cursorBefore
        )
    }
}

/**
 * Types of edit operations.
 */
enum class EditType {
    INSERT, DELETE, REPLACE, COMPOUND
}

/**
 * Represents a compound operation (group of operations treated as one unit).
 */
data class CompoundOperation(
    val operations: List<EditOperation>
) {
    /**
     * Converts this compound operation to a single EditOperation.
     */
    fun toEditOperation(): EditOperation {
        return EditOperation(
            type = EditType.COMPOUND,
            range = operations.first().range,
            oldText = operations.joinToString("") { it.oldText },
            newText = operations.joinToString("") { it.newText },
            cursorBefore = operations.first().cursorBefore,
            cursorAfter = operations.last().cursorAfter,
            isCompound = true
        )
    }
}

/**
 * Interface for undo/redo management.
 * Uses a version tree to support branching history.
 */
interface UndoManager {
    /**
     * Maximum number of operations to keep in history.
     */
    val maxHistory: Int

    /**
     * Records an edit operation.
     * Returns a new UndoManager with the operation recorded.
     */
    fun record(operation: EditOperation): UndoManager

    /**
     * Undoes the last operation.
     * Returns the inverse operation to apply, or null if nothing to undo.
     */
    fun undo(): EditOperation?

    /**
     * Redoes the last undone operation.
     * Returns the operation to apply, or null if nothing to redo.
     */
    fun redo(): EditOperation?

    /**
     * Returns true if undo is available.
     */
    fun canUndo(): Boolean

    /**
     * Returns true if redo is available.
     */
    fun canRedo(): Boolean

    /**
     * Returns the current history size.
     */
    fun historySize(): Int

    /**
     * Clears all history.
     * Returns a new empty UndoManager.
     */
    fun clear(): UndoManager

    /**
     * Starts a compound operation group.
     * Subsequent operations will be grouped together.
     */
    fun startGroup(): UndoManager

    /**
     * Ends a compound operation group.
     * All operations since startGroup are combined into one.
     */
    fun endGroup(): UndoManager

    /**
     * Returns true if currently inside a compound operation group.
     */
    fun isGrouping(): Boolean
}

/**
 * Default implementation of UndoManager.
 */
class UndoManagerImpl(
    override val maxHistory: Int = DEFAULT_MAX_HISTORY,
    private val history: List<EditOperation> = emptyList(),
    private val currentIndex: Int = -1,
    private val isInGroup: Boolean = false,
    private val groupOperations: List<EditOperation> = emptyList()
) : UndoManager {

    companion object {
        const val DEFAULT_MAX_HISTORY = 10000
        const val GROUP_TIME_THRESHOLD_MS = 500L
    }

    override fun record(operation: EditOperation): UndoManager {
        return if (isInGroup) {
            // Add to current group
            UndoManagerImpl(
                maxHistory = maxHistory,
                history = history,
                currentIndex = currentIndex,
                isInGroup = true,
                groupOperations = groupOperations + operation
            )
        } else {
            // Check if we should auto-group with previous operation
            val shouldGroup = shouldAutoGroup(operation)

            if (shouldGroup && currentIndex >= 0) {
                // Merge with previous operation
                val previousOp = history[currentIndex]
                val mergedOp = mergeOperations(previousOp, operation)
                val newHistory = history.take(currentIndex) + mergedOp
                UndoManagerImpl(
                    maxHistory = maxHistory,
                    history = trimHistory(newHistory),
                    currentIndex = currentIndex,
                    isInGroup = false,
                    groupOperations = emptyList()
                )
            } else {
                // Add as new operation
                val newHistory = history.take(currentIndex + 1) + operation
                UndoManagerImpl(
                    maxHistory = maxHistory,
                    history = trimHistory(newHistory),
                    currentIndex = currentIndex + 1,
                    isInGroup = false,
                    groupOperations = emptyList()
                )
            }
        }
    }

    override fun undo(): EditOperation? {
        if (!canUndo()) return null

        val operation = history[currentIndex]
        return operation.inverse()
    }

    override fun redo(): EditOperation? {
        if (!canRedo()) return null

        val operation = history[currentIndex + 1]
        return operation
    }

    override fun canUndo(): Boolean = currentIndex >= 0

    override fun canRedo(): Boolean = currentIndex < history.size - 1

    override fun historySize(): Int = history.size

    override fun clear(): UndoManager {
        return UndoManagerImpl(maxHistory = maxHistory)
    }

    override fun startGroup(): UndoManager {
        return UndoManagerImpl(
            maxHistory = maxHistory,
            history = history,
            currentIndex = currentIndex,
            isInGroup = true,
            groupOperations = emptyList()
        )
    }

    override fun endGroup(): UndoManager {
        if (!isInGroup || groupOperations.isEmpty()) {
            return UndoManagerImpl(
                maxHistory = maxHistory,
                history = history,
                currentIndex = currentIndex,
                isInGroup = false,
                groupOperations = emptyList()
            )
        }

        val compoundOp = if (groupOperations.size == 1) {
            groupOperations.first()
        } else {
            CompoundOperation(groupOperations).toEditOperation()
        }

        val newHistory = history.take(currentIndex + 1) + compoundOp
        return UndoManagerImpl(
            maxHistory = maxHistory,
            history = trimHistory(newHistory),
            currentIndex = currentIndex + 1,
            isInGroup = false,
            groupOperations = emptyList()
        )
    }

    override fun isGrouping(): Boolean = isInGroup

    /**
     * Returns a new UndoManager with the index decremented (for undo).
     */
    fun withDecrementedIndex(): UndoManager {
        return UndoManagerImpl(
            maxHistory = maxHistory,
            history = history,
            currentIndex = currentIndex - 1,
            isInGroup = isInGroup,
            groupOperations = groupOperations
        )
    }

    /**
     * Returns a new UndoManager with the index incremented (for redo).
     */
    fun withIncrementedIndex(): UndoManager {
        return UndoManagerImpl(
            maxHistory = maxHistory,
            history = history,
            currentIndex = currentIndex + 1,
            isInGroup = isInGroup,
            groupOperations = groupOperations
        )
    }

    /**
     * Checks if the new operation should be auto-grouped with the previous one.
     */
    private fun shouldAutoGroup(newOp: EditOperation): Boolean {
        if (currentIndex < 0 || currentIndex >= history.size) return false
        val previousOp = history[currentIndex]

        // Must be same type
        if (previousOp.type != newOp.type) return false

        // Must be within time threshold
        if (newOp.timestamp - previousOp.timestamp > GROUP_TIME_THRESHOLD_MS) return false

        // Must be adjacent positions
        return when (previousOp.type) {
            EditType.INSERT -> {
                // Insertions at end of previous insertion
                previousOp.range.end == newOp.range.start
            }
            EditType.DELETE -> {
                // Deletions at same position
                previousOp.range.start == newOp.range.start
            }
            else -> false
        }
    }

    /**
     * Merges two operations into one.
     */
    private fun mergeOperations(first: EditOperation, second: EditOperation): EditOperation {
        return when (first.type) {
            EditType.INSERT -> {
                EditOperation(
                    type = EditType.INSERT,
                    range = Range(first.range.start, second.range.end),
                    oldText = first.oldText + second.oldText,
                    newText = first.newText + second.newText,
                    cursorBefore = first.cursorBefore,
                    cursorAfter = second.cursorAfter
                )
            }
            EditType.DELETE -> {
                EditOperation(
                    type = EditType.DELETE,
                    range = Range(second.range.start, first.range.end),
                    oldText = first.oldText + second.oldText,
                    newText = first.newText + second.newText,
                    cursorBefore = first.cursorBefore,
                    cursorAfter = second.cursorAfter
                )
            }
            else -> {
                // For other types, create compound
                CompoundOperation(listOf(first, second)).toEditOperation()
            }
        }
    }

    /**
     * Trims history to maxHistory size, keeping most recent operations.
     */
    private fun trimHistory(history: List<EditOperation>): List<EditOperation> {
        return if (history.size > maxHistory) {
            history.takeLast(maxHistory)
        } else {
            history
        }
    }

    override fun toString(): String {
        return "UndoManager(history=${history.size}, current=$currentIndex, canUndo=$canUndo(), canRedo=$canRedo())"
    }
}
