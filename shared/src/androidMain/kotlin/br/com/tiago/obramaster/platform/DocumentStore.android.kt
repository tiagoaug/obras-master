package br.com.tiago.obramaster.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual class DocumentStore(private val context: Context) {
    private val diretorio: File
        get() = File(context.filesDir, "documentos").apply { mkdirs() }

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun salvar(pdfBytes: ByteArray, nome: String): String = withContext(Dispatchers.IO) {
        val chave = "${Uuid.random()}.pdf"
        File(diretorio, chave).writeBytes(pdfBytes)
        chave
    }

    actual suspend fun abrir(key: String): ByteArray? = withContext(Dispatchers.IO) {
        val arquivo = File(diretorio, key)
        if (arquivo.exists()) arquivo.readBytes() else null
    }

    actual suspend fun excluir(key: String) {
        withContext(Dispatchers.IO) { File(diretorio, key).delete() }
    }

    /** Caminho real do arquivo, usado pelo PdfOpener (Android precisa de um File pra gerar a URI do FileProvider). */
    fun caminhoDoArquivo(key: String): File = File(diretorio, key)
}
