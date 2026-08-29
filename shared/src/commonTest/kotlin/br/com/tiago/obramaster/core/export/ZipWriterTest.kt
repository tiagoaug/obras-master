package br.com.tiago.obramaster.core.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Reimplementa a leitura do ZIP de forma independente do ZipWriter (não reaproveita nenhuma
 * função dele) — o objetivo é verificar a estrutura de bytes gerada como um leitor de verdade
 * leria, não só que o próprio escritor concorda consigo mesmo. */
class ZipWriterTest {

    private fun ByteArray.u16(offset: Int): Int = (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)
    private fun ByteArray.u32(offset: Int): Long =
        (0 until 4).fold(0L) { acc, i -> acc or ((this[offset + i].toLong() and 0xFF) shl (8 * i)) }

    @Test
    fun escrever_arquivoVazioTemEocdValido() {
        val bytes = ZipWriter.escrever(emptyList())
        assertEquals(0x06054b50L, bytes.u32(0))
        assertEquals(0, bytes.u16(8)) // total de entradas
    }

    @Test
    fun escrever_localHeadersTemAssinaturaNomeECrcCorretos() {
        val arquivos = listOf(
            "a.txt" to "conteudo do arquivo a".encodeToByteArray(),
            "pasta/b.xml" to "<root>conteudo b</root>".encodeToByteArray(),
        )
        val bytes = ZipWriter.escrever(arquivos)

        var offset = 0
        arquivos.forEach { (nome, conteudo) ->
            assertEquals(0x04034b50L, bytes.u32(offset), "assinatura local file header errada em $nome")
            val metodo = bytes.u16(offset + 8)
            assertEquals(0, metodo, "esperava método STORED (0) em $nome")
            val crcGravado = bytes.u32(offset + 14)
            assertEquals(Crc32.calcular(conteudo), crcGravado, "CRC32 errado em $nome")
            val tamanhoComprimido = bytes.u32(offset + 18)
            val tamanhoOriginal = bytes.u32(offset + 22)
            assertEquals(conteudo.size.toLong(), tamanhoComprimido)
            assertEquals(conteudo.size.toLong(), tamanhoOriginal)
            val tamanhoNome = bytes.u16(offset + 26)
            val extraLen = bytes.u16(offset + 28)
            val nomeBytes = nome.encodeToByteArray()
            assertEquals(nomeBytes.size, tamanhoNome)
            val nomeLido = bytes.copyOfRange(offset + 30, offset + 30 + tamanhoNome).decodeToString()
            assertEquals(nome, nomeLido)
            val inicioConteudo = offset + 30 + tamanhoNome + extraLen
            val conteudoLido = bytes.copyOfRange(inicioConteudo, inicioConteudo + conteudo.size)
            assertTrue(conteudoLido.contentEquals(conteudo), "conteúdo bruto não bate em $nome")
            offset = inicioConteudo + conteudo.size
        }
    }

    @Test
    fun escrever_centralDirectoryApontaParaOffsetsLocaisCorretos() {
        val arquivos = listOf(
            "um.xml" to "1".repeat(50).encodeToByteArray(),
            "dois.xml" to "2".repeat(30).encodeToByteArray(),
            "tres.xml" to "3".repeat(10).encodeToByteArray(),
        )
        val bytes = ZipWriter.escrever(arquivos)

        // acha o EOCD procurando a assinatura a partir do fim (arquivo pequeno, sem comment variável)
        var offsetEocd = -1
        for (i in bytes.size - 22 downTo 0) {
            if (bytes.u32(i) == 0x06054b50L) { offsetEocd = i; break }
        }
        assertTrue(offsetEocd >= 0, "EOCD não encontrado")

        val totalEntradas = bytes.u16(offsetEocd + 10)
        val offsetDiretorioCentral = bytes.u32(offsetEocd + 16).toInt()
        assertEquals(arquivos.size, totalEntradas)

        var offset = offsetDiretorioCentral
        arquivos.forEach { (nome, conteudo) ->
            assertEquals(0x02014b50L, bytes.u32(offset), "assinatura central directory errada em $nome")
            val tamanhoNome = bytes.u16(offset + 28)
            val offsetLocal = bytes.u32(offset + 42).toInt()
            assertEquals(0x04034b50L, bytes.u32(offsetLocal), "offset local gravado no diretório central não aponta pro local header de $nome")
            val nomeNoLocal = bytes.copyOfRange(offsetLocal + 30, offsetLocal + 30 + nome.encodeToByteArray().size).decodeToString()
            assertEquals(nome, nomeNoLocal)
            offset += 46 + tamanhoNome
        }
    }
}
