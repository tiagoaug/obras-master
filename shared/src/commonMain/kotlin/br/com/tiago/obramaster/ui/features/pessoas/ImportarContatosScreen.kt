package br.com.tiago.obramaster.ui.features.pessoas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.ContatoImportado
import br.com.tiago.obramaster.domain.TagPessoa
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportarContatosScreen(onVoltar: () -> Unit, viewModel: PessoasViewModel = koinInject()) {
    var candidatos by remember { mutableStateOf<List<ContatoImportado>?>(null) }
    var selecionados by remember { mutableStateOf(setOf<ContatoImportado>()) }
    var tagPadrao by remember { mutableStateOf(TagPessoa.CLIENTE) }
    var agendaDisponivel by remember { mutableStateOf<Boolean?>(null) }
    var textoColado by remember { mutableStateOf("") }
    val importando by viewModel.importandoDaAgenda.collectAsState()

    LaunchedEffect(Unit) { agendaDisponivel = viewModel.agendaDisponivel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar Contatos") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            val listaAtual = candidatos
            when {
                listaAtual == null && agendaDisponivel == true -> {
                    Text("Toque para escolher da agenda do seu dispositivo (dá pra selecionar vários).")
                    Button(
                        onClick = { viewModel.buscarContatosDaAgenda { candidatos = it } },
                        enabled = !importando,
                        modifier = Modifier.padding(top = 12.dp),
                    ) { Text(if (importando) "Buscando..." else "Importar da agenda") }
                    if (importando) CircularProgressIndicator(Modifier.padding(top = 12.dp))
                }

                listaAtual == null && agendaDisponivel == false -> {
                    Text("Acesso à agenda não disponível aqui. Cole abaixo um CSV (colunas nome, telefone, email) ou um vCard (.vcf).")
                    OutlinedTextField(
                        value = textoColado,
                        onValueChange = { textoColado = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        minLines = 6,
                    )
                    Button(
                        onClick = { candidatos = viewModel.parsearArquivoImportacao(textoColado) },
                        enabled = textoColado.isNotBlank(),
                        modifier = Modifier.padding(top = 12.dp),
                    ) { Text("Processar") }
                }

                listaAtual == null -> CircularProgressIndicator()

                else -> {
                    Text("${selecionados.size} selecionado(s) — marque com que papel entram no cadastro:")
                    Row(Modifier.padding(vertical = 8.dp)) {
                        TagPessoa.entries.forEach { tag ->
                            FilterChip(
                                selected = tagPadrao == tag,
                                onClick = { tagPadrao = tag },
                                label = { Text(tag.name) },
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                    }
                    LazyColumn(Modifier.weight(1f)) {
                        items(listaAtual) { contato ->
                            ListItem(
                                headlineContent = { Text(contato.nome) },
                                supportingContent = { Text(contato.telefone ?: contato.email ?: "") },
                                leadingContent = {
                                    Checkbox(
                                        checked = contato in selecionados,
                                        onCheckedChange = { marcado ->
                                            selecionados = if (marcado) selecionados + contato else selecionados - contato
                                        },
                                    )
                                },
                            )
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.importarSelecionados(selecionados.toList(), tagPadrao)
                            onVoltar()
                        },
                        enabled = selecionados.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Importar ${selecionados.size}") }
                }
            }
        }
    }
}
