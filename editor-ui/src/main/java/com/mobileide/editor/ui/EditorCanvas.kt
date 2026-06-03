package com.mobileide.editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileide.editor.core.EditorState
import com.mobileide.editor.core.Position
import com.mobileide.editor.core.Selection

/**
 * Advanced editor canvas with custom rendering.
 * Uses Compose Canvas for GPU-accelerated rendering of text, cursor, and selection.
 */
@Composable
fun EditorCanvasAdvanced(
    state: EditorState,
    onEdit: (String, Position, Position) -> Unit,
    onCursorMove: (Position) -> Unit,
    onSelectionChange: (Selection) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val lineHeight = with(density) { 20.sp.toDp() }
    val charWidth = with(density) { 9.sp.toDp() }
    val textStyle = TextStyle(
        fontSize = 14.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFBBBBBB)
    )
    val selectionColor = Color(0xFF264F78)
    val cursorColor = Color(0xFFAEAFAD)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state) {
                    detectTapGestures { offset ->
                        val line = (offset.y / lineHeight.toPx()).toInt()
                            .coerceIn(0, state.document.content.getLineCount() - 1)
                        val column = (offset.x / charWidth.toPx()).toInt()
                            .coerceIn(0, state.document.content.getLine(line).length)
                        onCursorMove(Position(line, column))
                    }
                }
                .pointerInput(state) {
                    detectDragGestures { change, _ ->
                        val offset = change.position
                        val line = (offset.y / lineHeight.toPx()).toInt()
                            .coerceIn(0, state.document.content.getLineCount() - 1)
                        val column = (offset.x / charWidth.toPx()).toInt()
                            .coerceIn(0, state.document.content.getLine(line).length)
                        onSelectionChange(
                            com.mobileide.editor.core.SelectionImpl(
                                state.cursor.primary.position,
                                Position(line, column)
                            )
                        )
                    }
                }
        ) {
            val topLine = state.viewport.topLine
            val visibleLines = state.viewport.visibleLines()
            val endLine = (topLine + visibleLines).coerceAtMost(state.document.content.getLineCount())

            for (line in topLine until endLine) {
                val y = (line - topLine) * lineHeight.toPx()
                val lineText = state.document.content.getLine(line)

                // Draw selection background if this line is in selection
                val selection = state.selection.primary
                if (!selection.isEmpty && line >= selection.start.line && line <= selection.end.line) {
                    val selectionStartX = if (line == selection.start.line) {
                        selection.start.column * charWidth.toPx()
                    } else {
                        0f
                    }
                    val selectionEndX = if (line == selection.end.line) {
                        selection.end.column * charWidth.toPx()
                    } else {
                        lineText.length * charWidth.toPx()
                    }

                    drawRect(
                        color = selectionColor,
                        topLeft = Offset(selectionStartX, y),
                        size = Size(selectionEndX - selectionStartX, lineHeight.toPx())
                    )
                }

                // Draw line text
                if (lineText.isNotEmpty()) {
                    val textLayoutResult = textMeasurer.measure(lineText, textStyle)
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(0f, y)
                    )
                }

                // Draw cursor if on this line
                if (line == state.cursor.primary.position.line) {
                    val cursorX = state.cursor.primary.position.column * charWidth.toPx()
                    drawLine(
                        color = cursorColor,
                        start = Offset(cursorX, y),
                        end = Offset(cursorX, y + lineHeight.toPx()),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}

/**
 * Virtual scrolling editor using LazyColumn.
 * More efficient for very large files.
 */
@Composable
fun VirtualEditor(
    state: EditorState,
    onCursorMove: (Position) -> Unit,
    onSelectionChange: (Selection) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.viewport.topLine
    )
    val lineHeight = with(LocalDensity.current) { 20.sp.toDp() }
    val textStyle = TextStyle(
        fontSize = 14.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFBBBBBB)
    )

    LaunchedEffect(listState.firstVisibleItemIndex) {
        // Update viewport state when scrolling
        // This would normally update the ViewModel
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = (0 until state.document.content.getLineCount()).toList(),
            key = { index, _ -> index }
        ) { index, line ->
            LineView(
                lineNumber = index,
                text = state.document.content.getLine(line),
                isCursorLine = index == state.cursor.primary.position.line,
                cursorColumn = if (index == state.cursor.primary.position.line) {
                    state.cursor.primary.position.column
                } else null,
                selection = if (index >= state.selection.primary.start.line &&
                    index <= state.selection.primary.end.line) {
                    state.selection.primary
                } else null,
                textStyle = textStyle,
                lineHeight = lineHeight,
                onTap = { column -> onCursorMove(Position(index, column)) }
            )
        }
    }
}

/**
 * Single line view composable.
 */
@Composable
private fun LineView(
    lineNumber: Int,
    text: String,
    isCursorLine: Boolean,
    cursorColumn: Int?,
    selection: Selection?,
    textStyle: TextStyle,
    lineHeight: androidx.compose.ui.unit.Dp,
    onTap: (Int) -> Unit
) {
    val backgroundColor = if (isCursorLine) {
        Color(0xFF2B2B2B)
    } else {
        Color(0xFF1E1E1E)
    }

    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .height(lineHeight)
            .padding(horizontal = 8.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = textStyle,
            modifier = Modifier.fillMaxSize()
        )
    }
}
