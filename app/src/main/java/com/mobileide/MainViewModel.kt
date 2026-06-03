package com.mobileide

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileide.editor.core.DocumentState
import com.mobileide.editor.core.EditorState
import com.mobileide.editor.core.Position
import com.mobileide.editor.core.TextBufferFactory
import com.mobileide.editor.files.FileEntry
import com.mobileide.editor.files.FileManagerImpl
import com.mobileide.editor.files.Tab
import com.mobileide.editor.files.TabManagerImpl
import com.mobileide.editor.search.SearchEngineImpl
import com.mobileide.editor.search.SearchOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Main ViewModel for the IDE.
 * Manages the full IDE state including files, tabs, editor, search, and settings.
 */
@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val fileManager = FileManagerImpl()
    private val tabManager = TabManagerImpl()
    private val searchEngine = SearchEngineImpl()

    // UI State
    var uiState by mutableStateOf(IdeUiState())
        private set

    // Current editor state for active tab
    var editorState by mutableStateOf(EditorState())
        private set

    // Tab-specific editor states
    private val tabEditorStates = mutableMapOf<String, EditorState>()

    init {
        // Initialize with empty document
        editorState = EditorState(
            document = DocumentState(
                content = TextBufferFactory.createEmpty()
            )
        )
    }

    // ============================================
    // FILE EXPLORER OPERATIONS
    // ============================================

    /**
     * Opens a folder/project in the file explorer.
     */
    fun openFolder(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val rootDir = File(path)
            if (rootDir.exists() && rootDir.isDirectory) {
                val rootEntry = loadDirectoryRecursive(rootDir, 2)
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        projectRoot = rootEntry,
                        currentProjectPath = path
                    )
                }
            }
        }
    }

    /**
     * Expands a directory in the file explorer.
     */
    fun expandDirectory(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                val updatedEntry = loadDirectoryRecursive(dir, 2)
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        expandedDirectories = uiState.expandedDirectories + path,
                        projectRoot = updateDirectoryInTree(uiState.projectRoot, path, updatedEntry)
                    )
                }
            }
        }
    }

    /**
     * Collapses a directory in the file explorer.
     */
    fun collapseDirectory(path: String) {
        uiState = uiState.copy(
            expandedDirectories = uiState.expandedDirectories - path
        )
    }

    /**
     * Creates a new file.
     */
    fun createNewFile(parentPath: String? = null) {
        val path = parentPath ?: uiState.currentProjectPath ?: return
        val newFileName = "new_file_${System.currentTimeMillis()}.txt"
        val newFilePath = File(path, newFileName).absolutePath

        if (fileManager.createFile(newFilePath)) {
            // Refresh the parent directory
            openFolder(path)
            // Open the new file
            openFile(newFilePath)
        }
    }

    /**
     * Creates a new folder.
     */
    fun createNewFolder(parentPath: String? = null) {
        val path = parentPath ?: uiState.currentProjectPath ?: return
        val newFolderName = "new_folder_${System.currentTimeMillis()}"
        val newFolderPath = File(path, newFolderName).absolutePath

        if (fileManager.createDirectory(newFolderPath)) {
            openFolder(path)
        }
    }

    /**
     * Renames a file or folder.
     */
    fun renameFile(oldPath: String, newName: String) {
        val parent = File(oldPath).parentFile?.absolutePath ?: return
        val newPath = File(parent, newName).absolutePath

        if (fileManager.renameFile(oldPath, newPath)) {
            // Update tab if file is open
            val tab = tabManager.getOpenTabs().find { it.filePath == oldPath }
            if (tab != null) {
                tabManager.closeTab(tab.id)
                tabManager.openFile(newPath)
            }

            // Refresh explorer
            openFolder(parent)
        }
    }

    /**
     * Deletes a file or folder.
     */
    fun deleteFile(path: String) {
        val isDir = fileManager.isDirectory(path)
        val success = if (isDir) {
            fileManager.deleteDirectory(path)
        } else {
            fileManager.deleteFile(path)
        }

        if (success) {
            // Close tab if open
            tabManager.getOpenTabs().find { it.filePath == path }?.let {
                closeFile(it.id)
            }

            // Refresh explorer
            uiState.currentProjectPath?.let { openFolder(it) }
        }
    }

    // ============================================
    // TAB OPERATIONS
    // ============================================

    /**
     * Opens a file in a new tab.
     */
    fun openFile(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = fileManager.readFile(filePath) ?: ""
                val textBuffer = TextBufferFactory.fromText(content)

            withContext(Dispatchers.Main) {
                // Save current editor state if there's an active tab
                uiState.activeTabId?.let { activeId ->
                    tabEditorStates[activeId] = editorState
                }

                // Open in tab manager
                val tab = tabManager.openFile(filePath)

                // Create new editor state for this file
                val newEditorState = EditorState(
                    document = DocumentState(
                        filePath = filePath,
                        content = textBuffer,
                        isDirty = false,
                        encoding = "UTF-8"
                    )
                )

                tabEditorStates[tab.id] = newEditorState
                editorState = newEditorState

                uiState = uiState.copy(
                    openTabs = tabManager.getOpenTabs(),
                    activeTabId = tab.id
                )
            }
        }
    }

    /**
     * Switches to a tab.
     */
    fun switchTab(tabId: String) {
        // Save current editor state
        uiState.activeTabId?.let { activeId ->
            tabEditorStates[activeId] = editorState
        }

        // Switch tab
        tabManager.switchTab(tabId)

        // Restore editor state for the new tab
        val restoredState = tabEditorStates[tabId] ?: EditorState()
        editorState = restoredState

        uiState = uiState.copy(
            activeTabId = tabId,
            openTabs = tabManager.getOpenTabs()
        )
    }

    /**
     * Closes a tab.
     */
    fun closeFile(tabId: String) {
        tabManager.closeTab(tabId)
        tabEditorStates.remove(tabId)

        val remainingTabs = tabManager.getOpenTabs()
        val activeTab = tabManager.getActiveTab()

        editorState = if (activeTab != null) {
            tabEditorStates[activeTab.id] ?: EditorState()
        } else {
            EditorState(
                document = DocumentState(
                    content = TextBufferFactory.createEmpty()
                )
            )
        }

        uiState = uiState.copy(
            openTabs = remainingTabs,
            activeTabId = activeTab?.id
        )
    }

    // ============================================
    // EDITOR OPERATIONS
    // ============================================

    /**
     * Handles text edit operations.
     */
    fun onEdit(text: String, start: Position, end: Position) {
        val currentDoc = editorState.document
        val startOffset = currentDoc.content.positionToOffset(start)
        val endOffset = currentDoc.content.positionToOffset(end)
        val length = endOffset - startOffset

        val newContent = currentDoc.content.replace(startOffset, length, text)
        val newDocument = currentDoc.withContent(newContent)
        editorState = editorState.withDocument(newDocument)

        // Update tab dirty state
        updateTabDirtyState(true)
    }

    /**
     * Handles cursor movement.
     */
    fun onCursorMove(position: Position) {
        val newCursor = editorState.cursor.primary.moveTo(position)
        editorState = editorState.withCursor(
            editorState.cursor.withPrimary(newCursor)
        )
    }

    /**
     * Saves the current file.
     */
    fun saveCurrentFile() {
        val filePath = editorState.document.filePath ?: return
        val content = editorState.document.content.getText()

        viewModelScope.launch(Dispatchers.IO) {
            val success = fileManager.writeFile(filePath, content)
            if (success) {
                withContext(Dispatchers.Main) {
                    val savedDoc = editorState.document.saved()
                    editorState = editorState.withDocument(savedDoc)
                    updateTabDirtyState(false)
                }
            }
        }
    }

    /**
     * Undo last operation.
     */
    fun undo() {
        val undoManager = editorState.undoManager
        val operation = undoManager.undo()
        if (operation != null) {
            // Apply the inverse operation
            // This is simplified - real implementation would be more complex
        }
    }

    /**
     * Redo last undone operation.
     */
    fun redo() {
        val undoManager = editorState.undoManager
        val operation = undoManager.redo()
        if (operation != null) {
            // Apply the operation
        }
    }

    // ============================================
    // SEARCH OPERATIONS
    // ============================================

    /**
     * Opens the search panel.
     */
    fun openSearch() {
        uiState = uiState.copy(isSearchOpen = true)
    }

    /**
     * Closes the search panel.
     */
    fun closeSearch() {
        uiState = uiState.copy(isSearchOpen = false)
    }

    /**
     * Performs search in current file.
     */
    fun search(query: String, options: SearchOptions) {
        val result = searchEngine.search(editorState.document.content, query, options)
        val searchState = editorState.search
            .withQuery(query)
            .withMatches(result.currentIndex, result.totalMatches())
        editorState = editorState.withSearch(searchState)
    }

    /**
     * Replaces current match.
     */
    fun replace(replacement: String) {
        // Implementation would replace current match
    }

    /**
     * Replaces all matches.
     */
    fun replaceAll(query: String, replacement: String, options: SearchOptions) {
        searchEngine.replaceAll(editorState.document.content, query, replacement, options)
    }

    // ============================================
    // SETTINGS OPERATIONS
    // ============================================

    /**
     * Opens the settings panel.
     */
    fun openSettings() {
        uiState = uiState.copy(isSettingsOpen = true)
    }

    /**
     * Closes the settings panel.
     */
    fun closeSettings() {
        uiState = uiState.copy(isSettingsOpen = false)
    }

    /**
     * Updates editor settings.
     */
    fun updateSettings(settings: com.mobileide.editor.core.EditorSettings) {
        editorState = editorState.withSettings(settings)
    }

    // ============================================
    // TOGGLE SIDEBAR
    // ============================================

    /**
     * Toggles the sidebar visibility.
     */
    fun toggleSidebar() {
        uiState = uiState.copy(isSidebarOpen = !uiState.isSidebarOpen)
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================

    private fun updateTabDirtyState(isDirty: Boolean) {
        val activeTabId = uiState.activeTabId ?: return
        val updatedTabs = uiState.openTabs.map { tab ->
            if (tab.id == activeTabId) {
                tab.copy(isDirty = isDirty)
            } else {
                tab
            }
        }
        uiState = uiState.copy(openTabs = updatedTabs)
    }

    private fun loadDirectoryRecursive(dir: File, depth: Int): FileEntry.Directory {
        val children = dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))?.map { child ->
            if (child.isDirectory && depth > 0) {
                loadDirectoryRecursive(child, depth - 1)
            } else if (child.isDirectory) {
                FileEntry.Directory(
                    path = child.absolutePath,
                    name = child.name,
                    children = emptyList(),
                    isExpanded = false
                )
            } else {
                FileEntry.File(
                    path = child.absolutePath,
                    name = child.name,
                    extension = child.extension,
                    size = child.length(),
                    lastModified = child.lastModified()
                )
            }
        } ?: emptyList()

        return FileEntry.Directory(
            path = dir.absolutePath,
            name = dir.name,
            children = children,
            isExpanded = uiState.expandedDirectories.contains(dir.absolutePath)
        )
    }

    private fun updateDirectoryInTree(
        root: FileEntry.Directory?,
        targetPath: String,
        newEntry: FileEntry.Directory
    ): FileEntry.Directory? {
        if (root == null) return null

        if (root.path == targetPath) {
            return newEntry.copy(name = root.name)
        }

        val updatedChildren = root.children.map { child ->
            when (child) {
                is FileEntry.Directory -> updateDirectoryInTree(child, targetPath, newEntry) ?: child
                else -> child
            }
        }

        return root.copy(children = updatedChildren)
    }
}

/**
 * UI state for the IDE.
 */
data class IdeUiState(
    val projectRoot: FileEntry.Directory? = null,
    val currentProjectPath: String? = null,
    val expandedDirectories: Set<String> = emptySet(),
    val openTabs: List<Tab> = emptyList(),
    val activeTabId: String? = null,
    val isSearchOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isSidebarOpen: Boolean = true
)
