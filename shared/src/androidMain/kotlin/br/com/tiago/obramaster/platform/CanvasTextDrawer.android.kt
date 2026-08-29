package br.com.tiago.obramaster.platform

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

actual fun DrawScope.desenharTexto(texto: String, x: Float, y: Float, tamanhoPx: Float, cor: Color) {
    val paint = Paint().apply {
        color = cor.toArgb()
        textSize = tamanhoPx
        isAntiAlias = true
        typeface = Typeface.DEFAULT
    }
    drawContext.canvas.nativeCanvas.drawText(texto, x, y, paint)
}
