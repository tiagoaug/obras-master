package br.com.tiago.obramaster.ui.features.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.platform.obterGoogleIdToken
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onAutenticado: (Colaborador) -> Unit,
    viewModel: LoginViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        val estado = uiState
        if (estado is LoginUiState.Autenticado) onAutenticado(estado.colaborador)
    }

    when (val estado = uiState) {
        is LoginUiState.TelaLogin -> LoginForm(
            erro = estado.erro,
            autenticando = estado.autenticando,
            onEntrar = viewModel::login,
            onEntrarComGoogle = {
                scope.launch {
                    val idToken = obterGoogleIdToken()
                    if (idToken != null) viewModel.entrarComGoogle(idToken)
                }
            },
            onCriarContaComConvite = viewModel::criarContaEAceitarConvite,
        )

        is LoginUiState.Autenticado -> Unit // navegação tratada no LaunchedEffect acima
    }
}
