package br.com.tiago.obramaster.platform

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.paraBytesJpeg(qualidade: Int): ByteArray {
    val saida = ByteArrayOutputStream()
    asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, qualidade, saida)
    return saida.toByteArray()
}
