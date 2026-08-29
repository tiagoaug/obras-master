package br.com.tiago.obramaster.server.db

import org.jetbrains.exposed.sql.Table

/** SPEC_OBRA_MASTER_KMP.md §6.2 — toda tabela carrega `empresaId` (multi-tenant). IDs são String
 * UUID gerados no cliente (nunca autoincrement), mesma regra do banco local SQLDelight. */

object Empresas : Table("empresas") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val nome = varchar("nome", 200)
}

object Colaboradores : Table("colaboradores") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val login = varchar("login", 100)
    val senhaHash = varchar("senha_hash", 100)
    val salt = varchar("salt", 100)
    val ativo = bool("ativo").default(true)
    val ehGestor = bool("eh_gestor").default(false)
}

object Permissoes : Table("permissoes") {
    val colaboradorId = varchar("colaborador_id", 36).references(Colaboradores.id)
    val moduleId = varchar("module_id", 50)
    val nivel = varchar("nivel", 20)
    override val primaryKey = PrimaryKey(colaboradorId, moduleId)
}

object Projetos : Table("projetos") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val clienteId = varchar("cliente_id", 36).nullable()
    val endereco = varchar("endereco", 500).nullable()
    val areaConstruidaM2 = double("area_construida_m2").nullable()
    val areaTerrenoM2 = double("area_terreno_m2").nullable()
    val orcamentoTotal = long("orcamento_total")
    val dataInicio = long("data_inicio").nullable()
    val dataPrevisaoFim = long("data_previsao_fim").nullable()
    val status = varchar("status", 30)
    val fotoCapaUri = varchar("foto_capa_uri", 500).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object Etapas : Table("etapas") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val projetoId = varchar("projeto_id", 36).references(Projetos.id)
    val nome = varchar("nome", 200)
    val ordem = integer("ordem")
    val orcamentoEtapa = long("orcamento_etapa")
    val dataInicio = long("data_inicio").nullable()
    val dataFim = long("data_fim").nullable()
    val dataInicioReal = long("data_inicio_real").nullable()
    val dataFimReal = long("data_fim_real").nullable()
    val progressoPercent = integer("progresso_percent").default(0)
    val status = varchar("status", 30)
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object Contas : Table("contas") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val tipo = varchar("tipo", 30)
    val banco = varchar("banco", 200).nullable()
    val agencia = varchar("agencia", 30).nullable()
    val numeroConta = varchar("numero_conta", 50).nullable()
    val saldoInicial = long("saldo_inicial")
    val dataSaldoInicial = long("data_saldo_inicial")
    val ativo = bool("ativo").default(true)
    val cor = varchar("cor", 20).nullable()
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object CategoriasFinanceiras : Table("categorias_financeiras") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val tipo = varchar("tipo", 20)
    val naturezaPadrao = varchar("natureza_padrao", 20)
    val categoriaPaiId = varchar("categoria_pai_id", 36).nullable()
    val cor = varchar("cor", 20)
    val icone = varchar("icone", 50).nullable()
    val padraoDoSistema = bool("padrao_do_sistema").default(false)
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object CentrosDeCusto : Table("centros_de_custo") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val tipo = varchar("tipo", 30)
    val projetoId = varchar("projeto_id", 36).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

/** `tags` guardado como string delimitada por vírgula (ex.: "CLIENTE,FORNECEDOR") — é um conjunto
 * fechado de 3 valores (TagPessoa), não vale a pena uma tabela de junção só pra isso. */
object Pessoas : Table("pessoas") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val tags = varchar("tags", 100)
    val telefone = varchar("telefone", 30).nullable()
    val email = varchar("email", 200).nullable()
    val endereco = varchar("endereco", 500).nullable()
    val documento = varchar("documento", 30).nullable()
    val fotoUri = varchar("foto_uri", 500).nullable()
    val observacoes = varchar("observacoes", 1000).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object Equipes : Table("equipes") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val liderPessoaId = varchar("lider_pessoa_id", 36).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

/** Relação N:N Equipe↔Pessoa (Equipe.membrosIds) — essa sim justifica tabela de junção própria,
 * ao contrário de Pessoa.tags (conjunto fechado pequeno, guardado como string delimitada). */
