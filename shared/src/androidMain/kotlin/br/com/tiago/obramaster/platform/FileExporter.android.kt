package br.com.tiago.obramaster.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class FileExporter(private val context: Context) {

    actual suspend fun isAvailable(): Boolean = true

    actual suspend fun compartilhar(nomeArquivo: String, bytes: ByteArray, mimeType: String): Boolean =
        withContext(Dispatchers.IO) {
            val diretorio = File(context.cacheDir, "exportados").apply { mkdirs() }
            val arquivo = File(diretorio, nomeArquivo)
            arquivo.writeBytes(bytes)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(Intent.createChooser(intent, nomeArquivo).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess
        }
}
