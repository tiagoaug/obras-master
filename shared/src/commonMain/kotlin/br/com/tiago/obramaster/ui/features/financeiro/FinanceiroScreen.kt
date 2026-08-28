package br.com.tiago.obramaster.ui.features.financeiro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private sealed interface DestinoFinanceiro {
    data object Dashboard : DestinoFinanceiro
    data object Lancamentos : DestinoFinanceiro
    data object Categorias : DestinoFinanceiro
    data object CentrosDeCusto : DestinoFinanceiro
}

/** Ponto de entrada do módulo Financeiro — Dashboard é a "home", com atalhos pros cadastros. */
@Composable
fun FinanceiroScreen(onVoltar: () -> Unit) {
    var destino by remember { mutableStateOf<DestinoFinanceiro>(DestinoFinanceiro.Dashboard) }

    when (destino) {
        DestinoFinanceiro.Dashboard -> FinanceiroDashboardScreen(
            onVoltar = onVoltar,
            onAbrirLancamentos = { destino = DestinoFinanceiro.Lancamentos },
            onAbrirCategorias = { destino = DestinoFinanceiro.Categorias },
            onAbrirCentrosDeCusto = { destino = DestinoFinanceiro.CentrosDeCusto },
        )

        DestinoFinanceiro.Lancamentos -> LancamentosScreen(onVoltar = { destino = DestinoFinanceiro.Dashboard })
        DestinoFinanceiro.Categorias -> CategoriasFinanceirasScreen(onVoltar = { destino = DestinoFinanceiro.Dashboard })
        DestinoFinanceiro.CentrosDeCusto -> CentrosDeCustoScreen(onVoltar = { destino = DestinoFinanceiro.Dashboard })
    }
}
