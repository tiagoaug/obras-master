package br.com.tiago.obramaster.core.modules

import br.com.tiago.obramaster.data.repository.ModuleConfigRepository
import br.com.tiago.obramaster.platform.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModuleAvailability(
    val module: AppModule,
    val enabled: Boolean,
    val platforms: Set<Platform> = Platform.ALL,
)

/**
 * Singleton (via Koin) que expõe o estado ligado/desligado de cada módulo.
 * Só o Gestor pode alterar (checagem de permissão fica na camada de UI/ViewModel).
 */
class ModuleRegistry(
    private val repository: ModuleConfigRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(defaultState())
    val state: StateFlow<Map<AppModule, ModuleAvailability>> = _state.asStateFlow()

    init {
        scope.launch {
            val persisted = repository.listarTodos()
            _state.value = defaultState().mapValues { (module, availability) ->
                persisted[module.id]?.let { enabled -> availability.copy(enabled = enabled) } ?: availability
            }
        }
    }

    suspend fun setEnabled(module: AppModule, enabled: Boolean) {
        repository.definir(module.id, enabled)
        _state.value = _state.value.toMutableMap().apply {
            this[module] = (this[module] ?: ModuleAvailability(module, enabled)).copy(enabled = enabled)
        }
    }

    private fun defaultState(): Map<AppModule, ModuleAvailability> =
        AppModule.entries.associateWith { ModuleAvailability(module = it, enabled = true) }
}
