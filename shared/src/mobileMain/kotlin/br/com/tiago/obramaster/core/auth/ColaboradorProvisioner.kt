package br.com.tiago.obramaster.core.auth

/** O Firebase Auth do lado do cliente não deixa criar a conta de outra pessoa sem derrubar a
 * sessão de quem está criando — por isso essa operação roda numa instância *secundária* e
 * descartável do Firebase App (ver `AndroidColaboradorProvisioner`/`IosColaboradorProvisioner`),
 * deixando a sessão do Gestor intocada. Usado por `FirebaseSessionManager.criarColaborador`
 * (Configurações → Colaboradores e pelo passo "Colaboradores" do onboarding). */
interface ColaboradorProvisioner {
    suspend fun criarConta(email: String, senha: String): ResultadoAuth
}
