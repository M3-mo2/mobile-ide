package com.mobileide.editor.files

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Unit tests for FileManager.
 */
class FileManagerTest {

    private val fileManager = FileManagerImpl()
    private val testDir = File(System.getProperty("java.io.tmpdir"), "mobile-ide-test")

    @Test
    fun `create file`() {
        val testFile = File(testDir, "test.txt")
        val result = fileManager.createFile(testFile.absolutePath)
        assertTrue(result)
        assertTrue(testFile.exists())
        testFile.delete()
    }

    @Test
    fun `delete file`() {
        val testFile = File(testDir, "delete-test.txt")
        testFile.createNewFile()
        val result = fileManager.deleteFile(testFile.absolutePath)
        assertTrue(result)
        assertFalse(testFile.exists())
    }

    @Test
    fun `read file`() {
        val testFile = File(testDir, "read-test.txt")
        testFile.writeText("Hello, World!")
        val content = fileManager.readFile(testFile.absolutePath)
        assertEquals("Hello, World!", content)
        testFile.delete()
    }

    @Test
    fun `write file`() {
        val testFile = File(testDir, "write-test.txt")
        val result = fileManager.writeFile(testFile.absolutePath, "Test content")
        assertTrue(result)
        assertEquals("Test content", testFile.readText())
        testFile.delete()
    }

    @Test
    fun `rename file`() {
        val oldFile = File(testDir, "old-name.txt")
        val newFile = File(testDir, "new-name.txt")
        oldFile.createNewFile()
        val result = fileManager.renameFile(oldFile.absolutePath, newFile.absolutePath)
        assertTrue(result)
        assertFalse(oldFile.exists())
        assertTrue(newFile.exists())
        newFile.delete()
    }

    @Test
    fun `create directory`() {
        val testDir = File(testDir, "test-dir")
        val result = fileManager.createDirectory(testDir.absolutePath)
        assertTrue(result)
        assertTrue(testDir.exists())
        assertTrue(testDir.isDirectory)
        testDir.delete()
    }

    @Test
    fun `list directory`() {
        val testDir = File(testDir, "list-test")
        testDir.mkdirs()
        File(testDir, "file1.txt").createNewFile()
        File(testDir, "file2.txt").createNewFile()
        File(testDir, "subdir").mkdir()

        val entries = fileManager.listDirectory(testDir.absolutePath)
        assertNotNull(entries)
        assertEquals(3, entries!!.size)

        testDir.deleteRecursively()
    }

    @Test
    fun `check file exists`() {
        val testFile = File(testDir, "exists-test.txt")
        assertFalse(fileManager.exists(testFile.absolutePath))
        testFile.createNewFile()
        assertTrue(fileManager.exists(testFile.absolutePath))
        testFile.delete()
    }

    @Test
    fun `check is directory`() {
        val testDir = File(testDir, "is-dir-test")
        assertFalse(fileManager.isDirectory(testDir.absolutePath))
        testDir.mkdir()
        assertTrue(fileManager.isDirectory(testDir.absolutePath))
        testDir.delete()
    }
}
