package br.com.tiago.obramaster.ui.features.projetos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.DiarioObraRepository
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.domain.DiarioObra
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.platform.ImagePicker
import br.com.tiago.obramaster.platform.ImageStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class DiarioObraUiState(
    val registros: List<DiarioObra> = emptyList(),
    val etapas: List<Etapa> = emptyList(),
)

/** SPEC_OBRA_MASTER.md §4.8 — diário de obra com registro fotográfico por data/etapa. */
class DiarioObraViewModel(
    private val projetoId: String,
    private val diarioRepository: DiarioObraRepository,
    private val etapaRepository: EtapaRepository,
    private val imagePicker: ImagePicker,
    private val imageStore: ImageStore,
) : ViewModel() {

    val uiState: StateFlow<DiarioObraUiState> = combine(
        diarioRepository.observarDoProjeto(projetoId),
        etapaRepository.observarDoProjeto(projetoId),
    ) { registros, etapas -> DiarioObraUiState(registros = registros, etapas = etapas.sortedBy { it.ordem }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiarioObraUiState())

    suspend fun imagemDisponivel(): Boolean = imagePicker.isAvailable()

    /** Fotos são comprimidas e salvas no storage interno assim que capturadas/escolhidas
     * (SPEC_OBRA_MASTER.md §4.8) — a chave retornada é o que fica em DiarioObra.fotosUris. */
    suspend fun tirarFoto(): String? = imagePicker.takePhoto()?.let { imageStore.save(it) }
    suspend fun escolherDaGaleria(): String? = imagePicker.pickFromGallery().firstOrNull()?.let { imageStore.save(it) }
    suspend fun carregarFoto(chave: String): ByteArray? = imageStore.load(chave)

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(existente: DiarioObra?, etapaId: String?, data: Long, texto: String, clima: String?, fotosUris: List<String>) {
        viewModelScope.launch {
            val diario = DiarioObra(
                id = existente?.id ?: Uuid.random().toString(),
                projetoId = projetoId,
                etapaId = etapaId,
                data = data,
                texto = texto,
                clima = clima,
                fotosUris = fotosUris,
            )
            if (existente != null) diarioRepository.atualizar(diario) else diarioRepository.salvar(diario)
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { diarioRepository.desativar(id) }
    }
}
