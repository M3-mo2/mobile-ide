package com.mobileide.editor.highlight

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Represents a token type for syntax highlighting.
 */
enum class TokenType {
    KEYWORD,
    STRING,
    NUMBER,
    COMMENT,
    OPERATOR,
    IDENTIFIER,
    TYPE,
    FUNCTION,
    VARIABLE,
    PUNCTUATION,
    TAG,
    ATTRIBUTE,
    PLAIN
}

/**
 * Represents a token with its type and position.
 */
data class Token(
    val type: TokenType,
    val start: Int,
    val end: Int,
    val text: String
) {
    fun length(): Int = end - start
}

/**
 * Represents a syntax highlighting pattern.
 */
data class HighlightPattern(
    val regex: Regex,
    val tokenType: TokenType,
    val priority: Int = 0
)

/**
 * Represents a language definition for syntax highlighting.
 */
data class LanguageDefinition(
    val id: String,
    val name: String,
    val fileExtensions: List<String>,
    val patterns: List<HighlightPattern>
)

/**
 * Represents a color theme for syntax highlighting.
 */
data class Theme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val colors: EditorColors,
    val tokenStyles: Map<TokenType, TokenStyle>
) {
    /**
     * Returns the style for a given token type.
     */
    fun styleFor(tokenType: TokenType): TokenStyle {
        return tokenStyles[tokenType] ?: TokenStyle()
    }
}

/**
 * Editor colors.
 */
data class EditorColors(
    val background: Color = Color(0xFF1E1E1E),
    val foreground: Color = Color(0xFFBBBBBB),
    val gutterBackground: Color = Color(0xFF2B2B2B),
    val gutterForeground: Color = Color(0xFF606366),
    val lineHighlight: Color = Color(0xFF2B2B2B),
    val selection: Color = Color(0xFF264F78),
    val cursor: Color = Color(0xFFAEAFAD)
)

/**
 * Token style.
 */
data class TokenStyle(
    val foreground: Color? = null,
    val background: Color? = null,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
) {
    /**
     * Converts this token style to a Compose TextStyle.
     */
    fun toTextStyle(baseStyle: TextStyle): TextStyle {
        return baseStyle.copy(
            color = foreground ?: baseStyle.color,
            fontWeight = if (isBold) FontWeight.Bold else baseStyle.fontWeight,
            fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else baseStyle.fontStyle
        )
    }
}

/**
 * Interface for syntax highlighter.
 */
interface Highlighter {
    /**
     * Highlights the given text.
     * Returns a list of tokens with their types.
     */
    fun highlight(text: String, language: String): List<Token>

    /**
     * Returns the list of supported languages.
     */
    fun getSupportedLanguages(): List<String>

    /**
     * Detects the language from a file name.
     */
    fun detectLanguage(fileName: String): String?
}

/**
 * Regex-based highlighter for V1.
 */
class RegexHighlighter : Highlighter {

    private val languages = mutableMapOf<String, LanguageDefinition>()
    private val themes = mutableMapOf<String, Theme>()

    init {
        // Register built-in languages
        registerKotlin()
        registerJava()
        registerJavaScript()
        registerPython()
        registerJson()
        registerXml()
        registerMarkdown()

        // Register built-in themes
        registerDarkTheme()
        registerLightTheme()
    }

    override fun highlight(text: String, language: String): List<Token> {
        val langDef = languages[language] ?: return emptyList()
        val tokens = mutableListOf<Token>()
        val lines = text.lines()
        var offset = 0

        for (line in lines) {
            val lineTokens = highlightLine(line, langDef, offset)
            tokens.addAll(lineTokens)
            offset += line.length + 1 // +1 for newline
        }

        return tokens
    }

    private fun highlightLine(line: String, langDef: LanguageDefinition, lineOffset: Int): List<Token> {
        val tokens = mutableListOf<Token>()
        val matchedRanges = mutableListOf<IntRange>()

        // Sort patterns by priority (highest first)
        val sortedPatterns = langDef.patterns.sortedByDescending { it.priority }

        for (pattern in sortedPatterns) {
            pattern.regex.findAll(line).forEach { matchResult ->
                val range = matchResult.range

                // Check if this range overlaps with already matched ranges
                val overlaps = matchedRanges.any { it.overlaps(range) }
                if (!overlaps) {
                    tokens.add(
                        Token(
                            type = pattern.tokenType,
                            start = lineOffset + range.first,
                            end = lineOffset + range.last + 1,
                            text = matchResult.value
                        )
                    )
                    matchedRanges.add(range)
                }
            }
        }

        return tokens.sortedBy { it.start }
    }

    override fun getSupportedLanguages(): List<String> {
        return languages.keys.toList()
    }

