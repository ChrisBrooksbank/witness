@file:Suppress("ktlint:standard:function-naming")

package org.witness.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WitnessColorScheme: ColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White,
)

@Composable
@Suppress("FunctionName")
fun WitnessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WitnessColorScheme,
        content = content,
    )
}
