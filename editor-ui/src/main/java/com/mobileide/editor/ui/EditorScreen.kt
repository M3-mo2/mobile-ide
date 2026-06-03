package com.mobileide.editor.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileide.editor.core.EditorState
import com.mobileide.editor.core.Position
import com.mobileide.editor.core.TextBufferFactory

/**
 * Main editor screen composable.
 * This is the root composable for the editor UI.
 */
@Composable
fun EditorScreen(
    state: EditorState,
    onEdit: (String, Position, Position) -> Unit,
    onCursorMove: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
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

                // Text editor canvas
                EditorCanvas(
                    state = state,
                    onEdit = onEdit,
                    onCursorMove = onCursorMove,
                    modifier = Modifier.weight(1f)
                )
            }

            // Status bar
            StatusBar(
                line = state.cursor.primary.position.line + 1,
                column = state.cursor.primary.position.column + 1,
                lineCount = state.document.content.getLineCount(),
                encoding = state.document.encoding,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterStart)
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
        Column {
            val endLine = (topLine + visibleLines).coerceAtMost(lineCount)
            for (line in topLine until endLine) {
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
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}

/**
 * Editor canvas composable.
 * This is where the text is rendered.
 */
@Composable
private fun EditorCanvas(
    state: EditorState,
    onEdit: (String, Position, Position) -> Unit,
    onCursorMove: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = Color(0xFFBBBBBB)
    val backgroundColor = Color(0xFF1E1E1E)
    val lineHeight = with(LocalDensity.current) { 20.sp.toDp() }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column {
            val endLine = (state.viewport.topLine + state.viewport.visibleLines())
                .coerceAtMost(state.document.content.getLineCount())

            for (line in state.viewport.topLine until endLine) {
                val lineText = state.document.content.getLine(line)
                Text(
                    text = lineText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = textColor
                    ),
                    modifier = Modifier.height(lineHeight)
                )
            }
        }
    }
}

/**
 * Status bar composable.
 */
@Composable
internal fun StatusBar(
    line: Int,
    column: Int,
    lineCount: Int,
    encoding: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Ln $line, Col $column | $encoding | Lines: $lineCount",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd)
            )
        }
    }
}
