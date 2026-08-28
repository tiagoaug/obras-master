package br.com.tiago.obramaster.ui.features.equipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.EquipeRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.domain.Equipe
import br.com.tiago.obramaster.domain.Pessoa
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EquipesUiState(
    val equipes: List<Equipe> = emptyList(),
    val pessoas: List<Pessoa> = emptyList(),
)

class EquipesViewModel(
    private val equipeRepository: EquipeRepository,
    private val pessoaRepository: PessoaRepository,
) : ViewModel() {

    val uiState: StateFlow<EquipesUiState> = combine(
        equipeRepository.observarAtivas(),
        pessoaRepository.observarAtivas(),
    ) { equipes, pessoas -> EquipesUiState(equipes = equipes, pessoas = pessoas) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EquipesUiState())

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(existente: Equipe?, nome: String, liderPessoaId: String?, membrosIds: Set<String>) {
        viewModelScope.launch {
            if (existente != null) {
                equipeRepository.atualizar(existente.copy(nome = nome, liderPessoaId = liderPessoaId, membrosIds = membrosIds))
            } else {
                equipeRepository.salvar(Equipe(id = Uuid.random().toString(), nome = nome, liderPessoaId = liderPessoaId, membrosIds = membrosIds))
            }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { equipeRepository.desativar(id) }
    }
}
