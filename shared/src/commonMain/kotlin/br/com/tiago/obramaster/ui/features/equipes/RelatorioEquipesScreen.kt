package br.com.tiago.obramaster.ui.features.equipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.equipes.ResumoTrabalho
import br.com.tiago.obramaster.core.util.MoneyFormatter
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioEquipesScreen(onVoltar: () -> Unit, viewModel: RelatorioEquipesViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório de Equipes") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Por funcionário", style = MaterialTheme.typography.titleMedium)
            if (uiState.porPessoa.isEmpty()) {
                Text("Nenhum registro de trabalho ainda.", style = MaterialTheme.typography.bodyMedium)
            }
            uiState.porPessoa.forEach { item -> CartaoResumo(item.pessoa.nome, item.resumo) }

            Text("Por equipe", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
            if (uiState.porEquipe.isEmpty()) {
                Text("Nenhuma equipe cadastrada ainda.", style = MaterialTheme.typography.bodyMedium)
            }
            uiState.porEquipe.forEach { item -> CartaoResumo(item.equipe.nome, item.resumo) }
        }
    }
}

@Composable
private fun CartaoResumo(titulo: String, resumo: ResumoTrabalho) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text("${resumo.diasTrabalhados} diária(s) trabalhada(s)", style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("A pagar: ${MoneyFormatter.formatar(resumo.totalAPagar)}", style = MaterialTheme.typography.labelMedium)
                Text("Pago: ${MoneyFormatter.formatar(resumo.totalPago)}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
