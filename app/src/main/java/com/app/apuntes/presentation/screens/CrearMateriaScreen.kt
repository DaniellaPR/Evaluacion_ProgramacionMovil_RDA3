package com.app.apuntes.presentation.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.apuntes.presentation.viewmodel.CrearMateriaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMateriaScreen(navController: NavController) {

    // ViewModel con SavedStateHandle — los campos sobreviven a rotaciones
    val viewModel: CrearMateriaViewModel = viewModel()

    // Estado local de UI vinculado al ViewModel
    var nombre by rememberSaveable { mutableStateOf(viewModel.nombre) }
    var docente by rememberSaveable { mutableStateOf(viewModel.docente) }
    var descripcion by rememberSaveable { mutableStateOf(viewModel.descripcion) }

    val guardadoExitoso by viewModel.guardadoExitoso.collectAsState()
    val error by viewModel.error.collectAsState()

    // Navegar atrás automáticamente al guardar
    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Materia") },
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

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    viewModel.nombre = it
                    viewModel.limpiarError()
                },
                label = { Text("Nombre de la materia *") },
                modifier = Modifier.fillMaxWidth(),
                isError = error != null && nombre.isBlank(),
                singleLine = true
            )

            OutlinedTextField(
                value = docente,
                onValueChange = {
                    docente = it
                    viewModel.docente = it
                },
                label = { Text("Docente (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                    viewModel.descripcion = it
                },
                label = { Text("Descripción o notas adicionales (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )

            // Mensaje de error de validación
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.guardar() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Materia")
            }
        }
    }
}