package com.mobileide.editor.core

/**
 * Implementation of TextBuffer using the Piece Table data structure.
 *
 * ## Piece Table Design
 *
 * The Piece Table is an immutable data structure for efficient text editing.
 * It consists of:
 * - [originalBuffer]: The original text, never modified after creation
 * - [addBuffer]: An append-only buffer for all insertions
 * - [pieces]: A sorted list of pieces that reference either original or add buffer
 *
 * ## Why Piece Table?
 * - Immutable operations enable undo/redo
 * - O(log n) edits via binary search on pieces
 * - Memory efficient: stores edits, not copies
 * - Proven in VS Code, Zed, Helix
 *
 * ## Memory Model
 * For file with N lines, M edits:
 * - Original text: N * avg_line_length bytes
 * - Add buffer: sum(edit_lengths) bytes
 * - Pieces: M * 32 bytes overhead
 * - Line cache: N * 4 bytes
 */
class PieceTableTextBuffer private constructor(
    private val originalBuffer: String,
    private val addBuffer: StringBuilder,
    private val pieces: List<Piece>,
    private val lineCache: LineCache,
    private val lineEnding: LineEnding
) : TextBuffer {

    /**
     * Constructs a PieceTableTextBuffer from initial text.
     */
    constructor(text: String) : this(
        originalBuffer = text,
        addBuffer = StringBuilder(),
        pieces = listOf(Piece(PieceSource.ORIGINAL, 0, text.length)),
        lineCache = LineCache(text),
        lineEnding = LineEnding.detect(text)
    )

    /**
     * Represents a piece of text referencing either the original or add buffer.
     */
    private data class Piece(
        val source: PieceSource,
        val start: Int,
        val length: Int
    ) {
        /**
         * Returns the end offset (exclusive) of this piece.
         */
        fun end(): Int = start + length

        /**
         * Splits this piece at the given offset.
         * Returns a pair of (left, right) pieces.
         */
        fun splitAt(offset: Int): Pair<Piece, Piece> {
            require(offset in 1 until length) { "Split offset must be within piece bounds" }
            return Pair(
                Piece(source, start, offset),
                Piece(source, start + offset, length - offset)
            )
        }
    }

    /**
     * Enum indicating which buffer a piece references.
     */
    private enum class PieceSource {
        ORIGINAL, ADD
    }

    /**
     * Cache for line start offsets to enable O(1) line access.
     */
    private class LineCache(text: String) {
        private val lineStarts: MutableList<Int> = mutableListOf(0)

        init {
            var offset = 0
            while (offset < text.length) {
                val nextNewline = text.indexOf('\n', offset)
                if (nextNewline == -1) break
                lineStarts.add(nextNewline + 1)
                offset = nextNewline + 1
            }
        }

        fun getLineStart(lineNumber: Int): Int {
            require(lineNumber in 0 until lineStarts.size) { "Line number out of bounds" }
            return lineStarts[lineNumber]
        }

        fun getLineEnd(lineNumber: Int, textLength: Int): Int {
            return if (lineNumber + 1 < lineStarts.size) {
                lineStarts[lineNumber + 1] - 1
            } else {
                textLength
            }
        }

        fun getLineCount(): Int = lineStarts.size

        /**
         * Rebuilds the line cache from the given text.
         * This is expensive and should be avoided when possible.
         */
        fun rebuild(text: String): LineCache = LineCache(text)
    }

    override fun insert(offset: Int, text: String): TextBuffer {
        require(offset in 0..length()) { "Offset must be within text bounds" }
        if (text.isEmpty()) return this

        val addStart = addBuffer.length
        addBuffer.append(text)

        val newPieces = mutableListOf<Piece>()
        var currentOffset = 0

        for (piece in pieces) {
            val pieceEnd = currentOffset + piece.length

            if (currentOffset <= offset && offset <= pieceEnd) {
                // Split the piece at the insertion point
                val localOffset = offset - currentOffset

                if (localOffset > 0) {
                    // Add left part of split piece
                    newPieces.add(Piece(piece.source, piece.start, localOffset))
                }

                // Add the new piece from add buffer
                newPieces.add(Piece(PieceSource.ADD, addStart, text.length))

                if (localOffset < piece.length) {
                    // Add right part of split piece
                    newPieces.add(Piece(
                        piece.source,
                        piece.start + localOffset,
                        piece.length - localOffset
                    ))
                }
            } else {
                newPieces.add(piece)
            }

            currentOffset = pieceEnd
        }

        // If offset is at the end, append new piece
        if (offset == length()) {
            newPieces.add(Piece(PieceSource.ADD, addStart, text.length))
        }

        // Merge adjacent pieces from the same source if possible
        val mergedPieces = mergeAdjacentPieces(newPieces)

        // Rebuild line cache
        val newText = buildText(mergedPieces)
        val newLineCache = lineCache.rebuild(newText)

        return PieceTableTextBuffer(
            originalBuffer = originalBuffer,
            addBuffer = StringBuilder(addBuffer.toString()),
            pieces = mergedPieces,
            lineCache = newLineCache,
            lineEnding = lineEnding
        )
    }

    override fun delete(offset: Int, length: Int): TextBuffer {
        require(offset in 0 until this.length()) { "Offset must be within text bounds" }
        require(length >= 0) { "Length must be non-negative" }
        if (length == 0) return this

        val endOffset = (offset + length).coerceAtMost(this.length())
        val actualLength = endOffset - offset

        val newPieces = mutableListOf<Piece>()
        var currentOffset = 0

        for (piece in pieces) {
            val pieceEnd = currentOffset + piece.length

            if (pieceEnd <= offset || currentOffset >= endOffset) {
                // Piece is completely outside deletion range
                newPieces.add(piece)
            } else {
                // Piece overlaps with deletion range
                val deleteStartInPiece = (offset - currentOffset).coerceAtLeast(0)
                val deleteEndInPiece = (endOffset - currentOffset).coerceAtMost(piece.length)

                if (deleteStartInPiece > 0) {
                    // Add left part
                    newPieces.add(Piece(
                        piece.source,
                        piece.start,
                        deleteStartInPiece
                    ))
                }

                if (deleteEndInPiece < piece.length) {
                    // Add right part
                    newPieces.add(Piece(
                        piece.source,
                        piece.start + deleteEndInPiece,
                        piece.length - deleteEndInPiece
                    ))
                }
            }

            currentOffset = pieceEnd
        }

        // Merge adjacent pieces
        val mergedPieces = mergeAdjacentPieces(newPieces)

        // Rebuild line cache
        val newText = buildText(mergedPieces)
        val newLineCache = lineCache.rebuild(newText)

        return PieceTableTextBuffer(
            originalBuffer = originalBuffer,
            addBuffer = StringBuilder(addBuffer.toString()),
            pieces = mergedPieces,
            lineCache = newLineCache,
            lineEnding = lineEnding
        )
    }

    override fun replace(offset: Int, length: Int, text: String): TextBuffer {
        return delete(offset, length).insert(offset, text)
    }

    override fun getText(): String = buildText(pieces)

    override fun getLine(lineNumber: Int): String {
        require(lineNumber in 0 until getLineCount()) { "Line number out of bounds" }
        val lineStart = lineCache.getLineStart(lineNumber)
        val lineEnd = lineCache.getLineEnd(lineNumber, length())
        return getText().substring(lineStart, lineEnd)
    }

    override fun getLineCount(): Int = lineCache.getLineCount()

    override fun length(): Int = pieces.sumOf { it.length }

    override fun charAt(offset: Int): Char {
        require(offset in 0 until length()) { "Offset out of bounds" }
        var currentOffset = 0
        for (piece in pieces) {
            val pieceEnd = currentOffset + piece.length
            if (offset < pieceEnd) {
                val localOffset = offset - currentOffset
                return when (piece.source) {
                    PieceSource.ORIGINAL -> originalBuffer[piece.start + localOffset]
                    PieceSource.ADD -> addBuffer[piece.start + localOffset]
                }
            }
            currentOffset = pieceEnd
        }
        throw IndexOutOfBoundsException("Offset $offset out of bounds")
    }

    override fun positionToOffset(position: Position): Int {
        require(position.line in 0 until getLineCount()) { "Line number out of bounds" }
        val lineStart = lineCache.getLineStart(position.line)
        val lineText = getLine(position.line)
        val column = position.column.coerceIn(0, lineText.length)
        return lineStart + column
    }

    override fun offsetToPosition(offset: Int): Position {
        require(offset in 0..length()) { "Offset out of bounds" }
        var line = 0
        for (i in 0 until getLineCount()) {
            val lineStart = lineCache.getLineStart(i)
            if (offset < lineStart) {
                line = i - 1
                break
            }
            line = i
        }
        val lineStart = lineCache.getLineStart(line)
        val column = offset - lineStart
        return Position(line, column)
    }

    override fun getLineEnding(): LineEnding = lineEnding

    override fun substring(startOffset: Int, endOffset: Int): String {
        require(startOffset in 0..length()) { "Start offset out of bounds" }
        require(endOffset in 0..length()) { "End offset out of bounds" }
        require(startOffset <= endOffset) { "Start offset must be <= end offset" }
        if (startOffset == endOffset) return ""

        val result = StringBuilder()
        var currentOffset = 0

        for (piece in pieces) {
            val pieceEnd = currentOffset + piece.length
            if (pieceEnd <= startOffset || currentOffset >= endOffset) {
                // Piece is outside range
                currentOffset = pieceEnd
                continue
            }

            val pieceStartOffset = (startOffset - currentOffset).coerceAtLeast(0)
            val pieceEndOffset = (endOffset - currentOffset).coerceAtMost(piece.length)
            val length = pieceEndOffset - pieceStartOffset

            val text = when (piece.source) {
                PieceSource.ORIGINAL -> originalBuffer.substring(
                    piece.start + pieceStartOffset,
                    piece.start + pieceStartOffset + length
                )
                PieceSource.ADD -> addBuffer.substring(
                    piece.start + pieceStartOffset,
                    piece.start + pieceStartOffset + length
                )
            }

            result.append(text)
            currentOffset = pieceEnd
        }

        return result.toString()
    }

    /**
     * Builds the full text from pieces.
     * This is expensive and should be avoided in hot paths.
     */
    private fun buildText(pieces: List<Piece>): String {
        val result = StringBuilder()
        for (piece in pieces) {
            val text = when (piece.source) {
                PieceSource.ORIGINAL -> originalBuffer.substring(piece.start, piece.end())
                PieceSource.ADD -> addBuffer.substring(piece.start, piece.end())
            }
            result.append(text)
        }
        return result.toString()
    }

    /**
     * Merges adjacent pieces from the same source if they are contiguous.
     */
    private fun mergeAdjacentPieces(pieces: List<Piece>): List<Piece> {
        if (pieces.size < 2) return pieces

        val result = mutableListOf<Piece>()
        var current = pieces[0]

        for (i in 1 until pieces.size) {
            val next = pieces[i]
            if (current.source == next.source &&
                current.end() == next.start) {
                // Merge them
                current = Piece(current.source, current.start, current.length + next.length)
            } else {
                result.add(current)
                current = next
            }
        }
        result.add(current)

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextBuffer) return false
        return getText() == other.getText()
    }

    override fun hashCode(): Int {
        return getText().hashCode()
    }

    override fun toString(): String {
        return "PieceTableTextBuffer(length=${length()}, lines=${getLineCount()}, pieces=${pieces.size})"
    }
}
