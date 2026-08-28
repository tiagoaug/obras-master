package br.com.tiago.obramaster.ui.features.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.TipoCentroDeCusto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CentrosDeCustoViewModel(
    private val repository: CentroDeCustoRepository,
) : ViewModel() {

    val centros: StateFlow<List<CentroDeCusto>> = repository.observarAtivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(existente: CentroDeCusto?, nome: String, tipo: TipoCentroDeCusto) {
        viewModelScope.launch {
            if (existente != null) {
                repository.atualizar(existente.copy(nome = nome, tipo = tipo))
            } else {
                repository.salvar(CentroDeCusto(id = Uuid.random().toString(), nome = nome, tipo = tipo))
            }
        }
    }

    /** Centro de tipo PROJETO é gerado automaticamente 1:1 com um Projeto — não é excluível por aqui. */
    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }
}
