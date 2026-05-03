package org.witness.app.ui.camouflage

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.witness.app.MainActivity
import org.witness.app.ui.theme.WitnessTheme

class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WitnessTheme {
                CalculatorScreen(
                    onUnlocked = {
                        startActivity(Intent(this, MainActivity::class.java))
                    },
                )
            }
        }
    }
}
