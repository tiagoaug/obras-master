package br.com.tiago.obramaster.ui.features.pessoas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.TagPessoa
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

private sealed interface DestinoPessoas {
    data object Lista : DestinoPessoas
    data object Importar : DestinoPessoas
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PessoasScreen(onVoltar: () -> Unit, viewModel: PessoasViewModel = koinInject()) {
    var destino by remember { mutableStateOf<DestinoPessoas>(DestinoPessoas.Lista) }

    when (destino) {
        DestinoPessoas.Importar -> ImportarContatosScreen(
            onVoltar = { destino = DestinoPessoas.Lista },
            viewModel = viewModel,
        )

        DestinoPessoas.Lista -> {
            val pessoas by viewModel.pessoas.collectAsState()
            var editando by remember { mutableStateOf<Pessoa?>(null) }
            var mostrarForm by remember { mutableStateOf(false) }

            LcrudListScaffold(
                titulo = "Pessoas",
                itens = pessoas,
                filtro = { pessoa, busca ->
                    pessoa.nome.contains(busca, ignoreCase = true) ||
                        (pessoa.telefone?.contains(busca) == true) ||
                        (pessoa.email?.contains(busca, ignoreCase = true) == true)
                },
                itemHeadline = { it.nome },
                itemSupporting = { pessoa -> pessoa.tags.joinToString(", ") { it.name } },
                onItemClicado = { editando = it; mostrarForm = true },
                onNovoClicado = { editando = null; mostrarForm = true },
                onExcluirConfirmado = { viewModel.excluir(it.id) },
                onVoltar = onVoltar,
                acoesTopBar = {
                    IconButton(onClick = { destino = DestinoPessoas.Importar }) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Importar contatos")
                    }
                },
            )

            if (mostrarForm) {
                PessoaFormSheet(
                    existente = editando,
                    onDismiss = { mostrarForm = false },
                    onSalvar = { nome, tags, telefone, email, endereco, documento, observacoes ->
                        viewModel.salvar(editando, nome, tags, telefone, email, endereco, documento, observacoes)
                        mostrarForm = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PessoaFormSheet(
    existente: Pessoa?,
    onDismiss: () -> Unit,
    onSalvar: (
        nome: String,
        tags: Set<TagPessoa>,
        telefone: String?,
        email: String?,
        endereco: String?,
        documento: String?,
        observacoes: String?,
    ) -> Unit,
) {
    var nome by remember { mutableStateOf(existente?.nome ?: "") }
    var tags by remember { mutableStateOf(existente?.tags ?: emptySet()) }
    var telefone by remember { mutableStateOf(existente?.telefone ?: "") }
    var email by remember { mutableStateOf(existente?.email ?: "") }
    var endereco by remember { mutableStateOf(existente?.endereco ?: "") }
    var documento by remember { mutableStateOf(existente?.documento ?: "") }
    var observacoes by remember { mutableStateOf(existente?.observacoes ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (existente == null) "Nova pessoa" else "Editar pessoa", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())

            Text("Tags", style = MaterialTheme.typography.labelLarge)
            Row {
                TagPessoa.entries.forEach { tag ->
                    FilterChip(
                        selected = tag in tags,
                        onClick = { tags = if (tag in tags) tags - tag else tags + tag },
                        label = { Text(tag.name) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }

            OutlinedTextField(telefone, { telefone = it }, label = { Text("Telefone (opcional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("E-mail (opcional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(endereco, { endereco = it }, label = { Text("Endereço (opcional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(documento, { documento = it }, label = { Text("Documento (opcional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(observacoes, { observacoes = it }, label = { Text("Observações (opcional)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    onSalvar(
                        nome,
                        tags,
                        telefone.ifBlank { null },
                        email.ifBlank { null },
                        endereco.ifBlank { null },
                        documento.ifBlank { null },
                        observacoes.ifBlank { null },
                    )
                },
                enabled = nome.isNotBlank() && tags.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar") }
        }
    }
}