object EquipeMembros : Table("equipe_membros") {
    val equipeId = varchar("equipe_id", 36).references(Equipes.id)
    val pessoaId = varchar("pessoa_id", 36)
    override val primaryKey = PrimaryKey(equipeId, pessoaId)
}

/** Chave primária é o próprio `pessoaId` — Funcionario é extensão 1:1 de Pessoa (ver domain). */
object Funcionarios : Table("funcionarios") {
    val pessoaId = varchar("pessoa_id", 36)
    override val primaryKey = PrimaryKey(pessoaId)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val funcao = varchar("funcao", 200)
    val tipoContratacao = varchar("tipo_contratacao", 20)
    val valorBase = long("valor_base")
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object Pagamentos : Table("pagamentos") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val pessoaId = varchar("pessoa_id", 36)
    val projetoId = varchar("projeto_id", 36).nullable()
    val periodo = varchar("periodo", 50)
    val valorTotal = long("valor_total")
    val dataPagamento = long("data_pagamento")
    val status = varchar("status", 20)
    val comprovanteUri = varchar("comprovante_uri", 500).nullable()
    val lancamentoFinanceiroId = varchar("lancamento_financeiro_id", 36).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object RegistrosTrabalho : Table("registros_trabalho") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val pessoaId = varchar("pessoa_id", 36)
    val projetoId = varchar("projeto_id", 36)
    val etapaId = varchar("etapa_id", 36).nullable()
    val data = long("data")
    val tipo = varchar("tipo", 30)
    val valor = long("valor")
    val observacao = varchar("observacao", 1000).nullable()
    val pago = bool("pago").default(false)
    val pagamentoId = varchar("pagamento_id", 36).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object Cores : Table("cores") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val hex = varchar("hex", 10)
    val codigoFabricante = varchar("codigo_fabricante", 100).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object UnidadesMedida : Table("unidades_medida") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val sigla = varchar("sigla", 20)
    val nome = varchar("nome", 100)
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object Materiais : Table("materiais") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val unidadePadrao = varchar("unidade_padrao", 20)
    val precoReferencia = long("preco_referencia").nullable()
    val categoria = varchar("categoria", 100).nullable()
    val corId = varchar("cor_id", 36).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

/** Chave é `pessoaId` — Fornecedor é extensão 1:1 de Pessoa, mesmo padrão de Funcionario. */
object Fornecedores : Table("fornecedores") {
    val pessoaId = varchar("pessoa_id", 36)
    override val primaryKey = PrimaryKey(pessoaId)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val cnpjCpf = varchar("cnpj_cpf", 30).nullable()
    val observacoes = varchar("observacoes", 1000).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object PedidosCompra : Table("pedidos_compra") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val projetoId = varchar("projeto_id", 36)
    val etapaId = varchar("etapa_id", 36).nullable()
    val fornecedorId = varchar("fornecedor_id", 36).nullable()
    val data = long("data")
    val status = varchar("status", 20)
    val valorTotal = long("valor_total")
    val lancamentoFinanceiroId = varchar("lancamento_financeiro_id", 36).nullable()
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object ItensCompra : Table("itens_compra") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val pedidoId = varchar("pedido_id", 36).references(PedidosCompra.id)
    val materialId = varchar("material_id", 36)
    val quantidade = double("quantidade")
    val unidade = varchar("unidade", 20)
    val valorUnitario = long("valor_unitario")
    val valorTotal = long("valor_total")
}

object ConfigsBDI : Table("configs_bdi") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val nome = varchar("nome", 200)
    val administracaoCentral = double("administracao_central")
    val seguroGarantia = double("seguro_garantia")
    val riscos = double("riscos")
    val despesasFinanceiras = double("despesas_financeiras")
    val lucro = double("lucro")
    val tributos = double("tributos")
    val padrao = bool("padrao").default(false)
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object Orcamentos : Table("orcamentos") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val projetoId = varchar("projeto_id", 36).nullable()
    val clientePessoaId = varchar("cliente_pessoa_id", 36).nullable()
    val titulo = varchar("titulo", 200)
    val data = long("data")
    val validadeDias = integer("validade_dias")
    val status = varchar("status", 20)
    val descontoPercent = double("desconto_percent").nullable()
    val observacoes = varchar("observacoes", 1000).nullable()
    val configBdiId = varchar("config_bdi_id", 36).nullable()
    val bdiPercentualCalculado = double("bdi_percentual_calculado").default(0.0)
    val bdiCustomizado = bool("bdi_customizado").default(false)
    val custoDiretoTotal = long("custo_direto_total").default(0)
    val precoVendaTotal = long("preco_venda_total").default(0)
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object ItensOrcamento : Table("itens_orcamento") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val orcamentoId = varchar("orcamento_id", 36).references(Orcamentos.id)
    val tipo = varchar("tipo", 20)
    val descricao = varchar("descricao", 500)
    val materialId = varchar("material_id", 36).nullable()
    val quantidade = double("quantidade")
    val unidade = varchar("unidade", 20)
    val valorUnitario = long("valor_unitario")
    val valorTotal = long("valor_total")
}

