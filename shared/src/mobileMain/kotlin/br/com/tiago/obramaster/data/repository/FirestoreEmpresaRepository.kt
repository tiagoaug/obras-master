package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.EmpresaContexto
import br.com.tiago.obramaster.domain.DadosEmpresa
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

/** Diferente dos outros: DadosEmpresa é o próprio documento raiz `empresas/{empresaId}` (não uma
 * subcoleção) — é ele que os recursos de negócio penduram embaixo (`empresas/{empresaId}/projetos/...` etc). */
class FirestoreEmpresaRepository(private val empresaContexto: EmpresaContexto) : EmpresaRepository {

    private fun doc() = Firebase.firestore.document("empresas/${empresaContexto.exigir()}")

    override suspend fun buscar(): DadosEmpresa? {
        val snapshot = doc().get()
        return if (snapshot.exists) snapshot.data(DadosEmpresa.serializer()) else null
    }

    override suspend fun salvar(empresa: DadosEmpresa) {
        doc().set(DadosEmpresa.serializer(), empresa)
    }
}
