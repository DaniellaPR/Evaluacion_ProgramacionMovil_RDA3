package com.app.apuntes.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.apuntes.data.local.room.DatabaseProvider
import com.app.apuntes.data.remote.retrofit.RetrofitClient
import com.app.apuntes.data.repository.MateriaRepositoryImpl
import com.app.apuntes.data.repository.RecursoRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = MateriaRepositoryImpl(db.materiaDao())
    private val recursoRepository = RecursoRepositoryImpl(RetrofitClient.apiService)

    private val _uiState = MutableStateFlow<MateriasUiState>(MateriasUiState.Loading)
    val uiState: StateFlow<MateriasUiState> = _uiState

    private val _recursosState = MutableStateFlow<RecursosUiState>(RecursosUiState.Loading)
    val recursosState: StateFlow<RecursosUiState> = _recursosState

    init {
        cargarMaterias()
        cargarRecursos()
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

    private fun cargarRecursos() {
        viewModelScope.launch {
            try {
                val list = recursoRepository.obtenerRecursos()
                _recursosState.value = RecursosUiState.Success(list.take(15)) // Mostrar top 15 recursos
            } catch (e: Exception) {
                _recursosState.value = RecursosUiState.Error(e.message ?: "Error al cargar recursos educativos")
            }
        }
    }
}