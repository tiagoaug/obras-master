package br.com.tiago.obramaster.ui.features.configuracoes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.Colaborador
import org.koin.compose.koinInject

private sealed interface Destino {
    data object Inicio : Destino
    data object Colaboradores : Destino
    data object Acessibilidade : Destino
    data object Bdi : Destino
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    colaboradorLogado: Colaborador,
    onVoltar: () -> Unit,
    onAbrirAjuda: () -> Unit,
    viewModel: ConfiguracoesViewModel = koinInject(),
) {
    var destino by remember { mutableStateOf<Destino>(Destino.Inicio) }
    val uiState by viewModel.uiState.collectAsState()

    when (val atual = destino) {
        Destino.Inicio -> Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Configurações") },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                if (colaboradorLogado.ehGestor) {
                    Text(
                        "Apenas o Gestor pode ligar/desligar módulos",
                        modifier = Modifier.padding(16.dp),
                    )
                    uiState.modulos.values.forEach { disponibilidade ->
                        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ListItem(
                                headlineContent = { Text(disponibilidade.module.labelPtBr) },
                                trailingContent = {
                                    Switch(
                                        checked = disponibilidade.enabled,
                                        onCheckedChange = { habilitado ->
                                            viewModel.alternarModulo(disponibilidade.module, habilitado)
                                        },
                                    )
                                },
                            )
                        }
                    }
                    Text("Outras Configurações", modifier = Modifier.padding(16.dp))
                    Card(
                        onClick = { destino = Destino.Colaboradores },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        ListItem(
                            headlineContent = { Text("Colaboradores e Permissões") },
                            leadingContent = { Icon(Icons.Filled.Group, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Card(
                        onClick = { destino = Destino.Bdi },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        ListItem(
                            headlineContent = { Text("BDI (Orçamentos)") },
                            leadingContent = { Icon(Icons.Filled.Calculate, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Card(
                    onClick = { destino = Destino.Acessibilidade },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    ListItem(
                        headlineContent = { Text("Acessibilidade (Tema / Fonte)") },
                        leadingContent = { Icon(Icons.Filled.AccessibilityNew, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Card(
                    onClick = onAbrirAjuda,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    ListItem(
                        headlineContent = { Text("Ajuda (Manual do Programa)") },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Destino.Colaboradores -> ColaboradoresScreen(
            uiState = uiState,
            onVoltar = { destino = Destino.Inicio },
            onCriarColaborador = viewModel::criarColaborador,
            onErroCriarColaboradorConsumido = viewModel::erroCriarColaboradorConsumido,
            onDefinirPermissao = viewModel::definirPermissao,
            onDesativarColaborador = viewModel::desativarColaborador,
        )

        Destino.Acessibilidade -> AcessibilidadeScreen(onVoltar = { destino = Destino.Inicio })

        Destino.Bdi -> ConfigBdiScreen(onVoltar = { destino = Destino.Inicio })
    }
}
