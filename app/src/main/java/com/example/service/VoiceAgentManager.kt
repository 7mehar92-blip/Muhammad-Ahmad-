package com.example.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAgentManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentPhrase = MutableStateFlow("AI Agent Tayyar Hai")
    val currentPhrase: StateFlow<String> = _currentPhrase.asStateFlow()

    private val _isVoiceEnabled = MutableStateFlow(true)
    val isVoiceEnabled: StateFlow<Boolean> = _isVoiceEnabled.asStateFlow()

    private var toneGen: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
            toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 65)
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) { }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // Attempt Urdu first, fallback to Hindi or English phonetics
            val urduLocale = Locale("ur", "PK")
            val result = tts?.setLanguage(urduLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val hindiLocale = Locale("hi", "IN")
                val hiResult = tts?.setLanguage(hindiLocale)
                if (hiResult == TextToSpeech.LANG_MISSING_DATA || hiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.US
                }
            }
            tts?.setPitch(1.05f)
            tts?.setSpeechRate(0.95f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun toggleVoice() {
        _isVoiceEnabled.value = !_isVoiceEnabled.value
        if (!_isVoiceEnabled.value) {
            stopSpeech()
        }
    }

    fun speakUrdu(textUrdu: String, phoneticFallback: String? = null) {
        _currentPhrase.value = textUrdu
        playChime()
        triggerHaptic()

        if (!_isVoiceEnabled.value) return

        val textToSpeak = phoneticFallback ?: textUrdu
        if (isInitialized && tts != null) {
            try {
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "UTTER_${System.currentTimeMillis()}")
            } catch (_: Exception) { }
        }
    }

    fun announceActivation() {
        speakUrdu(
            textUrdu = "Compounding agent activate ho chuka hai. Timeframe choose karein.",
            phoneticFallback = "Compounding agent activate ho chuka hai. Timeframe choose karein."
        )
    }

    fun announceAnalyzing() {
        speakUrdu(
            textUrdu = "AI ko chart details share kar di hain, analyze kar raha hai...",
            phoneticFallback = "AI ko chart details share kar di hain, analyze kar raha hai."
        )
    }

    fun announceSignal(direction: String) {
        val dirText = if (direction.equals("BUY", ignoreCase = true)) "BUY" else "SELL"
        speakUrdu(
            textUrdu = "Signal aa gaya aur maine $dirText ki trade laga di hai. Tension free rahein yeh Insha'Allah win hai!",
            phoneticFallback = "Signal aa gaya aur maine $dirText ki trade laga di hai. Tension free rahein yeh Insha Allah win hai!"
        )
    }

    fun announceWin(step: Int) {
        speakUrdu(
            textUrdu = "Alhamdulillah! Shandar win mili humein! Step $step mukammal ho gaya hai.",
            phoneticFallback = "Alhamdulillah! Shandaar win mili humein! Step $step mukammal ho gaya hai."
        )
    }

    fun announceSessionComplete() {
        speakUrdu(
            textUrdu = "Khushi ki baat hai! Session mukammal taur par jeet liya gaya hai!",
            phoneticFallback = "Khushi ki baat hai! Session mukammal taur par jeet liya gaya hai!"
        )
    }

    fun announceLossReset() {
        speakUrdu(
            textUrdu = "Trade loss hui. Risk management ke tehat agla step calculate kar liya gaya hai.",
            phoneticFallback = "Trade loss hui. Risk management ke tehat agla step calculate kar liya gaya hai."
        )
    }

    fun announceRuleSaved() {
        speakUrdu(
            textUrdu = "Aapki nayi strategy aur compounding steps save ho chuke hain.",
            phoneticFallback = "Aapki nayi strategy aur compounding steps save ho chuke hain."
        )
    }

    fun announceWebhookReceived(source: String) {
        speakUrdu(
            textUrdu = "$source se alert mosool hua hai. Market analyze ki ja rahi hai.",
            phoneticFallback = "$source se alert mosool hua hai. Market analyze ki ja rahi hai."
        )
    }

    fun stopSpeech() {
        tts?.stop()
        _isSpeaking.value = false
    }

    private fun playChime() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
        } catch (_: Exception) { }
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) { }
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        toneGen?.release()
    }
}
