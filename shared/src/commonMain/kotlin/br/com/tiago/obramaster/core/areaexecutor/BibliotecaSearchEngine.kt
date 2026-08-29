package br.com.tiago.obramaster.core.areaexecutor

import br.com.tiago.obramaster.domain.NormaABNT
import br.com.tiago.obramaster.domain.labelPtBr

/** SPEC_AREA_EXECUTOR.md §3 — busca simples no catálogo de normas (poucos registros, sem
 * necessidade de índice). A busca full-text nos manuais em PDF (`buscarDocumentos`) entra na
 * Fase 8.7, junto com o `DocumentStore`/`PdfTextExtractor` da Fase 8.6. */
object BibliotecaSearchEngine {

    fun buscarNormas(query: String, normas: List<NormaABNT>): List<NormaABNT> {
        if (query.isBlank()) return normas
        val termo = query.trim()
        return normas.filter { norma ->
            norma.numero.contains(termo, ignoreCase = true) ||
                norma.titulo.contains(termo, ignoreCase = true) ||
                norma.categoria.labelPtBr.contains(termo, ignoreCase = true)
        }
    }
}
