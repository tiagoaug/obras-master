package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §5 — hierárquica (categoriaPaiId), com natureza padrão pré-preenchida no lançamento. */
@Serializable
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

/** Adição técnica sinalizada (Fase 6.3): a spec não define nenhuma categoria RECEITA padrão, mas
 * "Venda fechada → gera LancamentoFinanceiro (RECEITA)" (SPEC_OBRA_MASTER.md §4.6) exige uma —
 * mesmo padrão de busca-por-nome já usado em Compras com "Materiais". */
const val CATEGORIA_PADRAO_RECEITA_VENDAS = "Venda de Obra"
