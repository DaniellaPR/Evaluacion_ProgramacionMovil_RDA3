package com.app.apuntes.presentation.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.app.apuntes.presentation.viewmodel.QRViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoQRScreen(navController: NavController) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: QRViewModel = viewModel(viewModelStoreOwner = activity)

    val textoEscaneado by viewModel.textoEscaneado.collectAsState()
    val materiasUiState by viewModel.materiasUiState.collectAsState()
    val guardarFormState by viewModel.guardarFormState.collectAsState()
    val guardadoState by viewModel.guardadoState.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(guardadoState) {
        val state = guardadoState
        if (state is GuardadoApunteUiState.Success) {
            viewModel.reiniciarFlujoEscaneo()
            navController.navigate(Apuntes(state.materiaId)) {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apunte recibido por QR") },
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
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Texto recibido",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Revisa el texto y luego guárdalo como apunte en la materia que desees.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                SelectionContainer {
                    Text(
                        text = textoEscaneado.ifBlank { "No se recibió texto del código QR." },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = "Guardar como apunte",
                style = MaterialTheme.typography.titleMedium
            )

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
                            text = "Primero debes crear una materia para poder guardar el apunte.",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        MateriaSelectorQR(
                            materias = state.materias,
                            selectedMateriaId = guardarFormState.materiaId,
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            onMateriaSelected = {
                                viewModel.actualizarMateriaGuardar(it)
                                expanded = false
                            }
                        )

                        OutlinedTextField(
                            value = guardarFormState.titulo,
                            onValueChange = viewModel::actualizarTituloGuardar,
                            label = { Text("Título del apunte *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (guardadoState is GuardadoApunteUiState.Error) {
                            Text(
                                text = (guardadoState as GuardadoApunteUiState.Error).mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { viewModel.guardarApunteDesdeQR() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = guardadoState !is GuardadoApunteUiState.Guardando &&
                                    textoEscaneado.isNotBlank()
                        ) {
                            if (guardadoState is GuardadoApunteUiState.Guardando) {
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

            OutlinedButton(
                onClick = {
                    viewModel.reiniciarFlujoEscaneo()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Escanear otro QR")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MateriaSelectorQR(
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
