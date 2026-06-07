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

class DetalleApunteViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val apunteId: Long = savedStateHandle["apunteId"] ?: 0L

    private val db = DatabaseProvider.getDatabase(application)
    private val repository = ApunteRepositoryImpl(db.apunteDao())

    private val _apunte = MutableStateFlow<Apunte?>(null)
    val apunte: StateFlow<Apunte?> = _apunte

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    init {
        viewModelScope.launch {
            _apunte.value = repository.obtenerApuntePorId(apunteId)
            _cargando.value = false
        }
    }

    companion object {
        fun provideFactory(apunteId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                    val handle = createSavedStateHandle()
                    handle["apunteId"] = apunteId
                    DetalleApunteViewModel(app, handle)
                }
            }
    }
}