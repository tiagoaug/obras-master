package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.LancamentoFinanceiro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreLancamentoFinanceiroRepository(empresaContexto: EmpresaContexto) : LancamentoFinanceiroRepository {
    private val colecao = FirestoreCollection(empresaContexto, "lancamentosFinanceiros", LancamentoFinanceiro.serializer())

    override suspend fun listarAtivos(): List<LancamentoFinanceiro> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(lancamento: LancamentoFinanceiro) = colecao.salvar(lancamento.id, lancamento)
    override suspend fun atualizar(lancamento: LancamentoFinanceiro) = colecao.salvar(lancamento.id, lancamento)
    override suspend fun marcarPago(id: String, pago: Boolean) = colecao.atualizarCampos(id, "pago" to pago)
    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivos(): Flow<List<LancamentoFinanceiro>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
