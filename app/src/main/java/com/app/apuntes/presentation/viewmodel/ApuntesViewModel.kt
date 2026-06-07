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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ApuntesViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // SavedStateHandle protege el materiaId ante Process Death
    private val materiaId: Long = savedStateHandle["materiaId"] ?: 0L

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = ApunteRepositoryImpl(db.apunteDao())

    private val _uiState = MutableStateFlow<ApuntesUiState>(ApuntesUiState.Loading)
    val uiState: StateFlow<ApuntesUiState> = _uiState

    init {
        cargarApuntes()
    }

    private fun cargarApuntes() {
        viewModelScope.launch {
            repository.obtenerApuntesPorMateria(materiaId)
                .catch { e ->
                    _uiState.value = ApuntesUiState.Error(e.message ?: "Error al cargar apuntes")
                }
                .collect { apuntes ->
                    _uiState.value = ApuntesUiState.Success(apuntes)
                }
        }
    }

    companion object {
        fun provideFactory(materiaId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                    val handle = createSavedStateHandle()
                    handle["materiaId"] = materiaId
                    ApuntesViewModel(app, handle)
                }
            }
    }
}