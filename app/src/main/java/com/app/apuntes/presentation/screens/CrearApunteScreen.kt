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
import com.app.apuntes.presentation.viewmodel.CrearApunteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearApunteScreen(materiaId: Long, navController: NavController) {

    val viewModel: CrearApunteViewModel = viewModel(
        factory = CrearApunteViewModel.provideFactory(materiaId)
    )

    var titulo by rememberSaveable { mutableStateOf(viewModel.titulo) }
    var contenido by rememberSaveable { mutableStateOf(viewModel.contenido) }

    val guardadoExitoso by viewModel.guardadoExitoso.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Apunte") },
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
                value = titulo,
                onValueChange = {
                    titulo = it
                    viewModel.titulo = it
                    viewModel.limpiarError()
                },
                label = { Text("Título del apunte *") },
                modifier = Modifier.fillMaxWidth(),
                isError = error != null && titulo.isBlank(),
                singleLine = true
            )

            OutlinedTextField(
                value = contenido,
                onValueChange = {
                    contenido = it
                    viewModel.contenido = it
                    viewModel.limpiarError()
                },
                label = { Text("Contenido *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                isError = error != null && contenido.isBlank(),
                maxLines = 10
            )

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
                Text("Guardar Apunte")
            }
        }
    }
}