package br.com.tiago.obramaster.ui.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.tiago.obramaster.core.modules.AppModule
import br.com.tiago.obramaster.domain.Colaborador
import br.com.tiago.obramaster.ui.components.ScreenSize
import br.com.tiago.obramaster.ui.components.WithScreenSize
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    colaborador: Colaborador,
    onAbrirConfiguracoes: () -> Unit,
    onLogout: () -> Unit,
) {
    val viewModel: HomeViewModel = koinInject { parametersOf(colaborador) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onModuloClicado: (AppModule) -> Unit = { modulo ->
        scope.launch { snackbarHostState.showSnackbar("${modulo.labelPtBr}: chega numa próxima fase") }
    }
    val onLogoutClicado: () -> Unit = {
        viewModel.logout()
        onLogout()
    }

    WithScreenSize { screenSize ->
        when (screenSize) {
            ScreenSize.EXPANDED -> HomeExpanded(uiState, screenSize, onModuloClicado, onAbrirConfiguracoes, onLogoutClicado)
            ScreenSize.MEDIUM -> HomeMedium(uiState, screenSize, onModuloClicado, onAbrirConfiguracoes, onLogoutClicado)
            ScreenSize.COMPACT -> HomeCompact(uiState, screenSize, onModuloClicado, onAbrirConfiguracoes, onLogoutClicado)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeCompact(
    uiState: HomeUiState,
    screenSize: ScreenSize,
    onModuloClicado: (AppModule) -> Unit,
    onAbrirConfiguracoes: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Olá, ${uiState.colaborador.nome}") },
                actions = {
                    IconButton(onClick = onAbrirConfiguracoes) {
                        Icon(Icons.Filled.Settings, contentDescription = "Configurações")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair")
                    }
                },
            )
        },
    ) { padding ->
        ModuloGrid(uiState.modulosVisiveis, screenSize, onModuloClicado, padding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeMedium(
    uiState: HomeUiState,
    screenSize: ScreenSize,
    onModuloClicado: (AppModule) -> Unit,
    onAbrirConfiguracoes: () -> Unit,
    onLogout: () -> Unit,
) {
    Row(Modifier.fillMaxSize()) {
        NavigationRail {
            NavigationRailItem(
                selected = false,
                onClick = onAbrirConfiguracoes,
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Configurações") },
                label = { Text("Config.") },
            )
            NavigationRailItem(
                selected = false,
                onClick = onLogout,
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair") },
                label = { Text("Sair") },
            )
        }
        Scaffold(
            topBar = { TopAppBar(title = { Text("Olá, ${uiState.colaborador.nome}") }) },
        ) { padding ->
            ModuloGrid(uiState.modulosVisiveis, screenSize, onModuloClicado, padding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeExpanded(
    uiState: HomeUiState,
    screenSize: ScreenSize,
    onModuloClicado: (AppModule) -> Unit,
    onAbrirConfiguracoes: () -> Unit,
    onLogout: () -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(modifier = Modifier.padding(12.dp)) {
                Text("ObraMaster", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                uiState.modulosVisiveis.forEach { modulo ->
                    NavigationRailItem(
                        selected = false,
                        onClick = { onModuloClicado(modulo) },
                        icon = {},
                        label = { Text(modulo.labelPtBr) },
                    )
                }
                NavigationRailItem(
                    selected = false,
                    onClick = onAbrirConfiguracoes,
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Configurações") },
                )
                NavigationRailItem(
                    selected = false,
                    onClick = onLogout,
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    label = { Text("Sair") },
                )
            }
        },
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Olá, ${uiState.colaborador.nome}") }) },
        ) { padding ->
            ModuloGrid(uiState.modulosVisiveis, screenSize, onModuloClicado, padding)
        }
    }
}

@Composable
private fun ModuloGrid(
    modulos: List<AppModule>,
    screenSize: ScreenSize,
    onModuloClicado: (AppModule) -> Unit,
    padding: PaddingValues,
) {
    val colunas = when (screenSize) {
        ScreenSize.COMPACT -> 2
        ScreenSize.MEDIUM -> 3
        ScreenSize.EXPANDED -> 5
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(colunas),
        modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
    ) {
        items(modulos) { modulo ->
            Card(
                onClick = { onModuloClicado(modulo) },
                modifier = Modifier.padding(8.dp).fillMaxSize(),
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text(modulo.labelPtBr, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
