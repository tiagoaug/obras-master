package br.com.tiago.obramaster.ui.features.cadastros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.CorRepository
import br.com.tiago.obramaster.domain.Cor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CoresViewModel(
    private val repository: CorRepository,
) : ViewModel() {

    val cores: StateFlow<List<Cor>> = repository.observarAtivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(existente: Cor?, nome: String, hex: String, codigoFabricante: String?) {
        viewModelScope.launch {
            if (existente != null) {
                repository.atualizar(existente.copy(nome = nome, hex = hex, codigoFabricante = codigoFabricante))
            } else {
                repository.salvar(Cor(id = Uuid.random().toString(), nome = nome, hex = hex, codigoFabricante = codigoFabricante))
            }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }
}
