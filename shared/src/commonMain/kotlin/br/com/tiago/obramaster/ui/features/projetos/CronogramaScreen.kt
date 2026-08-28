package br.com.tiago.obramaster.ui.features.projetos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.planejamento.CronogramaEngine
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.domain.Etapa
import br.com.tiago.obramaster.domain.Pessoa
import br.com.tiago.obramaster.domain.StatusEtapa
import br.com.tiago.obramaster.domain.Tarefa
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CronogramaScreen(
    projetoId: String,
    onVoltar: () -> Unit,
    viewModel: CronogramaViewModel = koinInject { parametersOf(projetoId) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val hoje = remember { Clock.System.now().toEpochMilliseconds() }
    val janela = remember(uiState.etapas) { CronogramaEngine.janelaPrevista(uiState.etapas) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cronograma") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.etapas) { etapa ->
                EtapaCronogramaCard(
                    etapa = etapa,
                    janela = janela,
                    hoje = hoje,
                    funcionarios = uiState.funcionarios,
                    tarefas = viewModel.tarefasDaEtapa(etapa.id).collectAsState(emptyList()).value,
                    onAtualizarDatas = { dataInicio, dataFim, dataInicioReal, dataFimReal ->
                        viewModel.atualizarDatas(etapa, dataInicio, dataFim, dataInicioReal, dataFimReal)
                    },
                    onSalvarTarefa = { existente, descricao, responsavelId, prazo ->
                        viewModel.salvarTarefa(existente, etapa.id, descricao, responsavelId, prazo)
                    },
                    onAlternarConcluida = viewModel::alternarConcluida,
                    onExcluirTarefa = viewModel::excluirTarefa,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EtapaCronogramaCard(
    etapa: Etapa,
    janela: Pair<Long, Long>?,
    hoje: Long,
    funcionarios: List<Pessoa>,
    tarefas: List<Tarefa>,
    onAtualizarDatas: (Long?, Long?, Long?, Long?) -> Unit,
    onSalvarTarefa: (Tarefa?, String, String?, Long?) -> Unit,
    onAlternarConcluida: (Tarefa) -> Unit,
    onExcluirTarefa: (String) -> Unit,
) {
    var dataInicioTexto by remember(etapa.id) { mutableStateOf(etapa.dataInicio?.let(DataFormatter::formatar) ?: "") }
    var dataFimTexto by remember(etapa.id) { mutableStateOf(etapa.dataFim?.let(DataFormatter::formatar) ?: "") }
    var dataInicioRealTexto by remember(etapa.id) { mutableStateOf(etapa.dataInicioReal?.let(DataFormatter::formatar) ?: "") }
    var dataFimRealTexto by remember(etapa.id) { mutableStateOf(etapa.dataFimReal?.let(DataFormatter::formatar) ?: "") }
    var mostrarNovaTarefa by remember { mutableStateOf(false) }
    var tarefaEditando by remember { mutableStateOf<Tarefa?>(null) }

    val atrasada = CronogramaEngine.estaAtrasada(etapa, hoje)

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(etapa.nome, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (atrasada) "Atrasada" else etapa.status.name,
                    color = if (atrasada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            if (janela != null && etapa.dataInicio != null && etapa.dataFim != null) {
                BarraGantt(etapa = etapa, janela = janela, atrasada = atrasada)
            }

            Text("Datas previstas", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    dataInicioTexto, { dataInicioTexto = it }, label = { Text("Início") }, modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    dataFimTexto, { dataFimTexto = it }, label = { Text("Fim") }, modifier = Modifier.weight(1f),
                )
            }

            Text("Datas reais", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    dataInicioRealTexto, { dataInicioRealTexto = it }, label = { Text("Início") }, modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    dataFimRealTexto, { dataFimRealTexto = it }, label = { Text("Fim") }, modifier = Modifier.weight(1f),
                )
            }

            OutlinedButton(
                onClick = {
                    onAtualizarDatas(
                        DataFormatter.parseOuNulo(dataInicioTexto),
                        DataFormatter.parseOuNulo(dataFimTexto),
                        DataFormatter.parseOuNulo(dataInicioRealTexto),
                        DataFormatter.parseOuNulo(dataFimRealTexto),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar datas") }

            HorizontalDivider()
            Text("Tarefas (${tarefas.count { it.concluida }}/${tarefas.size})", style = MaterialTheme.typography.labelLarge)
            tarefas.forEach { tarefa ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        Checkbox(checked = tarefa.concluida, onCheckedChange = { onAlternarConcluida(tarefa) })
                        Column(Modifier.padding(top = 12.dp)) {
                            Text(tarefa.descricao, modifier = Modifier.padding(0.dp))
                            val nomeResponsavel = funcionarios.firstOrNull { it.id == tarefa.responsavelPessoaId }?.nome
                            val detalhe = listOfNotNull(
                                nomeResponsavel,
                                tarefa.prazo?.let { "prazo ${DataFormatter.formatar(it)}" },
                            ).joinToString(" · ")
                            if (detalhe.isNotBlank()) Text(detalhe, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    IconButton(onClick = { onExcluirTarefa(tarefa.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Excluir tarefa") }
                }
            }
            OutlinedButton(onClick = { tarefaEditando = null; mostrarNovaTarefa = true }) { Text("+ Tarefa") }
        }
    }

    if (mostrarNovaTarefa) {
        NovaTarefaSheet(
            existente = tarefaEditando,
            funcionarios = funcionarios,
            onSalvar = { descricao, responsavelId, prazo -> onSalvarTarefa(tarefaEditando, descricao, responsavelId, prazo); mostrarNovaTarefa = false },
            onDismiss = { mostrarNovaTarefa = false },
        )
    }
}

@Composable
private fun BarraGantt(etapa: Etapa, janela: Pair<Long, Long>, atrasada: Boolean) {
    val (inicioJanela, fimJanela) = janela
    val duracaoJanela = (fimJanela - inicioJanela).coerceAtLeast(1L).toFloat()
    val inicio = (etapa.dataInicio!! - inicioJanela).coerceAtLeast(0L).toFloat()
    val fim = (etapa.dataFim!! - inicioJanela).coerceAtLeast(0L).toFloat()
    val cor = when {
        atrasada -> Color(0xFFEF5350)
        etapa.status == StatusEtapa.CONCLUIDA -> Color(0xFF66BB6A)
        etapa.status == StatusEtapa.EM_ANDAMENTO -> Color(0xFF42A5F5)
        else -> Color(0xFFB0BEC5)
    }
    Canvas(Modifier.fillMaxWidth().height(20.dp)) {
        val xInicio = (inicio / duracaoJanela) * size.width
        val largura = ((fim - inicio) / duracaoJanela) * size.width
        drawRect(color = cor, topLeft = Offset(xInicio, 0f), size = Size(largura.coerceAtLeast(2f), size.height))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaTarefaSheet(
    existente: Tarefa?,
    funcionarios: List<Pessoa>,
    onSalvar: (String, String?, Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    var descricao by remember { mutableStateOf(existente?.descricao ?: "") }
    var responsavelId by remember { mutableStateOf(existente?.responsavelPessoaId) }
    var prazoTexto by remember { mutableStateOf(existente?.prazo?.let(DataFormatter::formatar) ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (existente == null) "Nova tarefa" else "Editar tarefa", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(descricao, { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())

            Text("Responsável (opcional)", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = responsavelId == null, onClick = { responsavelId = null }, label = { Text("Nenhum") })
                funcionarios.forEach { pessoa ->
                    FilterChip(selected = responsavelId == pessoa.id, onClick = { responsavelId = pessoa.id }, label = { Text(pessoa.nome) })
                }
            }

            OutlinedTextField(prazoTexto, { prazoTexto = it }, label = { Text("Prazo (dd/mm/aaaa, opcional)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = { onSalvar(descricao, responsavelId, DataFormatter.parseOuNulo(prazoTexto)) },
                enabled = descricao.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar") }
        }
    }
}
