package com.app.apuntes.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.apuntes.core.qr.QrCodeGenerator
import com.app.apuntes.data.local.room.DatabaseProvider
import com.app.apuntes.data.repository.ApunteRepositoryImpl
import com.app.apuntes.data.repository.MateriaRepositoryImpl
import com.app.apuntes.domain.model.Apunte
import com.app.apuntes.domain.usecase.GuardarApunteUseCase
import com.app.apuntes.domain.usecase.ObtenerMateriasUseCase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class QRViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)
    private val apunteRepository = ApunteRepositoryImpl(database.apunteDao())
    private val obtenerMateriasUseCase = ObtenerMateriasUseCase(
        MateriaRepositoryImpl(database.materiaDao())
    )
    private val guardarApunteUseCase = GuardarApunteUseCase(
        ApunteRepositoryImpl(database.apunteDao())
    )

    // Estado para generación de QR
    private val _generarState = MutableStateFlow<QrGenerarState>(QrGenerarState.Idle)
    val generarState: StateFlow<QrGenerarState> = _generarState

    // Estado para escaneo de QR
    private val _escanearState = MutableStateFlow<QrEscanearState>(QrEscanearState.Esperando)
    val escanearState: StateFlow<QrEscanearState> = _escanearState

    // Texto escaneado persistido (separado del estado de proceso)
    private val _textoEscaneado = MutableStateFlow("")
    val textoEscaneado: StateFlow<String> = _textoEscaneado

    // Materias para guardar apunte desde QR
    private val _materiasUiState = MutableStateFlow<MateriasUiState>(MateriasUiState.Loading)
    val materiasUiState: StateFlow<MateriasUiState> = _materiasUiState

    // Formulario para guardar apunte escaneado
    private val _guardarFormState = MutableStateFlow(QrGuardarFormState())
    val guardarFormState: StateFlow<QrGuardarFormState> = _guardarFormState

    // Estado del guardado
    private val _guardadoState = MutableStateFlow<GuardadoApunteUiState>(GuardadoApunteUiState.Idle)
    val guardadoState: StateFlow<GuardadoApunteUiState> = _guardadoState

    init {
        cargarMaterias()
    }

    private fun cargarMaterias() {
        viewModelScope.launch {
            obtenerMateriasUseCase()
                .catch { e ->
                    _materiasUiState.value = MateriasUiState.Error(
                        e.message ?: "Error al cargar materias"
                    )
                }
                .collect { materias ->
                    _materiasUiState.value = MateriasUiState.Success(materias)
                    if (materias.isNotEmpty() && _guardarFormState.value.materiaId == null) {
                        _guardarFormState.update { it.copy(materiaId = materias.first().id) }
                    }
                }
        }
    }

    // ── Generación de QR ──────────────────────────────────────────────────────

    fun generarQRParaApunte(apunteId: Long) {
        viewModelScope.launch {
            _generarState.value = QrGenerarState.Generando
            val apunte = apunteRepository.obtenerApuntePorId(apunteId)
            if (apunte == null) {
                _generarState.value = QrGenerarState.Error("Apunte no encontrado")
                return@launch
            }
            val textoCompleto = "${apunte.titulo}\n\n${apunte.contenido}"
            val excedioLimite = QrCodeGenerator.esDemasiadoLargo(textoCompleto)
            val textoFinal = if (excedioLimite) {
                QrCodeGenerator.recortarTexto(textoCompleto)
            } else {
                textoCompleto
            }
            val bitmap = QrCodeGenerator.generarBitmap(textoFinal)
            if (bitmap != null) {
                _generarState.value = QrGenerarState.QrGenerado(bitmap, excedioLimite, apunte.titulo)
            } else {
                _generarState.value = QrGenerarState.Error("No se pudo generar el código QR")
            }
        }
    }

    fun reiniciarGeneracion() {
        _generarState.value = QrGenerarState.Idle
    }

    // ── Escaneo de QR ────────────────────────────────────────────────────────

    fun procesarImagenParaQR(uri: Uri) {
        viewModelScope.launch {
            _escanearState.value = QrEscanearState.Procesando
            try {
                val texto = leerCodigoQRDesdeUri(uri)
                if (texto.isBlank()) {
                    _escanearState.value = QrEscanearState.Error(
                        "No se encontró un código QR en la imagen. Asegúrate de enfocar el código correctamente."
                    )
                } else {
                    _textoEscaneado.value = texto
                    _guardarFormState.update { it.copy(titulo = sugerirTitulo(texto)) }
                    _escanearState.value = QrEscanearState.TextoRecibido
                }
            } catch (e: Exception) {
                _escanearState.value = QrEscanearState.Error(
                    e.message ?: "Error al leer el código QR"
                )
            }
        }
    }

    private suspend fun leerCodigoQRDesdeUri(uri: Uri): String = suspendCoroutine { cont ->
        try {
            val image = InputImage.fromFilePath(getApplication(), uri)
            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    cont.resume(barcodes.firstOrNull()?.rawValue ?: "")
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

    fun notificarErrorEscaneo(mensaje: String) {
        _escanearState.value = QrEscanearState.Error(mensaje)
    }

    fun consumirResultadoEscaneado() {
        if (_escanearState.value is QrEscanearState.TextoRecibido) {
            _escanearState.value = QrEscanearState.Esperando
        }
    }

    fun reiniciarFlujoEscaneo() {
        val materiaActual = _guardarFormState.value.materiaId
        _escanearState.value = QrEscanearState.Esperando
        _textoEscaneado.value = ""
        _guardarFormState.value = QrGuardarFormState(materiaId = materiaActual)
        _guardadoState.value = GuardadoApunteUiState.Idle
    }

    // ── Guardar apunte desde QR ───────────────────────────────────────────────

    fun actualizarTituloGuardar(titulo: String) {
        _guardarFormState.update { it.copy(titulo = titulo) }
        if (_guardadoState.value is GuardadoApunteUiState.Error) {
            _guardadoState.value = GuardadoApunteUiState.Idle
        }
    }

    fun actualizarMateriaGuardar(materiaId: Long) {
        _guardarFormState.update { it.copy(materiaId = materiaId) }
        if (_guardadoState.value is GuardadoApunteUiState.Error) {
            _guardadoState.value = GuardadoApunteUiState.Idle
        }
    }

    fun guardarApunteDesdeQR() {
        val form = _guardarFormState.value
        val texto = _textoEscaneado.value
        val materiaId = form.materiaId
        val titulo = form.titulo.trim()

        when {
            materiaId == null -> {
                _guardadoState.value = GuardadoApunteUiState.Error("Selecciona una materia")
                return
            }
            titulo.isBlank() -> {
                _guardadoState.value = GuardadoApunteUiState.Error("El título no puede estar vacío")
                return
            }
            texto.isBlank() -> {
                _guardadoState.value = GuardadoApunteUiState.Error("No hay texto para guardar")
                return
            }
        }

        viewModelScope.launch {
            _guardadoState.value = GuardadoApunteUiState.Guardando
            try {
                guardarApunteUseCase(
                    Apunte(
                        materiaId = materiaId!!,
                        titulo = titulo,
                        contenido = texto,
                        fechaCreacion = System.currentTimeMillis(),
                        origen = "qr"
                    )
                )
                _guardadoState.value = GuardadoApunteUiState.Success(materiaId!!)
            } catch (e: Exception) {
                _guardadoState.value = GuardadoApunteUiState.Error(
                    e.message ?: "Error al guardar el apunte"
                )
            }
        }
    }

    private fun sugerirTitulo(texto: String): String {
        val primeraLinea = texto
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            ?: "Apunte desde QR"
        return primeraLinea.take(60)
    }
}
