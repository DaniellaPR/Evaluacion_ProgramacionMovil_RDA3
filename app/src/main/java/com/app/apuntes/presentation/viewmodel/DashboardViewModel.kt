package com.app.apuntes.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.apuntes.data.local.room.DatabaseProvider
import com.app.apuntes.data.remote.retrofit.RetrofitClient
import com.app.apuntes.data.repository.MateriaRepositoryImpl
import com.app.apuntes.data.repository.RecursoRepositoryImpl
import com.app.apuntes.domain.model.Materia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = MateriaRepositoryImpl(db.materiaDao())
    private val recursoRepository = RecursoRepositoryImpl(RetrofitClient.apiService)

    private val _uiState = MutableStateFlow<MateriasUiState>(MateriasUiState.Loading)
    val uiState: StateFlow<MateriasUiState> = _uiState

    // Idle: esperando materias | Loading: buscando | Success/Error: resultado
    private val _recursosState = MutableStateFlow<RecursosUiState>(RecursosUiState.Idle)
    val recursosState: StateFlow<RecursosUiState> = _recursosState

    // 3 materias de ejemplo que se insertan si Room está vacío
    private val materiasDemoEjemplo = listOf(
        Materia(nombre = "Programación Móvil", docente = "Ing. Juan Chafla", descripcion = "Desarrollo de apps nativas en Android con Kotlin"),
        Materia(nombre = "Diseño de Interiores", docente = "Arq. María López", descripcion = "Fundamentos del diseño de espacios interiores"),
        Materia(nombre = "Estructuras y Resistencia", docente = "Ing. Carlos Vega", descripcion = "Análisis estructural de materiales de construcción")
    )

    init {
        cargarMateriasConDemo()
    }

    private fun cargarMateriasConDemo() {
        viewModelScope.launch {
            // Insertar demo si Room está vacío
            val actuales = repository.obtenerMaterias().first()
            if (actuales.isEmpty()) {
                materiasDemoEjemplo.forEach { repository.guardarMateria(it) }
            }
            // Suscribirse reactivamente y disparar búsqueda de recursos al cargar
            repository.obtenerMaterias()
                .catch { e ->
                    _uiState.value = MateriasUiState.Error(e.message ?: "Error al cargar materias")
                }
                .collect { materias ->
                    _uiState.value = MateriasUiState.Success(materias)
                    // Buscar recursos dinámicamente según las materias actuales
                    cargarRecursos(materias.map { it.nombre })
                }
        }
    }

    private fun cargarRecursos(nombresMaterias: List<String>) {
        if (nombresMaterias.isEmpty()) {
            _recursosState.value = RecursosUiState.Idle
            return
        }
        viewModelScope.launch {
            _recursosState.value = RecursosUiState.Loading
            try {
                val lista = recursoRepository.obtenerRecursosPorMaterias(nombresMaterias)
                _recursosState.value = if (lista.isEmpty()) {
                    RecursosUiState.Error("No se encontraron recursos para tus materias")
                } else {
                    RecursosUiState.Success(lista)
                }
            } catch (e: Exception) {
                _recursosState.value = RecursosUiState.Error(
                    e.message ?: "Error al cargar recursos. Verifica tu conexión."
                )
            }
        }
    }
}