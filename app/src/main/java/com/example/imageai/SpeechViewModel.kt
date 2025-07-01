package com.example.imageai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.*

class SpeechViewModel : ViewModel() {
    
    // TTS related
    private var textToSpeech: TextToSpeech? = null
    var isTtsInitialized = mutableStateOf(false)
    var availableLanguages = mutableStateOf<List<Locale>>(emptyList())
    var availableVoices = mutableStateOf<List<Voice>>(emptyList())
    var selectedLanguage = mutableStateOf<Locale?>(null)
    var selectedVoice = mutableStateOf<Voice?>(null)
    var isSpeaking = mutableStateOf(false)
    
    // STT related
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening = mutableStateOf(false)
    var recognizedText = mutableStateOf("")
    var sttError = mutableStateOf("")
    var availableSttLanguages = mutableStateOf<List<Locale>>(emptyList())
    var selectedSttLanguage = mutableStateOf<Locale?>(null)
    
    // General
    var inputText = mutableStateOf("")
    
    fun initializeTts(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized.value = true
                loadAvailableLanguages()
                setupUtteranceProgressListener()
            }
        }
    }
    
    private fun setupUtteranceProgressListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking.value = true
            }
            
            override fun onDone(utteranceId: String?) {
                isSpeaking.value = false
            }
            
            override fun onError(utteranceId: String?) {
                isSpeaking.value = false
                Log.e("TTS", "TTS Error occurred")
            }
        })
    }
    
    private fun loadAvailableLanguages() {
        textToSpeech?.let { tts ->
            val locales = mutableListOf<Locale>()
            val voices = mutableListOf<Voice>()
            
            // Common languages for TTS
            val commonLanguages = listOf(
                Locale.ENGLISH,
                Locale("en", "US"),
                Locale("en", "GB"),
                Locale("es", "ES"),
                Locale("fr", "FR"),
                Locale("de", "DE"),
                Locale("it", "IT"),
                Locale("pt", "PT"),
                Locale("ru", "RU"),
                Locale("zh", "CN"),
                Locale("ja", "JP"),
                Locale("ko", "KR"),
                Locale("hi", "IN"),
                Locale("ar", "SA")
            )
            
            commonLanguages.forEach { locale ->
                if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                    locales.add(locale)
                }
            }
            
            // Get available voices
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                tts.voices?.let { voiceSet ->
                    voices.addAll(voiceSet.filter { voice -> 
                        !voice.isNetworkConnectionRequired 
                    })
                }
            }
            
            availableLanguages.value = locales
            availableVoices.value = voices
            
            if (locales.isNotEmpty()) {
                selectedLanguage.value = locales.first()
                setLanguage(locales.first())
            }
        }
    }
    
    fun setLanguage(locale: Locale) {
        textToSpeech?.setLanguage(locale)
        selectedLanguage.value = locale
        
        // Update available voices for this language
        val voicesForLanguage = availableVoices.value.filter { voice ->
            voice.locale.language == locale.language
        }
        
        if (voicesForLanguage.isNotEmpty()) {
            setVoice(voicesForLanguage.first())
        }
    }
    
    fun setVoice(voice: Voice) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech?.voice = voice
            selectedVoice.value = voice
        }
    }
    
    fun speak(text: String) {
        if (isTtsInitialized.value && text.isNotEmpty()) {
            val params = Bundle()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "TTS_UTTERANCE_ID")
        }
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
        isSpeaking.value = false
    }
    
    fun initializeStt(context: Context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening.value = true
                    sttError.value = ""
                }
                
                override fun onBeginningOfSpeech() {}
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    isListening.value = false
                }
                
                override fun onError(error: Int) {
                    isListening.value = false
                    sttError.value = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error"
                    }
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        recognizedText.value = matches[0]
                        inputText.value = matches[0]
                    }
                    isListening.value = false
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        recognizedText.value = matches[0]
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            loadSttLanguages()
        }
    }
    
    private fun loadSttLanguages() {
        // Common languages for STT
        val sttLanguages = listOf(
            Locale.ENGLISH,
            Locale("en", "US"),
            Locale("en", "GB"),
            Locale("es", "ES"),
            Locale("fr", "FR"),
            Locale("de", "DE"),
            Locale("it", "IT"),
            Locale("pt", "PT"),
            Locale("ru", "RU"),
            Locale("zh", "CN"),
            Locale("ja", "JP"),
            Locale("ko", "KR"),
            Locale("hi", "IN"),
            Locale("ar", "SA")
        )
        
        availableSttLanguages.value = sttLanguages
        selectedSttLanguage.value = sttLanguages.first()
    }
    
    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedSttLanguage.value?.toString() ?: "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.startListening(intent)
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening.value = false
    }
    
    fun setSttLanguage(locale: Locale) {
        selectedSttLanguage.value = locale
    }
    
    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }
} 