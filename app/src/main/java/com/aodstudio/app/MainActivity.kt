package com.aodstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aodstudio.app.navigation.AODNavHost
import com.aodstudio.app.ui.theme.AODStudioTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity — all UI is handled via Compose Navigation.
 * This is the only Activity in the app (single-Activity architecture).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AODStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AODNavHost()
                }
            }
        }
    }
}
