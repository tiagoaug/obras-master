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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.calc.VolumeEngine

private enum class Solido(val rotulo: String) {
    PARALELEPIPEDO("Paralelepípedo"), CILINDRO("Cilindro"), ESFERA("Esfera"),
    CONE("Cone"), PRISMA("Prisma"), TRONCO_PIRAMIDE("Tronco de pirâmide"),
}

private fun numero(texto: String) = texto.replace(',', '.').toDoubleOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumesScreen(onVoltar: () -> Unit) {
    var solido by remember { mutableStateOf(Solido.PARALELEPIPEDO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volumes") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Solido.entries.forEach { opcao ->
                    FilterChip(selected = solido == opcao, onClick = { solido = opcao }, label = { Text(opcao.rotulo) })
                }
            }
            HorizontalDivider()
            when (solido) {
                Solido.PARALELEPIPEDO -> FormularioParalelepipedo()
                Solido.CILINDRO -> FormularioCilindro()
                Solido.ESFERA -> FormularioEsfera()
                Solido.CONE -> FormularioCone()
                Solido.PRISMA -> FormularioPrisma()
                Solido.TRONCO_PIRAMIDE -> FormularioTroncoDePiramide()
            }
        }
    }
}

@Composable
private fun ResultadoVolume(volume: Double?, tocado: Boolean) {
    if (volume != null) {
        Text("Volume: ${formatarNumero(volume)} m³", style = MaterialTheme.typography.titleMedium)
    } else if (tocado) {
        Text("Preencha os campos e toque em Calcular.", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun FormularioParalelepipedo() {
    var comprimento by remember { mutableStateOf("") }
    var largura by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(comprimento, { comprimento = it }, label = { Text("Comprimento (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(largura, { largura = it }, label = { Text("Largura (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(altura, { altura = it }, label = { Text("Altura (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val c = numero(comprimento); val l = numero(largura); val a = numero(altura)
                volume = if (c != null && l != null && a != null) VolumeEngine.paralelepipedo(c, l, a) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        ResultadoVolume(volume, tocado)
    }
}

@Composable
private fun FormularioCilindro() {
    var raio by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(raio, { raio = it }, label = { Text("Raio (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(altura, { altura = it }, label = { Text("Altura (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val r = numero(raio); val a = numero(altura)
                volume = if (r != null && a != null) VolumeEngine.cilindro(r, a) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        ResultadoVolume(volume, tocado)
    }
}

@Composable
private fun FormularioEsfera() {
    var raio by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(raio, { raio = it }, label = { Text("Raio (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                volume = numero(raio)?.let { VolumeEngine.esfera(it) }
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        ResultadoVolume(volume, tocado)
    }
}

@Composable
private fun FormularioCone() {
    var raio by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(raio, { raio = it }, label = { Text("Raio (m)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(altura, { altura = it }, label = { Text("Altura (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val r = numero(raio); val a = numero(altura)
                volume = if (r != null && a != null) VolumeEngine.cone(r, a) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        ResultadoVolume(volume, tocado)
    }
}

@Composable
private fun FormularioPrisma() {
    var areaBase by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Use a calculadora de Áreas/Perímetros pra obter a área da base do prisma.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(areaBase, { areaBase = it }, label = { Text("Área da base (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(altura, { altura = it }, label = { Text("Altura (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val ab = numero(areaBase); val a = numero(altura)
                volume = if (ab != null && a != null) VolumeEngine.prisma(ab, a) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        ResultadoVolume(volume, tocado)
    }
}

@Composable
private fun FormularioTroncoDePiramide() {
    var areaBaseMaior by remember { mutableStateOf("") }
    var areaBaseMenor by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var volume by remember { mutableStateOf<Double?>(null) }
    var tocado by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(areaBaseMaior, { areaBaseMaior = it }, label = { Text("Área da base maior (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(areaBaseMenor, { areaBaseMenor = it }, label = { Text("Área da base menor (m²)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(altura, { altura = it }, label = { Text("Altura entre as bases (m)") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                val abm = numero(areaBaseMaior); val abn = numero(areaBaseMenor); val a = numero(altura)
                volume = if (abm != null && abn != null && a != null) VolumeEngine.troncoDePiramide(abm, abn, a) else null
                tocado = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Calcular") }
        ResultadoVolume(volume, tocado)
    }
}

private fun formatarNumero(valor: Double): String {
    val arredondado = kotlin.math.round(valor * 10_000) / 10_000
    return if (arredondado == arredondado.toLong().toDouble()) arredondado.toLong().toString() else arredondado.toString()
}
