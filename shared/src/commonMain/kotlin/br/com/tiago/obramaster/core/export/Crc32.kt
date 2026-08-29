package br.com.tiago.obramaster.core.export

/** CRC-32 (polinômio padrão 0xEDB88320, o mesmo do ZIP/PNG/gzip) — não existe no
 * kotlin-stdlib-common (java.util.zip.CRC32 é JVM-only); precisamos calcular o checksum de cada
 * entrada do ZIP na mão pra XlsxWriter (ver ZipWriter.kt, Fase 9.4). */
object Crc32 {
    private val tabela = IntArray(256).also { tabela ->
        for (i in 0 until 256) {
            var c = i
            repeat(8) {
                c = if (c and 1 != 0) (POLINOMIO xor (c ushr 1)) else (c ushr 1)
            }
            tabela[i] = c
        }
    }

    private const val POLINOMIO = -0x12477ce0 // 0xEDB88320 como Int

    fun calcular(bytes: ByteArray): Long {
        var crc = -1 // 0xFFFFFFFF
        for (b in bytes) {
            val indice = (crc xor b.toInt()) and 0xFF
            crc = tabela[indice] xor (crc ushr 8)
        }
        return (crc.toLong() xor 0xFFFFFFFFL) and 0xFFFFFFFFL
    }
}
