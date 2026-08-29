package br.com.tiago.obramaster.core.export

import br.com.tiago.obramaster.domain.ExportableDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PdfWriterTest {

    private fun documentoSimples(linhas: Int = 3) = ExportableDocument(
        titulo = "Relatório de Teste",
        subtitulo = "Gerado automaticamente",
        colunas = listOf("Nome", "Valor"),
        linhas = (1..linhas).map { listOf("Item $it", "R$ ${it * 10},00") },
        resumo = listOf("Total" to "R$ 60,00"),
        rodape = "Rodapé de teste",
    )

    private fun bytesParaLatin1String(bytes: ByteArray): String =
        buildString { bytes.forEach { append((it.toInt() and 0xFF).toChar()) } }

    @Test
    fun escrever_comecaComCabecalhoPdf() {
        val bytes = PdfWriter.escrever(documentoSimples())
        val texto = bytesParaLatin1String(bytes)
        assertTrue(texto.startsWith("%PDF-1.4"))
    }

    @Test
    fun escrever_terminaComEof() {
        val bytes = PdfWriter.escrever(documentoSimples())
        val texto = bytesParaLatin1String(bytes)
        assertTrue(texto.trimEnd().endsWith("%%EOF"))
    }

    @Test
    fun escrever_contemUmaPaginaEObjetosEsperados() {
        val texto = bytesParaLatin1String(PdfWriter.escrever(documentoSimples()))
        assertTrue(texto.contains("/Type /Catalog"))
        assertTrue(texto.contains("/Type /Pages"))
        assertTrue(texto.contains("/Type /Page "))
        assertTrue(texto.contains("/Type /Font"))
        assertTrue(texto.contains("/BaseFont /Helvetica"))
        assertTrue(texto.contains("/Count 1"))
    }

    @Test
    fun escrever_xrefOffsetsApontamParaOInicioDeCadaObjeto() {
        val texto = bytesParaLatin1String(PdfWriter.escrever(documentoSimples()))
        val inicioXref = texto.indexOf("\nxref\n") + 1
        val fimXref = texto.indexOf("trailer")
        val blocoXref = texto.substring(inicioXref, fimXref)
        // pula as linhas "xref" e "0 N" (cabeçalho da subseção), sobram só as entradas de 20 bytes
        val linhasEntrada = blocoXref.lines().drop(2).filter { it.isNotBlank() }

        // primeira entrada é sempre o objeto livre "0000000000 65535 f "
        assertEquals("0000000000 65535 f ", linhasEntrada.first().trimEnd('\r'))

        linhasEntrada.drop(1).forEachIndexed { indice, entrada ->
            val offset = entrada.substring(0, 10).toInt()
            val numeroObjetoEsperado = indice + 1
            val trechoNoOffset = texto.substring(offset, minOf(offset + 20, texto.length))
            assertTrue(
                trechoNoOffset.startsWith("$numeroObjetoEsperado 0 obj"),
                "offset $offset deveria apontar pro objeto $numeroObjetoEsperado, achou: \"$trechoNoOffset\"",
            )
        }
    }

    @Test
    fun escrever_textoComAcentuacaoNaoQuebra() {
        val doc = ExportableDocument(
            titulo = "Relatório com Ç, Ã, Õ, É",
            colunas = listOf("Descrição"),
            linhas = listOf(listOf("Instalação elétrica — não contábil")),
        )
        val texto = bytesParaLatin1String(PdfWriter.escrever(doc))
        assertTrue(texto.startsWith("%PDF-1.4"))
        assertTrue(texto.trimEnd().endsWith("%%EOF"))
    }

    @Test
    fun escrever_muitasLinhasGeraMaisDeUmaPagina() {
        val doc = documentoSimples(linhas = 200)
        val texto = bytesParaLatin1String(PdfWriter.escrever(doc))
        val quantidadePaginas = Regex("""/Count (\d+)""").find(texto)?.groupValues?.get(1)?.toInt()
        assertTrue(quantidadePaginas != null && quantidadePaginas > 1, "esperava paginação com muitas linhas, achou /Count $quantidadePaginas")
    }

    @Test
    fun escrever_parentesesEBarrasNoTextoSaoEscapados() {
        val doc = ExportableDocument(
            titulo = "Teste (com parênteses) e \\barra",
            colunas = listOf("Coluna"),
            linhas = listOf(listOf("valor (x)")),
        )
        // não deve lançar exceção, e o conteúdo deve ter as barras de escape
        val texto = bytesParaLatin1String(PdfWriter.escrever(doc))
        assertTrue(texto.contains("\\(") && texto.contains("\\)"))
    }
}
