package com.mobileide.editor.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Performance tests for editor core components.
 * These tests verify performance characteristics of the editor.
 */
class PerformanceTest {

    @Test
    fun `piece table handles large file efficiently`() {
        // Create a large file (10,000 lines)
        val lines = (1..10000).map { "Line $it with some content to make it realistic and longer than usual" }
        val text = lines.joinToString("\n")

        val startTime = System.currentTimeMillis()
        val buffer = TextBufferFactory.fromText(text)
        val createTime = System.currentTimeMillis() - startTime

        assertTrue("Creating buffer took too long: ${createTime}ms", createTime < 1000)
        assertEquals(10000, buffer.getLineCount())

        // Insert at beginning
        val insertStart = System.currentTimeMillis()
        val newBuffer = buffer.insert(0, "// Header comment\n")
        val insertTime = System.currentTimeMillis() - insertStart

        assertTrue("Insert took too long: ${insertTime}ms", insertTime < 100)
        assertEquals(10001, newBuffer.getLineCount())
    }

    @Test
    fun `piece table handles many edits efficiently`() {
        val buffer = TextBufferFactory.fromText("")
        val startTime = System.currentTimeMillis()

        var currentBuffer = buffer
        for (i in 1..1000) {
            currentBuffer = currentBuffer.insert(currentBuffer.length(), "Edit $i ")
        }

        val editTime = System.currentTimeMillis() - startTime
        assertTrue("1000 edits took too long: ${editTime}ms", editTime < 5000)
        assertEquals(1000, currentBuffer.getLineCount())
    }

    @Test
    fun `cursor movement is fast`() {
        val lines = (1..1000).map { "Line $it" }
        val state = EditorState(
            document = DocumentState(
                content = TextBufferFactory.fromText(lines.joinToString("\n"))
            )
        )

        val lineLengths = state.lineLengths()
        val startTime = System.currentTimeMillis()

        var cursor = state.cursor.primary
        for (i in 1..100) {
            cursor = cursor.move(Direction.DOWN, lineLengths)
        }

        val moveTime = System.currentTimeMillis() - startTime
        assertTrue("100 cursor movements took too long: ${moveTime}ms", moveTime < 50)
        assertEquals(100, cursor.position.line)
    }

    @Test
    fun `line access is fast`() {
        val lines = (1..10000).map { "Line $it with some content" }
        val buffer = TextBufferFactory.fromText(lines.joinToString("\n"))

        val startTime = System.currentTimeMillis()

        // Access lines at various positions
        buffer.getLine(0)
        buffer.getLine(100)
        buffer.getLine(1000)
        buffer.getLine(5000)
        buffer.getLine(9999)

        val accessTime = System.currentTimeMillis() - startTime
        assertTrue("Line access took too long: ${accessTime}ms", accessTime < 100)
    }

    @Test
    fun `undo manager handles large history`() {
        val undoManager = UndoManagerImpl(maxHistory = 10000)
        val startTime = System.currentTimeMillis()

        var currentManager = undoManager
        for (i in 1..1000) {
            val op = EditOperation(
                type = EditType.INSERT,
                range = Range(Position(0, 0), Position(0, 1)),
                oldText = "",
                newText = "a",
                cursorBefore = Position(0, 0),
                cursorAfter = Position(0, 1)
            )
            currentManager = currentManager.record(op)
        }

        val recordTime = System.currentTimeMillis() - startTime
        assertTrue("Recording 1000 operations took too long: ${recordTime}ms", recordTime < 1000)
        assertEquals(1000, currentManager.historySize())

        // Undo all
        val undoStart = System.currentTimeMillis()
        for (i in 1..1000) {
            currentManager.undo()
            currentManager = currentManager.withDecrementedIndex()
        }
        val undoTime = System.currentTimeMillis() - undoStart
        assertTrue("Undoing 1000 operations took too long: ${undoTime}ms", undoTime < 500)
    }

    @Test
    fun `position offset conversion is fast`() {
        val lines = (1..10000).map { "Line $it with some content to make lines longer" }
        val buffer = TextBufferFactory.fromText(lines.joinToString("\n"))

        val startTime = System.currentTimeMillis()

        // Convert positions to offsets and back
        for (i in listOf(0, 100, 1000, 5000, 9999)) {
            val position = Position(i, 0)
            val offset = buffer.positionToOffset(position)
            val backToPosition = buffer.offsetToPosition(offset)
            assertEquals(position.line, backToPosition.line)
        }

        val conversionTime = System.currentTimeMillis() - startTime
        assertTrue("Position conversion took too long: ${conversionTime}ms", conversionTime < 100)
    }

    @Test
    fun `memory usage is reasonable`() {
        val runtime = Runtime.getRuntime()

        // Measure memory before
        runtime.gc()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()

        // Create large buffer
        val lines = (1..10000).map { "Line $it with some content to make it realistic and longer than usual lines in a typical code file" }
        val buffer = TextBufferFactory.fromText(lines.joinToString("\n"))

        // Make some edits
        var currentBuffer = buffer
        for (i in 1..100) {
            currentBuffer = currentBuffer.insert(0, "// Comment $i\n")
        }

        // Measure memory after
        runtime.gc()
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore

        // Should use less than 100MB for 10k lines with 100 edits
        assertTrue("Memory usage too high: ${memoryUsed / 1024 / 1024}MB", memoryUsed < 100 * 1024 * 1024)
    }

    @Test
    fun `substring operation is fast`() {
        val text = (1..10000).joinToString("\n") { "Line $it with some content" }
        val buffer = TextBufferFactory.fromText(text)

        val startTime = System.currentTimeMillis()

        // Get substrings at various positions
        buffer.substring(0, 100)
        buffer.substring(1000, 1100)
        buffer.substring(50000, 50100)
        buffer.substring(100000, 100100)

        val substringTime = System.currentTimeMillis() - startTime
        assertTrue("Substring operations took too long: ${substringTime}ms", substringTime < 100)
    }

    @Test
    fun `selection operations are fast`() {
        val lines = (1..1000).map { "Line $it" }
        val state = EditorState(
            document = DocumentState(
                content = TextBufferFactory.fromText(lines.joinToString("\n"))
            )
        )

        val lineLengths = state.lineLengths()
        val startTime = System.currentTimeMillis()

        // Create and manipulate selections
        var selection = SelectionImpl(Position(0, 0), Position(0, 0))
        for (i in 1..100) {
            selection = selection.extend(Direction.DOWN, lineLengths)
        }

        val selectionTime = System.currentTimeMillis() - startTime
        assertTrue("Selection operations took too long: ${selectionTime}ms", selectionTime < 50)
        assertEquals(100, selection.end.line)
    }
}
