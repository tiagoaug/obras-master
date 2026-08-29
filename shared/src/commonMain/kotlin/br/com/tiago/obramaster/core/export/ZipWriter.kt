package br.com.tiago.obramaster.core.export

/** ZIP mínimo — usado pra empacotar o XLSX (Fase 9.4), que é OOXML dentro de um ZIP. Só método
 * STORED (sem compressão): evita precisar de uma implementação de DEFLATE em commonMain, troca
 * arquivo um pouco maior por muito menos código/risco — decisão já registrada na aprovação da
 * Fase 9. Formato ZIP: local file header + dados por entrada, central directory ao final, end of
 * central directory record. Datas/horas do ZIP são fixas (não afetam a leitura do arquivo). */
object ZipWriter {

    private const val DATA_DOS = 0x21 // 1/1/1980, válido mas arbitrário — datas de entrada de zip não importam pra leitura
    private const val HORA_DOS = 0x00

    fun escrever(arquivos: List<Pair<String, ByteArray>>): ByteArray {
        val corpo = ByteWriter()
        val diretorioCentral = ByteWriter()
        val offsetsLocais = IntArray(arquivos.size)

        arquivos.forEachIndexed { indice, (nome, conteudo) ->
            offsetsLocais[indice] = corpo.tamanho
            val crc = Crc32.calcular(conteudo)
            val nomeBytes = nome.encodeToByteArray()

            corpo.u32(0x04034b50) // assinatura local file header
            corpo.u16(20) // versão mínima
            corpo.u16(0) // flags
            corpo.u16(0) // método = STORED
            corpo.u16(HORA_DOS)
            corpo.u16(DATA_DOS)
            corpo.u32(crc)
            corpo.u32(conteudo.size.toLong()) // tamanho comprimido = tamanho original (STORED)
            corpo.u32(conteudo.size.toLong())
            corpo.u16(nomeBytes.size)
            corpo.u16(0) // extra field length
            corpo.bytes(nomeBytes)
            corpo.bytes(conteudo)
        }

        arquivos.forEachIndexed { indice, (nome, conteudo) ->
            val crc = Crc32.calcular(conteudo)
            val nomeBytes = nome.encodeToByteArray()

            diretorioCentral.u32(0x02014b50) // assinatura central directory header
            diretorioCentral.u16(20) // versão que criou
            diretorioCentral.u16(20) // versão mínima
            diretorioCentral.u16(0) // flags
            diretorioCentral.u16(0) // método = STORED
            diretorioCentral.u16(HORA_DOS)
            diretorioCentral.u16(DATA_DOS)
            diretorioCentral.u32(crc)
            diretorioCentral.u32(conteudo.size.toLong())
            diretorioCentral.u32(conteudo.size.toLong())
            diretorioCentral.u16(nomeBytes.size)
            diretorioCentral.u16(0) // extra field length
            diretorioCentral.u16(0) // comment length
            diretorioCentral.u16(0) // número do disco
            diretorioCentral.u16(0) // atributos internos
            diretorioCentral.u32(0) // atributos externos
            diretorioCentral.u32(offsetsLocais[indice].toLong())
            diretorioCentral.bytes(nomeBytes)
        }

        val offsetDiretorioCentral = corpo.tamanho
        val saida = ByteWriter()
        saida.bytes(corpo.paraByteArray())
        saida.bytes(diretorioCentral.paraByteArray())

        saida.u32(0x06054b50) // assinatura end of central directory
        saida.u16(0) // número do disco
        saida.u16(0) // disco onde começa o diretório central
        saida.u16(arquivos.size) // entradas neste disco
        saida.u16(arquivos.size) // total de entradas
        saida.u32(diretorioCentral.tamanho.toLong()) // tamanho do diretório central
        saida.u32(offsetDiretorioCentral.toLong())
        saida.u16(0) // comment length

        return saida.paraByteArray()
    }
}

/** Buffer de bytes crescente com escrita little-endian — Kotlin não tem um ByteArrayOutputStream
 * multiplataforma (o do JVM é java.io, específico da plataforma). */
private class ByteWriter {
    private val dados = mutableListOf<Byte>()
    val tamanho: Int get() = dados.size

    fun u16(valor: Int) {
        dados += (valor and 0xFF).toByte()
        dados += ((valor ushr 8) and 0xFF).toByte()
    }

    fun u32(valor: Long) {
        for (i in 0 until 4) dados += ((valor ushr (8 * i)) and 0xFF).toByte()
    }

    fun bytes(valor: ByteArray) {
        valor.forEach { dados += it }
    }

    fun paraByteArray(): ByteArray = dados.toByteArray()
}
