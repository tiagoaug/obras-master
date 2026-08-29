package br.com.tiago.obramaster.ui.features.configuracoes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.auth.NivelPermissao
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.domain.Permissao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColaboradoresScreen(
    uiState: ConfiguracoesUiState,
    onVoltar: () -> Unit,
    onConvidarColaborador: (nome: String, email: String) -> Unit,
    onDefinirPermissao: (colaboradorId: String, modulo: AppModule, nivel: NivelPermissao) -> Unit,
    onDesativarColaborador: (String) -> Unit,
) {
    var mostrarFormNovo by remember { mutableStateOf(false) }
    var colaboradorSelecionado by remember { mutableStateOf<Colaborador?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Colaboradores") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarFormNovo = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Novo colaborador")
            }
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(uiState.colaboradores) { colaborador ->
                Card(
                    onClick = { if (!colaborador.ehGestor) colaboradorSelecionado = colaborador },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    ListItem(
                        headlineContent = { Text(colaborador.nome) },
                        supportingContent = {
                            Text(if (colaborador.ehGestor) "Gestor (acesso total)" else colaborador.email)
                        },
                        trailingContent = if (!colaborador.ehGestor) {
                            {
                                IconButton(onClick = { onDesativarColaborador(colaborador.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Desativar")
                                }
                            }
                        } else null,
                    )
                }
            }
        }
    }

    if (mostrarFormNovo) {
        NovoColaboradorSheet(
            onDismiss = { mostrarFormNovo = false },
            onConvidar = { nome, email ->
                onConvidarColaborador(nome, email)
                mostrarFormNovo = false
            },
        )
    }

    colaboradorSelecionado?.let { colaborador ->
        PermissoesSheet(
            colaborador = colaborador,
            permissoesAtuais = uiState.permissoes.filter { it.colaboradorId == colaborador.id },
            onDismiss = { colaboradorSelecionado = null },
            onDefinirPermissao = onDefinirPermissao,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovoColaboradorSheet(
    onDismiss: () -> Unit,
    onConvidar: (nome: String, email: String) -> Unit,
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Convidar colaborador", style = MaterialTheme.typography.titleMedium)
            Text(
                "A pessoa recebe acesso ao entrar com o próprio Google ou definir a própria senha — nenhuma senha passa por aqui.",
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
            androidx.compose.material3.Button(
                onClick = { onConvidar(nome, email) },
                enabled = nome.isNotBlank() && email.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enviar convite") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissoesSheet(
    colaborador: Colaborador,
    permissoesAtuais: List<Permissao>,
    onDismiss: () -> Unit,
    onDefinirPermissao: (colaboradorId: String, modulo: AppModule, nivel: NivelPermissao) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Permissões de ${colaborador.nome}", style = MaterialTheme.typography.titleMedium)
            LazyColumn(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                items(AppModule.entries.toList()) { modulo ->
                    val nivelAtual = permissoesAtuais.firstOrNull { it.moduleId == modulo.id }?.nivel
                        ?: NivelPermissao.NENHUM
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Text(modulo.labelPtBr, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            NivelPermissao.entries.forEach { nivel ->
                                AssistChip(
                                    onClick = { onDefinirPermissao(colaborador.id, modulo, nivel) },
                                    label = { Text(nivel.name, style = MaterialTheme.typography.labelLarge) },
                                    colors = if (nivel == nivelAtual) {
                                        androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            labelColor = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    } else {
                                        androidx.compose.material3.AssistChipDefaults.assistChipColors()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
