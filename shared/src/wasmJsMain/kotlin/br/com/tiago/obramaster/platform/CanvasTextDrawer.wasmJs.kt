package br.com.tiago.obramaster.platform

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint

actual fun DrawScope.desenharTexto(texto: String, x: Float, y: Float, tamanhoPx: Float, cor: Color) {
    val fonte = Font(null, tamanhoPx)
    val paint = Paint().apply {
        color = cor.toArgb()
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawString(texto, x, y, fonte, paint)
}
