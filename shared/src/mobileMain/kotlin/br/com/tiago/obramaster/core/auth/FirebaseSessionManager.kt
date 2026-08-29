package br.com.tiago.obramaster.core.auth

import br.com.tiago.obramaster.data.repository.FirestoreColaboradorRepository
import br.com.tiago.obramaster.domain.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirebaseSessionManager(
    private val firestoreColaboradorRepository: FirestoreColaboradorRepository,
    private val colaboradorProvisioner: ColaboradorProvisioner,
    private val empresaContexto: EmpresaContexto,
) : SessionManager {

    private val _colaboradorLogado = MutableStateFlow<Colaborador?>(null)
    override val colaboradorLogado: StateFlow<Colaborador?> = _colaboradorLogado.asStateFlow()

    override suspend fun restaurar() {
        val uid = FirebaseAuthGateway.uidAtual ?: return
        carregarColaboradorDoUid(uid)
    }

    override suspend fun login(email: String, senha: String): SessionManager.LoginResult =
        quandoAutenticado(FirebaseAuthGateway.entrarComEmailSenha(email, senha))

    override suspend fun entrarComGoogle(idToken: String): SessionManager.LoginResult =
        quandoAutenticado(FirebaseAuthGateway.entrarComGoogle(idToken))

    private suspend fun quandoAutenticado(resultado: ResultadoAuth): SessionManager.LoginResult = when (resultado) {
        is ResultadoAuth.Sucesso -> carregarColaboradorDoUid(resultado.uid) ?: SessionManager.LoginResult.ContaSemEmpresaVinculada
        ResultadoAuth.CredenciaisInvalidas -> SessionManager.LoginResult.LoginOuSenhaInvalidos
        ResultadoAuth.EmailJaCadastrado -> SessionManager.LoginResult.Erro("E-mail já cadastrado")
        is ResultadoAuth.Erro -> SessionManager.LoginResult.Erro(resultado.mensagem)
    }

    /** Quando o uid administra mais de uma empresa, a primeira da lista vira a ativa por padrão —
     * trocar pra outra é feito depois, em "Minhas Empresas" (Configurações). */
    private suspend fun carregarColaboradorDoUid(uid: String): SessionManager.LoginResult.Sucesso? {
        val encontrado = firestoreColaboradorRepository.buscarComEmpresaIds(uid) ?: return null
        val (colaborador, empresaIds) = encontrado
        val empresaId = empresaIds.firstOrNull() ?: return null
        empresaContexto.definir(empresaId)
        _colaboradorLogado.value = colaborador
        return SessionManager.LoginResult.Sucesso(colaborador)
    }

    override suspend fun cadastrarGestor(nome: String, email: String, senha: String, empresaId: String): SessionManager.LoginResult {
        val resultado = FirebaseAuthGateway.cadastrarComEmailSenha(email, senha)
        if (resultado !is ResultadoAuth.Sucesso) return quandoAutenticado(resultado)
        val colaborador = Colaborador(id = resultado.uid, nome = nome, email = resultado.email, ativo = true, ehGestor = true)
        firestoreColaboradorRepository.criar(resultado.uid, listOf(empresaId), colaborador)
        empresaContexto.definir(empresaId)
        _colaboradorLogado.value = colaborador
        return SessionManager.LoginResult.Sucesso(colaborador)
    }

    override suspend fun criarColaborador(nome: String, email: String, senha: String): SessionManager.LoginResult {
        val empresaId = empresaContexto.exigir()
        val resultado = colaboradorProvisioner.criarConta(email, senha)
        if (resultado !is ResultadoAuth.Sucesso) return quandoAutenticado(resultado)
        val colaborador = Colaborador(id = resultado.uid, nome = nome, email = resultado.email, ativo = true, ehGestor = false)
        firestoreColaboradorRepository.criar(resultado.uid, listOf(empresaId), colaborador)
        return SessionManager.LoginResult.Sucesso(colaborador)
    }

    override suspend fun logout() {
        FirebaseAuthGateway.sair()
        _colaboradorLogado.value = null
        empresaContexto.limpar()
    }
}
