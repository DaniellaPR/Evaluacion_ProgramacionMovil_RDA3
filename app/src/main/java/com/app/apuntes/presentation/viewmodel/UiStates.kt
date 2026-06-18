package com.app.apuntes.presentation.viewmodel

import com.app.apuntes.domain.model.Apunte
import com.app.apuntes.domain.model.Horario
import com.app.apuntes.domain.model.Materia

sealed class ApuntesUiState {
    object Loading : ApuntesUiState()
    data class Success(val apuntes: List<Apunte>) : ApuntesUiState()
    data class Error(val mensaje: String) : ApuntesUiState()
}

sealed class MateriasUiState {
    object Loading : MateriasUiState()
    data class Success(val materias: List<Materia>) : MateriasUiState()
    data class Error(val mensaje: String) : MateriasUiState()
}

sealed class HorarioUiState {
    object Loading : HorarioUiState()
    data class Success(val horarios: List<Horario>) : HorarioUiState()
    data class Error(val mensaje: String) : HorarioUiState()
}

sealed class RecursosUiState {
    object Idle : RecursosUiState()      // sin materias: sección oculta
    object Loading : RecursosUiState()
    data class Success(val recursos: List<com.app.apuntes.domain.model.Recurso>) : RecursosUiState()
    data class Error(val mensaje: String) : RecursosUiState()
}


sealed interface ScannerUiState {
    data object EsperandoCaptura : ScannerUiState
    data object ProcesandoImagen : ScannerUiState
    data class TextoReconocido(val texto: String) : ScannerUiState
    data class Error(val mensaje: String) : ScannerUiState
}

sealed interface GuardadoApunteUiState {
    data object Idle : GuardadoApunteUiState
    data object Guardando : GuardadoApunteUiState
    data class Success(val materiaId: Long) : GuardadoApunteUiState
    data class Error(val mensaje: String) : GuardadoApunteUiState
}

data class OcrEditorFormState(
    val materiaId: Long? = null,
    val titulo: String = "",
    val contenido: String = ""
)

sealed interface TtsUiState {
    data object Idle : TtsUiState
    data object Hablando : TtsUiState
    data class Error(val mensaje: String) : TtsUiState
}

sealed interface QrGenerarState {
    data object Idle : QrGenerarState
    data object Generando : QrGenerarState
    data class QrGenerado(
        val bitmap: android.graphics.Bitmap,
        val excedioLimite: Boolean,
        val titulo: String
    ) : QrGenerarState
    data class Error(val mensaje: String) : QrGenerarState
}

sealed interface QrEscanearState {
    data object Esperando : QrEscanearState
    data object Procesando : QrEscanearState
    data object TextoRecibido : QrEscanearState
    data class Error(val mensaje: String) : QrEscanearState
}

data class QrGuardarFormState(
    val titulo: String = "",
    val materiaId: Long? = null
)
