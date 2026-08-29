package br.com.tiago.obramaster.ui.features.configuracoes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import br.com.tiago.obramaster.core.orcamentos.BdiEngine
import br.com.tiago.obramaster.domain.ConfigBDI
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigBdiScreen(onVoltar: () -> Unit, viewModel: ConfigBdiViewModel = koinInject()) {
    val perfis by viewModel.perfis.collectAsState()
    var editando by remember { mutableStateOf<ConfigBDI?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Perfis de BDI",
        itens = perfis,
        filtro = { perfil, busca -> perfil.nome.contains(busca, ignoreCase = true) },
        itemHeadline = { it.nome + (if (it.padrao) " · padrão" else "") },
        itemSupporting = { "BDI calculado: ${percentTexto(BdiEngine.calcularBdi(it) * 100)}%" },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.id) },
        onVoltar = onVoltar,
        exportar = { itens ->
            ExportableDocument(
                titulo = "Perfis de BDI",
                colunas = listOf("Nome", "Padrão", "BDI calculado"),
                linhas = itens.map { listOf(it.nome, if (it.padrao) "Sim" else "Não", "${percentTexto(BdiEngine.calcularBdi(it) * 100)}%") },
            )
        },
    )

    if (mostrarForm) {
        val atual = editando
        var nome by remember { mutableStateOf(atual?.nome ?: "") }
        var ac by remember { mutableStateOf(percentTexto((atual?.administracaoCentral ?: 0.0) * 100)) }
        var s by remember { mutableStateOf(percentTexto((atual?.seguroGarantia ?: 0.0) * 100)) }
        var r by remember { mutableStateOf(percentTexto((atual?.riscos ?: 0.0) * 100)) }
        var df by remember { mutableStateOf(percentTexto((atual?.despesasFinanceiras ?: 0.0) * 100)) }
        var l by remember { mutableStateOf(percentTexto((atual?.lucro ?: 0.0) * 100)) }
        var i by remember { mutableStateOf(percentTexto((atual?.tributos ?: 0.0) * 100)) }
        var padrao by remember { mutableStateOf(atual?.padrao ?: false) }

        val tributosDecimal = (i.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100
        val tributosInvalidos = tributosDecimal >= 1.0
        val previa = if (!tributosInvalidos) {
            BdiEngine.calcularBdi(
                ConfigBDI(
                    id = "", nome = "",
                    administracaoCentral = (ac.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                    seguroGarantia = (s.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                    riscos = (r.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                    despesasFinanceiras = (df.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                    lucro = (l.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                    tributos = tributosDecimal,
                ),
            ) * 100
        } else null

        ModalBottomSheet(onDismissRequest = { mostrarForm = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (atual == null) "Novo perfil de BDI" else "Editar perfil de BDI", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(ac, { ac = it }, label = { Text("Administração Central (%)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(s, { s = it }, label = { Text("Seguros e Garantias (%)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(r, { r = it }, label = { Text("Riscos (%)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(df, { df = it }, label = { Text("Despesas Financeiras (%)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(l, { l = it }, label = { Text("Lucro (%)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    i, { i = it }, label = { Text("Tributos sobre faturamento (%)") },
                    isError = tributosInvalidos, modifier = Modifier.fillMaxWidth(),
                )
                if (tributosInvalidos) {
                    Text(
                        "Tributos não podem ser 100% ou mais — combinação inválida (divisão por zero/negativo).",
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall,
                    )
                }

                previa?.let { Text("BDI calculado: ${percentTexto(it)}%", style = MaterialTheme.typography.titleSmall) }

                Row {
                    Checkbox(checked = padrao, onCheckedChange = { padrao = it })
                    Text("Usar como padrão em novos orçamentos", modifier = Modifier.padding(top = 12.dp))
                }

                Button(
                    onClick = {
                        viewModel.salvar(
                            atual, nome,
                            (ac.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                            (s.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                            (r.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                            (df.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                            (l.replace(',', '.').toDoubleOrNull() ?: 0.0) / 100,
                            tributosDecimal,
                            padrao,
                        )
                        mostrarForm = false
                    },
                    enabled = nome.isNotBlank() && !tributosInvalidos,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Salvar") }
            }
        }
    }
}

private fun percentTexto(valor: Double): String {
    val arredondado = kotlin.math.round(valor * 100) / 100
    return if (arredondado == arredondado.toLong().toDouble()) arredondado.toLong().toString() else arredondado.toString()
}
