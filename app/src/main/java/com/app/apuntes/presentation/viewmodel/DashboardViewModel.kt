package com.app.apuntes.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.apuntes.data.local.room.DatabaseProvider
import com.app.apuntes.data.repository.MateriaRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = MateriaRepositoryImpl(db.materiaDao())

    private val _uiState = MutableStateFlow<MateriasUiState>(MateriasUiState.Loading)
    val uiState: StateFlow<MateriasUiState> = _uiState

    init {
        cargarMaterias()
    }

    private fun cargarMaterias() {
        viewModelScope.launch {
            repository.obtenerMaterias()
                .catch { e ->
                    _uiState.value = MateriasUiState.Error(e.message ?: "Error al cargar materias")
                }
                .collect { materias ->
                    _uiState.value = MateriasUiState.Success(materias)
                }
        }
    }
}