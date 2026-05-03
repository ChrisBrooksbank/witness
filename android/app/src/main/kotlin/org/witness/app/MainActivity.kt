package org.witness.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.witness.app.ui.WitnessApp
import org.witness.app.ui.theme.WitnessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WitnessTheme {
                WitnessApp()
            }
        }
    }
}
