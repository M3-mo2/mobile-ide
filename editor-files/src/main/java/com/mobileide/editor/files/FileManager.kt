package com.mobileide.editor.files

import java.io.File

/**
 * Represents a file system entry (file or directory).
 */
sealed class FileEntry {
    abstract val path: String
    abstract val name: String

    data class File(
        override val path: String,
        override val name: String,
        val extension: String,
        val size: Long,
        val lastModified: Long
    ) : FileEntry()

    data class Directory(
        override val path: String,
        override val name: String,
        val children: List<FileEntry>,
        val isExpanded: Boolean = false
    ) : FileEntry()
}

/**
 * Interface for workspace management.
 */
interface Workspace {
    /**
     * Opens a project at the given path.
     * Returns the project or null if it doesn't exist.
     */
    fun openProject(path: String): Project?

    /**
     * Closes the current project.
     */
    fun closeProject()

    /**
     * Returns the current project or null if none is open.
     */
    fun getCurrentProject(): Project?

    /**
     * Returns the list of recent projects.
     */
    fun getRecentProjects(): List<RecentProject>

    /**
     * Adds a project to recent projects.
     */
    fun addRecentProject(project: RecentProject)
}

/**
 * Represents a project/workspace.
 */
data class Project(
    val path: String,
    val name: String,
    val root: FileEntry.Directory
) {
    /**
     * Returns the full path for a relative path within the project.
     */
    fun resolve(relativePath: String): String {
        return java.io.File(path, relativePath).absolutePath
    }

    /**
     * Returns the relative path from the project root.
     */
    fun relativize(absolutePath: String): String {
        return absolutePath.removePrefix(path).removePrefix("/")
    }
}

/**
 * Represents a recently opened project.
 */
data class RecentProject(
    val path: String,
    val name: String,
    val lastOpened: Long,
    val isFavorite: Boolean = false
)

/**
 * Interface for file operations.
 */
interface FileManager {
    /**
     * Creates a new file at the given path.
     * Returns true if successful.
     */
    fun createFile(path: String): Boolean

    /**
     * Deletes a file at the given path.
     * Returns true if successful.
     */
    fun deleteFile(path: String): Boolean

    /**
     * Renames a file.
     * Returns true if successful.
     */
    fun renameFile(oldPath: String, newPath: String): Boolean

    /**
     * Reads the contents of a file.
     * Returns the file contents or null if it doesn't exist.
     */
    fun readFile(path: String): String?

    /**
     * Writes content to a file.
     * Returns true if successful.
     */
    fun writeFile(path: String, content: String): Boolean

    /**
     * Creates a directory at the given path.
     * Returns true if successful.
     */
    fun createDirectory(path: String): Boolean

    /**
     * Deletes a directory at the given path.
     * Returns true if successful.
     */
    fun deleteDirectory(path: String): Boolean

    /**
     * Lists the contents of a directory.
     * Returns the list of file entries or null if it doesn't exist.
     */
    fun listDirectory(path: String): List<FileEntry>?

    /**
     * Checks if a file exists.
     */
    fun exists(path: String): Boolean

    /**
     * Checks if a path is a directory.
     */
    fun isDirectory(path: String): Boolean
}

/**
 * Default implementation of FileManager.
 */
class FileManagerImpl : FileManager {

