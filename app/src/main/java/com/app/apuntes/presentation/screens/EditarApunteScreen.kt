package com.app.apuntes.presentation.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.apuntes.domain.model.Materia
import com.app.apuntes.presentation.navigation.Apuntes
import com.app.apuntes.presentation.viewmodel.GuardadoApunteUiState
import com.app.apuntes.presentation.viewmodel.MateriasUiState
import com.app.apuntes.presentation.viewmodel.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarApunteScreen(navController: NavController) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: ScannerViewModel = viewModel(viewModelStoreOwner = activity)
    val materiasUiState by viewModel.materiasUiState.collectAsState()
    val formState by viewModel.editorFormState.collectAsState()
    val guardadoUiState by viewModel.guardadoUiState.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(guardadoUiState) {
        val state = guardadoUiState
        if (state is GuardadoApunteUiState.Success) {
            val materiaId = state.materiaId
            viewModel.reiniciarFlujo()
            navController.navigate(Apuntes(materiaId))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Apunte Escaneado") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            when (val state = materiasUiState) {
                is MateriasUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is MateriasUiState.Error -> {
                    Text(
                        text = state.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is MateriasUiState.Success -> {
                    if (state.materias.isEmpty()) {
                        Text(
                            text = "Primero debes crear una materia para guardar el apunte escaneado.",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        MateriaSelector(
                            materias = state.materias,
                            selectedMateriaId = formState.materiaId,
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            onMateriaSelected = {
                                viewModel.actualizarMateria(it)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = formState.titulo,
                onValueChange = viewModel::actualizarTitulo,
                label = { Text("Título del apunte *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.contenido,
                onValueChange = viewModel::actualizarContenido,
                label = { Text("Contenido reconocido *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                maxLines = 12
            )

            if (guardadoUiState is GuardadoApunteUiState.Error) {
                Text(
                    text = (guardadoUiState as GuardadoApunteUiState.Error).mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { viewModel.guardarApunteEscaneado() },
                modifier = Modifier.fillMaxWidth(),
                enabled = guardadoUiState !is GuardadoApunteUiState.Guardando &&
                    materiasUiState is MateriasUiState.Success &&
                    (materiasUiState as MateriasUiState.Success).materias.isNotEmpty()
            ) {
                if (guardadoUiState is GuardadoApunteUiState.Guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar apunte")
                }
            }
        }
    }
}

@Composable
private fun MateriaSelector(
    materias: List<Materia>,
    selectedMateriaId: Long?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMateriaSelected: (Long) -> Unit
) {
    val materiaSeleccionada = materias.firstOrNull { it.id == selectedMateriaId }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = materiaSeleccionada?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Materia *") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) },
            trailingIcon = {
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            materias.forEach { materia ->
                DropdownMenuItem(
                    text = { Text(materia.nombre) },
                    onClick = { onMateriaSelected(materia.id) }
                )
            }
        }
    }
}
