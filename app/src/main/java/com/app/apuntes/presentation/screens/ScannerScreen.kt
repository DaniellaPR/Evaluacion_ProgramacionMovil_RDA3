package com.app.apuntes.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.app.apuntes.core.camera.createTempImageUri
import com.app.apuntes.presentation.navigation.ResultadoOCR
import com.app.apuntes.presentation.viewmodel.ScannerUiState
import com.app.apuntes.presentation.viewmodel.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val viewModel: ScannerViewModel = viewModel(viewModelStoreOwner = activity)
    val scannerUiState by viewModel.scannerUiState.collectAsState()

    var pendingImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val imageUri = pendingImageUri
        if (success && imageUri != null) {
            viewModel.procesarImagen(imageUri)
        } else {
            viewModel.notificarErrorEscaner(
                "No se pudo capturar la imagen. Inténtalo nuevamente."
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                val imageUri = context.createTempImageUri()
                pendingImageUri = imageUri
                takePictureLauncher.launch(imageUri)
            } catch (_: Exception) {
                viewModel.notificarErrorEscaner(
                    "La cámara no está disponible en este momento."
                )
            }
        } else {
            viewModel.notificarErrorEscaner(
                "Se requiere el permiso de cámara para escanear apuntes."
            )
        }
    }

    fun abrirCamara() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            try {
                val imageUri = context.createTempImageUri()
                pendingImageUri = imageUri
                takePictureLauncher.launch(imageUri)
            } catch (_: Exception) {
                viewModel.notificarErrorEscaner(
                    "La cámara no está disponible en este momento."
                )
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(scannerUiState) {
        val state = scannerUiState
        if (state is ScannerUiState.TextoReconocido) {
            navController.navigate(ResultadoOCR)
            viewModel.consumirResultadoReconocido()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digitalizador de Apuntes") },
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(88.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Escanea tus apuntes físicos",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Captura una foto, deja que ML Kit reconozca el texto y edítalo antes de guardarlo en Room.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            when (val state = scannerUiState) {
                ScannerUiState.EsperandoCaptura -> {
                    ScannerInfoCard(
                        title = "Consejos para un mejor OCR",
                        message = "Coloca el apunte sobre una superficie plana, procura buena iluminación y evita fotos borrosas."
                    )
                }

                ScannerUiState.ProcesandoImagen -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Procesando imagen y reconociendo texto...",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is ScannerUiState.Error -> {
                    ScannerInfoCard(
                        title = "No se pudo completar el escaneo",
                        message = state.mensaje,
                        isError = true
                    )
                    Button(onClick = { viewModel.limpiarErrorEscaner() }) {
                        Text("Intentar otra vez")
                    }
                }

                is ScannerUiState.TextoReconocido -> Unit
            }

            Button(
                onClick = { abrirCamara() },
                enabled = scannerUiState !is ScannerUiState.ProcesandoImagen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir cámara")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ScannerInfoCard(
    title: String,
    message: String,
    isError: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isError) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )
        }
    }
}
