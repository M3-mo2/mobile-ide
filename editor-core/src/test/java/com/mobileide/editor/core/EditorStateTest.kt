package com.mobileide.editor.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for EditorState.
 */
class EditorStateTest {

    @Test
    fun `create default editor state`() {
        val state = EditorState()
        assertNotNull(state.document)
        assertNotNull(state.cursor)
        assertNotNull(state.selection)
        assertNotNull(state.viewport)
        assertNotNull(state.search)
        assertNotNull(state.settings)
        assertNotNull(state.undoManager)
    }

    @Test
    fun `editor state with document`() {
        val document = DocumentState(
            filePath = "/test/file.kt",
            content = TextBufferFactory.fromText("Hello, World!"),
            isDirty = true
        )
        val state = EditorState().withDocument(document)
        assertEquals("/test/file.kt", state.document.filePath)
        assertTrue(state.document.isDirty)
    }

    @Test
    fun `editor state with cursor`() {
        val cursor = CursorState(
            primary = CursorImpl(Position(5, 10))
        )
        val state = EditorState().withCursor(cursor)
        assertEquals(Position(5, 10), state.cursor.primary.position)
    }

    @Test
    fun `editor state with selection`() {
        val selection = SelectionState(
            primary = SelectionImpl(Position(0, 0), Position(0, 5))
        )
        val state = EditorState().withSelection(selection)
        assertTrue(state.selection.hasSelection())
    }

    @Test
    fun `editor state with viewport`() {
        val viewport = ViewportState(topLine = 10, leftColumn = 5)
        val state = EditorState().withViewport(viewport)
        assertEquals(10, state.viewport.topLine)
        assertEquals(5, state.viewport.leftColumn)
    }

    @Test
    fun `editor state with search`() {
        val search = SearchState(query = "test", totalMatches = 5)
        val state = EditorState().withSearch(search)
        assertEquals("test", state.search.query)
        assertEquals(5, state.search.totalMatches)
    }

    @Test
    fun `editor state with settings`() {
        val settings = EditorSettings(fontSize = 16f, theme = "light")
        val state = EditorState().withSettings(settings)
        assertEquals(16f, state.settings.fontSize)
        assertEquals("light", state.settings.theme)
    }

    @Test
    fun `line lengths calculation`() {
        val state = EditorState(
            document = DocumentState(
                content = TextBufferFactory.fromText("Hello\nWorld\nTest")
            )
        )
        val lineLengths = state.lineLengths()
        assertEquals(3, lineLengths.size)
        assertEquals(5, lineLengths[0]) // "Hello"
        assertEquals(5, lineLengths[1]) // "World"
        assertEquals(4, lineLengths[2]) // "Test"
    }

    @Test
    fun `document state with content marks dirty`() {
        val document = DocumentState().withContent(TextBufferFactory.fromText("New content"))
        assertTrue(document.isDirty)
    }

    @Test
    fun `document state saved clears dirty`() {
        val document = DocumentState(
            content = TextBufferFactory.fromText("Content"),
            isDirty = true
        ).saved()
        assertFalse(document.isDirty)
    }

    @Test
    fun `viewport visible lines calculation`() {
        val viewport = ViewportState(
            height = 400f,
            lineHeight = 20f
        )
        assertEquals(20, viewport.visibleLines())
    }

    @Test
    fun `viewport scroll to`() {
        val viewport = ViewportState(topLine = 0)
        val scrolled = viewport.scrollTo(50)
        assertEquals(50, scrolled.topLine)
    }

    @Test
    fun `viewport scroll horizontal`() {
        val viewport = ViewportState(leftColumn = 0)
        val scrolled = viewport.scrollHorizontal(10)
        assertEquals(10, scrolled.leftColumn)
    }

    @Test
    fun `cursor state to cursor manager`() {
        val cursorState = CursorState(
            primary = CursorImpl(Position(0, 0)),
            secondary = listOf(CursorImpl(Position(1, 5)))
        )
        val manager = cursorState.toCursorManager()
        assertEquals(2, manager.allCursors.size)
    }

    @Test
    fun `selection state to selection manager`() {
        val selectionState = SelectionState(
            primary = SelectionImpl(Position(0, 0), Position(0, 5)),
            secondary = listOf(SelectionImpl(Position(1, 0), Position(1, 10)))
        )
        val manager = selectionState.toSelectionManager()
        assertEquals(2, manager.allSelections.size)
    }

    @Test
    fun `search state has query`() {
        val search = SearchState(query = "test")
        assertTrue(search.hasQuery())
    }

    @Test
    fun `search state no query`() {
        val search = SearchState()
        assertFalse(search.hasQuery())
    }

    @Test
    fun `editor settings indentation spaces`() {
        val settings = EditorSettings(useSpaces = true, tabSize = 4)
        assertEquals("    ", settings.indentation())
    }

    @Test
    fun `editor settings indentation tabs`() {
        val settings = EditorSettings(useSpaces = false)
        assertEquals("\t", settings.indentation())
    }
}
