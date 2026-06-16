package com.app.apuntes.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TextToSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var onDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("es", "ES"))
                isReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { onDoneCallback?.invoke() }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { onDoneCallback?.invoke() }
                })
            }
        }
    }

    fun hablar(texto: String, onDone: () -> Unit = {}) {
        if (texto.isBlank()) return
        onDoneCallback = onDone
        if (isReady) {
            tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "apuntes_tts")
        }
    }

    fun detener() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
    }

    fun liberar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }

    fun estaHablando(): Boolean = tts?.isSpeaking == true
}
