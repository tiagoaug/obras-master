package br.com.tiago.obramaster.ui.features.metas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import br.com.tiago.obramaster.core.util.DataFormatter
import br.com.tiago.obramaster.core.util.MoneyFormatter
import br.com.tiago.obramaster.domain.EscopoMeta
import br.com.tiago.obramaster.domain.ExportableDocument
import br.com.tiago.obramaster.domain.Meta
import br.com.tiago.obramaster.domain.TipoMeta
import br.com.tiago.obramaster.ui.components.CalculatorTextField
import br.com.tiago.obramaster.ui.components.LcrudListScaffold
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(onVoltar: () -> Unit, viewModel: MetasViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()
    var editando by remember { mutableStateOf<MetaComProgresso?>(null) }
    var mostrarForm by remember { mutableStateOf(false) }

    LcrudListScaffold(
        titulo = "Metas",
        itens = uiState.metas,
        filtro = { item, busca -> item.meta.titulo.contains(busca, ignoreCase = true) },
        itemHeadline = { it.meta.titulo },
        itemSupporting = { item -> descricaoProgresso(item) },
        onItemClicado = { editando = it; mostrarForm = true },
        onNovoClicado = { editando = null; mostrarForm = true },
        onExcluirConfirmado = { viewModel.excluir(it.meta.id) },
        onVoltar = onVoltar,
        exportar = { itens ->
            ExportableDocument(
                titulo = "Metas",
                colunas = listOf("Título", "Tipo", "Escopo", "Progresso"),
                linhas = itens.map { listOf(it.meta.titulo, it.meta.tipo.name, it.meta.escopo.name, descricaoProgresso(it)) },
            )
        },
    )

    if (mostrarForm) {
        MetaFormSheet(
            existente = editando,
            projetos = uiState.projetos,
            centrosDeCusto = uiState.centrosDeCusto,
            onDismiss = { mostrarForm = false },
            onSalvar = { escopo, referenciaId, titulo, tipo, valorAlvo, prazo ->
                viewModel.salvar(editando?.meta, escopo, referenciaId, titulo, tipo, valorAlvo, prazo)
                mostrarForm = false
            },
            onAlternarConcluida = { meta, concluida -> viewModel.marcarConcluida(meta, concluida); mostrarForm = false },
        )
    }
}

private fun descricaoProgresso(item: MetaComProgresso): String {
    val meta = item.meta
    val valores = when (meta.tipo) {
        TipoMeta.FINANCEIRA -> "${MoneyFormatter.formatar(item.valorAtual)} / ${MoneyFormatter.formatar(meta.valorAlvo)}"
        TipoMeta.PRAZO, TipoMeta.PROGRESSO -> "${item.valorAtual}% / ${meta.valorAlvo}%"
    }
    val prazoTexto = meta.prazo?.let { " · prazo ${DataFormatter.formatar(it)}" } ?: ""
    val status = when {
        meta.concluida -> " · concluída"
        item.atrasada -> " · atrasada"
        else -> ""
    }
    return "$valores$prazoTexto$status"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetaFormSheet(
    existente: MetaComProgresso?,
    projetos: List<br.com.tiago.obramaster.domain.Projeto>,
    centrosDeCusto: List<br.com.tiago.obramaster.domain.CentroDeCusto>,
    onDismiss: () -> Unit,
    onSalvar: (escopo: EscopoMeta, referenciaId: String?, titulo: String, tipo: TipoMeta, valorAlvo: Long, prazo: Long?) -> Unit,
    onAlternarConcluida: (Meta, Boolean) -> Unit,
) {
    val atual = existente?.meta
    var escopo by remember { mutableStateOf(atual?.escopo ?: EscopoMeta.GERAL) }
    var referenciaId by remember { mutableStateOf(atual?.referenciaId) }
    var titulo by remember { mutableStateOf(atual?.titulo ?: "") }
    var tipo by remember { mutableStateOf(atual?.tipo ?: TipoMeta.FINANCEIRA) }
    var valorAlvoCentavos by remember { mutableStateOf(if (atual?.tipo == TipoMeta.FINANCEIRA) atual.valorAlvo else 0L) }
    var valorAlvoPercentual by remember { mutableStateOf((if (atual != null && atual.tipo != TipoMeta.FINANCEIRA) atual.valorAlvo else 50L).toFloat()) }
    var prazoTexto by remember { mutableStateOf(atual?.prazo?.let(DataFormatter::formatar) ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (atual == null) "Nova meta" else "Editar meta", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(titulo, { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())

            Text("Escopo", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EscopoMeta.entries.forEach { opcao ->
                    FilterChip(
                        selected = escopo == opcao,
                        onClick = {
                            escopo = opcao
                            referenciaId = null
                            if (opcao == EscopoMeta.SETOR && tipo != TipoMeta.FINANCEIRA) tipo = TipoMeta.FINANCEIRA
                        },
                        label = { Text(opcao.name) },
                    )
                }
            }

            if (escopo == EscopoMeta.PROJETO && projetos.isNotEmpty()) {
                Text("Projeto", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    projetos.forEach { projeto ->
                        FilterChip(selected = referenciaId == projeto.id, onClick = { referenciaId = projeto.id }, label = { Text(projeto.nome) })
                    }
                }
            }
            if (escopo == EscopoMeta.SETOR && centrosDeCusto.isNotEmpty()) {
                Text("Centro de Custo", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    centrosDeCusto.forEach { centro ->
                        FilterChip(selected = referenciaId == centro.id, onClick = { referenciaId = centro.id }, label = { Text(centro.nome) })
                    }
                }
            }

            Text("Tipo", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tiposDisponiveis = if (escopo == EscopoMeta.SETOR) listOf(TipoMeta.FINANCEIRA) else TipoMeta.entries.toList()
                tiposDisponiveis.forEach { opcao ->
                    FilterChip(selected = tipo == opcao, onClick = { tipo = opcao }, label = { Text(opcao.name) })
                }
            }

            if (tipo == TipoMeta.FINANCEIRA) {
                CalculatorTextField(
                    valueCentavos = valorAlvoCentavos,
                    onValueChange = { valorAlvoCentavos = it },
                    label = "Meta de resultado (receitas − despesas)",
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text("Meta de progresso: ${valorAlvoPercentual.toInt()}%", style = MaterialTheme.typography.labelLarge)
                Slider(value = valorAlvoPercentual, onValueChange = { valorAlvoPercentual = it }, valueRange = 0f..100f)
            }

            OutlinedTextField(
                prazoTexto, { prazoTexto = it },
                label = { Text("Prazo (dd/mm/aaaa, opcional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (atual != null) {
                OutlinedButton(
                    onClick = { onAlternarConcluida(atual, !atual.concluida) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (atual.concluida) "Reabrir meta" else "Marcar como concluída") }
            }

            Button(
                onClick = {
                    val valorAlvo = if (tipo == TipoMeta.FINANCEIRA) valorAlvoCentavos else valorAlvoPercentual.toLong()
                    onSalvar(escopo, referenciaId, titulo, tipo, valorAlvo, DataFormatter.parseOuNulo(prazoTexto))
                },
                enabled = titulo.isNotBlank() && (escopo == EscopoMeta.GERAL || referenciaId != null),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Salvar") }
        }
    }
}
