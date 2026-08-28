package br.com.tiago.obramaster.ui.features.orcamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.tiago.obramaster.core.orcamentos.BdiEngine
import br.com.tiago.obramaster.core.projetos.TEMPLATE_ETAPAS_PADRAO
import br.com.tiago.obramaster.data.repository.CentroDeCustoRepository
import br.com.tiago.obramaster.data.repository.ConfigBDIRepository
import br.com.tiago.obramaster.data.repository.EtapaRepository
import br.com.tiago.obramaster.data.repository.MaterialRepository
import br.com.tiago.obramaster.data.repository.OrcamentoRepository
import br.com.tiago.obramaster.data.repository.PessoaRepository
import br.com.tiago.obramaster.data.repository.ProjetoRepository
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.ConfigBDI
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.StatusOrcamento
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.domain.TipoCentroDeCusto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class OrcamentosUiState(
    val orcamentos: List<Orcamento> = emptyList(),
    val projetos: List<Projeto> = emptyList(),
    val clientes: List<Pessoa> = emptyList(),
    val materiais: List<Material> = emptyList(),
    val configsBdi: List<ConfigBDI> = emptyList(),
)

private data class DadosReferenciaOrcamentos(
    val projetos: List<Projeto>,
    val clientes: List<Pessoa>,
    val materiais: List<Material>,
    val configsBdi: List<ConfigBDI>,
)

/**
 * SPEC_OBRA_MASTER.md §4.5 + SPEC_OBRA_MASTER_ADENDO_BDI.md.
 *
 * Decisão técnica sinalizada: o desconto (%) incide sobre o preço de venda já com BDI aplicado,
 * não sobre o custo direto — BDI existe para transformar o custo real em preço justo; desconto é
 * negociação comercial em cima desse preço. A spec não formaliza a ordem, mas essa é a única
 * leitura consistente com custoDiretoTotal = "soma dos ItemOrcamento" (literal, sem desconto).
 */
