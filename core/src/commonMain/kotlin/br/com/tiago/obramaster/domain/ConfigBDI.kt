package br.com.tiago.obramaster.domain

import kotlinx.serialization.Serializable

/** SPEC_OBRA_MASTER_ADENDO_BDI.md §3 — perfis de BDI, cadastrados em Configurações → BDI. Taxas em decimal (5% = 0.05). */
@Serializable
data class ConfigBDI(
    val id: String,
    val nome: String,
    val administracaoCentral: Double,
    val seguroGarantia: Double,
    val riscos: Double,
    val despesasFinanceiras: Double,
    val lucro: Double,
    val tributos: Double,
    val padrao: Boolean = false,
    val ativo: Boolean = true,
)
