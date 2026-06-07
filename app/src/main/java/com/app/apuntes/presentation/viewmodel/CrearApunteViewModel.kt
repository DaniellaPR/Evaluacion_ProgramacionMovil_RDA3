package com.app.apuntes.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.apuntes.data.local.room.DatabaseProvider
import com.app.apuntes.data.repository.ApunteRepositoryImpl
import com.app.apuntes.domain.model.Apunte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CrearApunteViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val materiaId: Long = savedStateHandle["materiaId"] ?: 0L

    // Cada campo del formulario sobrevive a rotaciones gracias a SavedStateHandle
    var titulo: String
        get() = savedStateHandle["titulo"] ?: ""
        set(value) { savedStateHandle["titulo"] = value }

    var contenido: String
        get() = savedStateHandle["contenido"] ?: ""
        set(value) { savedStateHandle["contenido"] = value }

    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = ApunteRepositoryImpl(db.apunteDao())

    fun guardar() {
        val tituloVal = titulo.trim()
        val contenidoVal = contenido.trim()

        if (tituloVal.isBlank()) {
            _error.value = "El título es obligatorio"
            return
        }
        if (contenidoVal.isBlank()) {
            _error.value = "El contenido no puede estar vacío"
            return
        }

        viewModelScope.launch {
            try {
                repository.guardarApunte(
                    Apunte(
                        materiaId = materiaId,
                        titulo = tituloVal,
                        contenido = contenidoVal,
                        fechaCreacion = System.currentTimeMillis(),
                        origen = "manual"
                    )
                )
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al guardar el apunte"
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    companion object {
        fun provideFactory(materiaId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                    val handle = createSavedStateHandle()
                    handle["materiaId"] = materiaId
                    CrearApunteViewModel(app, handle)
                }
            }
    }
}