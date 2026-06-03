package com.mobileide.editor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * Search panel composable with real search and replace functionality.
 */
@Composable
fun SearchPanel(
    state: EditorState,
    onSearch: (String, SearchOptions) -> Unit,
    onReplace: (String) -> Unit,
    onReplaceAll: (String, String, SearchOptions) -> Unit,
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
                        if (state.search.totalMatches > 0) {
                            Text(
                                text = "${state.search.currentMatch + 1}/${state.search.totalMatches}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
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

                IconButton(onClick = { /* Find previous */ }) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Previous")
                }

                IconButton(onClick = { /* Find next */ }) {
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

                TextButton(
                    onClick = { onReplace(replacement) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Replace")
                }

                TextButton(
                    onClick = {
                        onReplaceAll(query, replacement, SearchOptions(
                            isRegex = isRegex,
                            isCaseSensitive = isCaseSensitive,
                            isWholeWord = isWholeWord
                        ))
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Replace All")
                }
            }

            // Options row
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isRegex,
                        onCheckedChange = { isRegex = it }
                    )
                    Text("Regex", style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Checkbox(
                        checked = isCaseSensitive,
                        onCheckedChange = { isCaseSensitive = it }
                    )
                    Text("Case Sensitive", style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Checkbox(
                        checked = isWholeWord,
                        onCheckedChange = { isWholeWord = it }
                    )
                    Text("Whole Word", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
