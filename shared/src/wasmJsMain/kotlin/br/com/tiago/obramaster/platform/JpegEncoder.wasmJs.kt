package br.com.tiago.obramaster.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.paraBytesJpeg(qualidade: Int): ByteArray {
    val imagemSkia = Image.makeFromBitmap(asSkiaBitmap())
    return imagemSkia.encodeToData(EncodedImageFormat.JPEG, qualidade)?.bytes ?: ByteArray(0)
}
