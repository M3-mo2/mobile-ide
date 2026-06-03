package com.mobileide.editor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileide.editor.core.EditorState

/**
 * Main IDE layout composable.
 * Combines the sidebar (file explorer), editor area, and toolbars.
 */
@Composable
fun IdeLayout(
    state: EditorState,
    onFileSelect: (String) -> Unit,
    onNewFile: () -> Unit,
    onCloseFile: (String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top toolbar
            IdeToolbar(
                onNewFile = onNewFile,
                onSearch = onSearch,
                onSettings = onSettings,
                modifier = Modifier.fillMaxWidth()
            )

            // Main content area
            Row(modifier = Modifier.weight(1f)) {
                // Sidebar (file explorer)
                // TODO: Implement file explorer sidebar
                // For now, just a placeholder
                Surface(
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column {
                        Text(
                            text = "Explorer",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = "(File explorer will be implemented here)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Editor area
                Column(modifier = Modifier.weight(1f)) {
                    // Tab bar
                    TabBar(
                        openFiles = listOf(), // TODO: Get from state
                        activeFile = null, // TODO: Get from state
                        onFileSelect = onFileSelect,
                        onCloseFile = onCloseFile,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Editor content
                    EditorScreen(
                        state = state,
                        onEdit = { _, _, _ -> }, // TODO: Implement
                        onCursorMove = { _ -> }, // TODO: Implement
                        modifier = Modifier.weight(1f)
                    )
                }
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
 * IDE toolbar composable.
 */
@Composable
private fun IdeToolbar(
    onNewFile: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(48.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: Toggle sidebar */ }) {
                Icon(Icons.Default.Menu, contentDescription = "Toggle Sidebar")
            }

            IconButton(onClick = onNewFile) {
                Icon(Icons.Default.Add, contentDescription = "New File")
            }

            Text(
                text = "Mobile IDE",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }

            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

/**
 * Tab bar composable for open files.
 */
@Composable
private fun TabBar(
    openFiles: List<String>,
    activeFile: String?,
    onFileSelect: (String) -> Unit,
    onCloseFile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(36.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (openFiles.isEmpty()) {
                Text(
                    text = "No files open",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                openFiles.forEach { filePath ->
                    val isActive = filePath == activeFile
                    val backgroundColor = if (isActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    }

                    Surface(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        color = backgroundColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = filePath.substringAfterLast('/'),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 120.dp)
                            )

                            IconButton(
                                onClick = { onCloseFile(filePath) },
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .width(20.dp)
                                    .height(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extension function for widthIn
@Composable
private fun Modifier.widthIn(max: androidx.compose.ui.unit.Dp): Modifier {
    return this.then(Modifier.fillMaxWidth())
}
