package com.mobileide.editor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileide.editor.files.FileEntry

/**
 * File explorer sidebar composable.
 * Displays a tree view of files and directories with full CRUD operations.
 */
@Composable
fun FileExplorer(
    root: FileEntry.Directory,
    expandedDirs: Set<String>,
    onFileClick: (String) -> Unit,
    onDirectoryClick: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = root.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(root.children) { entry ->
            FileTreeItem(
                entry = entry,
                level = 0,
                expandedDirs = expandedDirs,
                onFileClick = onFileClick,
                onDirectoryClick = onDirectoryClick,
                onCreateFile = onCreateFile,
                onCreateFolder = onCreateFolder,
                onRename = onRename,
                onDelete = onDelete
            )
        }
    }
}

/**
 * Single item in the file tree.
 */
@Composable
private fun FileTreeItem(
    entry: FileEntry,
    level: Int,
    expandedDirs: Set<String>,
    onFileClick: (String) -> Unit,
    onDirectoryClick: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    val paddingStart = (level * 16 + 8).dp

    when (entry) {
        is FileEntry.File -> {
            FileItem(
                name = entry.name,
                path = entry.path,
                paddingStart = paddingStart,
                onClick = { onFileClick(entry.path) },
                onRename = { onRename(entry.path, it) },
                onDelete = { onDelete(entry.path) }
            )
        }
        is FileEntry.Directory -> {
            DirectoryItem(
                entry = entry,
                level = level,
                paddingStart = paddingStart,
                expandedDirs = expandedDirs,
                onFileClick = onFileClick,
                onDirectoryClick = onDirectoryClick,
                onCreateFile = onCreateFile,
                onCreateFolder = onCreateFolder,
                onRename = onRename,
                onDelete = onDelete
            )
        }
    }
}

/**
 * File item composable with context menu actions.
 */
@Composable
private fun FileItem(
    name: String,
    path: String,
    paddingStart: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingStart, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.InsertDriveFile,
            contentDescription = "File",
            tint = Color(0xFF6B9BFA),
            modifier = Modifier
                .padding(end = 8.dp)
                .width(20.dp)
                .height(20.dp)
        )

        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )

        // Action buttons (visible on hover or always visible for mobile)
        IconButton(
            onClick = { showRenameDialog = true },
            modifier = Modifier.width(28.dp).height(28.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Rename",
                modifier = Modifier.width(16.dp).height(16.dp)
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.width(28.dp).height(28.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.width(16.dp).height(16.dp)
            )
        }
    }

    // Click to open file
    androidx.compose.foundation.clickable { onClick() }

    // Rename dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename File") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(newName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Directory item composable with expand/collapse and actions.
 */
@Composable
private fun DirectoryItem(
    entry: FileEntry.Directory,
    level: Int,
    paddingStart: androidx.compose.ui.unit.Dp,
    expandedDirs: Set<String>,
    onFileClick: (String) -> Unit,
    onDirectoryClick: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    val isExpanded = expandedDirs.contains(entry.path)
    var showCreateMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(entry.name) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = paddingStart, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/collapse button
            IconButton(
                onClick = { onDirectoryClick(entry.path) },
                modifier = Modifier.width(24.dp).height(24.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.width(18.dp).height(18.dp)
                )
            }

            // Folder icon
            Icon(
                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = "Directory",
                tint = Color(0xFFFFC107),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(20.dp)
                    .height(20.dp)
            )

            // Directory name
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Action buttons
            IconButton(
                onClick = { showCreateMenu = true },
                modifier = Modifier.width(28.dp).height(28.dp)
            ) {
                Icon(
                    Icons.Default.CreateNewFolder,
                    contentDescription = "New",
                    modifier = Modifier.width(16.dp).height(16.dp)
                )
            }

            IconButton(
                onClick = { showRenameDialog = true },
                modifier = Modifier.width(28.dp).height(28.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Rename",
                    modifier = Modifier.width(16.dp).height(16.dp)
                )
            }

            IconButton(
                onClick = { onDelete(entry.path) },
                modifier = Modifier.width(28.dp).height(28.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.width(16.dp).height(16.dp)
                )
            }
        }

        // Show children if expanded
        if (isExpanded) {
            entry.children.forEach { child ->
                FileTreeItem(
                    entry = child,
                    level = level + 1,
                    expandedDirs = expandedDirs,
                    onFileClick = onFileClick,
                    onDirectoryClick = onDirectoryClick,
                    onCreateFile = onCreateFile,
                    onCreateFolder = onCreateFolder,
                    onRename = onRename,
                    onDelete = onDelete
                )
            }
        }
    }

    // Create menu dialog
    if (showCreateMenu) {
        AlertDialog(
            onDismissRequest = { showCreateMenu = false },
            title = { Text("Create in ${entry.name}") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onCreateFile(entry.path)
                            showCreateMenu = false
                        }
                    ) {
                        Text("New File")
                    }
                    TextButton(
                        onClick = {
                            onCreateFolder(entry.path)
                            showCreateMenu = false
                        }
                    ) {
                        Text("New Folder")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCreateMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Folder") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(entry.path, newName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
