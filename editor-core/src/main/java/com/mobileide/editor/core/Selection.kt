package com.mobileide.editor.core

/**
 * Interface for text selection management.
 * Supports multiple selection types and operations.
 * All operations are immutable.
 */
interface Selection {
    /**
     * The anchor position (fixed end of selection).
     */
    val anchor: Position

    /**
     * The head position (active end of selection).
     */
    val head: Position

    /**
     * The start position (min of anchor and head).
     */
    val start: Position
        get() = if (anchor <= head) anchor else head

    /**
     * The end position (max of anchor and head).
     */
    val end: Position
        get() = if (anchor <= head) head else anchor

    /**
     * Returns true if the selection is empty (anchor == head).
     */
    val isEmpty: Boolean
        get() = anchor == head

    /**
     * Returns true if the selection is reversed (head < anchor).
     */
    val isReversed: Boolean
        get() = head < anchor

    /**
     * Returns the range of this selection.
     */
    fun toRange(): Range = Range(start, end)

    /**
     * Extends the selection in the given direction.
     * Returns a new Selection with the head moved.
     */
    fun extend(direction: Direction, lineLengths: List<Int>): Selection

    /**
     * Extends the selection to the given position.
     * Returns a new Selection with the head at that position.
     */
    fun extendTo(position: Position): Selection

    /**
     * Shrinks the selection from the active end.
     * Returns a new Selection with the head moved towards the anchor.
     */
    fun shrink(direction: Direction, lineLengths: List<Int>): Selection

    /**
     * Inverts the selection (swaps anchor and head).
     * Returns a new Selection with swapped ends.
     */
    fun invert(): Selection

    /**
     * Clears the selection, keeping only the cursor at the head position.
     * Returns a new empty Selection with both ends at head.
     */
    fun clear(): Selection

    /**
     * Returns true if this selection contains the given position.
     */
    fun contains(position: Position): Boolean

    /**
     * Returns true if this selection overlaps with the other selection.
     */
    fun overlaps(other: Selection): Boolean

    /**
     * Merges this selection with another if they overlap.
     * Returns a new Selection covering both, or null if they don't overlap.
     */
    fun merge(other: Selection): Selection?
}

/**
 * Types of selection.
 */
enum class SelectionType {
    NORMAL,      // Click and drag
    WORD,        // Double-click
    LINE,        // Triple-click
    BLOCK        // Alt+drag (rectangular)
}

/**
 * Default implementation of Selection.
 */
data class SelectionImpl(
    override val anchor: Position,
    override val head: Position
) : Selection {

    override fun extend(direction: Direction, lineLengths: List<Int>): Selection {
        val cursor = CursorImpl(head)
        val newHead = cursor.move(direction, lineLengths).position
        return copy(head = newHead)
    }

    override fun extendTo(position: Position): Selection {
        return copy(head = position)
    }

    override fun shrink(direction: Direction, lineLengths: List<Int>): Selection {
        // Move head towards anchor
        val cursor = CursorImpl(head)
        val newHead = when {
            head > anchor -> {
                // Head is after anchor, move left/up
                when (direction) {
                    Direction.LEFT, Direction.UP -> cursor.move(direction, lineLengths).position
                    else -> head
                }
            }
            head < anchor -> {
                // Head is before anchor, move right/down
                when (direction) {
                    Direction.RIGHT, Direction.DOWN -> cursor.move(direction, lineLengths).position
                    else -> head
                }
            }
            else -> head
        }
        return copy(head = newHead)
    }

    override fun invert(): Selection {
        return copy(anchor = head, head = anchor)
    }

    override fun clear(): Selection {
        return copy(anchor = head, head = head)
    }

    override fun contains(position: Position): Boolean {
        return position >= start && position < end
    }

    override fun overlaps(other: Selection): Boolean {
        return this.toRange().overlaps(other.toRange())
    }

    override fun merge(other: Selection): Selection? {
        return if (overlaps(other)) {
            val newStart = if (start < other.start) start else other.start
            val newEnd = if (end > other.end) end else other.end
            SelectionImpl(newStart, newEnd)
        } else {
            null
        }
    }

    override fun toString(): String {
        return "Selection(anchor=$anchor, head=$head, start=$start, end=$end)"
    }
}

/**
 * Manages multiple selections.
 * Each cursor can have its own selection.
 */
interface SelectionManager {
    /**
     * The primary selection.
     */
    val primarySelection: Selection

    /**
     * Secondary selections.
     */
    val secondarySelections: List<Selection>

    /**
     * All selections including primary.
     */
    val allSelections: List<Selection>
        get() = listOf(primarySelection) + secondarySelections

    /**
     * Adds a selection.
     * Returns a new SelectionManager with the added selection.
     */
    fun addSelection(selection: Selection): SelectionManager

    /**
     * Removes a selection at the given index (0 = primary).
     * Returns a new SelectionManager without that selection.
     */
    fun removeSelection(index: Int): SelectionManager

    /**
     * Merges overlapping selections.
     * Returns a new SelectionManager with merged selections.
     */
    fun mergeOverlapping(): SelectionManager

    /**
     * Clears all selections (sets them to empty at current head positions).
     * Returns a new SelectionManager with empty selections.
     */
    fun clearAll(): SelectionManager

    /**
     * Applies an operation to all selections.
     * Returns a new SelectionManager with updated selections.
     */
    fun operateAll(operation: (Selection) -> Selection): SelectionManager
}

/**
 * Default implementation of SelectionManager.
 */
data class SelectionManagerImpl(
    override val primarySelection: Selection,
    override val secondarySelections: List<Selection> = emptyList()
) : SelectionManager {

    override fun addSelection(selection: Selection): SelectionManager {
        return copy(secondarySelections = secondarySelections + selection)
    }

    override fun removeSelection(index: Int): SelectionManager {
        return when {
            index < 0 || index >= allSelections.size -> this
            index == 0 -> {
                val newSecondary = secondarySelections.drop(1)
                if (newSecondary.isNotEmpty()) {
                    copy(
                        primarySelection = newSecondary.first(),
                        secondarySelections = newSecondary.drop(1)
                    )
                } else {
                    this
                }
            }
            else -> {
                val adjustedIndex = index - 1
                copy(secondarySelections = secondarySelections.filterIndexed { i, _ -> i != adjustedIndex })
            }
        }
    }

    override fun mergeOverlapping(): SelectionManager {
        val all = allSelections.toMutableList()
        var changed = true

        while (changed) {
            changed = false
            val merged = mutableListOf<Selection>()

            for (selection in all) {
                var mergedWithExisting = false
                for (i in merged.indices) {
                    val existing = merged[i]
                    val mergedSelection = existing.merge(selection)
                    if (mergedSelection != null) {
                        merged[i] = mergedSelection
                        mergedWithExisting = true
                        changed = true
                        break
                    }
                }
                if (!mergedWithExisting) {
                    merged.add(selection)
                }
            }

            all.clear()
            all.addAll(merged)
        }

        return if (all.isEmpty()) {
            this
        } else {
            SelectionManagerImpl(
                primarySelection = all.first(),
                secondarySelections = all.drop(1)
            )
        }
    }

    override fun clearAll(): SelectionManager {
        val newPrimary = primarySelection.clear()
        val newSecondary = secondarySelections.map { it.clear() }
        return SelectionManagerImpl(newPrimary, newSecondary)
    }

    override fun operateAll(operation: (Selection) -> Selection): SelectionManager {
        val newPrimary = operation(primarySelection)
        val newSecondary = secondarySelections.map { operation(it) }
        return SelectionManagerImpl(newPrimary, newSecondary).mergeOverlapping()
    }
}
