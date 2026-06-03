package com.mobileide.editor.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UndoManager.
 */
class UndoManagerTest {

    @Test
    fun `create empty undo manager`() {
        val manager = UndoManagerImpl()
        assertFalse(manager.canUndo())
        assertFalse(manager.canRedo())
        assertEquals(0, manager.historySize())
    }

    @Test
    fun `record single operation`() {
        val manager = UndoManagerImpl()
        val op = createInsertOp("Hello", Position(0, 0), Position(0, 5))
        val newManager = manager.record(op)
        assertTrue(newManager.canUndo())
        assertFalse(newManager.canRedo())
        assertEquals(1, newManager.historySize())
    }

    @Test
    fun `undo returns inverse operation`() {
        val manager = UndoManagerImpl()
        val op = createInsertOp("Hello", Position(0, 0), Position(0, 5))
        val newManager = manager.record(op)
        val inverse = newManager.undo()
        assertNotNull(inverse)
        assertEquals(EditType.DELETE, inverse!!.type)
        assertEquals("Hello", inverse.oldText)
        assertEquals("", inverse.newText)
    }

    @Test
    fun `undo and redo`() {
        val manager = UndoManagerImpl()
        val op = createInsertOp("Hello", Position(0, 0), Position(0, 5))
        var newManager = manager.record(op)

        // Undo
        val inverse = newManager.undo()
        assertNotNull(inverse)
        newManager = newManager.withDecrementedIndex()
        assertFalse(newManager.canUndo())
        assertTrue(newManager.canRedo())

        // Redo
        val redoOp = newManager.redo()
        assertNotNull(redoOp)
        newManager = newManager.withIncrementedIndex()
        assertTrue(newManager.canUndo())
        assertFalse(newManager.canRedo())
    }

    @Test
    fun `auto-group consecutive insertions`() {
        val manager = UndoManagerImpl()
        val op1 = createInsertOp("H", Position(0, 0), Position(0, 1))
        Thread.sleep(10) // Small delay to avoid grouping
        val op2 = createInsertOp("e", Position(0, 1), Position(0, 2))

        var newManager = manager.record(op1)
        newManager = newManager.record(op2)

        // Should be grouped into one operation
        assertEquals(1, newManager.historySize())
    }

    @Test
    fun `no auto-group for different types`() {
        val manager = UndoManagerImpl()
        val insertOp = createInsertOp("Hello", Position(0, 0), Position(0, 5))
        val deleteOp = createDeleteOp("Hello", Position(0, 0), Position(0, 5))

        var newManager = manager.record(insertOp)
        newManager = newManager.record(deleteOp)

        // Should be separate operations
        assertEquals(2, newManager.historySize())
    }

    @Test
    fun `compound operation group`() {
        val manager = UndoManagerImpl()
        val groupedManager = manager.startGroup()

        val op1 = createInsertOp("Hello", Position(0, 0), Position(0, 5))
        val op2 = createInsertOp(" World", Position(0, 5), Position(0, 11))

        var newManager = groupedManager.record(op1)
        newManager = newManager.record(op2)
        newManager = newManager.endGroup()

        assertTrue(newManager.canUndo())
        assertEquals(1, newManager.historySize())

        val inverse = newManager.undo()
        assertNotNull(inverse)
        assertTrue(inverse!!.isCompound)
    }

    @Test
    fun `max history limit`() {
        val manager = UndoManagerImpl(maxHistory = 3)
        var newManager = manager

        for (i in 1..5) {
            val op = createInsertOp("Op$i", Position(0, 0), Position(0, i))
            Thread.sleep(10)
            newManager = newManager.record(op)
        }

        // Should only keep last 3 operations
        assertEquals(3, newManager.historySize())
    }

    @Test
    fun `clear history`() {
        val manager = UndoManagerImpl()
        val op = createInsertOp("Hello", Position(0, 0), Position(0, 5))
        val newManager = manager.record(op).clear()
        assertFalse(newManager.canUndo())
        assertEquals(0, newManager.historySize())
    }

    @Test
    fun `undo after new operation clears redo stack`() {
        val manager = UndoManagerImpl()
        val op1 = createInsertOp("Hello", Position(0, 0), Position(0, 5))
        val op2 = createInsertOp(" World", Position(0, 5), Position(0, 11))
        val op3 = createInsertOp("!", Position(0, 11), Position(0, 12))

        var newManager = manager.record(op1)
        newManager = newManager.record(op2)

        // Undo op2
        newManager = newManager.withDecrementedIndex()
        assertTrue(newManager.canRedo())

        // Record new operation - should clear redo stack
        newManager = newManager.record(op3)
        assertFalse(newManager.canRedo())
        assertEquals(2, newManager.historySize())
    }

    @Test
    fun `isGrouping returns true during group`() {
        val manager = UndoManagerImpl().startGroup()
        assertTrue(manager.isGrouping())
    }

    @Test
    fun `isGrouping returns false after endGroup`() {
        val manager = UndoManagerImpl().startGroup().endGroup()
        assertFalse(manager.isGrouping())
    }

    @Test
    fun `empty group doesn't add operation`() {
        val manager = UndoManagerImpl().startGroup().endGroup()
        assertFalse(manager.canUndo())
        assertEquals(0, manager.historySize())
    }

    // Helper methods

    private fun createInsertOp(
        text: String,
        before: Position,
        after: Position
    ): EditOperation {
        return EditOperation(
            type = EditType.INSERT,
            range = Range(before, after),
            oldText = "",
            newText = text,
            cursorBefore = before,
            cursorAfter = after
        )
    }

    private fun createDeleteOp(
        text: String,
        before: Position,
        after: Position
    ): EditOperation {
        return EditOperation(
            type = EditType.DELETE,
            range = Range(before, after),
            oldText = text,
            newText = "",
            cursorBefore = before,
            cursorAfter = after
        )
    }
}
