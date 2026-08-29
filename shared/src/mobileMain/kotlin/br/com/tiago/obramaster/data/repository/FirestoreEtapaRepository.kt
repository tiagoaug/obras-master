package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.Etapa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirestoreEtapaRepository(empresaContexto: EmpresaContexto) : EtapaRepository {
    private val colecao = FirestoreCollection(empresaContexto, "etapas", Etapa.serializer())

    override suspend fun listarDoProjeto(projetoId: String): List<Etapa> =
        colecao.consultar { where { "projetoId" equalTo projetoId } }.filter { it.ativo }

    override suspend fun listarTodasAtivas(): List<Etapa> = colecao.listarTodos().filter { it.ativo }

    override suspend fun salvar(etapa: Etapa) = colecao.salvar(etapa.id, etapa)
    override suspend fun atualizar(etapa: Etapa) = colecao.salvar(etapa.id, etapa)

    override suspend fun reordenar(etapaId: String, novaOrdem: Int) {
        colecao.atualizarCampos(etapaId, "ordem" to novaOrdem)
    }

    override suspend fun desativar(id: String) = colecao.atualizarCampos(id, "ativo" to false)

    override fun observarDoProjeto(projetoId: String): Flow<List<Etapa>> =
        colecao.observarTodos().map { lista -> lista.filter { it.projetoId == projetoId && it.ativo } }
}
