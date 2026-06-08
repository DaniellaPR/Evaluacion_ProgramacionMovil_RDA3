package com.app.apuntes.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.apuntes.data.local.room.DatabaseProvider
import com.app.apuntes.data.repository.HorarioRepositoryImpl
import com.app.apuntes.data.repository.MateriaRepositoryImpl
import com.app.apuntes.domain.model.Horario
import com.app.apuntes.domain.model.Materia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HorarioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val horarioRepository = HorarioRepositoryImpl(db.horarioDao())
    private val materiaRepository = MateriaRepositoryImpl(db.materiaDao())

    private val _uiState = MutableStateFlow<HorarioUiState>(HorarioUiState.Loading)
    val uiState: StateFlow<HorarioUiState> = _uiState

    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias

    init {
        cargarMaterias()
        cargarHorarios()
    }

    fun cargarMaterias() {
        viewModelScope.launch {
            materiaRepository.obtenerMaterias()
                .catch { 
                    _uiState.value = HorarioUiState.Error(it.message ?: "Error al cargar materias")
                }
                .collect { list ->
                    _materias.value = list
                }
        }
    }

    fun cargarHorarios() {
        viewModelScope.launch {
            _uiState.value = HorarioUiState.Loading
            try {
                val list = horarioRepository.obtenerHorarios()
                _uiState.value = HorarioUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = HorarioUiState.Error(e.message ?: "Error al cargar horarios")
            }
        }
    }

    fun guardarHorario(horario: Horario) {
        viewModelScope.launch {
            try {
                horarioRepository.guardarHorario(horario)
                cargarHorarios()
            } catch (e: Exception) {
                _uiState.value = HorarioUiState.Error(e.message ?: "Error al guardar horario")
            }
        }
    }

    fun eliminarHorario(id: Long) {
        viewModelScope.launch {
            try {
                horarioRepository.eliminarHorario(id)
                cargarHorarios()
            } catch (e: Exception) {
                _uiState.value = HorarioUiState.Error(e.message ?: "Error al eliminar horario")
            }
        }
    }
}
