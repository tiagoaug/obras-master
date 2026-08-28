package br.com.tiago.obramaster.platform

import br.com.tiago.obramaster.domain.ImageRef
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

// Só entra em uso quando ImagePicker.isAvailable() virar true no iOS (ver ImagePicker.ios.kt) —
// mecânica simples (grava/lê arquivo), risco bem menor que cinterop de UI, mas ainda não
// compilada/testada nesta máquina (sem Mac/Xcode).
@OptIn(ExperimentalForeignApi::class)
actual class ImageStore {
    private val diretorioDocumentos: String
        get() = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String

    @OptIn(ExperimentalUuidApi::class)
    actual suspend fun save(image: ImageRef, compressQuality: Int): String {
        val chave = "planta_fundo_${Uuid.random()}.jpg"
        val caminho = "$diretorioDocumentos/$chave"
        val dados = image.bytes.toNSData()
        dados.writeToFile(caminho, atomically = true)
        return chave
    }

    actual suspend fun load(key: String): ByteArray? {
        val caminho = "$diretorioDocumentos/$key"
        if (!NSFileManager.defaultManager.fileExistsAtPath(caminho)) return null
        val dados = NSData.dataWithContentsOfFile(caminho) ?: return null
        return dados.toByteArray()
    }

    actual suspend fun delete(key: String) {
        val caminho = "$diretorioDocumentos/$key"
        NSFileManager.defaultManager.removeItemAtPath(caminho, error = null)
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
