package br.com.tiago.obramaster.platform

import android.content.Context
import br.com.tiago.obramaster.domain.ImageRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual class ImageStore(private val context: Context) {

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun save(image: ImageRef, compressQuality: Int): String = withContext(Dispatchers.IO) {
        val chave = "planta_fundo_${Uuid.random()}.jpg"
        File(context.filesDir, chave).writeBytes(image.bytes)
        chave
    }

    actual suspend fun load(key: String): ByteArray? = withContext(Dispatchers.IO) {
        val arquivo = File(context.filesDir, key)
        if (arquivo.exists()) arquivo.readBytes() else null
    }

    actual suspend fun delete(key: String) {
        withContext(Dispatchers.IO) { File(context.filesDir, key).delete() }
    }
}
