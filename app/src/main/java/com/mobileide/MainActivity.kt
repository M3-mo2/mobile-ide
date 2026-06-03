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
                    IdeLayout(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
