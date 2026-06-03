package com.mobileide.editor.highlight

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for RegexHighlighter.
 */
class HighlighterTest {

    @Test
    fun `create highlighter`() {
        val highlighter = RegexHighlighter()
        assertNotNull(highlighter)
    }

    @Test
    fun `get supported languages`() {
        val highlighter = RegexHighlighter()
        val languages = highlighter.getSupportedLanguages()
        assertTrue(languages.isNotEmpty())
        assertTrue(languages.contains("kotlin"))
        assertTrue(languages.contains("java"))
        assertTrue(languages.contains("javascript"))
        assertTrue(languages.contains("python"))
        assertTrue(languages.contains("json"))
        assertTrue(languages.contains("xml"))
        assertTrue(languages.contains("markdown"))
    }

    @Test
    fun `detect language from file extension`() {
        val highlighter = RegexHighlighter()
        assertEquals("kotlin", highlighter.detectLanguage("Main.kt"))
        assertEquals("kotlin", highlighter.detectLanguage("script.kts"))
        assertEquals("java", highlighter.detectLanguage("Main.java"))
        assertEquals("javascript", highlighter.detectLanguage("script.js"))
        assertEquals("python", highlighter.detectLanguage("script.py"))
        assertEquals("json", highlighter.detectLanguage("config.json"))
        assertEquals("xml", highlighter.detectLanguage("layout.xml"))
        assertEquals("markdown", highlighter.detectLanguage("README.md"))
    }

    @Test
    fun `highlight kotlin keywords`() {
        val highlighter = RegexHighlighter()
        val text = "fun main() { val x = 10 }"
        val tokens = highlighter.highlight(text, "kotlin")

        assertTrue(tokens.isNotEmpty())

        // Should find 'fun' keyword
        val funToken = tokens.find { it.text == "fun" }
        assertNotNull(funToken)
        assertEquals(TokenType.KEYWORD, funToken!!.type)

        // Should find 'val' keyword
        val valToken = tokens.find { it.text == "val" }
        assertNotNull(valToken)
        assertEquals(TokenType.KEYWORD, valToken!!.type)
    }

    @Test
    fun `highlight kotlin strings`() {
        val highlighter = RegexHighlighter()
        val text = "val greeting = \"Hello, World!\""
        val tokens = highlighter.highlight(text, "kotlin")

        val stringToken = tokens.find { it.text == "\"Hello, World!\"" }
        assertNotNull(stringToken)
        assertEquals(TokenType.STRING, stringToken!!.type)
    }

    @Test
    fun `highlight kotlin comments`() {
        val highlighter = RegexHighlighter()
        val text = "// This is a comment\nval x = 10"
        val tokens = highlighter.highlight(text, "kotlin")

        val commentToken = tokens.find { it.text == "// This is a comment" }
        assertNotNull(commentToken)
        assertEquals(TokenType.COMMENT, commentToken!!.type)
    }

    @Test
    fun `highlight kotlin numbers`() {
        val highlighter = RegexHighlighter()
        val text = "val x = 42"
        val tokens = highlighter.highlight(text, "kotlin")

        val numberToken = tokens.find { it.text == "42" }
        assertNotNull(numberToken)
        assertEquals(TokenType.NUMBER, numberToken!!.type)
    }

    @Test
    fun `highlight java keywords`() {
        val highlighter = RegexHighlighter()
        val text = "public class Main { public static void main() { } }"
        val tokens = highlighter.highlight(text, "java")

        val publicToken = tokens.find { it.text == "public" }
        assertNotNull(publicToken)
        assertEquals(TokenType.KEYWORD, publicToken!!.type)

        val classToken = tokens.find { it.text == "class" }
        assertNotNull(classToken)
        assertEquals(TokenType.KEYWORD, classToken!!.type)
    }

    @Test
    fun `highlight json`() {
        val highlighter = RegexHighlighter()
        val text = """{"name": "John", "age": 30, "active": true}"""
        val tokens = highlighter.highlight(text, "json")

        val stringTokens = tokens.filter { it.type == TokenType.STRING }
        assertTrue(stringTokens.isNotEmpty())

        val trueToken = tokens.find { it.text == "true" }
        assertNotNull(trueToken)
        assertEquals(TokenType.KEYWORD, trueToken!!.type)
    }

    @Test
    fun `highlight unknown language returns empty`() {
        val highlighter = RegexHighlighter()
        val tokens = highlighter.highlight("some text", "unknown")
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `detect language returns null for unknown extension`() {
        val highlighter = RegexHighlighter()
        assertNull(highlighter.detectLanguage("file.unknown"))
    }

    @Test
    fun `token positions are correct`() {
        val highlighter = RegexHighlighter()
        val text = "fun main()"
        val tokens = highlighter.highlight(text, "kotlin")

        val funToken = tokens.find { it.text == "fun" }
        assertNotNull(funToken)
        assertEquals(0, funToken!!.start)
        assertEquals(3, funToken.end)
    }

    @Test
    fun `dark theme exists`() {
        val highlighter = RegexHighlighter()
        val theme = highlighter.getTheme("dark")
        assertNotNull(theme)
        assertTrue(theme!!.isDark)
    }

    @Test
    fun `light theme exists`() {
        val highlighter = RegexHighlighter()
        val theme = highlighter.getTheme("light")
        assertNotNull(theme)
        assertFalse(theme!!.isDark)
    }

    @Test
    fun `theme has token styles`() {
        val highlighter = RegexHighlighter()
        val theme = highlighter.getTheme("dark")
        assertNotNull(theme)
        assertTrue(theme!!.tokenStyles.isNotEmpty())
        assertNotNull(theme.tokenStyles[TokenType.KEYWORD])
        assertNotNull(theme.tokenStyles[TokenType.STRING])
        assertNotNull(theme.tokenStyles[TokenType.COMMENT])
    }
}
