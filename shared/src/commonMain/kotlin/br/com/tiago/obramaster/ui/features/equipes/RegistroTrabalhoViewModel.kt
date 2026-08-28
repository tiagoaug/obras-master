package br.com.tiago.obramaster.ui.features.equipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.FuncionarioRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.data.repository.RegistroTrabalhoRepository
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.Funcionario
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.TipoRegistroTrabalho
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class RegistroTrabalhoUiState(
    val registros: List<RegistroTrabalho> = emptyList(),
    val funcionarios: List<Funcionario> = emptyList(),
    val pessoas: List<Pessoa> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val etapas: List<Etapa> = emptyList(),
)

class RegistroTrabalhoViewModel(
    private val registroRepository: RegistroTrabalhoRepository,
    private val funcionarioRepository: FuncionarioRepository,
    private val pessoaRepository: PessoaRepository,
    private val projetoRepository: ProjetoRepository,
    private val etapaRepository: EtapaRepository,
) : ViewModel() {

    val uiState: StateFlow<RegistroTrabalhoUiState> = combine(
        registroRepository.observarTodos(),
        funcionarioRepository.observarAtivos(),
        pessoaRepository.observarAtivas(),
        projetoRepository.observarAtivos(),
    ) { registros, funcionarios, pessoas, projetos ->
        RegistroTrabalhoUiState(registros = registros.sortedByDescending { it.data }, funcionarios = funcionarios, pessoas = pessoas, projetos = projetos)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RegistroTrabalhoUiState())

    suspend fun etapasDoProjeto(projetoId: String): List<Etapa> = etapaRepository.listarDoProjeto(projetoId)

    @OptIn(ExperimentalUuidApi::class)
    fun registrar(pessoaId: String, projetoId: String, etapaId: String?, data: Long, tipo: TipoRegistroTrabalho, valor: Long, observacao: String?) {
        viewModelScope.launch {
            registroRepository.salvar(
                RegistroTrabalho(
                    id = Uuid.random().toString(),
                    pessoaId = pessoaId,
                    projetoId = projetoId,
                    etapaId = etapaId,
                    data = data,
                    tipo = tipo,
                    valor = valor,
                    observacao = observacao,
                ),
            )
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { registroRepository.desativar(id) }
    }
}
