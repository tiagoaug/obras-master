package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Pagamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestorePagamentoRepository(empresaContexto: EmpresaContexto) : PagamentoRepository {
    private val colecao = FirestoreCollection(empresaContexto, "pagamentos", Pagamento.serializer())

    override suspend fun listarDaPessoa(pessoaId: String): List<Pagamento> =
        colecao.consultar { where { "pessoaId" equalTo pessoaId } }

    override suspend fun salvar(pagamento: Pagamento) = colecao.salvar(pagamento.id, pagamento)
    override fun observarTodos(): Flow<List<Pagamento>> = colecao.observarTodos()
}
