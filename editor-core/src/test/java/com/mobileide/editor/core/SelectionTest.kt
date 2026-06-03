package com.mobileide.editor.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Selection and SelectionManager.
 */
class SelectionTest {

    @Test
    fun `create empty selection`() {
        val selection = SelectionImpl(Position(0, 0), Position(0, 0))
        assertTrue(selection.isEmpty)
        assertEquals(Position(0, 0), selection.start)
        assertEquals(Position(0, 0), selection.end)
    }

    @Test
    fun `create forward selection`() {
        val selection = SelectionImpl(Position(0, 0), Position(0, 5))
        assertFalse(selection.isEmpty)
        assertFalse(selection.isReversed)
        assertEquals(Position(0, 0), selection.start)
        assertEquals(Position(0, 5), selection.end)
    }

    @Test
    fun `create reversed selection`() {
        val selection = SelectionImpl(Position(0, 5), Position(0, 0))
        assertFalse(selection.isEmpty)
        assertTrue(selection.isReversed)
        assertEquals(Position(0, 0), selection.start)
        assertEquals(Position(0, 5), selection.end)
    }

    @Test
    fun `extend selection right`() {
        val selection = SelectionImpl(Position(0, 0), Position(0, 0))
        val lineLengths = listOf(10)
        val extended = selection.extend(Direction.RIGHT, lineLengths)
        assertEquals(Position(0, 0), extended.anchor)
        assertEquals(Position(0, 1), extended.head)
    }

    @Test
    fun `extend selection to position`() {
        val selection = SelectionImpl(Position(0, 0), Position(0, 0))
        val extended = selection.extendTo(Position(0, 5))
        assertEquals(Position(0, 0), extended.anchor)
        assertEquals(Position(0, 5), extended.head)
    }

    @Test
    fun `clear selection keeps cursor at head`() {
        val selection = SelectionImpl(Position(0, 0), Position(0, 5))
        val cleared = selection.clear()
        assertTrue(cleared.isEmpty)
        assertEquals(Position(0, 5), cleared.anchor)
        assertEquals(Position(0, 5), cleared.head)
    }

    @Test
    fun `invert selection swaps anchor and head`() {
        val selection = SelectionImpl(Position(0, 0), Position(0, 5))
        val inverted = selection.invert()
        assertEquals(Position(0, 5), inverted.anchor)
        assertEquals(Position(0, 0), inverted.head)
    }

    @Test
    fun `contains position within range`() {
        val selection = SelectionImpl(Position(0, 0), Position(0, 5))
        assertTrue(selection.contains(Position(0, 2)))
        assertTrue(selection.contains(Position(0, 0)))
        assertFalse(selection.contains(Position(0, 5)))
        assertFalse(selection.contains(Position(0, 10)))
    }

    @Test
    fun `overlapping selections`() {
        val selection1 = SelectionImpl(Position(0, 0), Position(0, 5))
        val selection2 = SelectionImpl(Position(0, 3), Position(0, 8))
        assertTrue(selection1.overlaps(selection2))
    }

    @Test
    fun `non-overlapping selections`() {
        val selection1 = SelectionImpl(Position(0, 0), Position(0, 5))
        val selection2 = SelectionImpl(Position(0, 10), Position(0, 15))
        assertFalse(selection1.overlaps(selection2))
    }

    @Test
    fun `merge overlapping selections`() {
        val selection1 = SelectionImpl(Position(0, 0), Position(0, 5))
        val selection2 = SelectionImpl(Position(0, 3), Position(0, 8))
        val merged = selection1.merge(selection2)
        assertNotNull(merged)
        assertEquals(Position(0, 0), merged!!.start)
        assertEquals(Position(0, 8), merged.end)
    }

    @Test
    fun `merge non-overlapping returns null`() {
        val selection1 = SelectionImpl(Position(0, 0), Position(0, 5))
        val selection2 = SelectionImpl(Position(0, 10), Position(0, 15))
        val merged = selection1.merge(selection2)
        assertNull(merged)
    }

    @Test
    fun `selection manager with single selection`() {
        val selection = SelectionImpl(Position(0, 0), Position(0, 5))
        val manager = SelectionManagerImpl(selection)
        assertEquals(1, manager.allSelections.size)
        assertFalse(manager.allSelections[0].isEmpty)
    }

    @Test
    fun `add secondary selection`() {
        val manager = SelectionManagerImpl(SelectionImpl(Position(0, 0), Position(0, 5)))
            .addSelection(SelectionImpl(Position(1, 0), Position(1, 10)))
        assertEquals(2, manager.allSelections.size)
    }

    @Test
    fun `merge overlapping selections in manager`() {
        val manager = SelectionManagerImpl(SelectionImpl(Position(0, 0), Position(0, 5)))
            .addSelection(SelectionImpl(Position(0, 3), Position(0, 8)))
            .mergeOverlapping()
        assertEquals(1, manager.allSelections.size)
        assertEquals(Position(0, 0), manager.primarySelection.start)
        assertEquals(Position(0, 8), manager.primarySelection.end)
    }

    @Test
    fun `clear all selections`() {
        val manager = SelectionManagerImpl(SelectionImpl(Position(0, 0), Position(0, 5)))
            .addSelection(SelectionImpl(Position(1, 0), Position(1, 10)))
            .clearAll()
        assertTrue(manager.allSelections.all { it.isEmpty })
    }

    @Test
    fun `operateAll on selections`() {
        val manager = SelectionManagerImpl(SelectionImpl(Position(0, 0), Position(0, 5)))
            .addSelection(SelectionImpl(Position(1, 0), Position(1, 10)))
        val lineLengths = listOf(10, 15)
        val newManager = manager.operateAll { selection ->
            selection.extend(Direction.RIGHT, lineLengths)
        }
        assertEquals(Position(0, 6), newManager.primarySelection.head)
        assertEquals(Position(1, 11), newManager.secondarySelections[0].head)
    }

    @Test
    fun `selection state has selection`() {
        val state = SelectionState(
            primary = SelectionImpl(Position(0, 0), Position(0, 5))
        )
        assertTrue(state.hasSelection())
    }

    @Test
    fun `selection state no selection`() {
        val state = SelectionState(
            primary = SelectionImpl(Position(0, 0), Position(0, 0))
        )
        assertFalse(state.hasSelection())
    }
}
