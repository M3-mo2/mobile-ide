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
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileide.editor.core.EditorState
import com.mobileide.editor.files.FileEntry
import com.mobileide.editor.files.Tab

/**
 * Main IDE layout composable.
 * Combines the sidebar (file explorer), editor area, toolbars, search, and settings.
 */
@Composable
fun IdeLayout(
    editorState: EditorState,
    projectRoot: FileEntry.Directory?,
    expandedDirs: Set<String>,
    openTabs: List<Tab>,
    activeTabId: String?,
    isSearchOpen: Boolean,
    isSettingsOpen: Boolean,
    isSidebarOpen: Boolean,
    onNewFile: () -> Unit,
    onOpenFolder: () -> Unit,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onToggleSidebar: () -> Unit,
    onFileClick: (String) -> Unit,
    onDirectoryClick: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onTabClick: (String) -> Unit,
    onTabClose: (String) -> Unit,
    onEdit: (String, com.mobileide.editor.core.Position, com.mobileide.editor.core.Position) -> Unit,
    onCursorMove: (com.mobileide.editor.core.Position) -> Unit,
    onSearchQuery: (String, com.mobileide.editor.search.SearchOptions) -> Unit,
    onReplace: (String) -> Unit,
    onReplaceAll: (String, String, com.mobileide.editor.search.SearchOptions) -> Unit,
    onCloseSearch: () -> Unit,
    onSettingsChange: (com.mobileide.editor.core.EditorSettings) -> Unit,
    onCloseSettings: () -> Unit,
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
                onOpenFolder = onOpenFolder,
                onSave = onSave,
                onUndo = onUndo,
                onRedo = onRedo,
                onSearch = onSearch,
                onSettings = onSettings,
                onToggleSidebar = onToggleSidebar,
                modifier = Modifier.fillMaxWidth()
            )

            // Main content area
            Row(modifier = Modifier.weight(1f)) {
                // Sidebar (file explorer)
                if (isSidebarOpen) {
                    FileExplorerPanel(
                        root = projectRoot,
                        expandedDirs = expandedDirs,
                        onFileClick = onFileClick,
                        onDirectoryClick = onDirectoryClick,
                        onCreateFile = onCreateFile,
                        onCreateFolder = onCreateFolder,
                        onRename = onRename,
                        onDelete = onDelete,
                        modifier = Modifier.width(280.dp)
                    )
                }

                // Editor area
                Column(modifier = Modifier.weight(1f)) {
                    // Tab bar
                    TabBar(
                        openTabs = openTabs,
                        activeTabId = activeTabId,
                        onTabClick = onTabClick,
                        onTabClose = onTabClose,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Search panel (conditional)
                    if (isSearchOpen) {
                        SearchPanel(
                            state = editorState,
                            onSearch = onSearchQuery,
                            onReplace = onReplace,
                            onReplaceAll = onReplaceAll,
                            onClose = onCloseSearch,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Settings panel (conditional)
                    if (isSettingsOpen) {
                        SettingsPanel(
                            settings = editorState.settings,
                            onSettingsChange = onSettingsChange,
                            onClose = onCloseSettings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Editor content
                    EditorScreen(
                        state = editorState,
                        onEdit = onEdit,
                        onCursorMove = onCursorMove,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Status bar
            StatusBar(
                line = editorState.cursor.primary.position.line + 1,
                column = editorState.cursor.primary.position.column + 1,
                lineCount = editorState.document.content.getLineCount(),
                encoding = editorState.document.encoding,
                filePath = editorState.document.filePath,
                isDirty = editorState.document.isDirty,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * IDE toolbar composable with all working buttons.
 */
@Composable
private fun IdeToolbar(
    onNewFile: () -> Unit,
    onOpenFolder: () -> Unit,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onToggleSidebar: () -> Unit,
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
            IconButton(onClick = onToggleSidebar) {
                Icon(Icons.Default.Menu, contentDescription = "Toggle Sidebar")
            }

            IconButton(onClick = onNewFile) {
                Icon(Icons.Default.Add, contentDescription = "New File")
            }

            IconButton(onClick = onOpenFolder) {
                Icon(Icons.Default.FolderOpen, contentDescription = "Open Folder")
            }

            IconButton(onClick = onSave) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }

            IconButton(onClick = onUndo) {
                Icon(Icons.Default.Undo, contentDescription = "Undo")
            }

            IconButton(onClick = onRedo) {
                Icon(Icons.Default.Redo, contentDescription = "Redo")
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
 * File explorer panel with header and tree.
 */
@Composable
private fun FileExplorerPanel(
    root: FileEntry.Directory?,
    expandedDirs: Set<String>,
    onFileClick: (String) -> Unit,
    onDirectoryClick: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            // Explorer header with actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explorer",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )

                    if (root != null) {
                        IconButton(
                            onClick = { onCreateFile(root.path) },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "New File",
                                modifier = Modifier.height(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onCreateFolder(root.path) },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                Icons.Default.CreateNewFolder,
                                contentDescription = "New Folder",
                                modifier = Modifier.height(18.dp)
                            )
                        }
                    }
                }
            }

            // File tree
            if (root != null) {
                FileExplorer(
                    root = root,
                    expandedDirs = expandedDirs,
                    onFileClick = onFileClick,
                    onDirectoryClick = onDirectoryClick,
                    onCreateFile = onCreateFile,
                    onCreateFolder = onCreateFolder,
                    onRename = onRename,
                    onDelete = onDelete,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // No folder open state
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No folder open",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = "Tap the folder icon to open a project",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab bar composable for open files.
 */
@Composable
private fun TabBar(
    openTabs: List<Tab>,
    activeTabId: String?,
    onTabClick: (String) -> Unit,
    onTabClose: (String) -> Unit,
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
            if (openTabs.isEmpty()) {
                Text(
                    text = "No files open",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                openTabs.forEach { tab ->
                    val isActive = tab.id == activeTabId
                    val backgroundColor = if (isActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                    Surface(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        color = backgroundColor,
                        onClick = { onTabClick(tab.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tab.title + if (tab.isDirty) " *" else "",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 120.dp)
                            )

                            IconButton(
                                onClick = { onTabClose(tab.id) },
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

/**
 * Status bar composable with file info.
 */
@Composable
internal fun StatusBar(
    line: Int,
    column: Int,
    lineCount: Int,
    encoding: String,
    filePath: String?,
    isDirty: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File path on the left
            Text(
                text = filePath?.let { 
                    val name = it.substringAfterLast('/')
                    if (isDirty) "$name *" else name
                } ?: "Untitled",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Position and info on the right
            Text(
                text = "Ln $line, Col $column | $encoding | Lines: $lineCount",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// Extension function for widthIn
@Composable
private fun Modifier.widthIn(max: androidx.compose.ui.unit.Dp): Modifier {
    return this.then(Modifier.fillMaxWidth())
}
