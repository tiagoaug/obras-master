package br.com.tiago.obramaster.ui.features.metas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.metas.MetaEngine
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.MetaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.EscopoMeta
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.TipoMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class MetaComProgresso(
    val meta: Meta,
    val valorAtual: Long,
    val percentualAtingido: Double,
    val diasRestantes: Long?,
    val atrasada: Boolean,
)

data class MetasUiState(
    val carregando: Boolean = true,
    val metas: List<MetaComProgresso> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val centrosDeCusto: List<CentroDeCusto> = emptyList(),
)

/** SPEC_OBRA_MASTER.md §4.9 — a tela só monta a UI; quem decide como valorAtual é calculado é o
 * MetaEngine (puro). Recarrega sob demanda (sem Flow reativo entre 5 repositórios diferentes)
 * porque Metas é um painel de consulta, não uma tela que precisa refletir edições de outro
 * módulo em tempo real. */
class MetasViewModel(
    private val metaRepository: MetaRepository,
    private val lancamentoRepository: LancamentoFinanceiroRepository,
    private val etapaRepository: EtapaRepository,
    private val projetoRepository: ProjetoRepository,
    private val centroDeCustoRepository: CentroDeCustoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetasUiState())
    val uiState: StateFlow<MetasUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(carregando = true)
            val metas = metaRepository.listarAtivas()
            val lancamentos = lancamentoRepository.listarAtivos()
            val etapas = etapaRepository.listarTodasAtivas()
            val projetos = projetoRepository.listarAtivos()
            val centros = centroDeCustoRepository.listarAtivos()
            val agora = Clock.System.now().toEpochMilliseconds()

            val comProgresso = metas.map { meta ->
                val valorAtual = when (meta.tipo) {
                    TipoMeta.FINANCEIRA -> MetaEngine.resultadoFinanceiro(MetaEngine.lancamentosNoEscopo(meta, lancamentos))
                    TipoMeta.PRAZO, TipoMeta.PROGRESSO -> MetaEngine.progressoMedio(MetaEngine.etapasNoEscopo(meta, etapas)).toLong()
                }
                MetaComProgresso(
                    meta = meta,
                    valorAtual = valorAtual,
                    percentualAtingido = MetaEngine.percentualAtingido(valorAtual, meta.valorAlvo),
                    diasRestantes = MetaEngine.diasRestantes(meta.prazo, agora),
                    atrasada = MetaEngine.estaAtrasada(meta, agora),
                )
            }

            _uiState.value = MetasUiState(carregando = false, metas = comProgresso, projetos = projetos, centrosDeCusto = centros)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun salvar(
        existente: Meta?,
        escopo: EscopoMeta,
        referenciaId: String?,
        titulo: String,
        tipo: TipoMeta,
        valorAlvo: Long,
        prazo: Long?,
    ) {
        viewModelScope.launch {
            if (existente != null) {
                metaRepository.atualizar(existente.copy(titulo = titulo, valorAlvo = valorAlvo, prazo = prazo))
            } else {
                metaRepository.salvar(
                    Meta(
                        id = Uuid.random().toString(),
                        escopo = escopo,
                        referenciaId = referenciaId,
                        titulo = titulo,
                        tipo = tipo,
                        valorAlvo = valorAlvo,
                        prazo = prazo,
                    ),
                )
            }
            carregar()
        }
    }

    fun marcarConcluida(meta: Meta, concluida: Boolean) {
        viewModelScope.launch {
            metaRepository.atualizar(meta.copy(concluida = concluida))
            carregar()
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch {
            metaRepository.desativar(id)
            carregar()
        }
    }
}
