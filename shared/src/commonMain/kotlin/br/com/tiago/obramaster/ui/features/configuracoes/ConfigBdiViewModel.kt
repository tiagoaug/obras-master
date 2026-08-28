package br.com.tiago.obramaster.ui.features.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.ConfigBDIRepository
import br.com.tiago.obramaster.domain.ConfigBDI
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** SPEC_OBRA_MASTER_ADENDO_BDI.md §3 — cadastro em Configurações → BDI, múltiplos perfis. */
class ConfigBdiViewModel(
    private val repository: ConfigBDIRepository,
) : ViewModel() {

    val perfis: StateFlow<List<ConfigBDI>> = repository.observarAtivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(
        existente: ConfigBDI?,
        nome: String,
        administracaoCentral: Double,
        seguroGarantia: Double,
        riscos: Double,
        despesasFinanceiras: Double,
        lucro: Double,
        tributos: Double,
        padrao: Boolean,
    ) {
        viewModelScope.launch {
            if (existente != null) {
                repository.atualizar(
                    existente.copy(
                        nome = nome,
                        administracaoCentral = administracaoCentral,
                        seguroGarantia = seguroGarantia,
                        riscos = riscos,
                        despesasFinanceiras = despesasFinanceiras,
                        lucro = lucro,
                        tributos = tributos,
                        padrao = padrao,
                    ),
                )
            } else {
                repository.salvar(
                    ConfigBDI(
                        id = Uuid.random().toString(),
                        nome = nome,
                        administracaoCentral = administracaoCentral,
                        seguroGarantia = seguroGarantia,
                        riscos = riscos,
                        despesasFinanceiras = despesasFinanceiras,
                        lucro = lucro,
                        tributos = tributos,
                        padrao = padrao,
                    ),
                )
            }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }
}
