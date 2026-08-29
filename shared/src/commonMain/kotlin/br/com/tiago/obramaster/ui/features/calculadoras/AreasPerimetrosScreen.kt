package br.com.tiago.obramaster.ui.features.calculadoras

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.calc.GeometriaEngine

private enum class Figura(val rotulo: String) {
    QUADRADO("Quadrado"), RETANGULO("Retângulo"), TRIANGULO("Triângulo"),
    TRAPEZIO("Trapézio"), CIRCULO("Círculo"), POLIGONO_REGULAR("Polígono regular"), IRREGULAR("Irregular"),
}

private fun numero(texto: String) = texto.replace(',', '.').toDoubleOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreasPerimetrosScreen(onVoltar: () -> Unit) {
    var figura by remember { mutableStateOf(Figura.QUADRADO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Áreas e Perímetros") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Figura.entries.forEach { opcao ->
                    FilterChip(selected = figura == opcao, onClick = { figura = opcao }, label = { Text(opcao.rotulo) })
                }
            }
            HorizontalDivider()
            when (figura) {
                Figura.QUADRADO -> FormularioQuadrado()
                Figura.RETANGULO -> FormularioRetangulo()
                Figura.TRIANGULO -> FormularioTriangulo()
                Figura.TRAPEZIO -> FormularioTrapezio()
                Figura.CIRCULO -> FormularioCirculo()
                Figura.POLIGONO_REGULAR -> FormularioPoligonoRegular()
                Figura.IRREGULAR -> FormularioIrregular()
            }
        }
    }
}

