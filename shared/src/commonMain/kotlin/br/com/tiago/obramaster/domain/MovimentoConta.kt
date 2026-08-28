package br.com.tiago.obramaster.domain

enum class TipoMovimentoConta { PAGAMENTO, RECEBIMENTO, TRANSFERENCIA_SAIDA, TRANSFERENCIA_ENTRADA, AJUSTE }

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §4 — saldo de uma Conta = saldoInicial + soma destes movimentos até uma data. */
data class MovimentoConta(
    val id: String,
    val contaId: String,
    val tipo: TipoMovimentoConta,
    val valor: Long, // centavos, sempre positivo — o sinal (entra/sai) vem do tipo
    val data: Long, // epoch millis UTC
    val descricao: String,
    val lancamentoFinanceiroId: String? = null,
    val transferenciaVinculoId: String? = null,
    val conciliado: Boolean = false,
)
