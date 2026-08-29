package br.com.tiago.obramaster.ui.features.projetos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.domain.Projeto
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjetosScreen(
    onVoltar: () -> Unit,
    onAbrirProjeto: (String) -> Unit,
    viewModel: ProjetosViewModel = koinInject(),
) {
    val projetos by viewModel.projetos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Projetos",
        itens = projetos,
        filtro = { projeto, busca -> projeto.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.nome },
        itemSupporting = { projeto -> "${projeto.status.name} · ${MoneyFormatter.formatar(projeto.orcamentoTotal)}" },
        onItemClicado = { onAbrirProjeto(it.id) },
        onNovoClicado = { mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        exportar = { itens ->
            ExportableDocument(
                titulo = "Projetos",
                colunas = listOf("Nome", "Status", "Orçamento total"),
                linhas = itens.map { listOf(it.nome, it.status.name, MoneyFormatter.formatar(it.orcamentoTotal)) },
            )
        },
    )

    uiState.projetoPendenteDoOnboarding?.let { draft ->
        Card(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Você começou a cadastrar \"${draft.nome}\" no primeiro acesso.", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.criarDoOnboarding() }) { Text("Concluir agora") }
                    OutlinedButton(onClick = { viewModel.descartarPendenciaOnboarding() }) { Text("Descartar") }
                }
            }
        }
    }

    if (mostrarForm) {
        var nome by remember { mutableStateOf("") }
        var endereco by remember { mutableStateOf("") }
        var areaConstruida by remember { mutableStateOf("") }
        var areaTerreno by remember { mutableStateOf("") }
        var orcamentoCentavos by remember { mutableStateOf(0L) }
        var aplicarTemplate by remember { mutableStateOf(uiState.preferenciaTemplatePadrao) }

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Novo projeto", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(endereco, { endereco = it }, label = { Text("Endereço (opcional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(areaConstruida, { areaConstruida = it }, label = { Text("Área construída m² (opcional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(areaTerreno, { areaTerreno = it }, label = { Text("Área do terreno m² (opcional)") }, modifier = Modifier.fillMaxWidth())
                CalculatorTextField(
                    valueCentavos = orcamentoCentavos,
                    onValueChange = { orcamentoCentavos = it },
                    label = "Orçamento total",
                    modifier = Modifier.fillMaxWidth(),
                )
                Row {
                    Checkbox(checked = aplicarTemplate, onCheckedChange = { aplicarTemplate = it })
                    Text("Aplicar template padrão de etapas", modifier = Modifier.padding(top = 12.dp))
                }
                Button(
                    onClick = {
                        viewModel.criar(
                            nome = nome,
                            clienteId = null,
                            endereco = endereco.ifBlank { null },
                            areaConstruidaM2 = areaConstruida.toDoubleOrNull(),
                            areaTerrenoM2 = areaTerreno.toDoubleOrNull(),
                            orcamentoTotal = orcamentoCentavos,
                            aplicarTemplateEtapas = aplicarTemplate,
                        )
                        mostrarForm = false
                    },
                    enabled = nome.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}
