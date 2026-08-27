package br.com.tiago.obramaster.ui.features.cadastros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.CorRepository
import br.com.tiago.obramaster.data.repository.MaterialRepository
import br.com.tiago.obramaster.domain.Cor
import br.com.tiago.obramaster.domain.Material
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MateriaisUiState(
    val materiais: List<Material> = emptyList(),
    val cores: List<Cor> = emptyList(),
)

class MateriaisViewModel(
    private val repository: MaterialRepository,
    corRepository: CorRepository,
) : ViewModel() {

    val uiState: StateFlow<MateriaisUiState> = combine(
        repository.observarAtivos(),
        corRepository.observarAtivas(),
    ) { materiais, cores -> MateriaisUiState(materiais, cores) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MateriaisUiState())

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(
        existente: Material?,
        nome: String,
        unidadePadrao: String,
        precoReferencia: Long?,
        categoria: String?,
        corId: String?,
    ) {
        viewModelScope.launch {
            if (existente != null) {
                repository.atualizar(
                    existente.copy(
                        nome = nome,
                        unidadePadrao = unidadePadrao,
                        precoReferencia = precoReferencia,
                        categoria = categoria,
                        corId = corId,
                    ),
                )
            } else {
                repository.salvar(
                    Material(
                        id = Uuid.random().toString(),
                        nome = nome,
                        unidadePadrao = unidadePadrao,
                        precoReferencia = precoReferencia,
                        categoria = categoria,
                        corId = corId,
                    ),
                )
            }
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { repository.desativar(id) }
    }
}
