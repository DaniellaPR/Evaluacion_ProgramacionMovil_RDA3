package com.app.apuntes.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.app.apuntes.data.local.room.DatabaseProvider
import com.app.apuntes.data.repository.MateriaRepositoryImpl
import com.app.apuntes.domain.model.Materia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CrearMateriaViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // SavedStateHandle protege los campos del formulario ante rotación o Process Death
    var nombre: String
        get() = savedStateHandle["nombre"] ?: ""
        set(value) { savedStateHandle["nombre"] = value }

    var docente: String
        get() = savedStateHandle["docente"] ?: ""
        set(value) { savedStateHandle["docente"] = value }

    var descripcion: String
        get() = savedStateHandle["descripcion"] ?: ""
        set(value) { savedStateHandle["descripcion"] = value }

    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = MateriaRepositoryImpl(db.materiaDao())

    fun guardar() {
        val nombreVal = nombre.trim()
        if (nombreVal.isBlank()) {
            _error.value = "El nombre de la materia es obligatorio"
            return
        }
        viewModelScope.launch {
            try {
                repository.guardarMateria(
                    Materia(
                        nombre = nombreVal,
                        docente = docente.trim().ifBlank { null },
                        descripcion = descripcion.trim().ifBlank { null }
                    )
                )
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al guardar la materia"
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}