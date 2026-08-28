package br.com.tiago.obramaster.ui.features.calculadoras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.calc.ArithmeticEvaluator
import br.com.tiago.obramaster.core.calc.CientificaEngine

private enum class OperacaoBinaria { POTENCIA, RAIZ_N }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CientificaScreen(onVoltar: () -> Unit) {
    var display by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf(false) }
    var memoria by remember { mutableStateOf(0.0) }
    var historico by remember { mutableStateOf(listOf<String>()) }
    var baseAcumulada by remember { mutableStateOf<Double?>(null) }
    var operacaoPendente by remember { mutableStateOf<OperacaoBinaria?>(null) }
    var prefixoHistorico by remember { mutableStateOf("") }

    fun avaliarDisplay(): Double? = ArithmeticEvaluator.avaliar(display)

    fun registrarHistorico(expressao: String, resultado: Double) {
        historico = (listOf("$expressao = ${formatarNumero(resultado)}") + historico).take(20)
    }

    fun aplicarResultado(resultado: Double?) {
        if (resultado == null) {
            erro = true
        } else {
            display = formatarNumero(resultado)
            erro = false
        }
    }

    fun aplicarFuncaoUnaria(nome: String, funcao: (Double) -> Double?) {
        val valor = avaliarDisplay() ?: run { erro = true; return }
        val resultado = funcao(valor)
        if (resultado == null) {
            erro = true
        } else {
            registrarHistorico("$nome(${formatarNumero(valor)})", resultado)
            display = formatarNumero(resultado)
        }
    }

    fun iniciarOperacaoBinaria(operacao: OperacaoBinaria, simbolo: String) {
        val valor = avaliarDisplay() ?: run { erro = true; return }
        baseAcumulada = valor
        operacaoPendente = operacao
        prefixoHistorico = "${formatarNumero(valor)} $simbolo "
        display = ""
        erro = false
    }

    fun pressionarIgual() {
        val base = baseAcumulada
        val operacao = operacaoPendente
        if (base != null && operacao != null) {
            val segundoOperando = avaliarDisplay() ?: run { erro = true; return }
            val resultado = when (operacao) {
                OperacaoBinaria.POTENCIA -> CientificaEngine.potencia(base, segundoOperando)
                OperacaoBinaria.RAIZ_N -> CientificaEngine.raizN(base, segundoOperando)
            }
            if (resultado == null) {
                erro = true
            } else {
                registrarHistorico("$prefixoHistorico${formatarNumero(segundoOperando)}", resultado)
                display = formatarNumero(resultado)
            }
            baseAcumulada = null
            operacaoPendente = null
        } else {
            val resultado = avaliarDisplay()
            if (resultado == null) {
                erro = true
            } else {
                registrarHistorico(display, resultado)
                display = formatarNumero(resultado)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Científica") },
                navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                text = if (erro) "Erro" else display.ifBlank { "0" },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )

            LinhaMemoria(
                onMC = { memoria = 0.0 },
                onMR = { display = formatarNumero(memoria); erro = false },
                onMMais = { avaliarDisplay()?.let { memoria += it } },
                onMMenos = { avaliarDisplay()?.let { memoria -= it } },
            )

            LinhaFuncoes(
                onRaizQuadrada = { aplicarFuncaoUnaria("√", CientificaEngine::raizQuadrada) },
                onQuadrado = { aplicarFuncaoUnaria("²", { x -> CientificaEngine.potencia(x, 2.0) }) },
                onPotencia = { iniciarOperacaoBinaria(OperacaoBinaria.POTENCIA, "^") },
                onRaizN = { iniciarOperacaoBinaria(OperacaoBinaria.RAIZ_N, "ˣ√") },
                onLog = { aplicarFuncaoUnaria("log", CientificaEngine::log10) },
                onLn = { aplicarFuncaoUnaria("ln", CientificaEngine::ln) },
                onExp = { aplicarFuncaoUnaria("exp", CientificaEngine::exp) },
                onFatorial = { aplicarFuncaoUnaria("!", { x -> CientificaEngine.fatorial(x.toInt()) }) },
            )

            val botoes = listOf(
                "7", "8", "9", "÷",
                "4", "5", "6", "×",
                "1", "2", "3", "-",
                "0", ".", "%", "+",
                "(", ")", "C", "⌫",
            )
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxWidth()) {
                items(botoes) { simbolo ->
                    OutlinedButton(
                        onClick = {
                            when (simbolo) {
                                "C" -> { display = ""; erro = false; baseAcumulada = null; operacaoPendente = null }
                                "⌫" -> display = display.dropLast(1)
                                else -> { display += simbolo; erro = false }
                            }
                        },
                        modifier = Modifier.padding(4.dp).aspectRatio(1.5f),
                    ) { Text(simbolo) }
                }
            }

            Button(onClick = { pressionarIgual() }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) { Text("=") }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text("Histórico", style = MaterialTheme.typography.labelLarge)
            LazyColumn(Modifier.fillMaxWidth()) {
                items(historico) { linha -> Text(linha, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun LinhaMemoria(onMC: () -> Unit, onMR: () -> Unit, onMMais: () -> Unit, onMMenos: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        OutlinedButton(onClick = onMC, modifier = Modifier.weight(1f)) { Text("MC") }
        OutlinedButton(onClick = onMR, modifier = Modifier.weight(1f)) { Text("MR") }
        OutlinedButton(onClick = onMMais, modifier = Modifier.weight(1f)) { Text("M+") }
        OutlinedButton(onClick = onMMenos, modifier = Modifier.weight(1f)) { Text("M−") }
    }
}

@Composable
private fun LinhaFuncoes(
    onRaizQuadrada: () -> Unit,
    onQuadrado: () -> Unit,
    onPotencia: () -> Unit,
    onRaizN: () -> Unit,
    onLog: () -> Unit,
    onLn: () -> Unit,
    onExp: () -> Unit,
    onFatorial: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onRaizQuadrada, modifier = Modifier.weight(1f)) { Text("√") }
            OutlinedButton(onClick = onQuadrado, modifier = Modifier.weight(1f)) { Text("x²") }
            OutlinedButton(onClick = onPotencia, modifier = Modifier.weight(1f)) { Text("xʸ") }
            OutlinedButton(onClick = onRaizN, modifier = Modifier.weight(1f)) { Text("ˣ√y") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            OutlinedButton(onClick = onLog, modifier = Modifier.weight(1f)) { Text("log") }
            OutlinedButton(onClick = onLn, modifier = Modifier.weight(1f)) { Text("ln") }
            OutlinedButton(onClick = onExp, modifier = Modifier.weight(1f)) { Text("exp") }
            OutlinedButton(onClick = onFatorial, modifier = Modifier.weight(1f)) { Text("n!") }
        }
    }
}

private fun formatarNumero(valor: Double): String {
    val arredondado = kotlin.math.round(valor * 1_000_000) / 1_000_000
    return if (arredondado == arredondado.toLong().toDouble()) arredondado.toLong().toString() else arredondado.toString()
}
