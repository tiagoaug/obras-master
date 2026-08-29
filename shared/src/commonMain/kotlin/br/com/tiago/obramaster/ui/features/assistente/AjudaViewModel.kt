package br.com.tiago.obramaster.ui.features.assistente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.assistant.ManualSection
import br.com.tiago.obramaster.data.repository.ManualRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AjudaViewModel(manualRepository: ManualRepository) : ViewModel() {
    private val _secoes = MutableStateFlow<List<ManualSection>>(emptyList())
    val secoes: StateFlow<List<ManualSection>> = _secoes.asStateFlow()

    init {
        viewModelScope.launch { _secoes.value = manualRepository.listarSecoes() }
    }
}
