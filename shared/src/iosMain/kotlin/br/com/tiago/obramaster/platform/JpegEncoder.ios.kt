package br.com.tiago.obramaster.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

// Compose Multiplatform no iOS também é Skiko-backed (mesma API org.jetbrains.skia do wasmJs,
// diferente do resto do código específico de iOS desta sessão que evita UIKit) — ainda assim,
// não compilada/testada nesta máquina (sem Mac/Xcode).
actual fun ImageBitmap.paraBytesJpeg(qualidade: Int): ByteArray {
    val imagemSkia = Image.makeFromBitmap(asSkiaBitmap())
    return imagemSkia.encodeToData(EncodedImageFormat.JPEG, qualidade)?.bytes ?: ByteArray(0)
}
