package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class StatusPagamento { PENDENTE, PAGO }

/**
 * SPEC_OBRA_MASTER.md §4.3. `lancamentoFinanceiroId` não está no snippet original — sem ele não
 * dá pra rastrear qual despesa no Financeiro esse pagamento gerou. Mesma lógica já aplicada em
 * RegistroTrabalho.pagamentoId: completude técnica necessária, não uma regra de negócio nova.
 */
@Serializable
data class Pagamento(
    val id: String,
    val pessoaId: String,
    val projetoId: String? = null,
    val periodo: String, // rótulo livre, ex.: "Agosto/2026" — quais registros entram é escolha explícita do usuário, não uma faixa de datas
    val valorTotal: Long, // centavos, bruto (antes de retenções)
    val dataPagamento: Long, // epoch millis UTC
    val status: StatusPagamento = StatusPagamento.PAGO,
    val comprovanteUri: String? = null,
    val lancamentoFinanceiroId: String? = null,
)
