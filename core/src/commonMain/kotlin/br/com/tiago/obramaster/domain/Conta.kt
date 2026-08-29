package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

@Serializable
enum class TipoConta { CAIXA, CONTA_CORRENTE, POUPANCA, CARTAO_CREDITO, INVESTIMENTO }

/** SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §4 — saldoInicial em Long (centavos), dataSaldoInicial em epoch millis UTC. */
@Serializable
data class Conta(
    val id: String,
    val nome: String,
    val tipo: TipoConta,
    val banco: String? = null,
    val agencia: String? = null,
    val numeroConta: String? = null,
    val saldoInicial: Long,
    val dataSaldoInicial: Long,
    val ativo: Boolean = true,
    val cor: String? = null,
)
