package br.com.tiago.obramaster.domain

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §5 — hierárquica (categoriaPaiId), com natureza padrão pré-preenchida no lançamento. */
data class CategoriaFinanceira(
    val id: String,
    val nome: String,
    val tipo: TipoLancamento,
    val naturezaPadrao: NaturezaLancamento,
    val categoriaPaiId: String? = null,
    val cor: String,
    val icone: String? = null,
    val padraoDoSistema: Boolean = false,
    val ativo: Boolean = true,
)

/** Categorias padrão do sistema — não excluíveis, só inativáveis (SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §5). */
val CATEGORIAS_PADRAO_NOMES = listOf(
    "Materiais", "Mão de Obra", "Equipamentos", "Administrativo",
    "Impostos", "Transporte", "Alimentação de equipe", "Combustível",
)
