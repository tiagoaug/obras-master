package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class TipoLancamento { RECEITA, DESPESA }

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §2 — Contábil entra no DRE oficial, Não Contábil só no controle gerencial. */
@Serializable
enum class NaturezaLancamento { CONTABIL, NAO_CONTABIL }

@Serializable
data class LancamentoFinanceiro(
    val id: String,
    val tipo: TipoLancamento,
    val categoriaId: String,
    val centroDeCustoId: String,
    val natureza: NaturezaLancamento,
    val projetoId: String? = null,
    val etapaId: String? = null,
    val descricao: String,
    val valor: Long, // centavos
    val data: Long, // epoch millis UTC
    val formaPagamento: String,
    val pago: Boolean = false,
    val pessoaId: String? = null,
    val anexoUri: String? = null,
    val contaId: String? = null, // preparado pra Fase 4.2 (Contas) — não usado ainda nesta fase
    val ativo: Boolean = true,
)