    override fun createFile(path: String): Boolean {
        return try {
            val file = java.io.File(path)
            file.parentFile?.mkdirs()
            file.createNewFile()
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteFile(path: String): Boolean {
        return try {
            java.io.File(path).delete()
        } catch (e: Exception) {
            false
        }
    }

    override fun renameFile(oldPath: String, newPath: String): Boolean {
        return try {
            java.io.File(oldPath).renameTo(java.io.File(newPath))
        } catch (e: Exception) {
            false
        }
    }

    override fun readFile(path: String): String? {
        return try {
            java.io.File(path).readText()
        } catch (e: Exception) {
            null
        }
    }

    override fun writeFile(path: String, content: String): Boolean {
        return try {
            java.io.File(path).writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun createDirectory(path: String): Boolean {
        return try {
            java.io.File(path).mkdirs()
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteDirectory(path: String): Boolean {
        return try {
            java.io.File(path).deleteRecursively()
        } catch (e: Exception) {
            false
        }
    }

    override fun listDirectory(path: String): List<FileEntry>? {
        return try {
            val file = java.io.File(path)
            if (!file.isDirectory) return null

            file.listFiles()?.map { child ->
                if (child.isDirectory) {
                    FileEntry.Directory(
                        path = child.absolutePath,
                        name = child.name,
                        children = emptyList(), // Lazy loading
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
            }?.sortedWith(compareBy { it.name }) ?: emptyList()
        } catch (e: Exception) {
            null
        }
    }

    override fun exists(path: String): Boolean {
        return java.io.File(path).exists()
    }

    override fun isDirectory(path: String): Boolean {
        return java.io.File(path).isDirectory
    }
}

/**
 * Interface for tab management.
 */
interface TabManager {
    /**
     * Opens a file in a new tab.
     * Returns the tab.
     */
    fun openFile(filePath: String): Tab

    /**
     * Closes a tab by its ID.
     * Returns true if successful.
     */
    fun closeTab(tabId: String): Boolean

    /**
     * Switches to a tab by its ID.
     * Returns the tab or null if not found.
     */
    fun switchTab(tabId: String): Tab?

    /**
     * Returns all open tabs.
     */
    fun getOpenTabs(): List<Tab>

    /**
     * Returns the active tab or null if none.
     */
    fun getActiveTab(): Tab?

    /**
     * Pins a tab to prevent auto-close.
     */
    fun pinTab(tabId: String): Tab?

    /**
     * Unpins a tab.
     */
    fun unpinTab(tabId: String): Tab?
}

/**
 * Represents an open tab.
 */
data class Tab(
    val id: String,
    val filePath: String,
    val title: String,
    val isDirty: Boolean = false,
    val isPinned: Boolean = false,
    val order: Int = 0
)

/**
 * Default implementation of TabManager.
 */
class TabManagerImpl(private val maxTabs: Int = 20) : TabManager {
    private val tabs = mutableListOf<Tab>()
    private var activeTabId: String? = null

    override fun openFile(filePath: String): Tab {
        // Check if already open
        val existingTab = tabs.find { it.filePath == filePath }
        if (existingTab != null) {
            activeTabId = existingTab.id
            return existingTab
        }

        // Check if at max tabs
        if (tabs.size >= maxTabs) {
            // Close oldest unpinned tab
            val oldestUnpinned = tabs.filter { !it.isPinned }.minByOrNull { it.order }
            if (oldestUnpinned != null) {
                closeTab(oldestUnpinned.id)
            } else {
                throw IllegalStateException("Maximum number of tabs reached")
            }
        }

        val newTab = Tab(
            id = filePath, // Use file path as ID for simplicity
            filePath = filePath,
            title = java.io.File(filePath).name,
            order = tabs.size
        )

        tabs.add(newTab)
        activeTabId = newTab.id
        return newTab
    }

    override fun closeTab(tabId: String): Boolean {
        val tab = tabs.find { it.id == tabId } ?: return false

        // If dirty, should prompt user (handled at higher level)
        tabs.remove(tab)

        // Update active tab
        if (activeTabId == tabId) {
            activeTabId = tabs.lastOrNull()?.id
        }

        return true
    }

    override fun switchTab(tabId: String): Tab? {
        val tab = tabs.find { it.id == tabId } ?: return null
        activeTabId = tabId
        return tab
    }

    override fun getOpenTabs(): List<Tab> {
        return tabs.toList()
    }

    override fun getActiveTab(): Tab? {
        return activeTabId?.let { id -> tabs.find { it.id == id } }
    }

    override fun pinTab(tabId: String): Tab? {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index == -1) return null
        tabs[index] = tabs[index].copy(isPinned = true)
        return tabs[index]
    }

    override fun unpinTab(tabId: String): Tab? {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index == -1) return null
        tabs[index] = tabs[index].copy(isPinned = false)
        return tabs[index]
    }
}
