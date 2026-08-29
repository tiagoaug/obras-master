package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.domain.DadosEmpresa

/** Fase 10 (pivô Firebase) — implementação única é FirestoreEmpresaRepository (mobileMain). */
interface EmpresaRepository {
    suspend fun buscar(): DadosEmpresa?
    suspend fun salvar(empresa: DadosEmpresa)
}
