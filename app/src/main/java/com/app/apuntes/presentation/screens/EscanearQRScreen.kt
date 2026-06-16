package com.app.apuntes.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.app.apuntes.presentation.navigation.ResultadoQR
import com.app.apuntes.presentation.viewmodel.QRViewModel
import com.app.apuntes.presentation.viewmodel.QrEscanearState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanearQRScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val viewModel: QRViewModel = viewModel(viewModelStoreOwner = activity)
    val escanearState by viewModel.escanearState.collectAsState()

    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val imageUri = pendingImageUri
        if (success && imageUri != null) {
            viewModel.procesarImagenParaQR(imageUri)
        } else {
            viewModel.notificarErrorEscaneo("No se pudo capturar la imagen. Inténtalo nuevamente.")
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
                viewModel.notificarErrorEscaneo("La cámara no está disponible en este momento.")
            }
        } else {
            viewModel.notificarErrorEscaneo("Se requiere el permiso de cámara para escanear QR.")
        }
    }

    fun abrirCamara() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            try {
                val imageUri = context.createTempImageUri()
                pendingImageUri = imageUri
                takePictureLauncher.launch(imageUri)
            } catch (_: Exception) {
                viewModel.notificarErrorEscaneo("La cámara no está disponible en este momento.")
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(escanearState) {
        if (escanearState is QrEscanearState.TextoRecibido) {
            navController.navigate(ResultadoQR)
            viewModel.consumirResultadoEscaneado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear Código QR") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(88.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Escanea el código QR",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Apunta la cámara al código QR de un compañero para recuperar el apunte compartido sin internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            when (val state = escanearState) {
                QrEscanearState.Procesando -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Leyendo código QR...",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is QrEscanearState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = state.mensaje,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(
                        onClick = { viewModel.reiniciarFlujoEscaneo() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Intentar otra vez")
                    }
                }

                else -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Consejos para un mejor escaneo",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Asegúrate de que el código QR esté bien iluminado y completamente visible en la foto. Mantén el teléfono estable.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { abrirCamara() },
                enabled = escanearState !is QrEscanearState.Procesando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir cámara para escanear")
            }
        }
    }
}
