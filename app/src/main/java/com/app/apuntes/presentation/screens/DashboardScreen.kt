package com.app.apuntes.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.apuntes.domain.model.Materia
import com.app.apuntes.domain.model.Recurso
import com.app.apuntes.presentation.navigation.Apuntes
import com.app.apuntes.presentation.navigation.CrearMateria
import com.app.apuntes.presentation.viewmodel.DashboardViewModel
import com.app.apuntes.presentation.viewmodel.MateriasUiState
import com.app.apuntes.presentation.viewmodel.RecursosUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val recursosState by viewModel.recursosState.collectAsState()

    var recursoSeleccionado by remember { mutableStateOf<Recurso?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Materias") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(CrearMateria) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar materia")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección 1: Listado de Materias
                item {
                    Text(
                        text = "Mis Materias",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                when (val state = uiState) {
                    is MateriasUiState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                    is MateriasUiState.Success -> {
                        if (state.materias.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("No hay materias registradas", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Toca + para agregar tu primera materia",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        } else {
                            items(state.materias) { materia ->
                                MateriaCard(
                                    materia = materia,
                                    onClick = { navController.navigate(Apuntes(materiaId = materia.id)) }
                                )
                            }
                        }
                    }
                    is MateriasUiState.Error -> {
                        item {
                            Text(
                                text = state.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Sección 2: Recursos Académicos (Retrofit)
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recursos Recomendados",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    when (val recState = recursosState) {
                        is RecursosUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        is RecursosUiState.Success -> {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(recState.recursos) { recurso ->
                                    RecursoCard(recurso = recurso) {
                                        recursoSeleccionado = recurso
                                    }
                                }
                            }
                        }
                        is RecursosUiState.Error -> {
                            Text(
                                text = recState.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal de Detalle de Recurso
    recursoSeleccionado?.let { recurso ->
        AlertDialog(
            onDismissRequest = { recursoSeleccionado = null },
            title = {
                Text(
                    text = recurso.titulo.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = recurso.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { recursoSeleccionado = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun MateriaCard(materia: Materia, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = materia.nombre, style = MaterialTheme.typography.titleMedium)
            materia.docente?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Docente: $it", style = MaterialTheme.typography.bodySmall)
            }
            materia.horario?.let {
                Text(text = "Horario: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun RecursoCard(recurso: Recurso, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp)
            .clickable { onClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = recurso.titulo.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Consejo de Estudio",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}