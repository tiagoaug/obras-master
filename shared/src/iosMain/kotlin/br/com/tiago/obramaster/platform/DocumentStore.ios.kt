package br.com.tiago.obramaster.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Mesma mecânica do ImageStore.ios.kt (grava/lê arquivo em NSDocumentDirectory) — ainda não
// compilada/testada nesta máquina (sem Mac/Xcode), mesma ressalva já registrada lá.
@OptIn(ExperimentalForeignApi::class)
actual class DocumentStore {
    private val diretorioDocumentos: String
        get() = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun salvar(pdfBytes: ByteArray, nome: String): String {
        val chave = "${Uuid.random()}.pdf"
        val caminho = "$diretorioDocumentos/$chave"
        pdfBytes.toNSData().writeToFile(caminho, atomically = true)
        return chave
    }

    actual suspend fun abrir(key: String): ByteArray? {
        val caminho = "$diretorioDocumentos/$key"
        if (!NSFileManager.defaultManager.fileExistsAtPath(caminho)) return null
        val dados = NSData.dataWithContentsOfFile(caminho) ?: return null
        return dados.toByteArray()
    }

    actual suspend fun excluir(key: String) {
        NSFileManager.defaultManager.removeItemAtPath("$diretorioDocumentos/$key", error = null)
    }

    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.dataWithBytes(it.addressOf(0), size.toULong())
    }

    private fun NSData.toByteArray(): ByteArray {
        val tamanho = length.toInt()
        val resultado = ByteArray(tamanho)
        if (tamanho > 0) {
            resultado.usePinned { destino ->
                platform.posix.memcpy(destino.addressOf(0), bytes, length)
            }
        }
        return resultado
    }
}
