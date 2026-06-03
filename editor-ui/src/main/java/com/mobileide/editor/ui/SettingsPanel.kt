package com.mobileide.editor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobileide.editor.core.EditorSettings

/**
 * Settings panel composable.
 * Provides editor configuration options.
 */
@Composable
fun SettingsPanel(
    settings: EditorSettings,
    onSettingsChange: (EditorSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Editor Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Font size
            SettingsSlider(
                title = "Font Size",
                value = settings.fontSize,
                valueRange = 8f..24f,
                onValueChange = { onSettingsChange(settings.copy(fontSize = it)) }
            )

            // Tab size
            SettingsSlider(
                title = "Tab Size",
                value = settings.tabSize.toFloat(),
                valueRange = 2f..8f,
                steps = 5,
                onValueChange = { onSettingsChange(settings.copy(tabSize = it.toInt())) }
            )

            // Use spaces
            SettingsToggle(
                title = "Use Spaces",
                checked = settings.useSpaces,
                onCheckedChange = { onSettingsChange(settings.copy(useSpaces = it)) }
            )

            // Word wrap
            SettingsToggle(
                title = "Word Wrap",
                checked = settings.wordWrap,
                onCheckedChange = { onSettingsChange(settings.copy(wordWrap = it)) }
            )

            // Show line numbers
            SettingsToggle(
                title = "Show Line Numbers",
                checked = settings.showLineNumbers,
                onCheckedChange = { onSettingsChange(settings.copy(showLineNumbers = it)) }
            )

            // Show minimap
            SettingsToggle(
                title = "Show Minimap",
                checked = settings.showMinimap,
                onCheckedChange = { onSettingsChange(settings.copy(showMinimap = it)) }
            )

            // Theme selection
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            val themes = listOf("dark", "light", "high-contrast")
            themes.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = settings.theme == theme,
                            onClick = { onSettingsChange(settings.copy(theme = theme)) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.theme == theme,
                        onClick = { onSettingsChange(settings.copy(theme = theme)) }
                    )
                    Text(
                        text = theme.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Settings slider composable.
 */
@Composable
private fun SettingsSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "$title: ${value.toInt()}",
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Settings toggle composable.
 */
@Composable
private fun SettingsToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
