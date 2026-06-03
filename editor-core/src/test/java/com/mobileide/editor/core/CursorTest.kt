package com.mobileide.editor.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Cursor and CursorManager.
 */
class CursorTest {

    @Test
    fun `create cursor at position`() {
        val cursor = CursorImpl(Position(0, 0))
        assertEquals(Position(0, 0), cursor.position)
        assertEquals(0, cursor.preferredColumn)
    }

    @Test
    fun `move cursor right`() {
        val cursor = CursorImpl(Position(0, 0))
        val lineLengths = listOf(10)
        val newCursor = cursor.move(Direction.RIGHT, lineLengths)
        assertEquals(Position(0, 1), newCursor.position)
    }

    @Test
    fun `move cursor left from middle`() {
        val cursor = CursorImpl(Position(0, 5))
        val lineLengths = listOf(10)
        val newCursor = cursor.move(Direction.LEFT, lineLengths)
        assertEquals(Position(0, 4), newCursor.position)
    }

    @Test
    fun `move cursor left from start stays at start`() {
        val cursor = CursorImpl(Position(0, 0))
        val lineLengths = listOf(10)
        val newCursor = cursor.move(Direction.LEFT, lineLengths)
        assertEquals(Position(0, 0), newCursor.position)
    }

    @Test
    fun `move cursor down`() {
        val cursor = CursorImpl(Position(0, 3))
        val lineLengths = listOf(10, 15, 8)
        val newCursor = cursor.move(Direction.DOWN, lineLengths)
        assertEquals(Position(1, 3), newCursor.position)
        assertEquals(3, newCursor.preferredColumn)
    }

    @Test
    fun `move cursor down with shorter line clamps to end`() {
        val cursor = CursorImpl(Position(0, 10))
        val lineLengths = listOf(15, 5, 8)
        val newCursor = cursor.move(Direction.DOWN, lineLengths)
        assertEquals(Position(1, 5), newCursor.position)
    }

    @Test
    fun `move cursor up`() {
        val cursor = CursorImpl(Position(1, 5))
        val lineLengths = listOf(10, 15, 8)
        val newCursor = cursor.move(Direction.UP, lineLengths)
        assertEquals(Position(0, 5), newCursor.position)
    }

    @Test
    fun `move cursor up from first line stays`() {
        val cursor = CursorImpl(Position(0, 5))
        val lineLengths = listOf(10, 15, 8)
        val newCursor = cursor.move(Direction.UP, lineLengths)
        assertEquals(Position(0, 5), newCursor.position)
    }

    @Test
    fun `move cursor to line start`() {
        val cursor = CursorImpl(Position(0, 5))
        val lineLengths = listOf(10)
        val newCursor = cursor.move(Direction.LINE_START, lineLengths)
        assertEquals(Position(0, 0), newCursor.position)
    }

    @Test
    fun `move cursor to line end`() {
        val cursor = CursorImpl(Position(0, 0))
        val lineLengths = listOf(10)
        val newCursor = cursor.move(Direction.LINE_END, lineLengths)
        assertEquals(Position(0, 10), newCursor.position)
    }

    @Test
    fun `move cursor to file start`() {
        val cursor = CursorImpl(Position(2, 5))
        val lineLengths = listOf(10, 15, 8)
        val newCursor = cursor.move(Direction.FILE_START, lineLengths)
        assertEquals(Position(0, 0), newCursor.position)
    }

    @Test
    fun `move cursor to file end`() {
        val cursor = CursorImpl(Position(0, 0))
        val lineLengths = listOf(10, 15, 8)
        val newCursor = cursor.move(Direction.FILE_END, lineLengths)
        assertEquals(Position(2, 8), newCursor.position)
    }

    @Test
    fun `moveTo creates cursor at new position`() {
        val cursor = CursorImpl(Position(0, 0))
        val newCursor = cursor.moveTo(Position(5, 10))
        assertEquals(Position(5, 10), newCursor.position)
        assertEquals(10, newCursor.preferredColumn)
    }

    @Test
    fun `cursor manager with single cursor`() {
        val manager = CursorManagerImpl(CursorImpl(Position(0, 0)))
        assertEquals(1, manager.allCursors.size)
        assertEquals(Position(0, 0), manager.primaryCursor.position)
    }

    @Test
    fun `add secondary cursor`() {
        val manager = CursorManagerImpl(CursorImpl(Position(0, 0)))
            .addCursor(Position(1, 5))
        assertEquals(2, manager.allCursors.size)
        assertEquals(Position(1, 5), manager.secondaryCursors[0].position)
    }

    @Test
    fun `add cursor beyond max returns same manager`() {
        var manager: CursorManager = CursorManagerImpl(CursorImpl(Position(0, 0)))
        for (i in 1..15) {
            manager = manager.addCursor(Position(i, 0))
        }
        assertEquals(10, manager.allCursors.size)
    }

    @Test
    fun `remove secondary cursor`() {
        val manager = CursorManagerImpl(CursorImpl(Position(0, 0)))
            .addCursor(Position(1, 5))
            .addCursor(Position(2, 10))
            .removeCursor(1) // Remove first secondary
        assertEquals(2, manager.allCursors.size)
        assertEquals(Position(2, 10), manager.secondaryCursors[0].position)
    }

    @Test
    fun `merge overlapping cursors`() {
        val manager = CursorManagerImpl(CursorImpl(Position(0, 0)))
            .addCursor(Position(0, 0)) // Same as primary
            .addCursor(Position(1, 5))
        val merged = manager.mergeOverlapping()
        assertEquals(2, merged.allCursors.size)
    }

    @Test
    fun `operateAll moves all cursors`() {
        val manager = CursorManagerImpl(CursorImpl(Position(0, 0)))
            .addCursor(Position(1, 5))
        val lineLengths = listOf(10, 15, 8)
        val newManager = manager.operateAll { cursor ->
            cursor.move(Direction.RIGHT, lineLengths)
        }
        assertEquals(Position(0, 1), newManager.primaryCursor.position)
        assertEquals(Position(1, 6), newManager.secondaryCursors[0].position)
    }

    @Test
    fun `clear secondary removes all secondary cursors`() {
        val manager = CursorManagerImpl(CursorImpl(Position(0, 0)))
            .addCursor(Position(1, 5))
            .addCursor(Position(2, 10))
            .clearSecondary()
        assertEquals(1, manager.allCursors.size)
    }
}
