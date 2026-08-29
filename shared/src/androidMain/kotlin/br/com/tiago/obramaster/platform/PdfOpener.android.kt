package br.com.tiago.obramaster.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual class PdfOpener(private val context: Context) {

    actual suspend fun isAvailable(): Boolean = true

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun abrir(pdfBytes: ByteArray, nomeArquivo: String): Boolean = withContext(Dispatchers.IO) {
        val diretorio = File(context.cacheDir, "pdf_abrir").apply { mkdirs() }
        val arquivo = File(diretorio, "${Uuid.random()}.pdf")
        arquivo.writeBytes(pdfBytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.isSuccess
    }
}
