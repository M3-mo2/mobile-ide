package com.mobileide.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileide.editor.core.EditorState
import com.mobileide.editor.core.Position

/**
 * Main editor screen composable with real text editing.
 */
@Composable
fun EditorScreen(
    state: EditorState,
    onEdit: (String, Position, Position) -> Unit,
    onCursorMove: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = Color(0xFFBBBBBB)
    val backgroundColor = Color(0xFF1E1E1E)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar
            EditorToolbar(
                fileName = state.document.filePath?.substringAfterLast('/') ?: "Untitled",
                isDirty = state.document.isDirty,
                modifier = Modifier.fillMaxWidth()
            )

            // Main editor area
            Row(modifier = Modifier.weight(1f)) {
                // Line numbers gutter
                if (state.settings.showLineNumbers) {
                    Gutter(
                        lineCount = state.document.content.getLineCount(),
                        topLine = state.viewport.topLine,
                        visibleLines = state.viewport.visibleLines(),
                        cursorLine = state.cursor.primary.position.line,
                        modifier = Modifier.width(48.dp)
                    )
                }

                // Text editor
                EditorTextArea(
                    state = state,
                    onEdit = onEdit,
                    onCursorMove = onCursorMove,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Real text editor area using BasicTextField for actual editing.
 */
@Composable
private fun EditorTextArea(
    state: EditorState,
    onEdit: (String, Position, Position) -> Unit,
    onCursorMove: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = Color(0xFFBBBBBB)
    val backgroundColor = Color(0xFF1E1E1E)
    val cursorColor = Color(0xFFAEAFAD)

    // Convert TextBuffer to string for display
    val fullText = remember(state.document.content) {
        state.document.content.getText()
    }

    var textValue by remember(fullText) { mutableStateOf(fullText) }

    // Update text when document changes externally
    androidx.compose.runtime.LaunchedEffect(fullText) {
        if (textValue != fullText) {
            textValue = fullText
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        BasicTextField(
            value = textValue,
            onValueChange = { newText ->
                textValue = newText
                // Calculate what changed and send edit event
                val oldText = fullText
                if (newText != oldText) {
                    // Simple approach: replace entire content
                    val startPos = Position(0, 0)
                    val endPos = Position(
                        line = oldText.lines().size - 1,
                        column = oldText.lines().lastOrNull()?.length ?: 0
                    )
                    onEdit(newText, startPos, endPos)
                }
            },
            modifier = Modifier.fillMaxSize(),
            textStyle = TextStyle(
                fontSize = state.settings.fontSize.sp,
                fontFamily = FontFamily.Monospace,
                color = textColor,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(cursorColor),
            decorationBox = { innerTextField ->
                innerTextField()
            }
        )
    }
}

/**
 * Editor toolbar composable.
 */
@Composable
private fun EditorToolbar(
    fileName: String,
    isDirty: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(48.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = if (isDirty) "$fileName *" else fileName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}

/**
 * Line numbers gutter composable.
 */
@Composable
private fun Gutter(
    lineCount: Int,
    topLine: Int,
    visibleLines: Int,
    cursorLine: Int,
    modifier: Modifier = Modifier
) {
    val lineHeight = with(LocalDensity.current) { 20.sp.toDp() }
    val gutterBackground = Color(0xFF2B2B2B)
    val gutterForeground = Color(0xFF606366)
    val activeLineForeground = Color(0xFFAAAAAA)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = gutterBackground
    ) {
        LazyColumn {
            val endLine = (topLine + visibleLines).coerceAtMost(lineCount)
            val items = (topLine until endLine).toList()

            itemsIndexed(items) { _, line ->
                val isActiveLine = line == cursorLine
                Text(
                    text = "${line + 1}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isActiveLine) activeLineForeground else gutterForeground
                    ),
                    modifier = Modifier
                        .height(lineHeight)
                        .padding(horizontal = 8.dp),
                    maxLines = 1
                )
            }
        }
    }
}
