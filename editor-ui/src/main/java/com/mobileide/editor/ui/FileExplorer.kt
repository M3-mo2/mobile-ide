package com.mobileide.editor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mobileide.editor.files.FileEntry

/**
 * File explorer sidebar composable.
 * Displays a tree view of files and directories.
 */
@Composable
fun FileExplorer(
    root: FileEntry.Directory,
    onFileClick: (String) -> Unit,
    onDirectoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
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
                onFileClick = onFileClick,
                onDirectoryClick = onDirectoryClick
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
    onFileClick: (String) -> Unit,
    onDirectoryClick: (String) -> Unit
) {
    val paddingStart = (level * 16 + 8).dp

    when (entry) {
        is FileEntry.File -> {
            FileItem(
                name = entry.name,
                paddingStart = paddingStart,
                onClick = { onFileClick(entry.path) }
            )
        }
        is FileEntry.Directory -> {
            DirectoryItem(
                entry = entry,
                level = level,
                paddingStart = paddingStart,
                onFileClick = onFileClick,
                onDirectoryClick = onDirectoryClick
            )
        }
    }
}

/**
 * File item composable.
 */
@Composable
private fun FileItem(
    name: String,
    paddingStart: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(start = paddingStart, top = 4.dp, bottom = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.InsertDriveFile,
            contentDescription = "File",
            tint = Color(0xFF6B9BFA),
            modifier = androidx.compose.ui.Modifier.padding(end = 8.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            modifier = androidx.compose.ui.Modifier.padding(end = 8.dp)
        )
    }
}

/**
 * Directory item composable.
 */
@Composable
private fun DirectoryItem(
    entry: FileEntry.Directory,
    level: Int,
    paddingStart: androidx.compose.ui.unit.Dp,
    onFileClick: (String) -> Unit,
    onDirectoryClick: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(entry.isExpanded) }

    Column {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(start = paddingStart, top = 4.dp, bottom = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = androidx.compose.ui.Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = "Directory",
                tint = Color(0xFFFFC107),
                modifier = androidx.compose.ui.Modifier.padding(end = 8.dp)
            )

            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (isExpanded) {
            entry.children.forEach { child ->
                FileTreeItem(
                    entry = child,
                    level = level + 1,
                    onFileClick = onFileClick,
                    onDirectoryClick = onDirectoryClick
                )
            }
        }
    }
}
