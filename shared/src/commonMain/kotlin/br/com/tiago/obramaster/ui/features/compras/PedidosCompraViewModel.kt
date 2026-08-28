package br.com.tiago.obramaster.ui.features.compras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.data.repository.CategoriaFinanceiraRepository
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.FornecedorRepository
import br.com.tiago.obramaster.data.repository.LancamentoFinanceiroRepository
import br.com.tiago.obramaster.data.repository.MaterialRepository
import br.com.tiago.obramaster.data.repository.PedidoCompraRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import br.com.tiago.obramaster.domain.TipoLancamento
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class PedidosCompraUiState(
    val pedidos: List<PedidoCompra> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val fornecedores: List<Pessoa> = emptyList(), // pessoas com registro de Fornecedor
    val materiais: List<Material> = emptyList(),
    val centros: List<CentroDeCusto> = emptyList(),
)

private data class DadosReferenciaCompras(
    val projetos: List<Projeto>,
    val fornecedores: List<Pessoa>,
    val materiais: List<Material>,
    val centros: List<CentroDeCusto>,
)

/** SPEC_OBRA_MASTER.md §4.4 — "Ao marcar como COMPRADO → gera LancamentoFinanceiro (DESPESA, categoria 'Materiais') automaticamente." */
class PedidosCompraViewModel(
    private val pedidoRepository: PedidoCompraRepository,
    private val projetoRepository: ProjetoRepository,
    private val fornecedorRepository: FornecedorRepository,
    private val pessoaRepository: PessoaRepository,
    private val materialRepository: MaterialRepository,
    private val centroRepository: CentroDeCustoRepository,
    private val lancamentoRepository: LancamentoFinanceiroRepository,
    private val categoriaRepository: CategoriaFinanceiraRepository,
    private val etapaRepository: EtapaRepository,
) : ViewModel() {

    private val dadosReferencia = combine(
        projetoRepository.observarAtivos(),
        fornecedorRepository.observarAtivos(),
        pessoaRepository.observarAtivas(),
        materialRepository.observarAtivos(),
        centroRepository.observarAtivos(),
    ) { projetos, fornecedores, pessoas, materiais, centros ->
        val idsFornecedores = fornecedores.map { it.pessoaId }.toSet()
        DadosReferenciaCompras(projetos, pessoas.filter { it.id in idsFornecedores }, materiais, centros)
    }

    val uiState: StateFlow<PedidosCompraUiState> = combine(
        pedidoRepository.observarTodos(),
        dadosReferencia,
    ) { pedidos, referencia ->
        PedidosCompraUiState(
            pedidos = pedidos,
            projetos = referencia.projetos,
            fornecedores = referencia.fornecedores,
            materiais = referencia.materiais,
            centros = referencia.centros,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PedidosCompraUiState())

    suspend fun etapasDoProjeto(projetoId: String): List<Etapa> = etapaRepository.listarDoProjeto(projetoId)
    suspend fun itensDoPedido(pedidoId: String): List<ItemCompra> = pedidoRepository.itensDoPedido(pedidoId)
    suspend fun itensDeTodos(): List<ItemCompra> = pedidoRepository.itensDeTodos()

    @OptIn(ExperimentalUuidApi::class)
    fun salvarPedido(projetoId: String, etapaId: String?, fornecedorId: String?, data: Long, itens: List<ItemCompra>) {
        viewModelScope.launch {
            val pedidoId = Uuid.random().toString()
            val valorTotal = itens.sumOf { it.valorTotal }
            pedidoRepository.salvar(
                PedidoCompra(id = pedidoId, projetoId = projetoId, etapaId = etapaId, fornecedorId = fornecedorId, data = data, valorTotal = valorTotal),
                itens.map { it.copy(pedidoId = pedidoId) },
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun atualizarStatus(pedido: PedidoCompra, novoStatus: StatusPedidoCompra) {
        viewModelScope.launch {
            var lancamentoFinanceiroId = pedido.lancamentoFinanceiroId
            if (novoStatus == StatusPedidoCompra.COMPRADO && pedido.status != StatusPedidoCompra.COMPRADO) {
                val categoriaMateriais = categoriaRepository.listarAtivas().firstOrNull { it.nome == "Materiais" }
                val centro = uiState.value.centros.firstOrNull { it.projetoId == pedido.projetoId }
                if (categoriaMateriais != null && centro != null) {
                    val nomeFornecedor = pedido.fornecedorId?.let { id -> uiState.value.fornecedores.firstOrNull { it.id == id }?.nome }
                    val nomeProjeto = uiState.value.projetos.firstOrNull { it.id == pedido.projetoId }?.nome ?: ""
                    val id = Uuid.random().toString()
                    lancamentoRepository.salvar(
                        LancamentoFinanceiro(
                            id = id,
                            tipo = TipoLancamento.DESPESA,
                            categoriaId = categoriaMateriais.id,
                            centroDeCustoId = centro.id,
                            natureza = NaturezaLancamento.CONTABIL,
                            projetoId = pedido.projetoId,
                            etapaId = pedido.etapaId,
                            descricao = "Compra${nomeFornecedor?.let { " - $it" } ?: ""} - $nomeProjeto",
                            valor = pedido.valorTotal,
                            data = pedido.data,
                            formaPagamento = "",
                            pago = false,
                        ),
                    )
                    lancamentoFinanceiroId = id
                }
            }
            pedidoRepository.atualizarStatus(pedido.id, novoStatus, lancamentoFinanceiroId)
        }
    }

    fun excluir(id: String) {
        viewModelScope.launch { pedidoRepository.desativar(id) }
    }
}
