package br.com.tiago.obramaster.domain

/** SPEC_OBRA_MASTER_KMP.md §5.1 — modelo abstrato de documento exportável, sem nenhuma API
 * gráfica. [colunas] e [linhas] são texto já formatado por quem monta o documento (a tela já usa
 * MoneyFormatter/DataFormatter etc.) — o ExportEngine só desenha/serializa, não formata número
 * nem data (a spec deixava "Coluna"/"CellValue" indefinidos; texto simples resolve sem inventar
 * um tipo rico que nada usa). */
data class ExportableDocument(
    val titulo: String,
    val subtitulo: String? = null,
    val colunas: List<String>,
    val linhas: List<List<String>>,
    val resumo: List<Pair<String, String>> = emptyList(),
    val rodape: String? = null,
    val empresa: DadosEmpresa? = null,
)
