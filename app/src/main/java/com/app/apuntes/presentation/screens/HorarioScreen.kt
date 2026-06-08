package com.app.apuntes.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.apuntes.domain.model.Horario
import com.app.apuntes.domain.model.Materia
import com.app.apuntes.presentation.viewmodel.HorarioUiState
import com.app.apuntes.presentation.viewmodel.HorarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioScreen(navController: NavController) {
    val viewModel: HorarioViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val materias by viewModel.materias.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var horarioParaEditar by remember { mutableStateOf<Horario?>(null) }

    val diasOrdenados = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Horario") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    horarioParaEditar = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar horario")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HorarioUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HorarioUiState.Success -> {
                    val horariosPorDia = state.horarios.groupBy { it.dia }

                    if (state.horarios.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No hay horarios registrados")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca + para registrar un horario personalizado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(diasOrdenados.filter { horariosPorDia.containsKey(it) }) { dia ->
                                HorarioDiaSection(
                                    dia = dia,
                                    horarios = horariosPorDia[dia] ?: emptyList(),
                                    materias = materias,
                                    onHorarioClick = { horario ->
                                        horarioParaEditar = horario
                                        showDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                is HorarioUiState.Error -> {
                    Text(
                        text = state.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showDialog) {
        HorarioDialog(
            materiaList = materias,
            horarioToEdit = horarioParaEditar,
            onDismiss = { showDialog = false },
            onSave = { materiaId, dia, horaInicio, horaFin, aula ->
                val nuevoHorario = Horario(
                    id = horarioParaEditar?.id ?: 0L,
                    materiaId = materiaId,
                    dia = dia,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    aula = aula
                )
                viewModel.guardarHorario(nuevoHorario)
                showDialog = false
            },
            onDelete = { id ->
                viewModel.eliminarHorario(id)
                showDialog = false
            }
        )
    }
}

@Composable
private fun HorarioDiaSection(
    dia: String,
    horarios: List<Horario>,
    materias: List<Materia>,
    onHorarioClick: (Horario) -> Unit
) {
    Column {
        Text(
            text = dia,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        horarios.forEach { horario ->
            val materia = materias.find { it.id == horario.materiaId }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHorarioClick(horario) },
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = materia?.nombre ?: "Materia desconocida",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        horario.aula?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Text(
                        text = "${horario.horaInicio} - ${horario.horaFin}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun HorarioDialog(
    materiaList: List<Materia>,
    horarioToEdit: Horario? = null,
    onDismiss: () -> Unit,
    onSave: (materiaId: Long, dia: String, horaInicio: String, horaFin: String, aula: String?) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    var materiaId by remember { mutableStateOf(horarioToEdit?.materiaId ?: materiaList.firstOrNull()?.id ?: 0L) }
    var dia by remember { mutableStateOf(horarioToEdit?.dia ?: "Lunes") }
    var horaInicio by remember { mutableStateOf(horarioToEdit?.horaInicio ?: "") }
    var horaFin by remember { mutableStateOf(horarioToEdit?.horaFin ?: "") }
    var aula by remember { mutableStateOf(horarioToEdit?.aula ?: "") }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    var showMateriaDropdown by remember { mutableStateOf(false) }
    var showDiaDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (horarioToEdit == null) "Nuevo Horario" else "Editar Horario") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (materiaList.isEmpty()) {
                    Text(
                        text = "Primero debes registrar una materia desde el inicio para poder asignarle un horario.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    val selectedMateria = materiaList.find { it.id == materiaId } ?: materiaList.first()
                    materiaId = selectedMateria.id

                    // Selector de Materia
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedMateria.nombre,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Materia") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showMateriaDropdown = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showMateriaDropdown,
                            onDismissRequest = { showMateriaDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            materiaList.forEach { mat ->
                                DropdownMenuItem(
                                    text = { Text(mat.nombre) },
                                    onClick = {
                                        materiaId = mat.id
                                        showMateriaDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Selector de Día
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = dia,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Día") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDiaDropdown = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showDiaDropdown,
                            onDismissRequest = { showDiaDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            dias.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        dia = d
                                        showDiaDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Hora Inicio
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Hora Inicio (ej: 08:00)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Hora Fin
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { horaFin = it },
                        label = { Text("Hora Fin (ej: 10:00)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Aula
                    OutlinedTextField(
                        value = aula,
                        onValueChange = { aula = it },
                        label = { Text("Aula/Taller (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    errorMsg?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (horarioToEdit != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(horarioToEdit.id) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                }
                if (materiaList.isNotEmpty()) {
                    Button(
                        onClick = {
                            if (horaInicio.isBlank() || horaFin.isBlank()) {
                                errorMsg = "La hora de inicio y fin son obligatorias"
                            } else {
                                onSave(materiaId, dia, horaInicio.trim(), horaFin.trim(), aula.trim().ifBlank { null })
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}