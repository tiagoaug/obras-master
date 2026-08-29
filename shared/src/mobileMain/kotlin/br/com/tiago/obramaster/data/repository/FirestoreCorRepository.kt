package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Cor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreCorRepository(empresaContexto: EmpresaContexto) : CorRepository {
    private val colecao = FirestoreCollection(empresaContexto, "cores", Cor.serializer())

    override suspend fun listarAtivas(): List<Cor> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(cor: Cor) = colecao.salvar(cor.id, cor)
    override suspend fun atualizar(cor: Cor) = colecao.salvar(cor.id, cor)
    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivas(): Flow<List<Cor>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
