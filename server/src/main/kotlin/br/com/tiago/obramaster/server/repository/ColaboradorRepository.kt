package br.com.tiago.obramaster.server.repository

import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.domain.Permissao
import br.com.tiago.obramaster.server.db.Colaboradores
import br.com.tiago.obramaster.server.db.Permissoes
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ColaboradorRepository {

    /** `senhaHash`/`salt` ficam só aqui (não fazem mais parte de Colaborador em :core desde o
     * pivô pra Firebase Auth na Fase 10 — ver domain/Colaborador.kt). Módulo :server congelado,
     * mantido só como referência/fallback; este ajuste é o mínimo pra continuar compilando. */
    private data class LinhaColaborador(val colaborador: Colaborador, val senhaHash: String, val salt: String)

    private fun rowToColaborador(row: ResultRow) = Colaborador(
        id = row[Colaboradores.id],
        nome = row[Colaboradores.nome],
        email = row[Colaboradores.login],
        ativo = row[Colaboradores.ativo],
        ehGestor = row[Colaboradores.ehGestor],
    )

    private fun rowToLinha(row: ResultRow) = LinhaColaborador(
        colaborador = rowToColaborador(row),
        senhaHash = row[Colaboradores.senhaHash],
        salt = row[Colaboradores.salt],
    )

    fun buscarPorLoginEEmpresaComSenha(login: String, empresaId: String): Pair<Colaborador, Pair<String, String>>? = transaction {
        Colaboradores.selectAll()
            .andWhere { Colaboradores.login eq login }
            .andWhere { Colaboradores.empresaId eq empresaId }
            .andWhere { Colaboradores.ativo eq true }
            .map(::rowToLinha)
            .singleOrNull()
            ?.let { it.colaborador to (it.senhaHash to it.salt) }
    }

    fun buscarPorId(id: String): Colaborador? = transaction {
        Colaboradores.selectAll()
            .andWhere { Colaboradores.id eq id }
            .map(::rowToColaborador)
            .singleOrNull()
    }

    fun empresaIdDoColaborador(id: String): String? = transaction {
        Colaboradores.select(Colaboradores.empresaId)
            .andWhere { Colaboradores.id eq id }
            .map { it[Colaboradores.empresaId] }
            .singleOrNull()
    }

    fun permissoesDoColaborador(id: String): List<Permissao> = transaction {
        Permissoes.selectAll()
            .andWhere { Permissoes.colaboradorId eq id }
            .map { row ->
                Permissao(
                    colaboradorId = row[Permissoes.colaboradorId],
                    moduleId = row[Permissoes.moduleId],
                    nivel = NivelPermissao.valueOf(row[Permissoes.nivel]),
                )
            }
    }
}
