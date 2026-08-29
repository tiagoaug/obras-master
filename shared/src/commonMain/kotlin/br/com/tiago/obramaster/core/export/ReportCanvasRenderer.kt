package br.com.tiago.obramaster.core.export

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.platform.desenharTexto
import kotlin.math.max

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §1 — desenha o ExportableDocument num ImageBitmap "cru"
 * (CanvasDrawScope ligado direto a um Canvas de bitmap, sem composição) — a API de captura de
 * Composable (GraphicsLayer) não está disponível na versão de Compose deste projeto (ver decisão
 * registrada na Fase 9.2). Layout simples e fixo: colunas de largura igual, uma linha por célula
 * (sem quebra automática), texto truncado por estimativa de largura — não é tipografia de
 * verdade, é o suficiente pra um relatório tabular legível. */
object ReportCanvasRenderer {

    private const val LARGURA_PADRAO = 1240f
    private const val MARGEM = 32f
    private const val ALTURA_LINHA = 26f
    private const val ALTURA_LINHA_TITULO = 40f
    private const val ALTURA_LINHA_SECUNDARIA = 24f
    private const val TAMANHO_FONTE_EMPRESA = 15f
    private const val TAMANHO_FONTE_TITULO = 24f
    private const val TAMANHO_FONTE_SUBTITULO = 14f
    private const val TAMANHO_FONTE_CABECALHO = 14f
    private const val TAMANHO_FONTE_CORPO = 13f
    private const val TAMANHO_FONTE_RODAPE = 11f
    private const val LARGURA_MEDIA_CARACTERE = 0.55f
    private const val COR_TEXTO_PRINCIPAL = 0xFF111111
    private const val COR_TEXTO_SECUNDARIO = 0xFF666666
    private const val COR_LINHA_DIVISORIA = 0xFFCCCCCC

    fun renderizar(doc: ExportableDocument, largura: Float = LARGURA_PADRAO): ImageBitmap {
        val altura = calcularAltura(doc)
        val bitmap = ImageBitmap(largura.toInt(), altura.toInt())
        val canvas = Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(Density(1f), LayoutDirection.Ltr, canvas, Size(largura, altura)) {
            drawRect(Color.White, size = size)

            var y = MARGEM
            doc.empresa?.let { empresa ->
                desenharTexto(empresa.nome, MARGEM, y + TAMANHO_FONTE_EMPRESA, TAMANHO_FONTE_EMPRESA, Color(COR_TEXTO_SECUNDARIO))
                y += ALTURA_LINHA_SECUNDARIA
            }

            desenharTexto(doc.titulo, MARGEM, y + TAMANHO_FONTE_TITULO, TAMANHO_FONTE_TITULO, Color(COR_TEXTO_PRINCIPAL))
            y += ALTURA_LINHA_TITULO

            doc.subtitulo?.let { subtitulo ->
                desenharTexto(subtitulo, MARGEM, y + TAMANHO_FONTE_SUBTITULO, TAMANHO_FONTE_SUBTITULO, Color(COR_TEXTO_SECUNDARIO))
                y += ALTURA_LINHA_SECUNDARIA
            }

            y += 8f
            drawLine(Color(COR_LINHA_DIVISORIA), Offset(MARGEM, y), Offset(largura - MARGEM, y))
            y += 16f

            val larguraColuna = (largura - 2 * MARGEM) / max(doc.colunas.size, 1)
            doc.colunas.forEachIndexed { indice, coluna ->
                val x = MARGEM + indice * larguraColuna
                desenharTexto(truncar(coluna, larguraColuna), x, y + TAMANHO_FONTE_CABECALHO, TAMANHO_FONTE_CABECALHO, Color(COR_TEXTO_PRINCIPAL))
            }
            y += ALTURA_LINHA - 4f
            drawLine(Color(COR_LINHA_DIVISORIA), Offset(MARGEM, y), Offset(largura - MARGEM, y))
            y += 12f

            doc.linhas.forEach { linha ->
                linha.forEachIndexed { indice, valor ->
                    val x = MARGEM + indice * larguraColuna
                    desenharTexto(truncar(valor, larguraColuna), x, y + TAMANHO_FONTE_CORPO, TAMANHO_FONTE_CORPO, Color(COR_TEXTO_PRINCIPAL))
                }
                y += ALTURA_LINHA
            }

            if (doc.resumo.isNotEmpty()) {
                y += 4f
                drawLine(Color(COR_LINHA_DIVISORIA), Offset(MARGEM, y), Offset(largura - MARGEM, y))
                y += 16f
                doc.resumo.forEach { (rotulo, valor) ->
                    desenharTexto(rotulo, MARGEM, y + TAMANHO_FONTE_CORPO, TAMANHO_FONTE_CORPO, Color(COR_TEXTO_PRINCIPAL))
                    desenharTexto(valor, largura - MARGEM - valor.length * TAMANHO_FONTE_CORPO * LARGURA_MEDIA_CARACTERE, y + TAMANHO_FONTE_CORPO, TAMANHO_FONTE_CORPO, Color(COR_TEXTO_PRINCIPAL))
                    y += ALTURA_LINHA
                }
            }

            doc.rodape?.let { rodape ->
                y += 12f
                desenharTexto(rodape, MARGEM, y + TAMANHO_FONTE_RODAPE, TAMANHO_FONTE_RODAPE, Color(COR_TEXTO_SECUNDARIO))
            }
        }

        return bitmap
    }

    private fun calcularAltura(doc: ExportableDocument): Float {
        var altura = MARGEM * 2
        if (doc.empresa != null) altura += ALTURA_LINHA_SECUNDARIA
        altura += ALTURA_LINHA_TITULO
        if (doc.subtitulo != null) altura += ALTURA_LINHA_SECUNDARIA
        altura += 8f + 16f // divisória antes da tabela
        altura += ALTURA_LINHA - 4f + 12f // cabeçalho da tabela
        altura += doc.linhas.size * ALTURA_LINHA
        if (doc.resumo.isNotEmpty()) altura += 4f + 16f + doc.resumo.size * ALTURA_LINHA
        if (doc.rodape != null) altura += 12f + ALTURA_LINHA_SECUNDARIA
        return altura
    }

    /** Estimativa grosseira de caracteres por largura — não é medição de texto de verdade (isso
     * exigiria TextMeasurer, indisponível fora de composição nesta versão do Compose), só evita
     * que uma célula muito longa invada a coluna vizinha. */
    private fun truncar(texto: String, larguraColuna: Float, tamanhoFonte: Float = TAMANHO_FONTE_CORPO): String {
        val maxCaracteres = (larguraColuna / (tamanhoFonte * LARGURA_MEDIA_CARACTERE)).toInt().coerceAtLeast(1)
        if (texto.length <= maxCaracteres) return texto
        return texto.take((maxCaracteres - 1).coerceAtLeast(0)) + "…"
    }
}
