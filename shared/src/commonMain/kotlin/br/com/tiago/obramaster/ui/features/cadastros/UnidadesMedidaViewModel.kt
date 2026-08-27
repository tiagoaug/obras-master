package br.com.tiago.obramaster.ui.features.cadastros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.UnidadeMedidaRepository
import br.com.tiago.obramaster.domain.UnidadeMedida
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UnidadesMedidaViewModel(
    private val repository: UnidadeMedidaRepository,
) : ViewModel() {

    val unidades: StateFlow<List<UnidadeMedida>> = repository.observarAtivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(existente: UnidadeMedida?, sigla: String, nome: String) {
        viewModelScope.launch {
            if (existente != null) {
                repository.atualizar(existente.copy(sigla = sigla, nome = nome))
            } else {
                repository.salvar(UnidadeMedida(id = Uuid.random().toString(), sigla = sigla, nome = nome))
            }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }
}