class OrcamentosViewModel(
    private val orcamentoRepository: OrcamentoRepository,
    private val projetoRepository: ProjetoRepository,
    private val pessoaRepository: PessoaRepository,
    private val materialRepository: MaterialRepository,
    private val configBdiRepository: ConfigBDIRepository,
    private val centroDeCustoRepository: CentroDeCustoRepository,
    private val etapaRepository: EtapaRepository,
) : ViewModel() {

    private val dadosReferencia = combine(
        projetoRepository.observarAtivos(),
        pessoaRepository.observarAtivas(),
        materialRepository.observarAtivos(),
        configBdiRepository.observarAtivos(),
    ) { projetos, pessoas, materiais, configs ->
        DadosReferenciaOrcamentos(projetos, pessoas.filter { TagPessoa.CLIENTE in it.tags }, materiais, configs)
    }

    val uiState: StateFlow<OrcamentosUiState> = combine(
        orcamentoRepository.observarTodos(),
        dadosReferencia,
    ) { orcamentos, referencia ->
        OrcamentosUiState(
            orcamentos = orcamentos,
            projetos = referencia.projetos,
            clientes = referencia.clientes,
            materiais = referencia.materiais,
            configsBdi = referencia.configsBdi,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrcamentosUiState())

    suspend fun itensDoOrcamento(orcamentoId: String): List<ItemOrcamento> = orcamentoRepository.itensDoOrcamento(orcamentoId)

    /** [bdiPercentualOverridePercent] em % (ex.: 25.0), não decimal — vem direto do campo de texto da tela. */
    @OptIn(ExperimentalUuidApi::class)
    fun salvar(
        existente: Orcamento?,
        projetoId: String?,
        clientePessoaId: String?,
        titulo: String,
        data: Long,
        validadeDias: Int,
        descontoPercent: Double?,
        observacoes: String?,
        configBdiId: String?,
        bdiPercentualOverridePercent: Double?,
        itens: List<ItemOrcamento>,
    ) {
        viewModelScope.launch {
            val orcamentoId = existente?.id ?: Uuid.random().toString()
            val custoDireto = itens.sumOf { it.valorTotal }
            val statusAtual = existente?.status ?: StatusOrcamento.RASCUNHO

            // Snapshot de BDI só é recalculado enquanto o orçamento continua em RASCUNHO
            // (SPEC_OBRA_MASTER_ADENDO_BDI.md §3 — congelado ao sair do rascunho).
            val bdiPercentual: Double
            val bdiCustomizado: Boolean
            if (statusAtual == StatusOrcamento.RASCUNHO) {
                if (bdiPercentualOverridePercent != null) {
                    bdiPercentual = bdiPercentualOverridePercent / 100
                    bdiCustomizado = true
                } else {
                    val config = configBdiId?.let { id -> uiState.value.configsBdi.firstOrNull { it.id == id } }
                    bdiPercentual = config?.let { BdiEngine.calcularBdi(it) } ?: 0.0
                    bdiCustomizado = false
                }
            } else {
                bdiPercentual = existente?.bdiPercentualCalculado ?: 0.0
                bdiCustomizado = existente?.bdiCustomizado ?: false
            }

            val precoComBdi = BdiEngine.precoVendaComBdiPercentual(custoDireto, bdiPercentual)
            val precoVendaTotal = if (statusAtual == StatusOrcamento.RASCUNHO) {
                if (descontoPercent != null) (precoComBdi * (1 - descontoPercent / 100)).toLong() else precoComBdi
            } else {
                existente?.precoVendaTotal ?: precoComBdi
            }

            orcamentoRepository.salvar(
                Orcamento(
                    id = orcamentoId,
                    projetoId = projetoId,
                    clientePessoaId = clientePessoaId,
                    titulo = titulo,
                    data = data,
                    validadeDias = validadeDias,
                    status = statusAtual,
                    descontoPercent = descontoPercent,
                    observacoes = observacoes,
                    configBdiId = configBdiId,
                    bdiPercentualCalculado = bdiPercentual,
                    bdiCustomizado = bdiCustomizado,
                    custoDiretoTotal = custoDireto,
                    precoVendaTotal = precoVendaTotal,
                ),
                itens.map { it.copy(orcamentoId = orcamentoId) },
            )
        }
    }

    fun atualizarStatus(orcamento: Orcamento, novoStatus: StatusOrcamento) {
        viewModelScope.launch { orcamentoRepository.atualizarStatus(orcamento.id, novoStatus) }
    }

    fun excluir(id: String) {
        viewModelScope.launch { orcamentoRepository.desativar(id) }
    }

    /** SPEC_OBRA_MASTER.md §4.5 — "Orçamento aprovado → botão 'Converter em Projeto' (cria projeto com
     * orcamentoTotal = valor aprovado)". Replica o mesmo passo a passo de ProjetosViewModel.criarProjeto
     * (não reaproveitado direto pois é privado lá): Projeto + Centro de Custo 1:1 + template de etapas padrão. */
    @OptIn(ExperimentalUuidApi::class)
    fun converterEmProjeto(orcamento: Orcamento, onCriado: (String) -> Unit) {
        viewModelScope.launch {
            val projetoId = Uuid.random().toString()
            projetoRepository.salvar(
                Projeto(
                    id = projetoId,
                    nome = orcamento.titulo,
                    clienteId = orcamento.clientePessoaId,
                    orcamentoTotal = orcamento.precoVendaTotal,
                ),
            )
            centroDeCustoRepository.salvar(
                CentroDeCusto(id = Uuid.random().toString(), nome = orcamento.titulo, tipo = TipoCentroDeCusto.PROJETO, projetoId = projetoId),
            )
            TEMPLATE_ETAPAS_PADRAO.forEachIndexed { indice, nomeEtapa ->
                etapaRepository.salvar(
                    Etapa(id = Uuid.random().toString(), projetoId = projetoId, nome = nomeEtapa, ordem = indice, orcamentoEtapa = 0L),
                )
            }
            orcamentoRepository.salvar(orcamento.copy(projetoId = projetoId), itensDoOrcamento(orcamento.id))
            onCriado(projetoId)
        }
    }
}
