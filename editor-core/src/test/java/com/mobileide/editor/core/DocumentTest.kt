package com.mobileide.editor.core

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Document.
 */
class DocumentTest {

    @Test
    fun `create empty document`() {
        val doc = Document()
        assertNull(doc.filePath)
        assertEquals("", doc.content.getText())
        assertFalse(doc.isDirty)
    }

    @Test
    fun `create document with content`() {
        val doc = Document(
            filePath = "/test/file.kt",
            content = TextBufferFactory.fromText("Hello, World!")
        )
        assertEquals("/test/file.kt", doc.filePath)
        assertEquals("Hello, World!", doc.content.getText())
        assertFalse(doc.isDirty)
    }

    @Test
    fun `document becomes dirty after edit`() {
        val doc = Document(
            content = TextBufferFactory.fromText("Hello")
        )
        assertFalse(doc.isDirty)

        val newContent = doc.content.insert(5, ", World!")
        val newDoc = doc.withContent(newContent)
        assertTrue(newDoc.isDirty)
    }

    @Test
    fun `document saved clears dirty`() {
        val doc = Document(
            content = TextBufferFactory.fromText("Hello")
        )
        val edited = doc.withContent(doc.content.insert(5, ", World!"))
        assertTrue(edited.isDirty)

        val saved = edited.markSaved()
        assertFalse(saved.isDirty)
    }

    @Test
    fun `document with cursor`() {
        val doc = Document()
        val newCursor = CursorImpl(Position(5, 10))
        val newDoc = doc.withCursor(newCursor)
        assertEquals(Position(5, 10), newDoc.cursor.position)
    }

    @Test
    fun `document with selection`() {
        val doc = Document()
        val newSelection = SelectionImpl(Position(0, 0), Position(0, 5))
        val newDoc = doc.withSelection(newSelection)
        assertTrue(newDoc.selection.isEmpty.not())
    }

    @Test
    fun `document with undo manager`() {
        val doc = Document()
        val newUndoManager = UndoManagerImpl(maxHistory = 5000)
        val newDoc = doc.withUndoManager(newUndoManager)
        assertEquals(5000, (newDoc.undoManager as UndoManagerImpl).maxHistory)
    }

    @Test
    fun `document preserves encoding`() {
        val doc = Document(
            content = TextBufferFactory.fromText("Hello"),
            encoding = "UTF-16"
        )
        val newDoc = doc.withContent(TextBufferFactory.fromText("World"))
        assertEquals("UTF-16", newDoc.encoding)
    }

    @Test
    fun `document preserves language`() {
        val kotlinLang = Language("kotlin", "Kotlin", listOf(".kt", ".kts"))
        val doc = Document(
            content = TextBufferFactory.fromText("fun main() {}"),
            language = kotlinLang
        )
        val newDoc = doc.withContent(TextBufferFactory.fromText("class Test"))
        assertEquals("kotlin", newDoc.language?.id)
    }

    @Test
    fun `document preserves file path`() {
        val doc = Document(filePath = "/test/file.kt")
        val newDoc = doc.withContent(TextBufferFactory.fromText("New content"))
        assertEquals("/test/file.kt", newDoc.filePath)
    }

    @Test
    fun `document state transitions`() {
        // CREATED -> OPEN
        val doc = Document(filePath = "/test/file.kt", content = TextBufferFactory.fromText("Initial"))
        assertFalse(doc.isDirty)

        // OPEN -> MODIFIED
        val modified = doc.withContent(TextBufferFactory.fromText("Modified"))
        assertTrue(modified.isDirty)

        // MODIFIED -> SAVED
        val saved = modified.markSaved()
        assertFalse(saved.isDirty)

        // SAVED -> MODIFIED
        val modifiedAgain = saved.withContent(TextBufferFactory.fromText("Modified again"))
        assertTrue(modifiedAgain.isDirty)
    }

    @Test
    fun `multiple documents are independent`() {
        val doc1 = Document(content = TextBufferFactory.fromText("Doc 1"))
        val doc2 = Document(content = TextBufferFactory.fromText("Doc 2"))

        assertNotEquals(doc1.content.getText(), doc2.content.getText())

        val editedDoc1 = doc1.withContent(TextBufferFactory.fromText("Edited"))
        assertTrue(editedDoc1.isDirty)
        assertFalse(doc2.isDirty)
    }
}
