package br.com.tiago.obramaster.ui.features.calculadoras

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.calc.TrigonometriaEngine
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private enum class ModoResolucao(val rotulo: String) {
    LLL("3 lados"), LAL("2 lados + ângulo"), AAL("2 ângulos + 1 lado"), PITAGORAS("Pitágoras"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrigonometriaScreen(onVoltar: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trigonométrica") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SecaoRazoesTrigonometricas()
            HorizontalDivider()
            SecaoResolverTriangulo()
        }
    }
}

@Composable
private fun SecaoRazoesTrigonometricas() {
    var anguloTexto by remember { mutableStateOf("") }
    val angulo = anguloTexto.replace(',', '.').toDoubleOrNull()

    var valorInversaTexto by remember { mutableStateOf("") }
    val valorInversa = valorInversaTexto.replace(',', '.').toDoubleOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Razões trigonométricas (ângulo em graus)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(anguloTexto, { anguloTexto = it }, label = { Text("Ângulo (°)") }, modifier = Modifier.fillMaxWidth())
        if (angulo != null) {
            Text("sen = ${formatarNumero(TrigonometriaEngine.seno(angulo))}")
            Text("cos = ${formatarNumero(TrigonometriaEngine.cosseno(angulo))}")
            Text("tan = ${TrigonometriaEngine.tangente(angulo)?.let { formatarNumero(it) } ?: "indefinida"}")
        }

        Text("Inversas (valor entre -1 e 1)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        OutlinedTextField(valorInversaTexto, { valorInversaTexto = it }, label = { Text("Valor") }, modifier = Modifier.fillMaxWidth())
        if (valorInversa != null) {
            Text("arcsen = ${TrigonometriaEngine.arcoSeno(valorInversa)?.let { "${formatarNumero(it)}°" } ?: "fora do domínio"}")
            Text("arccos = ${TrigonometriaEngine.arcoCosseno(valorInversa)?.let { "${formatarNumero(it)}°" } ?: "fora do domínio"}")
            Text("arctan = ${formatarNumero(TrigonometriaEngine.arcoTangente(valorInversa))}°")
        }
    }
}

