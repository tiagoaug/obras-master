package br.com.tiago.obramaster.core.export

import br.com.tiago.obramaster.domain.ExportableDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Descompacta o ZIP gerado com um leitor independente (não reaproveita ZipWriter/XlsxWriter
 * internamente) pra verificar que as partes XML de verdade estão lá dentro e com o conteúdo
 * esperado — não só que XlsxWriter "acha" que gerou algo válido. */
class XlsxWriterTest {

    private fun ByteArray.u16(offset: Int): Int = (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)
    private fun ByteArray.u32(offset: Int): Long =
        (0 until 4).fold(0L) { acc, i -> acc or ((this[offset + i].toLong() and 0xFF) shl (8 * i)) }

    /** Descompacta um zip STORED-only lendo os local file headers sequencialmente (não usa o
     * central directory) — exercita exatamente o que ZipWriter gravou nos dados, ponta a ponta. */
    private fun descompactar(bytes: ByteArray): Map<String, String> {
        val arquivos = mutableMapOf<String, String>()
        var offset = 0
        while (offset < bytes.size && bytes.u32(offset) == 0x04034b50L) {
            val tamanhoComprimido = bytes.u32(offset + 18).toInt()
            val tamanhoNome = bytes.u16(offset + 26)
            val extraLen = bytes.u16(offset + 28)
            val nome = bytes.copyOfRange(offset + 30, offset + 30 + tamanhoNome).decodeToString()
            val inicioConteudo = offset + 30 + tamanhoNome + extraLen
            val conteudo = bytes.copyOfRange(inicioConteudo, inicioConteudo + tamanhoComprimido).decodeToString()
            arquivos[nome] = conteudo
            offset = inicioConteudo + tamanhoComprimido
        }
        return arquivos
    }

    private fun documentoExemplo() = ExportableDocument(
        titulo = "Relatório de Materiais",
        colunas = listOf("Nome", "Preço"),
        linhas = listOf(listOf("Cimento & Cia", "R$ 35,00"), listOf("Tijolo <especial>", "R$ 1,20")),
        resumo = listOf("Total" to "R$ 36,20"),
    )

    @Test
    fun escrever_contemAsSeisPartesEsperadas() {
        val arquivos = descompactar(XlsxWriter.escrever(documentoExemplo()))
        val esperados = setOf(
            "[Content_Types].xml", "_rels/.rels", "xl/workbook.xml",
            "xl/_rels/workbook.xml.rels", "xl/styles.xml", "xl/worksheets/sheet1.xml",
        )
        assertEquals(esperados, arquivos.keys)
    }

    @Test
    fun escrever_partesXmlSaoBemFormadasNoBasico() {
        val arquivos = descompactar(XlsxWriter.escrever(documentoExemplo()))
        arquivos.forEach { (nome, conteudo) ->
            assertTrue(conteudo.startsWith("<?xml"), "$nome não começa com declaração XML")
            assertEquals(conteudo.count { it == '<' }, conteudo.count { it == '>' }, "$nome tem tags malformadas (< != >)")
        }
    }

    @Test
    fun escrever_planilhaTemAsCelulasEsperadas() {
        val arquivos = descompactar(XlsxWriter.escrever(documentoExemplo()))
        val sheet = arquivos.getValue("xl/worksheets/sheet1.xml")
        assertTrue(sheet.contains("<c r=\"A1\""))
        assertTrue(sheet.contains("Relatório de Materiais"))
        // linha 2 = cabeçalho das colunas
        assertTrue(sheet.contains("<c r=\"A2\""))
        assertTrue(sheet.contains("Nome"))
        assertTrue(sheet.contains("<c r=\"B2\""))
        assertTrue(sheet.contains("Preço"))
        // caracteres especiais escapados corretamente
        assertTrue(sheet.contains("Cimento &amp; Cia"))
        assertTrue(sheet.contains("Tijolo &lt;especial&gt;"))
    }

    @Test
    fun escrever_referenciaDeColunaVaiAlemDeZ() {
        val doc = ExportableDocument(
            titulo = "t",
            colunas = (1..30).map { "Col$it" },
            linhas = emptyList(),
        )
        val arquivos = descompactar(XlsxWriter.escrever(doc))
        val sheet = arquivos.getValue("xl/worksheets/sheet1.xml")
        // 30 colunas: a última (índice 29) deve virar "AD" (A=0..Z=25, AA=26, AB=27, AC=28, AD=29)
        assertTrue(sheet.contains("r=\"AD2\""), "esperava referência de coluna AD pra 30 colunas")
    }
}
