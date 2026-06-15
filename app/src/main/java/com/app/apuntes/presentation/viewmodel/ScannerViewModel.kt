package com.app.apuntes.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.apuntes.core.ocr.TextRecognitionManager
import com.app.apuntes.data.local.room.DatabaseProvider
import com.app.apuntes.data.repository.ApunteRepositoryImpl
import com.app.apuntes.data.repository.MateriaRepositoryImpl
import com.app.apuntes.domain.model.Apunte
import com.app.apuntes.domain.usecase.GuardarApunteUseCase
import com.app.apuntes.domain.usecase.ObtenerMateriasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseProvider.getDatabase(application)
    private val obtenerMateriasUseCase = ObtenerMateriasUseCase(
        MateriaRepositoryImpl(database.materiaDao())
    )
    private val guardarApunteUseCase = GuardarApunteUseCase(
        ApunteRepositoryImpl(database.apunteDao())
    )
    private val textRecognitionManager = TextRecognitionManager()

    private val _scannerUiState =
        MutableStateFlow<ScannerUiState>(ScannerUiState.EsperandoCaptura)
    val scannerUiState: StateFlow<ScannerUiState> = _scannerUiState

    private val _materiasUiState =
        MutableStateFlow<MateriasUiState>(MateriasUiState.Loading)
    val materiasUiState: StateFlow<MateriasUiState> = _materiasUiState

    private val _editorFormState = MutableStateFlow(OcrEditorFormState())
    val editorFormState: StateFlow<OcrEditorFormState> = _editorFormState

    private val _guardadoUiState =
        MutableStateFlow<GuardadoApunteUiState>(GuardadoApunteUiState.Idle)
    val guardadoUiState: StateFlow<GuardadoApunteUiState> = _guardadoUiState

    init {
        cargarMaterias()
    }

    private fun cargarMaterias() {
        viewModelScope.launch {
            obtenerMateriasUseCase()
                .catch { exception ->
                    _materiasUiState.value = MateriasUiState.Error(
                        exception.message ?: "Error al cargar las materias"
                    )
                }
                .collect { materias ->
                    _materiasUiState.value = MateriasUiState.Success(materias)
                    if (materias.isNotEmpty() && _editorFormState.value.materiaId == null) {
                        _editorFormState.update { current ->
                            current.copy(materiaId = materias.first().id)
                        }
                    }
                }
        }
    }

    fun procesarImagen(imageUri: Uri) {
        viewModelScope.launch {
            _scannerUiState.value = ScannerUiState.ProcesandoImagen
            try {
                val text = textRecognitionManager
                    .recognizeTextFromUri(getApplication(), imageUri)
                    .trim()

                if (text.isBlank()) {
                    _scannerUiState.value = ScannerUiState.Error(
                        "No se reconoció texto. Intenta con una foto más nítida y bien iluminada."
                    )
                    return@launch
                }

                _editorFormState.update { current ->
                    current.copy(
                        titulo = sugerirTitulo(text),
                        contenido = text
                    )
                }
                _scannerUiState.value = ScannerUiState.TextoReconocido(text)
            } catch (exception: Exception) {
                _scannerUiState.value = ScannerUiState.Error(
                    exception.message ?: "No fue posible procesar la imagen capturada."
                )
            }
        }
    }

    fun notificarErrorEscaner(mensaje: String) {
        _scannerUiState.value = ScannerUiState.Error(mensaje)
    }

    fun limpiarErrorEscaner() {
        if (_scannerUiState.value is ScannerUiState.Error) {
            _scannerUiState.value = ScannerUiState.EsperandoCaptura
        }
    }

    fun consumirResultadoReconocido() {
        if (_scannerUiState.value is ScannerUiState.TextoReconocido) {
            _scannerUiState.value = ScannerUiState.EsperandoCaptura
        }
    }

    fun actualizarMateria(materiaId: Long) {
        _editorFormState.update { current -> current.copy(materiaId = materiaId) }
        if (_guardadoUiState.value is GuardadoApunteUiState.Error) {
            _guardadoUiState.value = GuardadoApunteUiState.Idle
        }
    }

    fun actualizarTitulo(titulo: String) {
        _editorFormState.update { current -> current.copy(titulo = titulo) }
        if (_guardadoUiState.value is GuardadoApunteUiState.Error) {
            _guardadoUiState.value = GuardadoApunteUiState.Idle
        }
    }

    fun actualizarContenido(contenido: String) {
        _editorFormState.update { current -> current.copy(contenido = contenido) }
        if (_guardadoUiState.value is GuardadoApunteUiState.Error) {
            _guardadoUiState.value = GuardadoApunteUiState.Idle
        }
    }

    fun guardarApunteEscaneado() {
        val form = _editorFormState.value
        val materiaId = form.materiaId
        val titulo = form.titulo.trim()
        val contenido = form.contenido.trim()

        when {
            materiaId == null -> {
                _guardadoUiState.value = GuardadoApunteUiState.Error(
                    "Selecciona una materia antes de guardar el apunte."
                )
                return
            }

            titulo.isBlank() -> {
                _guardadoUiState.value = GuardadoApunteUiState.Error(
                    "El título no puede quedar vacío."
                )
                return
            }

            contenido.isBlank() -> {
                _guardadoUiState.value = GuardadoApunteUiState.Error(
                    "El contenido reconocido no puede quedar vacío."
                )
                return
            }
        }

        viewModelScope.launch {
            _guardadoUiState.value = GuardadoApunteUiState.Guardando
            try {
                guardarApunteUseCase(
                    Apunte(
                        materiaId = materiaId,
                        titulo = titulo,
                        contenido = contenido,
                        fechaCreacion = System.currentTimeMillis(),
                        origen = "ocr"
                    )
                )
                _guardadoUiState.value = GuardadoApunteUiState.Success(materiaId)
            } catch (exception: Exception) {
                _guardadoUiState.value = GuardadoApunteUiState.Error(
                    exception.message ?: "Ocurrió un error al guardar el apunte."
                )
            }
        }
    }

    fun reiniciarFlujo() {
        val materiaSeleccionada = _editorFormState.value.materiaId
        _scannerUiState.value = ScannerUiState.EsperandoCaptura
        _editorFormState.value = OcrEditorFormState(materiaId = materiaSeleccionada)
        _guardadoUiState.value = GuardadoApunteUiState.Idle
    }

    private fun sugerirTitulo(texto: String): String {
        val primeraLinea = texto
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            ?: "Apunte escaneado"

        return primeraLinea.take(60)
    }

    override fun onCleared() {
        textRecognitionManager.close()
        super.onCleared()
    }
}
