package com.app.apuntes.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.apuntes.core.tts.TextToSpeechManager
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

    private val ttsManager = TextToSpeechManager(application)

    private val _ttsState = MutableStateFlow<TtsUiState>(TtsUiState.Idle)
    val ttsState: StateFlow<TtsUiState> = _ttsState

    init {
        viewModelScope.launch {
            _apunte.value = repository.obtenerApuntePorId(apunteId)
            _cargando.value = false
        }
    }

    fun escucharApunte() {
        val a = _apunte.value ?: return
        val texto = "${a.titulo}. ${a.contenido}".trim()
        if (texto.isBlank()) {
            _ttsState.value = TtsUiState.Error("El apunte no tiene contenido para reproducir")
            return
        }
        _ttsState.value = TtsUiState.Hablando
        ttsManager.hablar(texto) {
            _ttsState.value = TtsUiState.Idle
        }
    }

    fun detenerLectura() {
        ttsManager.detener()
        _ttsState.value = TtsUiState.Idle
    }

    fun limpiarErrorTts() {
        if (_ttsState.value is TtsUiState.Error) {
            _ttsState.value = TtsUiState.Idle
        }
    }

    override fun onCleared() {
        ttsManager.liberar()
        super.onCleared()
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
