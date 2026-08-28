package br.com.tiago.obramaster.ui.features.equipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.financeiro.FinanceEngine
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.ContaRepository
import br.com.tiago.obramaster.data.repository.FuncionarioRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.MovimentoContaRepository
import br.com.tiago.obramaster.data.repository.PagamentoRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.data.repository.RegistroTrabalhoRepository
import br.com.tiago.obramaster.data.repository.RetencaoLancamentoRepository
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.MovimentoConta
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.Pagamento
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.RetencaoLancamento
import br.com.tiago.obramaster.domain.StatusPagamento
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.domain.TipoMovimentoConta
import br.com.tiago.obramaster.domain.TipoRetencao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class GerarPagamentoUiState(
    val funcionarios: List<FuncionarioComPessoa> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val centros: List<CentroDeCusto> = emptyList(),
    val contas: List<Conta> = emptyList(),
)

/**
 * SPEC_OBRA_MASTER.md §4.3 — "registrar diárias/empreitadas → app acumula → gerar pagamento do
 * período → marcar como pago → gera automaticamente LancamentoFinanceiro (DESPESA, categoria
 * 'Mão de Obra') vinculado ao projeto/etapa"; SPEC_OBRA_MASTER_ADENDO_FINANCEIRO.md §6 — pagamento
 * de mão de obra debita a Conta escolhida, com suporte a retenções.
 */
class GerarPagamentoViewModel(
    private val registroRepository: RegistroTrabalhoRepository,
    private val pagamentoRepository: PagamentoRepository,
    private val lancamentoRepository: LancamentoFinanceiroRepository,
    private val retencaoRepository: RetencaoLancamentoRepository,
    private val movimentoContaRepository: MovimentoContaRepository,
    private val categoriaRepository: CategoriaFinanceiraRepository,
    private val centroRepository: CentroDeCustoRepository,
    private val contaRepository: ContaRepository,
    private val projetoRepository: ProjetoRepository,
    funcionarioRepository: FuncionarioRepository,
    pessoaRepository: PessoaRepository,
) : ViewModel() {

    val uiState: StateFlow<GerarPagamentoUiState> = combine(
        funcionarioRepository.observarAtivos(),
        pessoaRepository.observarAtivas(),
        projetoRepository.observarAtivos(),
        centroRepository.observarAtivos(),
        contaRepository.observarAtivas(),
    ) { funcionarios, pessoas, projetos, centros, contas ->
        val pessoasPorId = pessoas.associateBy { it.id }
        GerarPagamentoUiState(
            funcionarios = funcionarios.mapNotNull { funcionario -> pessoasPorId[funcionario.pessoaId]?.let { FuncionarioComPessoa(it, funcionario) } },
            projetos = projetos,
            centros = centros,
            contas = contas,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GerarPagamentoUiState())

    suspend fun registrosPendentes(pessoaId: String): List<RegistroTrabalho> = registroRepository.listarNaoPagosDaPessoa(pessoaId)

    @OptIn(ExperimentalUuidApi::class)
    fun gerarPagamento(
        pessoaId: String,
        nomePessoa: String,
        projetoId: String?,
        centroDeCustoId: String,
        periodo: String,
        registros: List<RegistroTrabalho>,
        dataPagamento: Long,
        contaId: String,
        retencoes: List<Pair<TipoRetencao, Double>>,
    ) {
        if (registros.isEmpty()) return
        viewModelScope.launch {
            val valorTotal = registros.sumOf { it.valor }
            val categoriaMaoDeObra = categoriaRepository.listarAtivas().firstOrNull { it.nome == "Mão de Obra" }
            val lancamentoId = Uuid.random().toString()
            val pagamentoId = Uuid.random().toString()
            val descricao = "Pagamento - $nomePessoa - $periodo"

            var lancamentoFinanceiroId: String? = null
            if (categoriaMaoDeObra != null) {
                lancamentoRepository.salvar(
                    LancamentoFinanceiro(
                        id = lancamentoId,
                        tipo = TipoLancamento.DESPESA,
                        categoriaId = categoriaMaoDeObra.id,
                        centroDeCustoId = centroDeCustoId,
                        natureza = NaturezaLancamento.CONTABIL,
                        projetoId = projetoId,
                        descricao = descricao,
                        valor = valorTotal,
                        data = dataPagamento,
                        formaPagamento = "",
                        pago = true,
                        contaId = contaId,
                    ),
                )
                val retencoesSalvas = retencoes.map { (tipo, percentual) ->
                    RetencaoLancamento(
                        id = Uuid.random().toString(),
                        lancamentoId = lancamentoId,
                        tipo = tipo,
                        percentual = percentual,
                        valorCalculado = FinanceEngine.calcularValorRetencao(valorTotal, percentual),
                    )
                }
                retencaoRepository.substituir(lancamentoId, retencoesSalvas)
                movimentoContaRepository.salvar(
                    MovimentoConta(
                        id = Uuid.random().toString(),
                        contaId = contaId,
                        tipo = TipoMovimentoConta.PAGAMENTO,
                        valor = FinanceEngine.valorLiquido(valorTotal, retencoesSalvas),
                        data = dataPagamento,
                        descricao = descricao,
                        lancamentoFinanceiroId = lancamentoId,
                    ),
                )
                lancamentoFinanceiroId = lancamentoId
            }

            pagamentoRepository.salvar(
                Pagamento(
                    id = pagamentoId,
                    pessoaId = pessoaId,
                    projetoId = projetoId,
                    periodo = periodo,
                    valorTotal = valorTotal,
                    dataPagamento = dataPagamento,
                    status = StatusPagamento.PAGO,
                    lancamentoFinanceiroId = lancamentoFinanceiroId,
                ),
            )
            registroRepository.marcarPagos(registros.map { it.id }, pagamentoId)
        }
    }
}
