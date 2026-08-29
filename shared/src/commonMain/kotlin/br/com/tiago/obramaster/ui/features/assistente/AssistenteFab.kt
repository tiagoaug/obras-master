package br.com.tiago.obramaster.ui.features.assistente

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** SPEC_ASSISTENTE_IA.md §5 — "presente em todas as telas, definido uma vez no Scaffold raiz".
 * Renderizado uma única vez em App.kt, sobreposto ao conteúdo — não precisa ser adicionado tela
 * por tela. Fica no canto oposto ao FAB de "adicionar" que várias telas já têm (bottom-end), pra
 * não colidir visualmente com eles. */
@Composable
fun AssistenteFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        Icon(Icons.Filled.QuestionMark, contentDescription = "Assistente de ajuda")
    }
}
