package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Conta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreContaRepository(empresaContexto: EmpresaContexto) : ContaRepository {
    private val colecao = FirestoreCollection(empresaContexto, "contas", Conta.serializer())

    override suspend fun listarAtivas(): List<Conta> = colecao.listarTodos().filter { it.ativo }
    override suspend fun buscarPorId(id: String): Conta? = colecao.buscarPorId(id)
    override suspend fun salvar(conta: Conta) = colecao.salvar(conta.id, conta)

    // saldoInicial/dataSaldoInicial não são editáveis — só os campos abaixo, mesmo contrato do SqlDelightContaRepository original.
    override suspend fun atualizar(conta: Conta) {
        colecao.atualizarCampos(
            conta.id,
            "nome" to conta.nome,
            "tipo" to conta.tipo.name,
            "banco" to conta.banco,
            "agencia" to conta.agencia,
            "numeroConta" to conta.numeroConta,
            "cor" to conta.cor,
        )
    }

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivas(): Flow<List<Conta>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
