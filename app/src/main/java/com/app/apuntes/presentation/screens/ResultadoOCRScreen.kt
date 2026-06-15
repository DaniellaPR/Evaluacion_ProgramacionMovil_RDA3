package com.app.apuntes.presentation.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.apuntes.presentation.navigation.EditarApunteOCR
import com.app.apuntes.presentation.viewmodel.ScannerViewModel
import androidx.activity.ComponentActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoOCRScreen(navController: NavController) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: ScannerViewModel = viewModel(viewModelStoreOwner = activity)
    val formState by viewModel.editorFormState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultado del OCR") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Texto reconocido",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Revisa el resultado antes de editarlo. Si la foto salió borrosa, puedes repetir el escaneo.",
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
                        text = if (formState.contenido.isBlank()) {
                            "Todavía no hay texto reconocido."
                        } else {
                            formState.contenido
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Button(
                onClick = { navController.navigate(EditarApunteOCR) },
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.contenido.isNotBlank()
            ) {
                Text("Editar y guardar")
            }

            OutlinedButton(
                onClick = {
                    viewModel.reiniciarFlujo()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Escanear nuevamente")
            }
        }
    }
}
