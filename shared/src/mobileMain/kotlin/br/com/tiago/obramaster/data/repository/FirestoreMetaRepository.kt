package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Meta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreMetaRepository(empresaContexto: EmpresaContexto) : MetaRepository {
    private val colecao = FirestoreCollection(empresaContexto, "metas", Meta.serializer())

    override suspend fun listarAtivas(): List<Meta> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(meta: Meta) = colecao.salvar(meta.id, meta)

    override suspend fun atualizar(meta: Meta) {
        colecao.atualizarCampos(meta.id, "titulo" to meta.titulo, "valorAlvo" to meta.valorAlvo, "prazo" to meta.prazo, "concluida" to meta.concluida)
    }

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivas(): Flow<List<Meta>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
