package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.RegistroTrabalho
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreRegistroTrabalhoRepository(empresaContexto: EmpresaContexto) : RegistroTrabalhoRepository {
    private val colecao = FirestoreCollection(empresaContexto, "registrosTrabalho", RegistroTrabalho.serializer())

    override suspend fun listarDaPessoa(pessoaId: String): List<RegistroTrabalho> =
        colecao.consultar { where { "pessoaId" equalTo pessoaId } }.filter { it.ativo }

    override suspend fun listarNaoPagosDaPessoa(pessoaId: String): List<RegistroTrabalho> =
        listarDaPessoa(pessoaId).filter { !it.pago }

    override suspend fun listarTodos(): List<RegistroTrabalho> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(registro: RegistroTrabalho) = colecao.salvar(registro.id, registro)

    override suspend fun marcarPagos(ids: List<String>, pagamentoId: String) {
        ids.forEach { id -> colecao.atualizarCampos(id, "pago" to true, "pagamentoId" to pagamentoId) }
    }

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarTodos(): Flow<List<RegistroTrabalho>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
