package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class TipoRegistroTrabalho { DIARIA, EMPREITADA_PARCELA, HORA_EXTRA }

/**
 * SPEC_OBRA_MASTER.md §4.3. `pago`/`pagamentoId` não estão no snippet original da spec — sem eles
 * não há como saber quais registros um Pagamento já cobriu ("app acumula → gera pagamento do
 * período" não define esse vínculo). Adição técnica necessária, não uma regra de negócio nova.
 */
@Serializable
data class RegistroTrabalho(
    val id: String,
    val pessoaId: String,
    val projetoId: String,
    val etapaId: String? = null,
    val data: Long, // epoch millis UTC
    val tipo: TipoRegistroTrabalho,
    val valor: Long, // centavos
    val observacao: String? = null,
    val pago: Boolean = false,
    val pagamentoId: String? = null,
    val ativo: Boolean = true,
)