    override fun detectLanguage(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "")
        return languages.values.find { language ->
            language.fileExtensions.any { ext ->
                ext.removePrefix(".") == extension
            }
        }?.id
    }

    /**
     * Registers a language definition.
     */
    fun registerLanguage(language: LanguageDefinition) {
        languages[language.id] = language
    }

    /**
     * Registers a theme.
     */
    fun registerTheme(theme: Theme) {
        themes[theme.id] = theme
    }

    /**
     * Returns a theme by ID.
     */
    fun getTheme(id: String): Theme? = themes[id]

    private fun registerKotlin() {
        registerLanguage(LanguageDefinition(
            id = "kotlin",
            name = "Kotlin",
            fileExtensions = listOf(".kt", ".kts"),
            patterns = listOf(
                HighlightPattern(
                    regex = Regex("\\b(fun|val|var|class|interface|object|package|import|return|if|else|when|for|while|do|try|catch|throw|true|false|null|this|super|is|as|in|out|by|lateinit|data|sealed|open|abstract|override|companion|operator|inline|crossinline|noinline|reified|suspend|expect|actual)\\b"),
                    tokenType = TokenType.KEYWORD,
                    priority = 100
                ),
                HighlightPattern(
                    regex = Regex("\".*?\"|\"\"\"[\\s\\S]*?\"\"\""),
                    tokenType = TokenType.STRING,
                    priority = 90
                ),
                HighlightPattern(
                    regex = Regex("//.*$|/\\*[\\s\\S]*?\\*/"),
                    tokenType = TokenType.COMMENT,
                    priority = 80
                ),
                HighlightPattern(
                    regex = Regex("\\b\\d+\\b"),
                    tokenType = TokenType.NUMBER,
                    priority = 70
                ),
                HighlightPattern(
                    regex = Regex("[+\\-*/%=!<>&|\\^~\\.\\?\\:;]"),
                    tokenType = TokenType.OPERATOR,
                    priority = 60
                ),
                HighlightPattern(
                    regex = Regex("\\b[A-Z][a-zA-Z0-9_]*\\b"),
                    tokenType = TokenType.TYPE,
                    priority = 50
                ),
                HighlightPattern(
                    regex = Regex("\\b[a-z][a-zA-Z0-9_]*\\s*(?=\\()"),
                    tokenType = TokenType.FUNCTION,
                    priority = 40
                ),
                HighlightPattern(
                    regex = Regex("[{}\\[\\]()]"),
                    tokenType = TokenType.PUNCTUATION,
                    priority = 30
                )
            )
        ))
    }

    private fun registerJava() {
        registerLanguage(LanguageDefinition(
            id = "java",
            name = "Java",
            fileExtensions = listOf(".java"),
            patterns = listOf(
                HighlightPattern(
                    regex = Regex("\\b(public|private|protected|static|final|abstract|class|interface|extends|implements|import|package|return|if|else|for|while|do|switch|case|break|continue|try|catch|finally|throw|new|this|super|true|false|null|void|int|long|double|float|boolean|char|byte|short|enum|assert|synchronized|volatile|transient|native|strictfp|goto|const)\\b"),
                    tokenType = TokenType.KEYWORD,
                    priority = 100
                ),
                HighlightPattern(
                    regex = Regex("\".*?\""),
                    tokenType = TokenType.STRING,
                    priority = 90
                ),
                HighlightPattern(
                    regex = Regex("//.*$|/\\*[\\s\\S]*?\\*/"),
                    tokenType = TokenType.COMMENT,
                    priority = 80
                ),
                HighlightPattern(
                    regex = Regex("\\b\\d+\\b"),
                    tokenType = TokenType.NUMBER,
                    priority = 70
                ),
                HighlightPattern(
                    regex = Regex("[+\\-*/%=!<>&|\\^~\\.\\?\\:;]"),
                    tokenType = TokenType.OPERATOR,
                    priority = 60
                ),
                HighlightPattern(
                    regex = Regex("\\b[A-Z][a-zA-Z0-9_]*\\b"),
                    tokenType = TokenType.TYPE,
                    priority = 50
                ),
                HighlightPattern(
                    regex = Regex("[{}\\[\\]()]"),
                    tokenType = TokenType.PUNCTUATION,
                    priority = 30
                )
            )
        ))
    }

    private fun registerJavaScript() {
        registerLanguage(LanguageDefinition(
            id = "javascript",
            name = "JavaScript",
            fileExtensions = listOf(".js", ".jsx", ".mjs"),
            patterns = listOf(
                HighlightPattern(
                    regex = Regex("\\b(function|var|let|const|return|if|else|for|while|do|switch|case|break|continue|try|catch|finally|throw|new|this|super|true|false|null|undefined|typeof|instanceof|in|of|import|export|default|class|extends|async|await|yield|debugger|with|delete|void)\\b"),
                    tokenType = TokenType.KEYWORD,
                    priority = 100
                ),
                HighlightPattern(
                    regex = Regex("\".*?\"|'.*?'|`[\\s\\S]*?`"),
                    tokenType = TokenType.STRING,
                    priority = 90
                ),
                HighlightPattern(
                    regex = Regex("//.*$|/\\*[\\s\\S]*?\\*/"),
                    tokenType = TokenType.COMMENT,
                    priority = 80
                ),
                HighlightPattern(
                    regex = Regex("\\b\\d+\\b"),
                    tokenType = TokenType.NUMBER,
                    priority = 70
                ),
                HighlightPattern(
                    regex = Regex("[+\\-*/%=!<>&|\\^~\\.\\?\\:;]"),
                    tokenType = TokenType.OPERATOR,
                    priority = 60
                ),
                HighlightPattern(
                    regex = Regex("[{}\\[\\]()]"),
                    tokenType = TokenType.PUNCTUATION,
                    priority = 30
                )
            )
        ))
    }

    private fun registerPython() {
        registerLanguage(LanguageDefinition(
            id = "python",
            name = "Python",
            fileExtensions = listOf(".py", ".pyw"),
            patterns = listOf(
                HighlightPattern(
                    regex = Regex("\\b(def|class|if|elif|else|for|while|return|import|from|as|try|except|finally|raise|with|pass|break|continue|lambda|yield|assert|del|global|nonlocal|True|False|None|and|or|not|in|is)\\b"),
                    tokenType = TokenType.KEYWORD,
                    priority = 100
                ),
                HighlightPattern(
                    regex = Regex("\".*?\"|'.*?'|\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''"),
                    tokenType = TokenType.STRING,
                    priority = 90
                ),
                HighlightPattern(
                    regex = Regex("#.*$"),
                    tokenType = TokenType.COMMENT,
                    priority = 80
                ),
                HighlightPattern(
                    regex = Regex("\\b\\d+\\b"),
                    tokenType = TokenType.NUMBER,
                    priority = 70
                ),
                HighlightPattern(
                    regex = Regex("[+\\-*/%=!<>&|\\^~\\.\\?\\:;]"),
                    tokenType = TokenType.OPERATOR,
                    priority = 60
                ),
                HighlightPattern(
                    regex = Regex("\\b[A-Z][a-zA-Z0-9_]*\\b"),
                    tokenType = TokenType.TYPE,
                    priority = 50
                ),
                HighlightPattern(
                    regex = Regex("[{}\\[\\]()]"),
                    tokenType = TokenType.PUNCTUATION,
                    priority = 30
                )
            )
        ))
    }

    private fun registerJson() {
        registerLanguage(LanguageDefinition(
            id = "json",
            name = "JSON",
            fileExtensions = listOf(".json"),
            patterns = listOf(
                HighlightPattern(
                    regex = Regex("\".*?\""),
                    tokenType = TokenType.STRING,
                    priority = 90
                ),
                HighlightPattern(
                    regex = Regex("\\b(true|false|null)\\b"),
                    tokenType = TokenType.KEYWORD,
                    priority = 80
                ),
                HighlightPattern(
                    regex = Regex("\\b\\d+\\b"),
                    tokenType = TokenType.NUMBER,
                    priority = 70
                ),
                HighlightPattern(
                    regex = Regex("[{}\\[\\](),:]"),
                    tokenType = TokenType.PUNCTUATION,
                    priority = 30
                )
            )
        ))
    }

    private fun registerXml() {
        registerLanguage(LanguageDefinition(
            id = "xml",
            name = "XML",
            fileExtensions = listOf(".xml", ".html", ".htm"),
            patterns = listOf(
                HighlightPattern(
                    regex = Regex("<\\?xml.*?\\?>"),
                    tokenType = TokenType.TAG,
                    priority = 100
                ),
                HighlightPattern(
                    regex = Regex("<\\/?[a-zA-Z][a-zA-Z0-9]*.*?\\/?>"),
                    tokenType = TokenType.TAG,
                    priority = 90
                ),
                HighlightPattern(
                    regex = Regex("\\b[a-zA-Z-]+(?==)"),
                    tokenType = TokenType.ATTRIBUTE,
                    priority = 80
                ),
                HighlightPattern(
                    regex = Regex("\".*?\"|'.*?'"),
                    tokenType = TokenType.STRING,
                    priority = 70
                ),
                HighlightPattern(
                    regex = Regex("<!--[\\s\\S]*?-->"),
                    tokenType = TokenType.COMMENT,
                    priority = 60
                )
            )
        ))
    }

    private fun registerMarkdown() {
        registerLanguage(LanguageDefinition(
            id = "markdown",
            name = "Markdown",
            fileExtensions = listOf(".md", ".markdown"),
            patterns = listOf(
                HighlightPattern(
                    regex = Regex("^#{1,6}\\s+.*$"),
                    tokenType = TokenType.KEYWORD,
                    priority = 100
                ),
                HighlightPattern(
                    regex = Regex("\\*\\*.*?\\*\\*|__.*?__"),
                    tokenType = TokenType.OPERATOR,
                    priority = 90
                ),
                HighlightPattern(
                    regex = Regex("\\*.*?\\*|_.*?_"),
                    tokenType = TokenType.OPERATOR,
                    priority = 80
                ),
                HighlightPattern(
                    regex = Regex("`.*?`"),
                    tokenType = TokenType.STRING,
                    priority = 70
                ),
                HighlightPattern(
                    regex = Regex("```[\\s\\S]*?```"),
                    tokenType = TokenType.STRING,
                    priority = 60
                ),
                HighlightPattern(
                    regex = Regex("\\[.*?\\]\\(.*?\\)"),
                    tokenType = TokenType.FUNCTION,
                    priority = 50
                )
            )
        ))
    }

    private fun registerDarkTheme() {
        registerTheme(Theme(
            id = "dark",
            name = "Dark",
            isDark = true,
            colors = EditorColors(),
            tokenStyles = mapOf(
                TokenType.KEYWORD to TokenStyle(foreground = Color(0xFFCC7832)),
                TokenType.STRING to TokenStyle(foreground = Color(0xFF6A8759)),
                TokenType.NUMBER to TokenStyle(foreground = Color(0xFF6897BB)),
                TokenType.COMMENT to TokenStyle(foreground = Color(0xFF808080), isItalic = true),
                TokenType.OPERATOR to TokenStyle(foreground = Color(0xFFA9B7C6)),
                TokenType.IDENTIFIER to TokenStyle(foreground = Color(0xFFA9B7C6)),
                TokenType.TYPE to TokenStyle(foreground = Color(0xFFBBB529)),
                TokenType.FUNCTION to TokenStyle(foreground = Color(0xFFFFC66D)),
                TokenType.VARIABLE to TokenStyle(foreground = Color(0xFF9876AA)),
                TokenType.PUNCTUATION to TokenStyle(foreground = Color(0xFFA9B7C6)),
                TokenType.TAG to TokenStyle(foreground = Color(0xFFE8BF6A)),
                TokenType.ATTRIBUTE to TokenStyle(foreground = Color(0xFFBBB529)),
                TokenType.PLAIN to TokenStyle(foreground = Color(0xFFA9B7C6))
            )
        ))
    }

    private fun registerLightTheme() {
        registerTheme(Theme(
            id = "light",
            name = "Light",
            isDark = false,
            colors = EditorColors(
                background = Color(0xFFFFFFFF),
                foreground = Color(0xFF000000),
                gutterBackground = Color(0xFFF0F0F0),
                gutterForeground = Color(0xFF808080),
                lineHighlight = Color(0xFFF5F5F5),
                selection = Color(0xFFADD6FF),
                cursor = Color(0xFF000000)
            ),
            tokenStyles = mapOf(
                TokenType.KEYWORD to TokenStyle(foreground = Color(0xFF000080)),
                TokenType.STRING to TokenStyle(foreground = Color(0xFF008000)),
                TokenType.NUMBER to TokenStyle(foreground = Color(0xFF0000FF)),
                TokenType.COMMENT to TokenStyle(foreground = Color(0xFF808080), isItalic = true),
                TokenType.OPERATOR to TokenStyle(foreground = Color(0xFF000000)),
                TokenType.IDENTIFIER to TokenStyle(foreground = Color(0xFF000000)),
                TokenType.TYPE to TokenStyle(foreground = Color(0xFF267F99)),
                TokenType.FUNCTION to TokenStyle(foreground = Color(0xFF795E26)),
                TokenType.VARIABLE to TokenStyle(foreground = Color(0xFF001080)),
                TokenType.PUNCTUATION to TokenStyle(foreground = Color(0xFF000000)),
                TokenType.TAG to TokenStyle(foreground = Color(0xFF800000)),
                TokenType.ATTRIBUTE to TokenStyle(foreground = Color(0xFFFF0000)),
                TokenType.PLAIN to TokenStyle(foreground = Color(0xFF000000))
            )
        ))
    }

    /**
     * Extension function to check if two IntRanges overlap.
     */
    private fun IntRange.overlaps(other: IntRange): Boolean {
        return this.first <= other.last && other.first <= this.last
    }
}
