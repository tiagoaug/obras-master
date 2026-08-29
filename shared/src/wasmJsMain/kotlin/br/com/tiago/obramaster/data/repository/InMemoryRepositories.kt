package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.domain.Abertura
import br.com.tiago.obramaster.domain.ArquivoImportado
import br.com.tiago.obramaster.domain.CATEGORIAS_PADRAO_NOMES
import br.com.tiago.obramaster.domain.CategoriaFinanceira
import br.com.tiago.obramaster.domain.CentroDeCusto
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.CATEGORIA_PADRAO_RECEITA_VENDAS
import br.com.tiago.obramaster.domain.ConfigBDI
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.Cor
import br.com.tiago.obramaster.domain.DadosEmpresa
import br.com.tiago.obramaster.domain.DiarioObra
import br.com.tiago.obramaster.domain.DocumentoTecnico
import br.com.tiago.obramaster.domain.Equipe
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.Fornecedor
import br.com.tiago.obramaster.domain.Funcionario
import br.com.tiago.obramaster.domain.ItemCompra
import br.com.tiago.obramaster.domain.ItemOrcamento
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.domain.MovimentoConta
import br.com.tiago.obramaster.domain.NaturezaLancamento
import br.com.tiago.obramaster.domain.Orcamento
import br.com.tiago.obramaster.domain.Pagamento
import br.com.tiago.obramaster.domain.ParcelaVenda
import br.com.tiago.obramaster.domain.Parede
import br.com.tiago.obramaster.domain.PedidoCompra
import br.com.tiago.obramaster.domain.RateioLancamento
import br.com.tiago.obramaster.domain.RegistroTrabalho
import br.com.tiago.obramaster.domain.RetencaoLancamento
import br.com.tiago.obramaster.domain.StatusOrcamento
import br.com.tiago.obramaster.domain.StatusPedidoCompra
import br.com.tiago.obramaster.domain.StatusVenda
import br.com.tiago.obramaster.domain.Tarefa
import br.com.tiago.obramaster.domain.TipoLancamento
import br.com.tiago.obramaster.domain.Venda
import br.com.tiago.obramaster.domain.Permissao
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.PlantaBaixa
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.domain.UnidadeMedida
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Substituto temporário só para o alvo Web enquanto não há backend (Fase 10) — a spec
 * (SPEC_OBRA_MASTER_KMP.md §6.1) já define Web como online-first, então o repositório real
 * de lá vai falar com a API, não com SQLDelight local (cujo driver web é assíncrono, ver
 * DatabaseDriverFactory.wasmJs.kt). Dados aqui não sobrevivem a um refresh da página.
 */
class InMemoryColaboradorRepository : ColaboradorRepository {
    private val state = MutableStateFlow<List<Colaborador>>(emptyList())

    override suspend fun listarAtivos(): List<Colaborador> = state.value.filter { it.ativo }
    override suspend fun buscarPorId(id: String): Colaborador? = state.value.firstOrNull { it.id == id }
    override suspend fun buscarPorLogin(login: String): Colaborador? =
        state.value.firstOrNull { it.login == login && it.ativo }

    override suspend fun existeAlgumColaborador(): Boolean = state.value.any { it.ativo }

    override suspend fun salvar(colaborador: Colaborador) {
        state.value = state.value + colaborador
    }

    override suspend fun atualizar(colaborador: Colaborador) {
        state.value = state.value.map { if (it.id == colaborador.id) colaborador else it }
    }

    override suspend fun atualizarSenha(id: String, senhaHash: String, salt: String) {
        state.value = state.value.map { if (it.id == id) it.copy(senhaHash = senhaHash, salt = salt) else it }
    }

    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }

    override fun observarAtivos(): Flow<List<Colaborador>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryPermissaoRepository : PermissaoRepository {
    private val state = MutableStateFlow<List<Permissao>>(emptyList())

    override suspend fun listarTodas(): List<Permissao> = state.value
    override suspend fun listarPorColaborador(colaboradorId: String): List<Permissao> =
        state.value.filter { it.colaboradorId == colaboradorId }

    override suspend fun definir(colaboradorId: String, moduleId: String, nivel: NivelPermissao) {
        val semAtual = state.value.filterNot { it.colaboradorId == colaboradorId && it.moduleId == moduleId }
        state.value = semAtual + Permissao(colaboradorId, moduleId, nivel)
    }

    override suspend fun removerTodasDoColaborador(colaboradorId: String) {
        state.value = state.value.filterNot { it.colaboradorId == colaboradorId }
    }

    override fun observarTodas(): Flow<List<Permissao>> = state
}

