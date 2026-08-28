package br.com.tiago.obramaster.platform

import android.content.Context
import android.graphics.Bitmap
import br.com.tiago.obramaster.domain.ImageRef
import java.io.ByteArrayOutputStream

actual class ImagePicker(private val context: Context) {

    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun takePhoto(): ImageRef? {
        val bitmap = ImagePickerBridge.tirarFoto() ?: return null
        return ImageRef(bitmap.paraJpegBytes())
    }

    actual suspend fun pickFromGallery(multiple: Boolean): List<ImageRef> {
        val uri = ImagePickerBridge.escolherDaGaleria() ?: return emptyList()
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return emptyList()
        return listOf(ImageRef(bytes))
    }

    private fun Bitmap.paraJpegBytes(qualidade: Int = 85): ByteArray {
        val saida = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, qualidade, saida)
        return saida.toByteArray()
    }
}
