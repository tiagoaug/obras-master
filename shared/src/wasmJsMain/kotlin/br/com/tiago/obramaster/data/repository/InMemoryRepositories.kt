package br.com.tiago.obramaster.data.repository

import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.domain.Conta
import br.com.tiago.obramaster.domain.Cor
import br.com.tiago.obramaster.domain.DadosEmpresa
import br.com.tiago.obramaster.domain.Material
import br.com.tiago.obramaster.domain.Permissao
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.UnidadeMedida
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Substituto temporário só para o alvo Web enquanto não há backend (Fase 10) — a spec
 * (SPEC_OBRA_MASTER_KMP.md §6.1) já define Web como online-first, então o repositório real
 * de lá vai falar com a API, não com SQLDelight local (cujo driver web é assíncrono, ver
 * DatabaseDriverFactory.wasmJs.kt). Dados aqui não sobrevivem a um refresh da página.
 */
class InMemoryColaboradorRepository : ColaboradorRepository {
    private val state = MutableStateFlow<List<Colaborador>>(emptyList())

    override suspend fun listarAtivos(): List<Colaborador> = state.value.filter { it.ativo }
    override suspend fun buscarPorId(id: String): Colaborador? = state.value.firstOrNull { it.id == id }
    override suspend fun buscarPorLogin(login: String): Colaborador? =
        state.value.firstOrNull { it.login == login && it.ativo }

    override suspend fun existeAlgumColaborador(): Boolean = state.value.any { it.ativo }

    override suspend fun salvar(colaborador: Colaborador) {
        state.value = state.value + colaborador
    }

    override suspend fun atualizar(colaborador: Colaborador) {
        state.value = state.value.map { if (it.id == colaborador.id) colaborador else it }
    }

    override suspend fun atualizarSenha(id: String, senhaHash: String, salt: String) {
        state.value = state.value.map { if (it.id == id) it.copy(senhaHash = senhaHash, salt = salt) else it }
    }

    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }

    override fun observarAtivos(): Flow<List<Colaborador>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryPermissaoRepository : PermissaoRepository {
    private val state = MutableStateFlow<List<Permissao>>(emptyList())

    override suspend fun listarTodas(): List<Permissao> = state.value
    override suspend fun listarPorColaborador(colaboradorId: String): List<Permissao> =
        state.value.filter { it.colaboradorId == colaboradorId }

    override suspend fun definir(colaboradorId: String, moduleId: String, nivel: NivelPermissao) {
        val semAtual = state.value.filterNot { it.colaboradorId == colaboradorId && it.moduleId == moduleId }
        state.value = semAtual + Permissao(colaboradorId, moduleId, nivel)
    }

    override suspend fun removerTodasDoColaborador(colaboradorId: String) {
        state.value = state.value.filterNot { it.colaboradorId == colaboradorId }
    }

    override fun observarTodas(): Flow<List<Permissao>> = state
}

class InMemoryModuleConfigRepository : ModuleConfigRepository {
    private val state = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    override suspend fun listarTodos(): Map<String, Boolean> = state.value

    override suspend fun definir(moduleId: String, enabled: Boolean) {
        state.value = state.value + (moduleId to enabled)
    }

    override fun observarTodos(): Flow<Map<String, Boolean>> = state
}

class InMemoryEmpresaRepository : EmpresaRepository {
    private var empresa: DadosEmpresa? = null

    override suspend fun buscar(): DadosEmpresa? = empresa
    override suspend fun salvar(empresa: DadosEmpresa) {
        this.empresa = empresa
    }
}

class InMemoryContaRepository : ContaRepository {
    private val state = MutableStateFlow<List<Conta>>(emptyList())

    override suspend fun listarAtivas(): List<Conta> = state.value.filter { it.ativo }
    override suspend fun salvar(conta: Conta) {
        state.value = state.value + conta
    }
}

class InMemoryPessoaRepository : PessoaRepository {
    private val state = MutableStateFlow<List<Pessoa>>(emptyList())

    override suspend fun listarAtivas(): List<Pessoa> = state.value.filter { it.ativo }
    override suspend fun salvar(pessoa: Pessoa) {
        state.value = state.value + pessoa
    }
    override suspend fun atualizar(pessoa: Pessoa) {
        state.value = state.value.map { if (it.id == pessoa.id) pessoa else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<Pessoa>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryCorRepository : CorRepository {
    private val state = MutableStateFlow<List<Cor>>(emptyList())

    override suspend fun listarAtivas(): List<Cor> = state.value.filter { it.ativo }
    override suspend fun salvar(cor: Cor) {
        state.value = state.value + cor
    }
    override suspend fun atualizar(cor: Cor) {
        state.value = state.value.map { if (it.id == cor.id) cor else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<Cor>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryMaterialRepository : MaterialRepository {
    private val state = MutableStateFlow<List<Material>>(emptyList())

    override suspend fun listarAtivos(): List<Material> = state.value.filter { it.ativo }
    override suspend fun salvar(material: Material) {
        state.value = state.value + material
    }
    override suspend fun atualizar(material: Material) {
        state.value = state.value.map { if (it.id == material.id) material else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivos(): Flow<List<Material>> = state.map { lista -> lista.filter { it.ativo } }
}

class InMemoryUnidadeMedidaRepository : UnidadeMedidaRepository {
    private val state = MutableStateFlow<List<UnidadeMedida>>(emptyList())

    override suspend fun listarAtivas(): List<UnidadeMedida> = state.value.filter { it.ativo }
    override suspend fun salvar(unidade: UnidadeMedida) {
        state.value = state.value + unidade
    }
    override suspend fun atualizar(unidade: UnidadeMedida) {
        state.value = state.value.map { if (it.id == unidade.id) unidade else it }
    }
    override suspend fun desativar(id: String) {
        state.value = state.value.map { if (it.id == id) it.copy(ativo = false) else it }
    }
    override fun observarAtivas(): Flow<List<UnidadeMedida>> = state.map { lista -> lista.filter { it.ativo } }
}
