package br.com.tiago.obramaster.server.db

import br.com.tiago.obramaster.server.Env
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/** GUIA_ANTIGRAVITY.md §6 / decisão da sessão: H2 embarcado para dev local sem instalar nada;
 * trocar para Postgres em produção é só mudar `OBRAMASTER_DB_URL` (Exposed fala com qualquer
 * banco via JDBC, o SQL gerado aqui não usa nada específico de um dialeto). */
object DatabaseFactory {
    fun init(url: String = Env.dbUrl, user: String = Env.dbUser, password: String = Env.dbPassword) {
        Database.connect(url = url, user = user, password = password)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Empresas, Colaboradores, Permissoes, Projetos, Etapas,
                Contas, CategoriasFinanceiras, CentrosDeCusto, LancamentosFinanceiros,
                Pessoas, Equipes, EquipeMembros, Funcionarios, Pagamentos, RegistrosTrabalho,
                Cores, UnidadesMedida, Materiais, Fornecedores,
                PedidosCompra, ItensCompra, ConfigsBDI, Orcamentos, ItensOrcamento, Vendas, ParcelasVenda,
                Metas, ModulosEmpresa,
            )
        }
    }
}
