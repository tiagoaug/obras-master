package br.com.tiago.obramaster.core.export

import br.com.tiago.obramaster.domain.ExportableDocument
import kotlin.math.max
import kotlin.math.round

/** SPEC_OBRA_MASTER_KMP.md §5.2 — gerador de PDF 1.4 escrito à mão em commonMain (decisão da
 * Fase 9.3): sem lib JVM-only (iText/PDFBox não rodam em iOS/Web), sem depender do backend
 * (Fase 10 ainda não existe). Só o essencial — texto (fonte Helvetica base14 + WinAnsiEncoding,
 * cobre acentuação do português) e linhas retas, paginação automática. Sem imagens, sem fontes
 * embutidas, sem compressão de stream — um PDF 1.4 válido e legível em qualquer leitor.
 *
 * Todo o arquivo é construído como uma única String (sintaxe PDF é ASCII; o único texto variável
 * é mapeado pra WinAnsi/Latin-1 em [escaparTexto], 1 char = 1 byte) e só convertido pra ByteArray
 * no fim — assim os offsets de byte da tabela xref são simplesmente `StringBuilder.length` ao
 * longo da construção, sem precisar contar bytes manualmente. */
object PdfWriter {

    private const val LARGURA_PAGINA = 595.0 // A4 em pontos (1/72 polegada)
    private const val ALTURA_PAGINA = 842.0
    private const val MARGEM = 40.0
    private const val TAMANHO_FONTE_EMPRESA = 10.0
    private const val TAMANHO_FONTE_TITULO = 18.0
    private const val TAMANHO_FONTE_SUBTITULO = 11.0
    private const val TAMANHO_FONTE_CABECALHO = 9.0
    private const val TAMANHO_FONTE_CORPO = 9.0
    private const val TAMANHO_FONTE_RODAPE = 8.0
    private const val ALTURA_LINHA = 16.0
    private const val LARGURA_MEDIA_CARACTERE = 0.5

    private sealed interface Comando
    private data class Texto(val texto: String, val x: Double, val y: Double, val tamanho: Double) : Comando
    private data class Linha(val x1: Double, val y1: Double, val x2: Double, val y2: Double) : Comando

    fun escrever(doc: ExportableDocument): ByteArray {
        val paginas = construirPaginas(doc)
        return serializar(paginas)
    }

    private fun construirPaginas(doc: ExportableDocument): List<List<Comando>> {
        val paginas = mutableListOf<List<Comando>>()
        var atual = mutableListOf<Comando>()
        var y = ALTURA_PAGINA - MARGEM
        val larguraUtil = LARGURA_PAGINA - 2 * MARGEM
        val larguraColuna = larguraUtil / max(doc.colunas.size, 1)

        fun desenharCabecalhoColunas() {
            doc.colunas.forEachIndexed { indice, coluna ->
                atual += Texto(truncar(coluna, larguraColuna, TAMANHO_FONTE_CABECALHO), MARGEM + indice * larguraColuna, y, TAMANHO_FONTE_CABECALHO)
            }
            y -= ALTURA_LINHA * 0.5
            atual += Linha(MARGEM, y, LARGURA_PAGINA - MARGEM, y)
            y -= ALTURA_LINHA * 0.7
        }

        fun novaPagina() {
            paginas += atual
            atual = mutableListOf()
            y = ALTURA_PAGINA - MARGEM
            desenharCabecalhoColunas()
        }

        fun garantirEspaco(altura: Double) {
            if (y - altura < MARGEM) novaPagina()
        }

        doc.empresa?.let { empresa ->
            atual += Texto(empresa.nome, MARGEM, y, TAMANHO_FONTE_EMPRESA)
            y -= ALTURA_LINHA
        }
        atual += Texto(doc.titulo, MARGEM, y, TAMANHO_FONTE_TITULO)
        y -= ALTURA_LINHA * 1.4
        doc.subtitulo?.let { subtitulo ->
            atual += Texto(subtitulo, MARGEM, y, TAMANHO_FONTE_SUBTITULO)
            y -= ALTURA_LINHA
        }
        y -= 6.0
        atual += Linha(MARGEM, y, LARGURA_PAGINA - MARGEM, y)
        y -= ALTURA_LINHA

        desenharCabecalhoColunas()

        doc.linhas.forEach { linha ->
            garantirEspaco(ALTURA_LINHA)
            linha.forEachIndexed { indice, valor ->
                atual += Texto(truncar(valor, larguraColuna, TAMANHO_FONTE_CORPO), MARGEM + indice * larguraColuna, y, TAMANHO_FONTE_CORPO)
            }
            y -= ALTURA_LINHA
        }

        if (doc.resumo.isNotEmpty()) {
            garantirEspaco(ALTURA_LINHA * 1.5)
            y -= 4.0
            atual += Linha(MARGEM, y, LARGURA_PAGINA - MARGEM, y)
            y -= ALTURA_LINHA
            doc.resumo.forEach { (rotulo, valor) ->
                garantirEspaco(ALTURA_LINHA)
                atual += Texto(rotulo, MARGEM, y, TAMANHO_FONTE_CORPO)
                atual += Texto(valor, LARGURA_PAGINA - MARGEM - valor.length * TAMANHO_FONTE_CORPO * LARGURA_MEDIA_CARACTERE, y, TAMANHO_FONTE_CORPO)
                y -= ALTURA_LINHA
            }
        }

        doc.rodape?.let { rodape ->
            garantirEspaco(ALTURA_LINHA * 1.5)
            y -= 8.0
            atual += Texto(rodape, MARGEM, y, TAMANHO_FONTE_RODAPE)
        }

        paginas += atual
        return paginas
    }

