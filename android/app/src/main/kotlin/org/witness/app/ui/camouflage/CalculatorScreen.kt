@file:Suppress("ktlint:standard:function-naming")

package org.witness.app.ui.camouflage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.witness.app.ui.theme.WitnessTheme

private val ScreenPadding = 16.dp
private val ButtonSpacing = 8.dp
private val DisplaySpacing = 24.dp

private val ButtonRows = listOf(
    listOf("7", "8", "9", "/"),
    listOf("4", "5", "6", "*"),
    listOf("1", "2", "3", "-"),
    listOf("C", "0", "=", "+"),
)

@Composable
@Suppress("FunctionName")
fun CalculatorScreen(onUnlocked: () -> Unit) {
    val engine = remember { CalculatorEngine() }
    var display by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = display,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Calculator display $display" },
            color = Color.Black,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
        Spacer(modifier = Modifier.height(DisplaySpacing))
        ButtonRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { token ->
                    CalculatorButton(
                        token = token,
                        onClick = {
                            val result = engine.input(display, token)
                            display = result.display
                            if (result.unlocked) onUnlocked()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(ButtonSpacing))
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun CalculatorButton(token: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
        ),
    ) {
        Text(
            text = token,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun CalculatorScreenPreview() {
    WitnessTheme {
        CalculatorScreen(onUnlocked = {})
    }
}