@Composable
private fun SecaoResolverTriangulo() {
    var modo by remember { mutableStateOf(ModoResolucao.LLL) }
    var campo1 by remember { mutableStateOf("") }
    var campo2 by remember { mutableStateOf("") }
    var campo3 by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<TrigonometriaEngine.ResultadoTriangulo?>(null) }
    var erro by remember { mutableStateOf(false) }

    fun numero(texto: String) = texto.replace(',', '.').toDoubleOrNull()

    fun calcular() {
        val a = numero(campo1)
        val b = numero(campo2)
        val c = numero(campo3)
        resultado = when (modo) {
            ModoResolucao.LLL -> if (a != null && b != null && c != null) TrigonometriaEngine.resolverTrianguloLLL(a, b, c) else null
            ModoResolucao.LAL -> if (a != null && b != null && c != null) TrigonometriaEngine.resolverTrianguloLAL(a, b, c) else null
            ModoResolucao.AAL -> if (a != null && b != null && c != null) TrigonometriaEngine.resolverTrianguloAAL(a, b, c) else null
            ModoResolucao.PITAGORAS -> {
                val hip = if (a != null && b != null) TrigonometriaEngine.pitagorasHipotenusa(a, b) else null
                if (hip != null && a != null && b != null) TrigonometriaEngine.ResultadoTriangulo(a, b, hip, 0.0, 0.0, 90.0) else null
            }
        }
        erro = resultado == null
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Resolução de triângulo", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModoResolucao.entries.forEach { opcao ->
                FilterChip(
                    selected = modo == opcao,
                    onClick = { modo = opcao; resultado = null; erro = false },
                    label = { Text(opcao.rotulo) },
                )
            }
        }

        when (modo) {
            ModoResolucao.LLL -> {
                OutlinedTextField(campo1, { campo1 = it }, label = { Text("Lado a") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(campo2, { campo2 = it }, label = { Text("Lado b") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(campo3, { campo3 = it }, label = { Text("Lado c") }, modifier = Modifier.fillMaxWidth())
            }
            ModoResolucao.LAL -> {
                OutlinedTextField(campo1, { campo1 = it }, label = { Text("Lado a") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(campo2, { campo2 = it }, label = { Text("Lado b") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(campo3, { campo3 = it }, label = { Text("Ângulo C entre a e b (°)") }, modifier = Modifier.fillMaxWidth())
            }
            ModoResolucao.AAL -> {
                OutlinedTextField(campo1, { campo1 = it }, label = { Text("Lado a") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(campo2, { campo2 = it }, label = { Text("Ângulo A, oposto a a (°)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(campo3, { campo3 = it }, label = { Text("Ângulo B (°)") }, modifier = Modifier.fillMaxWidth())
            }
            ModoResolucao.PITAGORAS -> {
                OutlinedTextField(campo1, { campo1 = it }, label = { Text("Cateto a") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(campo2, { campo2 = it }, label = { Text("Cateto b") }, modifier = Modifier.fillMaxWidth())
            }
        }

        Button(onClick = { calcular() }, modifier = Modifier.fillMaxWidth()) { Text("Calcular") }

        if (erro) {
            Text("Não foi possível resolver — confira os valores (desigualdade triangular, domínio dos ângulos).", color = MaterialTheme.colorScheme.error)
        }

        resultado?.let { res ->
            Text("Lado a: ${formatarNumero(res.ladoA)}")
            Text("Lado b: ${formatarNumero(res.ladoB)}")
            Text("Lado c: ${formatarNumero(res.ladoC)}")
            if (modo != ModoResolucao.PITAGORAS) {
                Text("Ângulo A: ${formatarNumero(res.anguloA)}°")
                Text("Ângulo B: ${formatarNumero(res.anguloB)}°")
                Text("Ângulo C: ${formatarNumero(res.anguloC)}°")
            }
            TrianguloEsquematico(res)
        }
    }
}

/** Desenho esquemático (SPEC_OBRA_MASTER.md §4.12.2) — vértice A na origem, B em (c, 0), C
 * posicionado por lei dos cossenos a partir do ângulo A; escalado pra caber no Canvas. */
@Composable
private fun TrianguloEsquematico(resultado: TrigonometriaEngine.ResultadoTriangulo) {
    Canvas(Modifier.fillMaxWidth().height(180.dp).padding(top = 8.dp)) {
        val anguloARad = resultado.anguloA * PI / 180.0
        val pontoA = Offset(0f, 0f)
        val pontoB = Offset(resultado.ladoC.toFloat(), 0f)
        val pontoC = Offset((resultado.ladoB * cos(anguloARad)).toFloat(), (resultado.ladoB * sin(anguloARad)).toFloat())

        val minX = minOf(pontoA.x, pontoB.x, pontoC.x)
        val maxX = maxOf(pontoA.x, pontoB.x, pontoC.x)
        val minY = minOf(pontoA.y, pontoB.y, pontoC.y)
        val maxY = maxOf(pontoA.y, pontoB.y, pontoC.y)
        val larguraFigura = max(maxX - minX, 0.0001f)
        val alturaFigura = max(maxY - minY, 0.0001f)
        val margem = 24f
        val escala = minOf((size.width - margem * 2) / larguraFigura, (size.height - margem * 2) / alturaFigura)

        fun paraTela(ponto: Offset): Offset {
            val x = margem + (ponto.x - minX) * escala
            val yMatematico = margem + (ponto.y - minY) * escala
            return Offset(x, size.height - yMatematico)
        }

        val telaA = paraTela(pontoA)
        val telaB = paraTela(pontoB)
        val telaC = paraTela(pontoC)

        drawLine(Color(0xFF5C6BC0), telaA, telaB, strokeWidth = 4f)
        drawLine(Color(0xFF5C6BC0), telaB, telaC, strokeWidth = 4f)
        drawLine(Color(0xFF5C6BC0), telaC, telaA, strokeWidth = 4f)
        drawCircle(Color(0xFF5C6BC0), radius = 5f, center = telaA)
        drawCircle(Color(0xFF5C6BC0), radius = 5f, center = telaB)
        drawCircle(Color(0xFF5C6BC0), radius = 5f, center = telaC)
    }
}

private fun formatarNumero(valor: Double): String {
    val arredondado = kotlin.math.round(valor * 10_000) / 10_000
    return if (arredondado == arredondado.toLong().toDouble()) arredondado.toLong().toString() else arredondado.toString()
}
