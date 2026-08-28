package br.com.tiago.obramaster.ui.features.plantabaixa

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.tiago.obramaster.core.util.DecimalFormatter
import br.com.tiago.obramaster.domain.Comodo
import br.com.tiago.obramaster.domain.PontoXY
import br.com.tiago.obramaster.domain.TipoAbertura
import br.com.tiago.obramaster.ui.components.decodeImageBitmap
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

private const val TAMANHO_GRADE_PX = 40.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorPlantaScreen(
    plantaId: String,
    onVoltar: () -> Unit,
    viewModel: PlantaBaixaViewModel = koinInject { parametersOf(plantaId) },
) {
    val uiState by viewModel.uiState.collectAsState()
    var arrastoAtual by remember { mutableStateOf<Pair<Offset, Offset>?>(null) }
    var mostrarEscala by remember { mutableStateOf(false) }
    var mostrarImagemSheet by remember { mutableStateOf(false) }
    var distanciaCalibracaoTexto by remember { mutableStateOf("") }

    val imagemBitmap = uiState.imagemFundoBytes?.let { bytes -> remember(bytes) { decodeImageBitmap(bytes) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.planta?.nome ?: "Planta Baixa") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
                actions = {
                    IconButton(onClick = { viewModel.importarArquivo() }) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "Importar arquivo (DXF/SVG)")
                    }
                    IconButton(onClick = { mostrarImagemSheet = true }) {
                        Icon(Icons.Filled.Image, contentDescription = "Imagem de fundo")
                    }
                    IconButton(onClick = { mostrarEscala = true }) {
                        Icon(Icons.Filled.Straighten, contentDescription = "Ajustar escala")
                    }
                    IconButton(onClick = { viewModel.desfazerUltimaForma() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Desfazer última forma")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FerramentaIcone(FerramentaDesenho.SELECIONAR, Icons.Filled.NearMe, uiState.ferramentaAtual, viewModel::selecionarFerramenta)
                FerramentaIcone(FerramentaDesenho.RETANGULO, Icons.Outlined.CropSquare, uiState.ferramentaAtual, viewModel::selecionarFerramenta)
                FerramentaIcone(FerramentaDesenho.POLIGONO, Icons.Filled.CheckCircle, uiState.ferramentaAtual, viewModel::selecionarFerramenta)
                FerramentaIcone(FerramentaDesenho.PORTA, Icons.Filled.DoorFront, uiState.ferramentaAtual, viewModel::selecionarFerramenta)
                FerramentaIcone(FerramentaDesenho.JANELA, Icons.Filled.Window, uiState.ferramentaAtual, viewModel::selecionarFerramenta)
                FerramentaIcone(FerramentaDesenho.MEDIR, Icons.Filled.Straighten, uiState.ferramentaAtual, viewModel::selecionarFerramenta)
                if (imagemBitmap != null || uiState.paredes.isNotEmpty() || uiState.comodos.isNotEmpty()) {
                    FerramentaIcone(FerramentaDesenho.CALIBRAR, Icons.Filled.Image, uiState.ferramentaAtual, viewModel::selecionarFerramenta)
                }
            }

            uiState.arquivoOrigemMaisRecente?.let { origem ->
                Text(
                    "Importado de ${origem.nomeArquivoOriginal}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }

            if (uiState.ferramentaAtual == FerramentaDesenho.POLIGONO && uiState.pontosPoligonoEmDesenho.isNotEmpty()) {
                Text(
                    "${uiState.pontosPoligonoEmDesenho.size} ponto(s) — toque perto do primeiro ponto pra fechar",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            uiState.medidaResultadoM?.let { medida ->
                Text(
                    "Distância medida: ${DecimalFormatter.formatar(medida)} m",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            if (uiState.ferramentaAtual == FerramentaDesenho.CALIBRAR) {
                Text(
                    if (uiState.pontoCalibracaoA == null) {
                        "Toque no início de uma medida conhecida no desenho"
                    } else {
                        "Toque no fim dessa medida"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }

            val textMeasurer = rememberTextMeasurer()

            Box(Modifier.weight(1f).fillMaxWidth()) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(uiState.ferramentaAtual) {
                            when (uiState.ferramentaAtual) {
                                FerramentaDesenho.RETANGULO -> detectDragGestures(
                                    onDragStart = { arrastoAtual = it to it },
                                    onDrag = { change, _ -> arrastoAtual = arrastoAtual?.first?.to(change.position) },
                                    onDragEnd = {
                                        arrastoAtual?.let { (inicio, fim) ->
                                            viewModel.criarRetangulo(
                                                PontoXY(inicio.x.toDouble(), inicio.y.toDouble()),
                                                PontoXY(fim.x.toDouble(), fim.y.toDouble()),
                                            )
                                        }
                                        arrastoAtual = null
                                    },
                                )

                                FerramentaDesenho.POLIGONO -> detectTapGestures { offset ->
                                    viewModel.tocarParaPoligono(PontoXY(offset.x.toDouble(), offset.y.toDouble()), TAMANHO_GRADE_PX)
                                }

                                FerramentaDesenho.PORTA -> detectTapGestures { offset ->
                                    viewModel.tocarParaAbertura(PontoXY(offset.x.toDouble(), offset.y.toDouble()), TipoAbertura.PORTA)
                                }

                                FerramentaDesenho.JANELA -> detectTapGestures { offset ->
                                    viewModel.tocarParaAbertura(PontoXY(offset.x.toDouble(), offset.y.toDouble()), TipoAbertura.JANELA)
                                }

                                FerramentaDesenho.MEDIR -> detectTapGestures { offset ->
                                    viewModel.tocarParaMedir(PontoXY(offset.x.toDouble(), offset.y.toDouble()))
                                }

                                FerramentaDesenho.SELECIONAR -> detectTapGestures { offset ->
                                    val ponto = PontoXY(offset.x.toDouble(), offset.y.toDouble())
                                    viewModel.selecionarComodo(viewModel.comodoNoPonto(ponto)?.id)
                                }

                                FerramentaDesenho.CALIBRAR -> detectTapGestures { offset ->
                                    viewModel.tocarParaCalibrar(PontoXY(offset.x.toDouble(), offset.y.toDouble()))
                                }
                            }
                        },
                ) {
                    // Imagem de fundo (Caminho B — SPEC_PLANTA_BAIXA.md §5)
                    if (uiState.mostrarImagemFundo && imagemBitmap != null) {
                        drawImage(
                            image = imagemBitmap,
                            dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()),
                            alpha = uiState.planta?.imagemFundoOpacidade ?: 0.5f,
                        )
                    }

                    // Grade
                    var x = 0f
                    while (x < size.width) {
                        drawLine(Color(0xFFE0E0E0), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += TAMANHO_GRADE_PX.toFloat()
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(Color(0xFFE0E0E0), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += TAMANHO_GRADE_PX.toFloat()
                    }

                    // Cômodos
                    uiState.comodos.forEach { comodo ->
                        if (comodo.pontos.size >= 3) {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(comodo.pontos[0].x.toFloat(), comodo.pontos[0].y.toFloat())
                                comodo.pontos.drop(1).forEach { lineTo(it.x.toFloat(), it.y.toFloat()) }
                                close()
                            }
                            val corBase = runCatching { Color(("FF" + comodo.corPreenchimento.removePrefix("#")).toLong(16)) }
                                .getOrDefault(Color(0xFF90CAF9))
                            val destacado = comodo.id == uiState.comodoSelecionadoId
                            drawPath(path, color = corBase.copy(alpha = if (destacado) 0.6f else 0.35f))
                            drawPath(path, color = corBase, style = Stroke(width = if (destacado) 4f else 2f))

                            val centroide = centroidePoligono(comodo.pontos)
                            drawText(
                                textMeasurer,
                                text = "${comodo.nome}\n${DecimalFormatter.formatar(comodo.areaM2, 1)} m²",
                                topLeft = Offset(centroide.x.toFloat() - 30f, centroide.y.toFloat() - 14f),
                                style = TextStyle(fontSize = 11.sp, color = Color.Black),
                            )
                        }
                    }

                    // Paredes
                    uiState.paredes.forEach { parede ->
                        drawLine(
                            color = Color(0xFF424242),
                            start = Offset(parede.pontoInicio.x.toFloat(), parede.pontoInicio.y.toFloat()),
                            end = Offset(parede.pontoFim.x.toFloat(), parede.pontoFim.y.toFloat()),
                            strokeWidth = 6f,
                        )
                    }

                    // Aberturas (marca colorida na posição ao longo da parede)
                    uiState.aberturas.forEach { abertura ->
                        val parede = uiState.paredes.firstOrNull { it.id == abertura.paredeId } ?: return@forEach
                        val px = parede.pontoInicio.x + (parede.pontoFim.x - parede.pontoInicio.x) * abertura.posicaoNaParede
                        val py = parede.pontoInicio.y + (parede.pontoFim.y - parede.pontoInicio.y) * abertura.posicaoNaParede
                        drawCircle(
                            color = if (abertura.tipo == TipoAbertura.PORTA) Color(0xFF8D6E63) else Color(0xFF4FC3F7),
                            radius = 6f,
                            center = Offset(px.toFloat(), py.toFloat()),
                        )
                    }

                    // Polígono em desenho (preview)
                    val pontosEmDesenho = uiState.pontosPoligonoEmDesenho
                    if (pontosEmDesenho.isNotEmpty()) {
                        for (i in 0 until pontosEmDesenho.size - 1) {
                            drawLine(
                                Color(0xFFFF6D00),
                                Offset(pontosEmDesenho[i].x.toFloat(), pontosEmDesenho[i].y.toFloat()),
                                Offset(pontosEmDesenho[i + 1].x.toFloat(), pontosEmDesenho[i + 1].y.toFloat()),
                                strokeWidth = 3f,
                            )
                        }
                        pontosEmDesenho.forEach { ponto ->
                            drawCircle(Color(0xFFFF6D00), radius = 5f, center = Offset(ponto.x.toFloat(), ponto.y.toFloat()))
                        }
                    }

                    // Ponto/linha de calibração
                    uiState.pontoCalibracaoA?.let { ponto ->
                        drawCircle(Color(0xFFD500F9), radius = 6f, center = Offset(ponto.x.toFloat(), ponto.y.toFloat()))
                    }
                    uiState.linhaCalibracaoPendente?.let { (a, b) ->
                        drawLine(
                            Color(0xFFD500F9),
                            Offset(a.x.toFloat(), a.y.toFloat()),
                            Offset(b.x.toFloat(), b.y.toFloat()),
                            strokeWidth = 3f,
                        )
                    }

                    // Preview do retângulo sendo arrastado
                    arrastoAtual?.let { (inicio, fim) ->
                        drawRect(
                            color = Color(0xFF1976D2).copy(alpha = 0.3f),
                            topLeft = Offset(minOf(inicio.x, fim.x), minOf(inicio.y, fim.y)),
                            size = androidx.compose.ui.geometry.Size(kotlin.math.abs(fim.x - inicio.x), kotlin.math.abs(fim.y - inicio.y)),
                        )
                    }
                }
            }

            val comodoSelecionado = uiState.comodos.firstOrNull { it.id == uiState.comodoSelecionadoId }
            if (comodoSelecionado != null) {
                PainelComodoSelecionado(
                    comodo = comodoSelecionado,
                    onRenomear = viewModel::renomearComodoSelecionado,
                    onExcluir = { viewModel.excluirComodo(comodoSelecionado.id) },
                )
            }
        }
    }

    if (mostrarEscala) {
        EscalaDialog(
            onConfirmar = { metros ->
                viewModel.definirEscalaManual(metros, TAMANHO_GRADE_PX)
                mostrarEscala = false
            },
            onDispensar = { mostrarEscala = false },
        )
    }

    if (mostrarImagemSheet) {
        ImagemFundoSheet(
            uiState = uiState,
            onImportar = { viewModel.importarImagemDaGaleria() },
            onAlternarVisibilidade = { viewModel.alternarVisibilidadeImagemFundo() },
            onOpacidadeMudou = { viewModel.definirOpacidadeImagemFundo(it) },
            onRecalibrar = {
                mostrarImagemSheet = false
                viewModel.selecionarFerramenta(FerramentaDesenho.CALIBRAR)
            },
            onDismiss = { mostrarImagemSheet = false },
        )
    }

    if (uiState.importandoArquivo) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Importando arquivo...") },
            text = { CircularProgressIndicator() },
            confirmButton = {},
        )
    }

    uiState.erroImportacaoArquivo?.let { erro ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelarImportacaoArquivo() },
            title = { Text("Não foi possível importar") },
            text = { Text(erro) },
            confirmButton = {
                Button(onClick = { viewModel.cancelarImportacaoArquivo() }) { Text("Ok") }
            },
        )
    }

    uiState.previaImportacao?.let { previa ->
        ImportacaoArquivoDialog(
            nomeArquivo = uiState.nomeArquivoImportado.orEmpty(),
            previa = previa,
            camadasSelecionadas = uiState.camadasSelecionadas ?: previa.camadasEncontradas.toSet(),
            onAlternarCamada = { viewModel.alternarCamadaSelecionada(it) },
            onCancelar = { viewModel.cancelarImportacaoArquivo() },
            onConfirmar = { viewModel.confirmarImportacaoArquivo() },
        )
    }

    uiState.linhaCalibracaoPendente?.let {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarCalibracao() },
            title = { Text("Qual a distância real dessa linha?") },
            text = {
                OutlinedTextField(
                    value = distanciaCalibracaoTexto,
                    onValueChange = { distanciaCalibracaoTexto = it },
                    label = { Text("Metros (ex.: 3,50)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = {
                    distanciaCalibracaoTexto.replace(",", ".").toDoubleOrNull()?.let { distancia ->
                        viewModel.confirmarCalibracao(distancia)
                        distanciaCalibracaoTexto = ""
                    }
                }) { Text("Calibrar") }
            },
            dismissButton = {
                Button(onClick = { viewModel.cancelarCalibracao() }) { Text("Cancelar") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagemFundoSheet(
    uiState: EditorPlantaUiState,
    onImportar: () -> Unit,
    onAlternarVisibilidade: () -> Unit,
    onOpacidadeMudou: (Float) -> Unit,
    onRecalibrar: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Imagem de fundo", style = MaterialTheme.typography.titleMedium)

            if (uiState.imagemFundoBytes == null) {
                Text("Importe uma foto do projeto pra usar como referência por baixo do desenho.")
                Button(onClick = onImportar, enabled = !uiState.importandoImagem) {
                    Text(if (uiState.importandoImagem) "Importando..." else "Importar da galeria")
                }
                if (uiState.importandoImagem) CircularProgressIndicator()
            } else {
                Row {
                    Text("Mostrar imagem", modifier = Modifier.weight(1f).padding(top = 12.dp))
                    Switch(checked = uiState.mostrarImagemFundo, onCheckedChange = { onAlternarVisibilidade() })
                }
                Text("Opacidade: ${((uiState.planta?.imagemFundoOpacidade ?: 0.5f) * 100).toInt()}%")
                Slider(
                    value = uiState.planta?.imagemFundoOpacidade ?: 0.5f,
                    onValueChange = onOpacidadeMudou,
                    valueRange = 0.1f..1f,
                )
                Button(onClick = onRecalibrar) { Text("Recalibrar com essa foto") }
                Button(onClick = onImportar, enabled = !uiState.importandoImagem) { Text("Trocar foto") }
            }
        }
    }
}

/** SPEC_PLANTA_BAIXA_ADENDO_IMPORTACAO.md §5 — pré-visualização do resultado de qualquer importador (DXF/SVG) antes de confirmar. */
@Composable
private fun ImportacaoArquivoDialog(
    nomeArquivo: String,
    previa: PreviaImportacao,
    camadasSelecionadas: Set<String>,
    onAlternarCamada: (String) -> Unit,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(nomeArquivo) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${previa.paredes.size} parede(s) e ${previa.comodos.size} cômodo(s) detectados")
                Text(
                    if (previa.unidadeDetectadaTexto == null) {
                        "Escala não detectada no arquivo — você vai calibrar manualmente depois de importar."
                    } else {
                        "Escala detectada automaticamente (unidade: ${previa.unidadeDetectadaTexto})."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (previa.elementosIgnorados > 0) {
                    Text(
                        "${previa.elementosIgnorados} elemento(s) ignorado(s) (círculos, camadas excluídas ou tipos não suportados).",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (previa.camadasEncontradas.isNotEmpty()) {
                    Text("Camadas encontradas — toque para incluir/excluir:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        previa.camadasEncontradas.forEach { camada ->
                            FilterChip(
                                selected = camada in camadasSelecionadas,
                                onClick = { onAlternarCamada(camada) },
                                label = { Text(camada) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmar) { Text("Importar para a Planta") }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        },
    )
}

@Composable
private fun FerramentaIcone(
    ferramenta: FerramentaDesenho,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    ferramentaAtual: FerramentaDesenho,
    onSelecionar: (FerramentaDesenho) -> Unit,
) {
    IconToggleButton(checked = ferramentaAtual == ferramenta, onCheckedChange = { onSelecionar(ferramenta) }) {
        Icon(icone, contentDescription = ferramenta.name)
    }
}

@Composable
private fun PainelComodoSelecionado(
    comodo: Comodo,
    onRenomear: (String) -> Unit,
    onExcluir: () -> Unit,
) {
    var nome by remember(comodo.id) { mutableStateOf(comodo.nome) }
    Card(Modifier.fillMaxWidth().padding(8.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it; onRenomear(it) },
                label = { Text("Nome do cômodo") },
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onExcluir) { Icon(Icons.Filled.Delete, contentDescription = "Excluir cômodo") }
        }
        Text(
            "Área: ${DecimalFormatter.formatar(comodo.areaM2)} m² · Perímetro: ${DecimalFormatter.formatar(comodo.perimetroM)} m",
            modifier = Modifier.padding(start = 12.dp, bottom = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EscalaDialog(onConfirmar: (Double) -> Unit, onDispensar: () -> Unit) {
    var texto by remember { mutableStateOf("0.5") }
    Card(Modifier.padding(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Escala do desenho", style = MaterialTheme.typography.titleMedium)
            Text("Sem calibrar por foto, defina: 1 quadrado da grade equivale a quantos metros?")
            OutlinedTextField(texto, { texto = it }, label = { Text("Metros por quadrado") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { texto.toDoubleOrNull()?.let(onConfirmar) }) { Text("Confirmar") }
            }
        }
    }
}

private fun centroidePoligono(pontos: List<PontoXY>): PontoXY {
    val x = pontos.sumOf { it.x } / pontos.size
    val y = pontos.sumOf { it.y } / pontos.size
    return PontoXY(x, y)
}