    private fun truncar(texto: String, larguraColuna: Double, tamanhoFonte: Double): String {
        val maxCaracteres = (larguraColuna / (tamanhoFonte * LARGURA_MEDIA_CARACTERE)).toInt().coerceAtLeast(1)
        if (texto.length <= maxCaracteres) return texto
        return texto.take((maxCaracteres - 1).coerceAtLeast(0)) + "…"
    }

    private fun serializar(paginas: List<List<Comando>>): ByteArray {
        val sb = StringBuilder()
        val offsets = mutableListOf<Int>()

        fun escreverObjeto(conteudo: String) {
            offsets += sb.length
            sb.append(conteudo)
        }

        val n = paginas.size.coerceAtLeast(1)
        val objCatalog = 1
        val objPages = 2
        val objFont = 3
        val paginaObjs = (0 until n).map { 4 + it }
        val streamObjs = (0 until n).map { 4 + n + it }

        sb.append("%PDF-1.4\n%").append('â').append('ã').append('Ï').append('Ó').append('\n')

        escreverObjeto("$objCatalog 0 obj\n<< /Type /Catalog /Pages $objPages 0 R >>\nendobj\n")
        escreverObjeto("$objPages 0 obj\n<< /Type /Pages /Kids [${paginaObjs.joinToString(" ") { "$it 0 R" }}] /Count $n >>\nendobj\n")
        escreverObjeto("$objFont 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n")

        for (i in 0 until n) {
            escreverObjeto(
                "${paginaObjs[i]} 0 obj\n<< /Type /Page /Parent $objPages 0 R /MediaBox [0 0 $LARGURA_PAGINA $ALTURA_PAGINA] " +
                    "/Resources << /Font << /F1 $objFont 0 R >> >> /Contents ${streamObjs[i]} 0 R >>\nendobj\n",
            )
        }

        val paginasSeguras = if (paginas.isEmpty()) listOf(emptyList()) else paginas
        for (i in 0 until n) {
            val conteudo = construirConteudo(paginasSeguras.getOrElse(i) { emptyList() })
            escreverObjeto("${streamObjs[i]} 0 obj\n<< /Length ${conteudo.length} >>\nstream\n$conteudo\nendstream\nendobj\n")
        }

        val xrefOffset = sb.length
        val totalObjs = 3 + 2 * n
        sb.append("xref\n0 ${totalObjs + 1}\n")
        sb.append("0000000000 65535 f \n")
        offsets.forEach { offset -> sb.append(offset.toString().padStart(10, '0')).append(" 00000 n \n") }
        sb.append("trailer\n<< /Size ${totalObjs + 1} /Root $objCatalog 0 R >>\nstartxref\n$xrefOffset\n%%EOF")

        return sb.toString().paraLatin1Bytes()
    }

    private fun construirConteudo(comandos: List<Comando>): String {
        val sb = StringBuilder("0.5 w\n")
        comandos.forEach { comando ->
            when (comando) {
                is Texto -> sb.append(
                    "BT /F1 ${formatarNumero(comando.tamanho)} Tf 1 0 0 1 ${formatarNumero(comando.x)} ${formatarNumero(comando.y)} Tm " +
                        "(${escaparTexto(comando.texto)}) Tj ET\n",
                )
                is Linha -> sb.append("${formatarNumero(comando.x1)} ${formatarNumero(comando.y1)} m ${formatarNumero(comando.x2)} ${formatarNumero(comando.y2)} l S\n")
            }
        }
        return sb.toString()
    }

    private fun escaparTexto(texto: String): String {
        val soLatin1 = texto.map { c -> if (c.code in 32..255) c else '?' }.joinToString("")
        return soLatin1.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
    }

    private fun formatarNumero(valor: Double): String {
        val arredondado = round(valor * 100) / 100
        return if (arredondado == arredondado.toLong().toDouble()) arredondado.toLong().toString() else arredondado.toString()
    }

    private fun String.paraLatin1Bytes(): ByteArray = ByteArray(length) { i ->
        val codigo = this[i].code
        (if (codigo in 0..255) codigo else '?'.code).toByte()
    }
}
