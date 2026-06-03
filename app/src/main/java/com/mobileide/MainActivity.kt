package com.mobileide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobileide.editor.ui.IdeLayout
import com.mobileide.ui.theme.MobileIDETheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for Mobile IDE.
 * Sets up the Compose UI with proper edge-to-edge handling.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileIDETheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = hiltViewModel()
                    val uiState = viewModel.uiState
                    val editorState = viewModel.editorState

                    IdeLayout(
                        editorState = editorState,
                        projectRoot = uiState.projectRoot,
                        expandedDirs = uiState.expandedDirectories,
                        openTabs = uiState.openTabs,
                        activeTabId = uiState.activeTabId,
                        isSearchOpen = uiState.isSearchOpen,
                        isSettingsOpen = uiState.isSettingsOpen,
                        isSidebarOpen = uiState.isSidebarOpen,
                        onNewFile = { viewModel.createNewFile() },
                        onOpenFolder = { /* Would open SAF picker */ },
                        onSave = { viewModel.saveCurrentFile() },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onSearch = { viewModel.openSearch() },
                        onSettings = { viewModel.openSettings() },
                        onToggleSidebar = { viewModel.toggleSidebar() },
                        onFileClick = { viewModel.openFile(it) },
                        onDirectoryClick = { path ->
                            if (uiState.expandedDirectories.contains(path)) {
                                viewModel.collapseDirectory(path)
                            } else {
                                viewModel.expandDirectory(path)
                            }
                        },
                        onCreateFile = { viewModel.createNewFile(it) },
                        onCreateFolder = { viewModel.createNewFolder(it) },
                        onRename = { oldPath, newName -> viewModel.renameFile(oldPath, newName) },
                        onDelete = { viewModel.deleteFile(it) },
                        onTabClick = { viewModel.switchTab(it) },
                        onTabClose = { viewModel.closeFile(it) },
                        onEdit = { text, start, end -> viewModel.onEdit(text, start, end) },
                        onCursorMove = { viewModel.onCursorMove(it) },
                        onSearchQuery = { query, options -> viewModel.search(query, options) },
                        onReplace = { viewModel.replace(it) },
                        onReplaceAll = { query, replacement, options ->
                            viewModel.replaceAll(query, replacement, options)
                        },
                        onCloseSearch = { viewModel.closeSearch() },
                        onSettingsChange = { viewModel.updateSettings(it) },
                        onCloseSettings = { viewModel.closeSettings() }
                    )
                }
            }
        }
    }
}
