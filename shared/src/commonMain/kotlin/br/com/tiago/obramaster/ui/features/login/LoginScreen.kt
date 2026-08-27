package br.com.tiago.obramaster.ui.features.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.tiago.obramaster.domain.Colaborador
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onAutenticado: (Colaborador) -> Unit,
    viewModel: LoginViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val estado = uiState
        if (estado is LoginUiState.Autenticado) onAutenticado(estado.colaborador)
    }

    when (val estado = uiState) {
        LoginUiState.Carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        LoginUiState.PrimeiroAcesso -> CriarGestorForm(onCriar = viewModel::criarGestor)

        is LoginUiState.TelaLogin -> LoginForm(
            erro = estado.erro,
            autenticando = estado.autenticando,
            onEntrar = viewModel::login,
        )

        is LoginUiState.Autenticado -> Unit // navegação tratada no LaunchedEffect acima
    }
}
