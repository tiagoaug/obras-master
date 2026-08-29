package br.com.tiago.obramaster.ui.features.areaexecutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.areaexecutor.BibliotecaSearchEngine
import br.com.tiago.obramaster.data.repository.NormaABNTRepository
import br.com.tiago.obramaster.domain.NormaABNT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AreaExecutorUiState(
    val carregando: Boolean = true,
    val busca: String = "",
    val todasAsNormas: List<NormaABNT> = emptyList(),
    val resultados: List<NormaABNT> = emptyList(),
)

class AreaExecutorViewModel(
    private val repository: NormaABNTRepository,
) : ViewModel() {

    private val normas = MutableStateFlow<List<NormaABNT>>(emptyList())
    private val busca = MutableStateFlow("")
    private val carregando = MutableStateFlow(true)

    val uiState: StateFlow<AreaExecutorUiState> = combine(normas, busca, carregando) { todas, query, loading ->
        AreaExecutorUiState(
            carregando = loading,
            busca = query,
            todasAsNormas = todas,
            resultados = BibliotecaSearchEngine.buscarNormas(query, todas),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AreaExecutorUiState())

    init {
        viewModelScope.launch {
            normas.value = repository.listarTodas()
            carregando.value = false
        }
    }

    fun buscar(query: String) {
        busca.value = query
    }
}
