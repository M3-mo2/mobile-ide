package com.mobileide.editor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileide.editor.core.EditorState
import com.mobileide.editor.search.SearchOptions

/**
 * Search panel composable.
 * Provides search and replace functionality.
 */
@Composable
fun SearchPanel(
    state: EditorState,
    onSearch: (String, SearchOptions) -> Unit,
    onReplace: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf(state.search.query) }
    var replacement by remember { mutableStateOf("") }
    var isRegex by remember { mutableStateOf(state.search.isRegex) }
    var isCaseSensitive by remember { mutableStateOf(state.search.isCaseSensitive) }
    var isWholeWord by remember { mutableStateOf(state.search.isWholeWord) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Search row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Find") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = {
                        Text(
                            text = "${state.search.currentMatch}/${state.search.totalMatches}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )

                IconButton(onClick = {
                    onSearch(query, SearchOptions(
                        isRegex = isRegex,
                        isCaseSensitive = isCaseSensitive,
                        isWholeWord = isWholeWord
                    ))
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }

                IconButton(onClick = { /* TODO: Find previous */ }) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Previous")
                }

                IconButton(onClick = { /* TODO: Find next */ }) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Next")
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Replace row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replacement,
                    onValueChange = { replacement = it },
                    placeholder = { Text("Replace") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                IconButton(onClick = { onReplace(replacement) }) {
                    Text("Replace")
                }

                IconButton(onClick = { /* TODO: Replace all */ }) {
                    Text("All")
                }
            }

            // Options row
            Row(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // TODO: Add toggle buttons for regex, case sensitive, whole word
                Text(
                    text = "Options: ${if (isRegex) "Regex " else ""}${if (isCaseSensitive) "Case Sensitive " else ""}${if (isWholeWord) "Whole Word" else ""}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
