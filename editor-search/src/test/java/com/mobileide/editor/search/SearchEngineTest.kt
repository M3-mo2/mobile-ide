package com.mobileide.editor.search

import com.mobileide.editor.core.Position
import com.mobileide.editor.core.TextBufferFactory
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SearchEngine.
 */
class SearchEngineTest {

    private val searchEngine = SearchEngineImpl()

    @Test
    fun `search for text`() {
        val buffer = TextBufferFactory.fromText("Hello, World! Hello, Universe!")
        val result = searchEngine.search(buffer, "Hello", SearchOptions())

        assertTrue(result.hasMatches())
        assertEquals(2, result.totalMatches())
    }

    @Test
    fun `search with case sensitivity`() {
        val buffer = TextBufferFactory.fromText("Hello, hello, HELLO")
        val result = searchEngine.search(buffer, "hello", SearchOptions(isCaseSensitive = true))

        assertTrue(result.hasMatches())
        assertEquals(1, result.totalMatches())
    }

    @Test
    fun `search with regex`() {
        val buffer = TextBufferFactory.fromText("Hello 123 World 456")
        val result = searchEngine.search(buffer, "\\d+", SearchOptions(isRegex = true))

        assertTrue(result.hasMatches())
        assertEquals(2, result.totalMatches())
    }

    @Test
    fun `search empty query returns no matches`() {
        val buffer = TextBufferFactory.fromText("Hello, World!")
        val result = searchEngine.search(buffer, "", SearchOptions())

        assertFalse(result.hasMatches())
        assertEquals(0, result.totalMatches())
    }

    @Test
    fun `search non-existent text`() {
        val buffer = TextBufferFactory.fromText("Hello, World!")
        val result = searchEngine.search(buffer, "xyz", SearchOptions())

        assertFalse(result.hasMatches())
    }

    @Test
    fun `find next match`() {
        val buffer = TextBufferFactory.fromText("Hello, World! Hello, Universe!")
        val result = searchEngine.search(buffer, "Hello", SearchOptions())

        val firstMatch = result.currentMatch()
        assertNotNull(firstMatch)
        assertEquals(Position(0, 0), firstMatch!!.start)

        val nextMatch = searchEngine.findNext(result)
        assertNotNull(nextMatch)
        assertEquals(Position(0, 14), nextMatch!!.start)
    }

    @Test
    fun `find previous match`() {
        val buffer = TextBufferFactory.fromText("Hello, World! Hello, Universe!")
        val result = searchEngine.search(buffer, "Hello", SearchOptions())

        val firstMatch = result.currentMatch()
        assertNotNull(firstMatch)

        val previousMatch = searchEngine.findPrevious(result)
        assertNotNull(previousMatch)
        // Should wrap around to last match
        assertEquals(Position(0, 14), previousMatch!!.start)
    }

    @Test
    fun `replace match`() {
        val match = Match(
            start = Position(0, 0),
            end = Position(0, 5),
            text = "Hello"
        )
        val operation = searchEngine.replace(match, "Hi")

        assertEquals("Hello", operation.oldText)
        assertEquals("Hi", operation.newText)
    }

    @Test
    fun `replace all`() {
        val buffer = TextBufferFactory.fromText("Hello, World! Hello, Universe!")
        val count = searchEngine.replaceAll(buffer, "Hello", "Hi", SearchOptions())

        assertEquals(2, count)
    }

    @Test
    fun `match to range`() {
        val match = Match(
            start = Position(0, 0),
            end = Position(0, 5),
            text = "Hello"
        )
        val range = match.toRange()

        assertEquals(Position(0, 0), range.start)
        assertEquals(Position(0, 5), range.end)
    }

    @Test
    fun `search result with no matches`() {
        val result = SearchResult(emptyList())
        assertNull(result.currentMatch())
        assertFalse(result.hasMatches())
        assertEquals(0, result.totalMatches())
    }

    @Test
    fun `search result with one match`() {
        val match = Match(
            start = Position(0, 0),
            end = Position(0, 5),
            text = "Hello"
        )
        val result = SearchResult(listOf(match))
        assertEquals(match, result.currentMatch())
        assertTrue(result.hasMatches())
        assertEquals(1, result.totalMatches())
    }
}
