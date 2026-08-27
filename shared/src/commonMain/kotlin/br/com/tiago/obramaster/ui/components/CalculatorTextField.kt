package br.com.tiago.obramaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.calc.ArithmeticEvaluator
import br.com.tiago.obramaster.core.util.MoneyFormatter
import kotlin.math.roundToLong

/**
 * SPEC_OBRA_MASTER.md §5.2 — campo de valor com calculadora embutida.
 * Obrigatório em 100% dos campos monetários do app. Valor sempre em Long (centavos).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTextField(
    valueCentavos: Long,
    onValueChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var mostrarCalculadora by remember { mutableStateOf(false) }
    var textoDigitado by remember(valueCentavos) { mutableStateOf(centavosParaTexto(valueCentavos)) }

    OutlinedTextField(
        value = textoDigitado,
        onValueChange = { novoTexto ->
            textoDigitado = novoTexto
            textoParaCentavosOuNulo(novoTexto)?.let(onValueChange)
        },
        label = { Text(label) },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        trailingIcon = {
            IconButton(onClick = { mostrarCalculadora = true }) {
                Icon(Icons.Filled.Calculate, contentDescription = "Abrir calculadora")
            }
        },
        modifier = modifier,
    )

    if (mostrarCalculadora) {
        ModalBottomSheet(
            onDismissRequest = { mostrarCalculadora = false },
            sheetState = rememberModalBottomSheetState(),
        ) {
            CalculadoraBottomSheetContent(
                valorInicial = valueCentavos,
                onConfirmar = { novoValorCentavos ->
                    onValueChange(novoValorCentavos)
                    textoDigitado = centavosParaTexto(novoValorCentavos)
                    mostrarCalculadora = false
                },
                onCancelar = { mostrarCalculadora = false },
            )
        }
    }
}

@Composable
private fun CalculadoraBottomSheetContent(
    valorInicial: Long,
    onConfirmar: (Long) -> Unit,
    onCancelar: () -> Unit,
) {
    var expressao by remember { mutableStateOf(centavosParaTexto(valorInicial)) }
    val resultadoAtual = ArithmeticEvaluator.avaliar(expressao)

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = resultadoAtual?.let { MoneyFormatter.formatar((it * 100).roundToLong()) } ?: "—",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = expressao,
            onValueChange = { expressao = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        val botoes = listOf(
            "7", "8", "9", "÷",
            "4", "5", "6", "×",
            "1", "2", "3", "-",
            "0", ".", "%", "+",
            "(", ")", "C", "⌫",
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            items(botoes) { simbolo ->
                OutlinedButton(
                    onClick = {
                        expressao = when (simbolo) {
                            "C" -> ""
                            "⌫" -> expressao.dropLast(1)
                            else -> expressao + simbolo
                        }
                    },
                    modifier = Modifier.padding(4.dp).aspectRatio(1.5f),
                ) {
                    Text(simbolo)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            Button(
                onClick = { resultadoAtual?.let { onConfirmar((it * 100).roundToLong()) } },
                enabled = resultadoAtual != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("OK") }
        }
    }
}

private fun centavosParaTexto(centavos: Long): String {
    val reais = centavos / 100
    val centavosParte = kotlin.math.abs(centavos % 100)
    return "$reais.${centavosParte.toString().padStart(2, '0')}"
}

private fun textoParaCentavosOuNulo(texto: String): Long? {
    val valor = texto.replace(',', '.').toDoubleOrNull() ?: return null
    return (valor * 100).roundToLong()
}
