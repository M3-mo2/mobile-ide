package com.mobileide.editor.search

import com.mobileide.editor.core.Position
import com.mobileide.editor.core.Range
import com.mobileide.editor.core.TextBuffer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Represents a search match.
 */
data class Match(
    val start: Position,
    val end: Position,
    val text: String
) {
    fun toRange(): Range = Range(start, end)
}

/**
 * Options for search operations.
 */
data class SearchOptions(
    val isRegex: Boolean = false,
    val isCaseSensitive: Boolean = false,
    val isWholeWord: Boolean = false,
    val searchForward: Boolean = true
)

/**
 * Result of a search operation.
 */
data class SearchResult(
    val matches: List<Match>,
    val currentIndex: Int = 0
) {
    /**
     * Returns the current match or null if no matches.
     */
    fun currentMatch(): Match? = matches.getOrNull(currentIndex)

    /**
     * Returns true if there are any matches.
     */
    fun hasMatches(): Boolean = matches.isNotEmpty()

    /**
     * Returns the total number of matches.
     */
    fun totalMatches(): Int = matches.size
}

/**
 * Interface for search engine.
 */
interface SearchEngine {
    /**
     * Searches for the query in the given text buffer.
     * Returns the search result.
     */
    fun search(buffer: TextBuffer, query: String, options: SearchOptions): SearchResult

    /**
     * Finds the next match.
     * Returns the next match or null if none.
     */
    fun findNext(result: SearchResult): Match?

    /**
     * Finds the previous match.
     * Returns the previous match or null if none.
     */
    fun findPrevious(result: SearchResult): Match?

    /**
     * Replaces the current match with the replacement text.
     * Returns the replacement operation.
     */
    fun replace(match: Match, replacement: String): ReplaceOperation

    /**
     * Replaces all matches with the replacement text.
     * Returns the number of replacements made.
     */
    fun replaceAll(buffer: TextBuffer, query: String, replacement: String, options: SearchOptions): Int
}

/**
 * Represents a replace operation.
 */
data class ReplaceOperation(
    val range: Range,
    val oldText: String,
    val newText: String
)

/**
 * Default implementation of SearchEngine.
 */
class SearchEngineImpl : SearchEngine {

    override fun search(buffer: TextBuffer, query: String, options: SearchOptions): SearchResult {
        if (query.isEmpty()) return SearchResult(emptyList())

        val matches = mutableListOf<Match>()
        val text = buffer.getText()

        if (options.isRegex) {
            // Regex search
            val regex = try {
                if (options.isCaseSensitive) {
                    Regex(query)
                } else {
                    Regex(query, RegexOption.IGNORE_CASE)
                }
            } catch (e: Exception) {
                return SearchResult(emptyList())
            }

            regex.findAll(text).forEach { matchResult ->
                val startOffset = matchResult.range.first
                val endOffset = matchResult.range.last + 1
                matches.add(
                    Match(
                        start = buffer.offsetToPosition(startOffset),
                        end = buffer.offsetToPosition(endOffset),
                        text = matchResult.value
                    )
                )
            }
        } else {
            // Simple text search
            val searchText = if (options.isCaseSensitive) query else query.lowercase()
            val sourceText = if (options.isCaseSensitive) text else text.lowercase()

            var index = 0
            while (index != -1 && index < sourceText.length) {
                val foundIndex = sourceText.indexOf(searchText, index)
                if (foundIndex == -1) break

                val endIndex = foundIndex + searchText.length
                matches.add(
                    Match(
                        start = buffer.offsetToPosition(foundIndex),
                        end = buffer.offsetToPosition(endIndex),
                        text = text.substring(foundIndex, endIndex)
                    )
                )

                index = endIndex
            }
        }

        return SearchResult(matches)
    }

    override fun findNext(result: SearchResult): Match? {
        return if (result.matches.isEmpty()) {
            null
        } else {
            val nextIndex = (result.currentIndex + 1) % result.matches.size
            result.matches[nextIndex]
        }
    }

    override fun findPrevious(result: SearchResult): Match? {
        return if (result.matches.isEmpty()) {
            null
        } else {
            val prevIndex = if (result.currentIndex - 1 < 0) {
                result.matches.size - 1
            } else {
                result.currentIndex - 1
            }
            result.matches[prevIndex]
        }
    }

    override fun replace(match: Match, replacement: String): ReplaceOperation {
        return ReplaceOperation(
            range = match.toRange(),
            oldText = match.text,
            newText = replacement
        )
    }

    override fun replaceAll(
        buffer: TextBuffer,
        query: String,
        replacement: String,
        options: SearchOptions
    ): Int {
        val result = search(buffer, query, options)
        return result.matches.size
    }
}

/**
 * Interface for project-wide search.
 */
interface ProjectSearch {
    /**
     * Searches for the query across all files in the project.
     * Returns a flow of search results.
     */
    fun searchInProject(query: String, scope: SearchScope): Flow<ProjectSearchResult>

    /**
     * Indexes the project for faster searching.
     * Returns a flow of indexing progress.
     */
    fun indexProject(projectPath: String): Flow<IndexingProgress>
}

/**
 * Scope for project search.
 */
data class SearchScope(
    val includePatterns: List<String> = listOf("*"),
    val excludePatterns: List<String> = listOf(".git", "node_modules", "build"),
    val fileTypes: List<String> = emptyList()
)

/**
 * Result of a project search.
 */
data class ProjectSearchResult(
    val filePath: String,
    val matches: List<Match>
)

/**
 * Progress of indexing.
 */
data class IndexingProgress(
    val totalFiles: Int,
    val processedFiles: Int,
    val isComplete: Boolean
) {
    fun percentage(): Int {
        return if (totalFiles == 0) 0 else (processedFiles * 100 / totalFiles)
    }
}

/**
 * Default implementation of ProjectSearch.
 */
class ProjectSearchImpl : ProjectSearch {

    override fun searchInProject(query: String, scope: SearchScope): Flow<ProjectSearchResult> = flow {
        // TODO: Implement project-wide search
        // This would:
        // 1. Walk the project directory
        // 2. Filter files based on scope
        // 3. Search each file
        // 4. Emit results
    }

    override fun indexProject(projectPath: String): Flow<IndexingProgress> = flow {
        // TODO: Implement project indexing
        // This would:
        // 1. Walk the project directory
        // 2. Read file contents
        // 3. Build search index
        // 4. Emit progress
    }
}
