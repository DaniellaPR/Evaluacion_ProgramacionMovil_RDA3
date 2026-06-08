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
    object Loading : RecursosUiState()
    data class Success(val recursos: List<com.app.apuntes.domain.model.Recurso>) : RecursosUiState()
    data class Error(val mensaje: String) : RecursosUiState()
}

