package br.com.tiago.obramaster.core.export

import kotlin.test.Test
import kotlin.test.assertEquals

class Crc32Test {

    @Test
    fun calcular_vazioRetornaZero() {
        assertEquals(0L, Crc32.calcular(ByteArray(0)))
    }

    @Test
    fun calcular_valorPadraoDeReferencia() {
        // "123456789" é o vetor de teste padrão universal do CRC-32 (poly 0xEDB88320) — resultado
        // conhecido 0xCBF43926, usado por toda implementação de referência pra se autoverificar.
        val resultado = Crc32.calcular("123456789".encodeToByteArray())
        assertEquals(0xCBF43926L, resultado)
    }
}
