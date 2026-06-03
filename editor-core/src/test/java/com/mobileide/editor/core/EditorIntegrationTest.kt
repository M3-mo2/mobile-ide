package com.mobileide.editor.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Integration tests for editor core components.
 * Tests interaction between TextBuffer, Cursor, Selection, and UndoManager.
 */
class EditorIntegrationTest {

    @Test
    fun `full editing workflow`() {
        // Create initial state
        var state = EditorState(
            document = DocumentState(
                content = TextBufferFactory.fromText("Hello, World!")
            )
        )

        // Type some text
        val buffer = state.document.content
        val newBuffer = buffer.insert(7, "Beautiful ")
        state = state.withDocument(
            state.document.withContent(newBuffer)
        )

        assertEquals("Hello, Beautiful World!", state.document.content.getText())
        assertTrue(state.document.isDirty)

        // Move cursor
        val lineLengths = state.lineLengths()
        val newCursor = state.cursor.primary.moveTo(Position(0, 9))
        state = state.withCursor(
            state.cursor.withPrimary(newCursor)
        )

        assertEquals(Position(0, 9), state.cursor.primary.position)

        // Make selection
        val selection = SelectionImpl(
            anchor = Position(0, 0),
            head = Position(0, 5)
        )
        state = state.withSelection(
            SelectionState(primary = selection)
        )

        assertTrue(state.selection.hasSelection())
        assertEquals("Hello", state.selection.primary.toRange().let { range ->
            state.document.content.substring(
                state.document.content.positionToOffset(range.start),
                state.document.content.positionToOffset(range.end)
            )
        })

        // Record undo
        val undoOp = EditOperation(
            type = EditType.INSERT,
            range = Range(Position(0, 7), Position(0, 17)),
            oldText = "",
            newText = "Beautiful ",
            cursorBefore = Position(0, 7),
            cursorAfter = Position(0, 17)
        )
        val newUndoManager = state.undoManager.record(undoOp)
        state = state.withUndoManager(newUndoManager)

        assertTrue(state.undoManager.canUndo())

        // Undo
        val undoResult = state.undoManager.undo()
        assertNotNull(undoResult)
        assertEquals(EditType.DELETE, undoResult!!.type)
        assertEquals("Beautiful ", undoResult.oldText)
    }

    @Test
    fun `multiple cursor editing`() {
        val state = EditorState(
            document = DocumentState(
                content = TextBufferFactory.fromText("Line 1\nLine 2\nLine 3")
            ),
            cursor = CursorState.from(
                CursorManagerImpl(CursorImpl(Position(0, 0)))
                    .addCursor(Position(1, 0))
                    .addCursor(Position(2, 0))
            )
        )

        assertEquals(3, state.cursor.allCursors.size)

        // Insert at all cursors
        val lineLengths = state.lineLengths()
        val newCursors = state.cursor.toCursorManager().operateAll { cursor ->
            val buffer = state.document.content
            val offset = buffer.positionToOffset(cursor.position)
            val newBuffer = buffer.insert(offset, "// ")
            cursor.moveTo(
                buffer.offsetToPosition(offset + 3)
            )
        }

        assertEquals(3, newCursors.allCursors.size)
    }

    @Test
    fun `search and replace workflow`() {
        val state = EditorState(
            document = DocumentState(
                content = TextBufferFactory.fromText("Hello, World! Hello, Universe!")
            )
        )

        // Search for "Hello"
        val text = state.document.content.getText()
        val matches = mutableListOf<Pair<Int, Int>>()
        var index = 0
        while (true) {
            val found = text.indexOf("Hello", index)
            if (found == -1) break
            matches.add(Pair(found, found + 5))
            index = found + 5
        }

        assertEquals(2, matches.size)

        // Replace first match
        val firstMatch = matches[0]
        val newBuffer = state.document.content
            .delete(firstMatch.first, firstMatch.second - firstMatch.first)
            .insert(firstMatch.first, "Hi")

        val newState = state.withDocument(
            state.document.withContent(newBuffer)
        )

        assertEquals("Hi, World! Hello, Universe!", newState.document.content.getText())
    }

    @Test
    fun `document lifecycle`() {
        // Create
        var state = EditorState(
            document = DocumentState(
                filePath = "/test/file.txt",
                content = TextBufferFactory.fromText("Initial content")
            )
        )

        // Open (already open in this case)
        assertEquals("Initial content", state.document.content.getText())
        assertFalse(state.document.isDirty)

        // Edit
        val newBuffer = state.document.content.insert(8, "modified ")
        state = state.withDocument(
            state.document.withContent(newBuffer)
        )
        assertTrue(state.document.isDirty)
        assertEquals("Initial modified content", state.document.content.getText())

        // Save
        state = state.withDocument(state.document.saved())
        assertFalse(state.document.isDirty)
    }

    @Test
    fun `viewport scrolling`() {
        val lines = (1..100).joinToString("\n") { "Line $it" }
        var state = EditorState(
            document = DocumentState(
                content = TextBufferFactory.fromText(lines)
            ),
            viewport = ViewportState(
                topLine = 0,
                height = 400f,
                lineHeight = 20f
            )
        )

        assertEquals(20, state.viewport.visibleLines())

        // Scroll down
        state = state.withViewport(state.viewport.scrollTo(50))
        assertEquals(50, state.viewport.topLine)
        assertEquals(70, state.viewport.bottomLine())

        // Scroll to cursor
        val cursorLine = 75
        state = state.withCursor(
            state.cursor.withPrimary(CursorImpl(Position(cursorLine, 0)))
        )

        // Viewport should adjust to show cursor
        val visibleTop = state.viewport.topLine
        val visibleBottom = state.viewport.bottomLine()
        assertTrue(cursorLine in visibleTop until visibleBottom)
    }

    @Test
    fun `settings affect editor`() {
        var state = EditorState(
            settings = EditorSettings(
                fontSize = 14f,
                tabSize = 4,
                useSpaces = true
            )
        )

        assertEquals("    ", state.settings.indentation())

        // Change to tabs
        state = state.withSettings(
            state.settings.copy(useSpaces = false)
        )
        assertEquals("\t", state.settings.indentation())

        // Change tab size
        state = state.withSettings(
            state.settings.copy(tabSize = 2, useSpaces = true)
        )
        assertEquals("  ", state.settings.indentation())
    }

    @Test
    fun `large file handling`() {
        val lines = (1..10000).map { "Line $it with some content to make it realistic" }
        val text = lines.joinToString("\n")

        val state = EditorState(
            document = DocumentState(
                content = TextBufferFactory.fromText(text)
            )
        )

        assertEquals(10000, state.document.content.getLineCount())

        // Edit at beginning
        val newBuffer = state.document.content.insert(0, "// Header\n")
        val newState = state.withDocument(
            state.document.withContent(newBuffer)
        )

        assertEquals(10001, newState.document.content.getLineCount())
        assertEquals("// Header", newState.document.content.getLine(0))
    }
}