class InMemoryModuleConfigRepository : ModuleConfigRepository {
    private val state = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    override suspend fun listarTodos(): Map<String, Boolean> = state.value

    override suspend fun definir(moduleId: String, enabled: Boolean) {
        state.value = state.value + (moduleId to enabled)
    }

    override fun observarTodos(): Flow<Map<String, Boolean>> = state
}

class InMemoryEmpresaRepository : EmpresaRepository {
    private var empresa: DadosEmpresa? = null

    override suspend fun buscar(): DadosEmpresa? = empresa
    override suspend fun salvar(empresa: DadosEmpresa) {
        this.empresa = empresa
    }
}

class InMemoryContaRepository : ContaRepository {
    private val state = MutableStateFlow<List<Conta>>(emptyList())

    override suspend fun listarAtivas(): List<Conta> = state.value.filter { it.ativo }
    override suspend fun buscarPorId(id: String): Conta? = state.value.firstOrNull { it.id == id }
    override suspend fun salvar(conta: Conta) {
        state.value = state.value + conta
    }
    override suspend fun atualizar(conta: Conta) {
        state.value = state.value.map { if (it.id == conta.id) conta else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<Conta>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryPessoaRepository : PessoaRepository {
    private val state = MutableStateFlow<List<Pessoa>>(emptyList())

    override suspend fun listarAtivas(): List<Pessoa> = state.value.filter { it.ativo }
    override suspend fun salvar(pessoa: Pessoa) {
        state.value = state.value + pessoa
    }
    override suspend fun atualizar(pessoa: Pessoa) {
        state.value = state.value.map { if (it.id == pessoa.id) pessoa else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<Pessoa>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryCorRepository : CorRepository {
    private val state = MutableStateFlow<List<Cor>>(emptyList())

    override suspend fun listarAtivas(): List<Cor> = state.value.filter { it.ativo }
    override suspend fun salvar(cor: Cor) {
        state.value = state.value + cor
    }
    override suspend fun atualizar(cor: Cor) {
        state.value = state.value.map { if (it.id == cor.id) cor else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<Cor>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryMaterialRepository : MaterialRepository {
    private val state = MutableStateFlow<List<Material>>(emptyList())

    override suspend fun listarAtivos(): List<Material> = state.value.filter { it.ativo }
    override suspend fun salvar(material: Material) {
        state.value = state.value + material
    }
    override suspend fun atualizar(material: Material) {
        state.value = state.value.map { if (it.id == material.id) material else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivos(): Flow<List<Material>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryUnidadeMedidaRepository : UnidadeMedidaRepository {
    private val state = MutableStateFlow<List<UnidadeMedida>>(emptyList())

    override suspend fun listarAtivas(): List<UnidadeMedida> = state.value.filter { it.ativo }
    override suspend fun salvar(unidade: UnidadeMedida) {
        state.value = state.value + unidade
    }
    override suspend fun atualizar(unidade: UnidadeMedida) {
        state.value = state.value.map { if (it.id == unidade.id) unidade else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<UnidadeMedida>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryProjetoRepository : ProjetoRepository {
    private val state = MutableStateFlow<List<Projeto>>(emptyList())

    override suspend fun listarAtivos(): List<Projeto> = state.value.filter { it.ativo }
    override suspend fun buscarPorId(id: String): Projeto? = state.value.firstOrNull { it.id == id }
    override suspend fun salvar(projeto: Projeto) {
        state.value = state.value + projeto
    }
    override suspend fun atualizar(projeto: Projeto) {
        state.value = state.value.map { if (it.id == projeto.id) projeto else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivos(): Flow<List<Projeto>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryEtapaRepository : EtapaRepository {
    private val state = MutableStateFlow<List<Etapa>>(emptyList())

    override suspend fun listarDoProjeto(projetoId: String): List<Etapa> =
        state.value.filter { it.projetoId == projetoId && it.ativo }.sortedBy { it.ordem }

    override suspend fun listarTodasAtivas(): List<Etapa> = state.value.filter { it.ativo }

    override suspend fun salvar(etapa: Etapa) {
        state.value = state.value + etapa
    }
    override suspend fun atualizar(etapa: Etapa) {
        state.value = state.value.map { if (it.id == etapa.id) etapa else it }
    }
    override suspend fun reordenar(etapaId: String, novaOrdem: Int) {
        state.value = state.value.map { if (it.id == etapaId) it.copy(ordem = novaOrdem) else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarDoProjeto(projetoId: String): Flow<List<Etapa>> =
        state.map { lista -> lista.filter { it.projetoId == projetoId && it.ativo }.sortedBy { it.ordem } }
}

class InMemoryPlantaBaixaRepository : PlantaBaixaRepository {
    private val state = MutableStateFlow<List<PlantaBaixa>>(emptyList())

    override suspend fun listarDoProjeto(projetoId: String): List<PlantaBaixa> =
        state.value.filter { it.projetoId == projetoId && it.ativo }

    override suspend fun buscarPorId(id: String): PlantaBaixa? = state.value.firstOrNull { it.id == id }

    override suspend fun salvar(planta: PlantaBaixa) {
        state.value = state.value + planta
    }

    override suspend fun atualizarEscala(id: String, escalaPxPorMetro: Double, atualizadaEm: Long) {
        state.value = state.value.map { if (it.id == id) it.copy(escalaPxPorMetro = escalaPxPorMetro, atualizadaEm = atualizadaEm) else it }
    }

    override suspend fun atualizarImagemFundo(id: String, imagemFundoKey: String?, atualizadaEm: Long) {
        state.value = state.value.map { if (it.id == id) it.copy(imagemFundoKey = imagemFundoKey, atualizadaEm = atualizadaEm) else it }
    }

    override suspend fun atualizarOpacidadeFundo(id: String, opacidade: Float) {
        state.value = state.value.map { if (it.id == id) it.copy(imagemFundoOpacidade = opacidade) else it }
    }

    override suspend fun renomear(id: String, nome: String, atualizadaEm: Long) {
        state.value = state.value.map { if (it.id == id) it.copy(nome = nome, atualizadaEm = atualizadaEm) else it }
    }

    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }

    override fun observarDoProjeto(projetoId: String): Flow<List<PlantaBaixa>> =
        state.map { lista -> lista.filter { it.projetoId == projetoId && it.ativo } }
}

class InMemoryComodoRepository : ComodoRepository {
    private val state = MutableStateFlow<List<Comodo>>(emptyList())

    override suspend fun listarDaPlanta(plantaId: String): List<Comodo> = state.value.filter { it.plantaId == plantaId && it.ativo }
    override suspend fun salvar(comodo: Comodo) {
        state.value = state.value + comodo
    }
    override suspend fun renomear(id: String, nome: String) {
        state.value = state.value.map { if (it.id == id) it.copy(nome = nome) else it }
    }
    override suspend fun atualizarAreaPerimetro(id: String, areaM2: Double, perimetroM: Double) {
        state.value = state.value.map { if (it.id == id) it.copy(areaM2 = areaM2, perimetroM = perimetroM) else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarDaPlanta(plantaId: String): Flow<List<Comodo>> =
        state.map { lista -> lista.filter { it.plantaId == plantaId && it.ativo } }
}

class InMemoryArquivoImportadoRepository : ArquivoImportadoRepository {
    private val state = MutableStateFlow<List<ArquivoImportado>>(emptyList())

    override suspend fun listarDaPlanta(plantaId: String): List<ArquivoImportado> =
        state.value.filter { it.plantaId == plantaId }.sortedByDescending { it.importadoEm }

    override suspend fun salvar(arquivo: ArquivoImportado) {
        state.value = state.value + arquivo
    }
}

class InMemoryCategoriaFinanceiraRepository : CategoriaFinanceiraRepository {
    private val state = MutableStateFlow<List<CategoriaFinanceira>>(emptyList())

    override suspend fun listarAtivas(): List<CategoriaFinanceira> = state.value.filter { it.ativo }
    override suspend fun salvar(categoria: CategoriaFinanceira) {
        state.value = state.value + categoria
    }
    override suspend fun atualizar(categoria: CategoriaFinanceira) {
        state.value = state.value.map { if (it.id == categoria.id) categoria else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun garantirCategoriasPadrao() {
        if (state.value.any { it.padraoDoSistema }) return
        state.value = state.value + CATEGORIAS_PADRAO_NOMES.map { nome ->
            CategoriaFinanceira(
                id = Uuid.random().toString(),
                nome = nome,
                tipo = TipoLancamento.DESPESA,
                naturezaPadrao = NaturezaLancamento.CONTABIL,
                cor = "#90A4AE",
                padraoDoSistema = true,
            )
        } + CategoriaFinanceira(
            id = Uuid.random().toString(),
            nome = CATEGORIA_PADRAO_RECEITA_VENDAS,
            tipo = TipoLancamento.RECEITA,
            naturezaPadrao = NaturezaLancamento.CONTABIL,
            cor = "#66BB6A",
            padraoDoSistema = true,
        )
    }

    override fun observarAtivas(): Flow<List<CategoriaFinanceira>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryCentroDeCustoRepository : CentroDeCustoRepository {
    private val state = MutableStateFlow<List<CentroDeCusto>>(emptyList())

    override suspend fun listarAtivos(): List<CentroDeCusto> = state.value.filter { it.ativo }
    override suspend fun buscarPorProjetoId(projetoId: String): CentroDeCusto? =
        state.value.firstOrNull { it.projetoId == projetoId && it.ativo }
    override suspend fun salvar(centro: CentroDeCusto) {
        state.value = state.value + centro
    }
    override suspend fun atualizar(centro: CentroDeCusto) {
        state.value = state.value.map { if (it.id == centro.id) centro else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivos(): Flow<List<CentroDeCusto>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryLancamentoFinanceiroRepository : LancamentoFinanceiroRepository {
    private val state = MutableStateFlow<List<LancamentoFinanceiro>>(emptyList())

    override suspend fun listarAtivos(): List<LancamentoFinanceiro> = state.value.filter { it.ativo }
    override suspend fun salvar(lancamento: LancamentoFinanceiro) {
        state.value = state.value + lancamento
    }
    override suspend fun atualizar(lancamento: LancamentoFinanceiro) {
        state.value = state.value.map { if (it.id == lancamento.id) lancamento else it }
    }
    override suspend fun marcarPago(id: String, pago: Boolean) {
        state.value = state.value.map { if (it.id == id) it.copy(pago = pago) else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivos(): Flow<List<LancamentoFinanceiro>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryRateioLancamentoRepository : RateioLancamentoRepository {
    private val state = MutableStateFlow<List<RateioLancamento>>(emptyList())

    override suspend fun listarDoLancamento(lancamentoId: String): List<RateioLancamento> =
        state.value.filter { it.lancamentoId == lancamentoId }

    override suspend fun substituir(lancamentoId: String, rateios: List<RateioLancamento>) {
        state.value = state.value.filterNot { it.lancamentoId == lancamentoId } + rateios
    }
}

class InMemoryParedeRepository : ParedeRepository {
    private val state = MutableStateFlow<List<Parede>>(emptyList())

    override suspend fun listarDaPlanta(plantaId: String): List<Parede> = state.value.filter { it.plantaId == plantaId && it.ativo }
    override suspend fun salvar(parede: Parede) {
        state.value = state.value + parede
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarDaPlanta(plantaId: String): Flow<List<Parede>> =
        state.map { lista -> lista.filter { it.plantaId == plantaId && it.ativo } }
}

class InMemoryAberturaRepository : AberturaRepository {
    private val state = MutableStateFlow<List<Abertura>>(emptyList())

    override suspend fun listarDaParede(paredeId: String): List<Abertura> = state.value.filter { it.paredeId == paredeId && it.ativo }
    override suspend fun salvar(abertura: Abertura) {
        state.value = state.value + abertura
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarTodas(): Flow<List<Abertura>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryMovimentoContaRepository : MovimentoContaRepository {
    private val state = MutableStateFlow<List<MovimentoConta>>(emptyList())

    override suspend fun listarDaConta(contaId: String): List<MovimentoConta> =
        state.value.filter { it.contaId == contaId }.sortedByDescending { it.data }
    override suspend fun listarTodos(): List<MovimentoConta> = state.value
    override suspend fun salvar(movimento: MovimentoConta) {
        state.value = state.value + movimento
    }
    override suspend fun marcarConciliado(id: String, conciliado: Boolean) {
        state.value = state.value.map { if (it.id == id) it.copy(conciliado = conciliado) else it }
    }
    override suspend fun excluir(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
    override suspend fun excluirPorLancamentoId(lancamentoId: String) {
        state.value = state.value.filterNot { it.lancamentoFinanceiroId == lancamentoId }
    }
    override fun observarDaConta(contaId: String): Flow<List<MovimentoConta>> =
        state.map { lista -> lista.filter { it.contaId == contaId }.sortedByDescending { it.data } }
    override fun observarTodos(): Flow<List<MovimentoConta>> = state
}

class InMemoryFuncionarioRepository : FuncionarioRepository {
    private val state = MutableStateFlow<List<Funcionario>>(emptyList())

    override suspend fun listarAtivos(): List<Funcionario> = state.value.filter { it.ativo }
    override suspend fun buscarPorPessoaId(pessoaId: String): Funcionario? = state.value.firstOrNull { it.pessoaId == pessoaId }
    override suspend fun salvar(funcionario: Funcionario) {
        state.value = state.value + funcionario
    }
    override suspend fun atualizar(funcionario: Funcionario) {
        state.value = state.value.map { if (it.pessoaId == funcionario.pessoaId) funcionario else it }
    }
    override suspend fun desativar(pessoaId: String) {
        state.value = state.value.map { if (it.pessoaId == pessoaId) it.copy(ativo = false) else it }
    }
    override fun observarAtivos(): Flow<List<Funcionario>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryEquipeRepository : EquipeRepository {
    private val state = MutableStateFlow<List<Equipe>>(emptyList())

    override suspend fun listarAtivas(): List<Equipe> = state.value.filter { it.ativo }
    override suspend fun salvar(equipe: Equipe) {
        state.value = state.value + equipe
    }
    override suspend fun atualizar(equipe: Equipe) {
        state.value = state.value.map { if (it.id == equipe.id) equipe else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<Equipe>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryRegistroTrabalhoRepository : RegistroTrabalhoRepository {
    private val state = MutableStateFlow<List<RegistroTrabalho>>(emptyList())

    override suspend fun listarDaPessoa(pessoaId: String): List<RegistroTrabalho> =
        state.value.filter { it.pessoaId == pessoaId && it.ativo }.sortedByDescending { it.data }
    override suspend fun listarNaoPagosDaPessoa(pessoaId: String): List<RegistroTrabalho> =
        state.value.filter { it.pessoaId == pessoaId && it.ativo && !it.pago }.sortedBy { it.data }
    override suspend fun listarTodos(): List<RegistroTrabalho> = state.value.filter { it.ativo }
    override suspend fun salvar(registro: RegistroTrabalho) {
        state.value = state.value + registro
    }
    override suspend fun marcarPagos(ids: List<String>, pagamentoId: String) {
        state.value = state.value.map { if (it.id in ids) it.copy(pago = true, pagamentoId = pagamentoId) else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarTodos(): Flow<List<RegistroTrabalho>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryRetencaoLancamentoRepository : RetencaoLancamentoRepository {
    private val state = MutableStateFlow<List<RetencaoLancamento>>(emptyList())

    override suspend fun listarDoLancamento(lancamentoId: String): List<RetencaoLancamento> =
        state.value.filter { it.lancamentoId == lancamentoId }

    override suspend fun substituir(lancamentoId: String, retencoes: List<RetencaoLancamento>) {
        state.value = state.value.filterNot { it.lancamentoId == lancamentoId } + retencoes
    }
}

class InMemoryPagamentoRepository : PagamentoRepository {
    private val state = MutableStateFlow<List<Pagamento>>(emptyList())

    override suspend fun listarDaPessoa(pessoaId: String): List<Pagamento> =
        state.value.filter { it.pessoaId == pessoaId }.sortedByDescending { it.dataPagamento }
    override suspend fun salvar(pagamento: Pagamento) {
        state.value = state.value + pagamento
    }
    override fun observarTodos(): Flow<List<Pagamento>> = state.map { lista -> lista.sortedByDescending { it.dataPagamento } }
}

class InMemoryFornecedorRepository : FornecedorRepository {
    private val state = MutableStateFlow<List<Fornecedor>>(emptyList())

    override suspend fun listarAtivos(): List<Fornecedor> = state.value.filter { it.ativo }
    override suspend fun salvar(fornecedor: Fornecedor) {
        state.value = state.value + fornecedor
    }
    override suspend fun atualizar(fornecedor: Fornecedor) {
        state.value = state.value.map { if (it.pessoaId == fornecedor.pessoaId) fornecedor else it }
    }
    override suspend fun desativar(pessoaId: String) {
        state.value = state.value.map { if (it.pessoaId == pessoaId) it.copy(ativo = false) else it }
    }
    override fun observarAtivos(): Flow<List<Fornecedor>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryPedidoCompraRepository : PedidoCompraRepository {
    private val pedidos = MutableStateFlow<List<PedidoCompra>>(emptyList())
    private val itens = MutableStateFlow<List<ItemCompra>>(emptyList())

    override suspend fun listarDoProjeto(projetoId: String): List<PedidoCompra> =
        pedidos.value.filter { it.projetoId == projetoId && it.ativo }.sortedByDescending { it.data }
    override suspend fun listarTodos(): List<PedidoCompra> = pedidos.value.filter { it.ativo }.sortedByDescending { it.data }

    override suspend fun salvar(pedido: PedidoCompra, itens: List<ItemCompra>) {
        pedidos.value = pedidos.value.filterNot { it.id == pedido.id } + pedido
        this.itens.value = this.itens.value.filterNot { it.pedidoId == pedido.id } + itens
    }

    override suspend fun atualizarStatus(id: String, status: StatusPedidoCompra, lancamentoFinanceiroId: String?) {
        pedidos.value = pedidos.value.map { if (it.id == id) it.copy(status = status, lancamentoFinanceiroId = lancamentoFinanceiroId) else it }
    }

    override suspend fun desativar(id: String) {
        pedidos.value = pedidos.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }

    override suspend fun itensDoPedido(pedidoId: String): List<ItemCompra> = itens.value.filter { it.pedidoId == pedidoId }
    override suspend fun itensDeTodos(): List<ItemCompra> = itens.value
    override fun observarTodos(): Flow<List<PedidoCompra>> = pedidos.map { lista -> lista.filter { it.ativo }.sortedByDescending { it.data } }
}

class InMemoryConfigBDIRepository : ConfigBDIRepository {
    private val state = MutableStateFlow<List<ConfigBDI>>(emptyList())

    override suspend fun listarAtivos(): List<ConfigBDI> = state.value.filter { it.ativo }

    override suspend fun salvar(config: ConfigBDI) {
        state.value = (if (config.padrao) state.value.map { it.copy(padrao = false) } else state.value) + config
    }

    override suspend fun atualizar(config: ConfigBDI) {
        state.value = state.value.map {
            when {
                it.id == config.id -> config
                config.padrao -> it.copy(padrao = false)
                else -> it
            }
        }
    }

    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }

    override fun observarAtivos(): Flow<List<ConfigBDI>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryOrcamentoRepository : OrcamentoRepository {
    private val orcamentos = MutableStateFlow<List<Orcamento>>(emptyList())
    private val itens = MutableStateFlow<List<ItemOrcamento>>(emptyList())

    override suspend fun listarTodos(): List<Orcamento> = orcamentos.value.filter { it.ativo }.sortedByDescending { it.data }

    override suspend fun salvar(orcamento: Orcamento, itens: List<ItemOrcamento>) {
        orcamentos.value = orcamentos.value.filterNot { it.id == orcamento.id } + orcamento
        this.itens.value = this.itens.value.filterNot { it.orcamentoId == orcamento.id } + itens
    }

    override suspend fun atualizarStatus(id: String, status: StatusOrcamento) {
        orcamentos.value = orcamentos.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    override suspend fun desativar(id: String) {
        orcamentos.value = orcamentos.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }

    override suspend fun itensDoOrcamento(orcamentoId: String): List<ItemOrcamento> = itens.value.filter { it.orcamentoId == orcamentoId }
    override fun observarTodos(): Flow<List<Orcamento>> = orcamentos.map { lista -> lista.filter { it.ativo }.sortedByDescending { it.data } }
}

class InMemoryVendaRepository : VendaRepository {
    private val vendas = MutableStateFlow<List<Venda>>(emptyList())
    private val parcelas = MutableStateFlow<List<ParcelaVenda>>(emptyList())

    override suspend fun listarTodos(): List<Venda> = vendas.value.filter { it.ativo }.sortedByDescending { it.data }

    override suspend fun salvar(venda: Venda, parcelas: List<ParcelaVenda>) {
        vendas.value = vendas.value.filterNot { it.id == venda.id } + venda
        this.parcelas.value = this.parcelas.value.filterNot { it.vendaId == venda.id } + parcelas
    }

    override suspend fun atualizarStatus(id: String, status: StatusVenda) {
        vendas.value = vendas.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    override suspend fun desativar(id: String) {
        vendas.value = vendas.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }

    override suspend fun parcelasDaVenda(vendaId: String): List<ParcelaVenda> =
        parcelas.value.filter { it.vendaId == vendaId }.sortedBy { it.numero }

    override suspend fun atualizarParcela(parcela: ParcelaVenda) {
        parcelas.value = parcelas.value.map { if (it.id == parcela.id) parcela else it }
    }

    override fun observarTodos(): Flow<List<Venda>> = vendas.map { lista -> lista.filter { it.ativo }.sortedByDescending { it.data } }
}

class InMemoryTarefaRepository : TarefaRepository {
    private val state = MutableStateFlow<List<Tarefa>>(emptyList())

    override suspend fun listarDaEtapa(etapaId: String): List<Tarefa> = state.value.filter { it.etapaId == etapaId }
    override suspend fun salvar(tarefa: Tarefa) {
        state.value = state.value + tarefa
    }
    override suspend fun atualizar(tarefa: Tarefa) {
        state.value = state.value.map { if (it.id == tarefa.id) tarefa else it }
    }
    override suspend fun excluir(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
    override fun observarDaEtapa(etapaId: String): Flow<List<Tarefa>> = state.map { lista -> lista.filter { it.etapaId == etapaId } }
}

class InMemoryDiarioObraRepository : DiarioObraRepository {
    private val state = MutableStateFlow<List<DiarioObra>>(emptyList())

    override suspend fun listarDoProjeto(projetoId: String): List<DiarioObra> =
        state.value.filter { it.projetoId == projetoId && it.ativo }.sortedByDescending { it.data }
    override suspend fun salvar(diario: DiarioObra) {
        state.value = state.value + diario
    }
    override suspend fun atualizar(diario: DiarioObra) {
        state.value = state.value.map { if (it.id == diario.id) diario else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarDoProjeto(projetoId: String): Flow<List<DiarioObra>> =
        state.map { lista -> lista.filter { it.projetoId == projetoId && it.ativo }.sortedByDescending { it.data } }
}

class InMemoryDocumentoTecnicoRepository : DocumentoTecnicoRepository {
    private val state = MutableStateFlow<List<DocumentoTecnico>>(emptyList())

    override suspend fun listarTodos(): List<DocumentoTecnico> = state.value.sortedByDescending { it.adicionadoEm }
    override suspend fun salvar(documento: DocumentoTecnico) {
        state.value = state.value + documento
    }
    override suspend fun excluir(id: String) {
        state.value = state.value.filter { it.id != id }
    }
    override suspend fun atualizarTextoExtraido(id: String, texto: String) {
        if (texto.isBlank()) return
        state.value = state.value.map { if (it.id == id) it.copy(textoExtraido = texto) else it }
    }
    override suspend fun buscarPorTexto(query: String): List<DocumentoTecnico> {
        if (query.isBlank()) return listarTodos()
        return state.value.filter { it.textoExtraido?.contains(query, ignoreCase = true) == true }
            .sortedByDescending { it.adicionadoEm }
    }
    override fun observarTodos(): Flow<List<DocumentoTecnico>> =
        state.map { lista -> lista.sortedByDescending { it.adicionadoEm } }
}

class InMemoryMetaRepository : MetaRepository {
    private val state = MutableStateFlow<List<Meta>>(emptyList())

    override suspend fun listarAtivas(): List<Meta> = state.value.filter { it.ativo }
    override suspend fun salvar(meta: Meta) {
        state.value = state.value + meta
    }
    override suspend fun atualizar(meta: Meta) {
        state.value = state.value.map { if (it.id == meta.id) meta else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<Meta>> = state.map { lista -> lista.filter { it.ativo } }
}