object Vendas : Table("vendas") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val projetoId = varchar("projeto_id", 36).nullable()
    val clientePessoaId = varchar("cliente_pessoa_id", 36)
    val descricao = varchar("descricao", 500)
    val valorTotal = long("valor_total")
    val data = long("data")
    val formaPagamento = varchar("forma_pagamento", 50)
    val status = varchar("status", 20)
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

object ParcelasVenda : Table("parcelas_venda") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val vendaId = varchar("venda_id", 36).references(Vendas.id)
    val numero = integer("numero")
    val valor = long("valor")
    val vencimento = long("vencimento")
    val pago = bool("pago").default(false)
    val lancamentoFinanceiroId = varchar("lancamento_financeiro_id", 36).nullable()
}

object Metas : Table("metas") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val escopo = varchar("escopo", 20)
    val referenciaId = varchar("referencia_id", 36).nullable()
    val titulo = varchar("titulo", 200)
    val tipo = varchar("tipo", 20)
    val valorAlvo = long("valor_alvo")
    val prazo = long("prazo").nullable()
    val concluida = bool("concluida").default(false)
    val ativo = bool("ativo").default(true)
    val updatedAt = long("updated_at")
    val deletedAt = long("deleted_at").nullable()
}

/** Espelha ModuleRegistry.defaultState()+persisted (client) — só guarda os *overrides* (módulo
 * desativado); um módulo sem linha aqui está habilitado por padrão. Só o Gestor pode alterar. */
object ModulosEmpresa : Table("modulos_empresa") {
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val moduleId = varchar("module_id", 50)
    val enabled = bool("enabled")
    override val primaryKey = PrimaryKey(empresaId, moduleId)
}

/** Sem `ativo` mutável nem endpoint de update/delete no server — SPEC_OBRA_MASTER_KMP.md §6.3 e
 * GUIA_ANTIGRAVITY.md §4 regra 9: "registros financeiros são imutáveis após criados; correção é
 * por estorno". A coluna `ativo` só existe para espelhar o schema local (SQLDelight já a tem). */
object LancamentosFinanceiros : Table("lancamentos_financeiros") {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val empresaId = varchar("empresa_id", 36).references(Empresas.id)
    val tipo = varchar("tipo", 20)
    val categoriaId = varchar("categoria_id", 36)
    val centroDeCustoId = varchar("centro_de_custo_id", 36)
    val natureza = varchar("natureza", 20)
    val projetoId = varchar("projeto_id", 36).nullable()
    val etapaId = varchar("etapa_id", 36).nullable()
    val descricao = varchar("descricao", 500)
    val valor = long("valor")
    val data = long("data")
    val formaPagamento = varchar("forma_pagamento", 50)
    val pago = bool("pago").default(false)
    val pessoaId = varchar("pessoa_id", 36).nullable()
    val anexoUri = varchar("anexo_uri", 500).nullable()
    val contaId = varchar("conta_id", 36).nullable()
    val ativo = bool("ativo").default(true)
    val criadoEm = long("criado_em")
}
