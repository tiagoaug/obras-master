package br.com.tiago.obramaster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ObraMasterLightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    secondary = androidx.compose.ui.graphics.Color(0xFFF9A825),
)

private val ObraMasterDarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF66BB6A),
    secondary = androidx.compose.ui.graphics.Color(0xFFFFCA28),
)

@Composable
fun ObraMasterTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) ObraMasterDarkColors else ObraMasterLightColors,
        content = content,
    )
}
