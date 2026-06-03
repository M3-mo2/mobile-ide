package com.mobileide.editor.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PieceTableTextBuffer.
 *
 * Tests cover:
 * - Basic operations (insert, delete, replace)
 * - Line handling
 * - Position/offset conversions
 * - Edge cases
 * - Immutability
 */
class PieceTableTextBufferTest {

    @Test
    fun `create empty buffer`() {
        val buffer = TextBufferFactory.createEmpty()
        assertEquals("", buffer.getText())
        assertEquals(1, buffer.getLineCount())
        assertEquals(0, buffer.length())
    }

    @Test
    fun `create buffer from text`() {
        val buffer = TextBufferFactory.fromText("Hello, World!")
        assertEquals("Hello, World!", buffer.getText())
        assertEquals(1, buffer.getLineCount())
        assertEquals(13, buffer.length())
    }

    @Test
    fun `insert text at beginning`() {
        val buffer = TextBufferFactory.fromText("World")
        val newBuffer = buffer.insert(0, "Hello, ")
        assertEquals("Hello, World", newBuffer.getText())
        assertEquals(12, newBuffer.length())
    }

    @Test
    fun `insert text at end`() {
        val buffer = TextBufferFactory.fromText("Hello")
        val newBuffer = buffer.insert(5, ", World!")
        assertEquals("Hello, World!", newBuffer.getText())
    }

    @Test
    fun `insert text in middle`() {
        val buffer = TextBufferFactory.fromText("Hello World")
        val newBuffer = buffer.insert(5, ", Beautiful")
        assertEquals("Hello, Beautiful World", newBuffer.getText())
    }

    @Test
    fun `insert empty text returns same buffer`() {
        val buffer = TextBufferFactory.fromText("Hello")
        val newBuffer = buffer.insert(3, "")
        assertEquals(buffer, newBuffer)
    }

    @Test
    fun `delete text from beginning`() {
        val buffer = TextBufferFactory.fromText("Hello, World!")
        val newBuffer = buffer.delete(0, 7)
        assertEquals("World!", newBuffer.getText())
    }

    @Test
    fun `delete text from end`() {
        val buffer = TextBufferFactory.fromText("Hello, World!")
        val newBuffer = buffer.delete(7, 6)
        assertEquals("Hello, ", newBuffer.getText())
    }

    @Test
    fun `delete text from middle`() {
        val buffer = TextBufferFactory.fromText("Hello, Beautiful World!")
        val newBuffer = buffer.delete(7, 10) // Delete "Beautiful "
        assertEquals("Hello, World!", newBuffer.getText())
    }

    @Test
    fun `delete zero length returns same buffer`() {
        val buffer = TextBufferFactory.fromText("Hello")
        val newBuffer = buffer.delete(3, 0)
        assertEquals(buffer, newBuffer)
    }

    @Test
    fun `delete beyond end clamps to end`() {
        val buffer = TextBufferFactory.fromText("Hello")
        val newBuffer = buffer.delete(3, 100)
        assertEquals("Hel", newBuffer.getText())
    }

    @Test
    fun `replace text`() {
        val buffer = TextBufferFactory.fromText("Hello, World!")
        val newBuffer = buffer.replace(7, 5, "Universe")
        assertEquals("Hello, Universe!", newBuffer.getText())
    }

    @Test
    fun `get line returns correct text`() {
        val buffer = TextBufferFactory.fromText("Line 1\nLine 2\nLine 3")
        assertEquals("Line 1", buffer.getLine(0))
        assertEquals("Line 2", buffer.getLine(1))
        assertEquals("Line 3", buffer.getLine(2))
    }

    @Test
    fun `get line count for multi-line text`() {
        val buffer = TextBufferFactory.fromText("Line 1\nLine 2\nLine 3")
        assertEquals(3, buffer.getLineCount())
    }

    @Test
    fun `get line count for single line`() {
        val buffer = TextBufferFactory.fromText("Single line")
        assertEquals(1, buffer.getLineCount())
    }

    @Test
    fun `charAt returns correct character`() {
        val buffer = TextBufferFactory.fromText("Hello")
        assertEquals('H', buffer.charAt(0))
        assertEquals('e', buffer.charAt(1))
        assertEquals('o', buffer.charAt(4))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `charAt out of bounds throws exception`() {
        val buffer = TextBufferFactory.fromText("Hello")
        buffer.charAt(100)
    }

    @Test
    fun `position to offset conversion`() {
        val buffer = TextBufferFactory.fromText("Hello\nWorld\nTest")
        assertEquals(0, buffer.positionToOffset(Position(0, 0)))
        assertEquals(6, buffer.positionToOffset(Position(1, 0)))
        assertEquals(7, buffer.positionToOffset(Position(1, 1)))
    }

    @Test
    fun `offset to position conversion`() {
        val buffer = TextBufferFactory.fromText("Hello\nWorld\nTest")
        assertEquals(Position(0, 0), buffer.offsetToPosition(0))
        assertEquals(Position(1, 0), buffer.offsetToPosition(6))
        assertEquals(Position(1, 1), buffer.offsetToPosition(7))
    }

    @Test
    fun `substring returns correct text`() {
        val buffer = TextBufferFactory.fromText("Hello, World!")
        assertEquals("Hello", buffer.substring(0, 5))
        assertEquals("World", buffer.substring(7, 12))
        assertEquals("", buffer.substring(3, 3))
    }

    @Test
    fun `line ending detection`() {
        val lfBuffer = TextBufferFactory.fromText("Line 1\nLine 2")
        assertEquals(LineEnding.LF, lfBuffer.getLineEnding())

        val crlfBuffer = TextBufferFactory.fromText("Line 1\r\nLine 2")
        assertEquals(LineEnding.CRLF, crlfBuffer.getLineEnding())
    }

    @Test
    fun `immutability - original buffer unchanged`() {
        val buffer = TextBufferFactory.fromText("Hello")
        buffer.insert(5, " World")
        assertEquals("Hello", buffer.getText())
    }

    @Test
    fun `multiple operations`() {
        val buffer = TextBufferFactory.fromText("Hello")
            .insert(5, " World")
            .insert(11, "!")
            .delete(0, 6)
        assertEquals("World!", buffer.getText())
    }

    @Test
    fun `large file handling`() {
        val lines = (1..1000).map { "Line $it" }
        val text = lines.joinToString("\n")
        val buffer = TextBufferFactory.fromText(text)

        assertEquals(1000, buffer.getLineCount())
        assertEquals(lines[0], buffer.getLine(0))
        assertEquals(lines[999], buffer.getLine(999))
    }

    @Test
    fun `buffer equality`() {
        val buffer1 = TextBufferFactory.fromText("Hello")
        val buffer2 = TextBufferFactory.fromText("Hello")
        val buffer3 = TextBufferFactory.fromText("World")

        assertEquals(buffer1, buffer2)
        assertNotEquals(buffer1, buffer3)
    }

    @Test
    fun `empty text has one line`() {
        val buffer = TextBufferFactory.createEmpty()
        assertEquals(1, buffer.getLineCount())
        assertEquals("", buffer.getLine(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative offset throws exception`() {
        val buffer = TextBufferFactory.fromText("Hello")
        buffer.insert(-1, "x")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `offset beyond length throws exception`() {
        val buffer = TextBufferFactory.fromText("Hello")
        buffer.insert(100, "x")
    }
}