@Composable
private fun Resultados(area: Double?, perimetro: Double?) {
    if (area != null) Text("Área: ${formatarNumero(area)} m²", style = MaterialTheme.typography.titleMedium)
    if (perimetro != null) Text("Perímetro: ${formatarNumero(perimetro)} m", style = MaterialTheme.typography.titleMedium)
    if (area == null && perimetro == null) {
        Text("Preencha os campos e toque em Calcular.", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun FormularioQuadrado() {
    var lado by remember { mutableStateOf("") }
    var area by remember { mutableStateOf<Double?>(null) }
    var perimetro by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(lado, { lado = it }, label = { Text("Lado (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val l = numero(lado)
                area = l?.let { GeometriaEngine.areaQuadrado(it) }
                perimetro = l?.let { GeometriaEngine.perimetroQuadrado(it) }
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        if (tocado) Resultados(area, perimetro)
    }
}

@Composable
private fun FormularioRetangulo() {
    var largura by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var area by remember { mutableStateOf<Double?>(null) }
    var perimetro by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(largura, { largura = it }, label = { Text("Largura (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(altura, { altura = it }, label = { Text("Altura (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val la = numero(largura)
                val al = numero(altura)
                area = if (la != null && al != null) GeometriaEngine.areaRetangulo(la, al) else null
                perimetro = if (la != null && al != null) GeometriaEngine.perimetroRetangulo(la, al) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        if (tocado) Resultados(area, perimetro)
    }
}

@Composable
private fun FormularioTriangulo() {
    var base by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var ladoA by remember { mutableStateOf("") }
    var ladoB by remember { mutableStateOf("") }
    var ladoC by remember { mutableStateOf("") }
    var area by remember { mutableStateOf<Double?>(null) }
    var perimetro by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Área (base × altura)", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(base, { base = it }, label = { Text("Base (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(altura, { altura = it }, label = { Text("Altura (m)") }, modifier = Modifier.fillMaxWidth())

        Text("Perímetro (3 lados)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        OutlinedTextField(ladoA, { ladoA = it }, label = { Text("Lado a (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(ladoB, { ladoB = it }, label = { Text("Lado b (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(ladoC, { ladoC = it }, label = { Text("Lado c (m)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val b = numero(base)
                val h = numero(altura)
                val a1 = numero(ladoA)
                val a2 = numero(ladoB)
                val a3 = numero(ladoC)
                area = if (b != null && h != null) GeometriaEngine.areaTriangulo(b, h) else null
                perimetro = if (a1 != null && a2 != null && a3 != null) GeometriaEngine.perimetroTriangulo(a1, a2, a3) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        if (tocado) Resultados(area, perimetro)
    }
}

@Composable
private fun FormularioTrapezio() {
    var baseMaior by remember { mutableStateOf("") }
    var baseMenor by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var ladoA by remember { mutableStateOf("") }
    var ladoB by remember { mutableStateOf("") }
    var area by remember { mutableStateOf<Double?>(null) }
    var perimetro by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(baseMaior, { baseMaior = it }, label = { Text("Base maior (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(baseMenor, { baseMenor = it }, label = { Text("Base menor (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(altura, { altura = it }, label = { Text("Altura (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(ladoA, { ladoA = it }, label = { Text("Lado oblíquo a (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(ladoB, { ladoB = it }, label = { Text("Lado oblíquo b (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val bm = numero(baseMaior)
                val bn = numero(baseMenor)
                val h = numero(altura)
                val a1 = numero(ladoA)
                val a2 = numero(ladoB)
                area = if (bm != null && bn != null && h != null) GeometriaEngine.areaTrapezio(bm, bn, h) else null
                perimetro = if (bm != null && bn != null && a1 != null && a2 != null) GeometriaEngine.perimetroTrapezio(bm, bn, a1, a2) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        if (tocado) Resultados(area, perimetro)
    }
}

@Composable
private fun FormularioCirculo() {
    var raio by remember { mutableStateOf("") }
    var area by remember { mutableStateOf<Double?>(null) }
    var perimetro by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(raio, { raio = it }, label = { Text("Raio (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val r = numero(raio)
                area = r?.let { GeometriaEngine.areaCirculo(it) }
                perimetro = r?.let { GeometriaEngine.perimetroCirculo(it) }
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        if (tocado) Resultados(area, perimetro)
    }
}

@Composable
private fun FormularioPoligonoRegular() {
    var numeroLados by remember { mutableStateOf("") }
    var lado by remember { mutableStateOf("") }
    var area by remember { mutableStateOf<Double?>(null) }
    var perimetro by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(numeroLados, { numeroLados = it }, label = { Text("Número de lados") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(lado, { lado = it }, label = { Text("Medida do lado (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val n = numeroLados.toIntOrNull()
                val l = numero(lado)
                area = if (n != null && l != null) GeometriaEngine.areaPoligonoRegular(n, l) else null
                perimetro = if (n != null && l != null) GeometriaEngine.perimetroPoligonoRegular(n, l) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        if (tocado) Resultados(area, perimetro)
    }
}

private data class LinhaPonto(val id: Int, val x: String = "", val y: String = "")

@Composable
private fun FormularioIrregular() {
    var proximoId by remember { mutableStateOf(3) }
    var pontos by remember {
        mutableStateOf(listOf(LinhaPonto(0), LinhaPonto(1), LinhaPonto(2)))
    }
    var area by remember { mutableStateOf<Double?>(null) }
    var perimetro by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Coordenadas dos vértices (fórmula de Shoelace)", style = MaterialTheme.typography.labelLarge)
        pontos.forEachIndexed { indice, linha ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    linha.x,
                    { novo -> pontos = pontos.toMutableList().also { it[indice] = linha.copy(x = novo) } },
                    label = { Text("X${indice + 1}") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    linha.y,
                    { novo -> pontos = pontos.toMutableList().also { it[indice] = linha.copy(y = novo) } },
                    label = { Text("Y${indice + 1}") },
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { pontos = pontos.filterIndexed { i, _ -> i != indice } }, enabled = pontos.size > 3) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remover ponto")
                }
            }
        }
        OutlinedButton(onClick = { pontos = pontos + LinhaPonto(proximoId); proximoId += 1 }, modifier = Modifier.fillMaxWidth()) {
            Text("Adicionar vértice")
        }
        Button(
            onClick = {
                val convertidos = pontos.mapNotNull { p -> val x = numero(p.x); val y = numero(p.y); if (x != null && y != null) GeometriaEngine.Ponto(x, y) else null }
                if (convertidos.size == pontos.size) {
                    area = GeometriaEngine.areaIrregularShoelace(convertidos)
                    perimetro = GeometriaEngine.perimetroIrregular(convertidos)
                } else {
                    area = null
                    perimetro = null
                }
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        if (tocado) Resultados(area, perimetro)
    }
}

private fun formatarNumero(valor: Double): String {
    val arredondado = kotlin.math.round(valor * 10_000) / 10_000
    return if (arredondado == arredondado.toLong().toDouble()) arredondado.toLong().toString() else arredondado.toString()
}
