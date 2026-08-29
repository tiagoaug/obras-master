package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Equipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreEquipeRepository(empresaContexto: EmpresaContexto) : EquipeRepository {
    private val colecao = FirestoreCollection(empresaContexto, "equipes", Equipe.serializer())

    override suspend fun listarAtivas(): List<Equipe> = colecao.listarTodos().filter { it.ativo }
    override suspend fun salvar(equipe: Equipe) = colecao.salvar(equipe.id, equipe)
    override suspend fun atualizar(equipe: Equipe) = colecao.salvar(equipe.id, equipe)
    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)
    override fun observarAtivas(): Flow<List<Equipe>> = colecao.observarTodos().map { lista -> lista.filter { it.ativo } }
}
