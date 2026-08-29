package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

/** SPEC_AREA_EXECUTOR.md §1-2 — só metadado (número, título, resumo próprio, link oficial),
 * nunca o texto integral da norma. O id é o próprio [numero] (ex.: "NBR 6118"): é uma chave
 * natural estável entre sessões, necessária pra Fase 8.6+ referenciar a norma a partir de um
 * documento salvo (`DocumentoTecnico.normaVinculadaId`). */
@Serializable
data class NormaABNT(
    val numero: String,
    val titulo: String,
    val categoria: CategoriaNorma,
    val escopoResumo: String,
    val urlCatalogoOficial: String,
    val normasRelacionadas: List<String> = emptyList(),
    val vinculadaCalculadoras: List<String> = emptyList(),
    val vinculadaEtapasTemplate: List<String> = emptyList(),
    val origemPersonalizada: Boolean = false,
) {
    val id: String get() = numero
}

@Serializable
enum class CategoriaNorma {
    FUNDACAO, ESTRUTURA, ALVENARIA, ELETRICA, HIDRAULICA,
    ACESSIBILIDADE, DESEMPENHO, ORCAMENTO_CUSTOS, SEGURANCA_TRABALHO,
    IMPERMEABILIZACAO, PROJETO_ARQUITETONICO, OUTRA,
}

val CategoriaNorma.labelPtBr: String
    get() = when (this) {
        CategoriaNorma.FUNDACAO -> "Fundação"
        CategoriaNorma.ESTRUTURA -> "Estrutura"
        CategoriaNorma.ALVENARIA -> "Alvenaria"
        CategoriaNorma.ELETRICA -> "Elétrica"
        CategoriaNorma.HIDRAULICA -> "Hidráulica"
        CategoriaNorma.ACESSIBILIDADE -> "Acessibilidade"
        CategoriaNorma.DESEMPENHO -> "Desempenho"
        CategoriaNorma.ORCAMENTO_CUSTOS -> "Orçamento e Custos"
        CategoriaNorma.SEGURANCA_TRABALHO -> "Segurança do Trabalho"
        CategoriaNorma.IMPERMEABILIZACAO -> "Impermeabilização"
        CategoriaNorma.PROJETO_ARQUITETONICO -> "Projeto Arquitetônico"
        CategoriaNorma.OUTRA -> "Outra"
    }
