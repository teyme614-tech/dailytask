package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale
import java.util.Random

/**
 * Manages Text-To-Speech for encouraging AI voice feedback when completing tasks.
 * Uses a deep, clear Gulf/Saudi male voice setting and delivers short, impactful phrases.
 * Supports an utterance completion listener so celebratory clapping can play immediately after.
 */
class AiEncouragementSpeaker(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingPhrase: String? = null
    private var pendingCallback: (() -> Unit)? = null
    private var onSpeechFinishedCallback: (() -> Unit)? = null
    private val random = Random()

    companion object {
        // Short, punchy, motivating phrases with Gulf / Saudi spoken flavor
        val ENCOURAGEMENT_PHRASES = listOf(
            "كفو والله! أحسنت، أنت الأفضل!",
            "عاش البطل! تابع، أنت الأفضل!",
            "ما قصرت! أحسنت وتابع!",
            "كفو عليك! استمر يا وحش!",
            "أحسنت! تابع، أنت الأفضل دائماً!",
            "يا مال العافية! أحسنت، كمل!",
            "تسلم يمينك! أحسنت يا بطل، تابع!",
            "كفو! إنجاز بطل، تابع!",
            "عاشت إيدك! أحسنت، أنت الأفضل!",
            "بيض الله وجهك! أحسنت، واصل!",
            "كفو يا ذيب! تابع تقدمك!",
            "أحسنت! شغلك عدل، تابع!"
        )
    }

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("AiEncouragementSpeaker", "Failed to init TextToSpeech: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Priority for Gulf / Saudi Arabic locales
            val gulfLocales = listOf(
                Locale("ar", "SA"), // Saudi Arabia (Gulf)
                Locale("ar", "KW"), // Kuwait
                Locale("ar", "AE"), // UAE
                Locale("ar", "QA"), // Qatar
                Locale("ar", "BH"), // Bahrain
                Locale("ar", "OM"), // Oman
                Locale("ar")
            )

            var matchedLocale = Locale("ar", "SA")
            for (loc in gulfLocales) {
                val avail = tts?.isLanguageAvailable(loc)
                if (avail != null && avail >= TextToSpeech.LANG_AVAILABLE) {
                    matchedLocale = loc
                    tts?.setLanguage(loc)
                    break
                }
            }

            // Prioritize male voice profile in TTS voices
            try {
                val voices = tts?.voices
                if (!voices.isNullOrEmpty()) {
                    val arabicVoices = voices.filter { it.locale.language == "ar" }
                    // Look for voice explicitly marked or named male / gulf
                    val maleVoice = arabicVoices.find { voice ->
                        val nameLower = voice.name.lowercase(Locale.ROOT)
                        nameLower.contains("male") ||
                        nameLower.contains("man") ||
                        nameLower.contains("boy") ||
                        nameLower.contains("masculine") ||
                        (nameLower.contains("ar-") && !nameLower.contains("female") && !nameLower.contains("woman"))
                    } ?: arabicVoices.find { it.locale.country.equals("SA", ignoreCase = true) }
                      ?: arabicVoices.firstOrNull()

                    if (maleVoice != null) {
                        tts?.voice = maleVoice
                    }
                }
            } catch (e: Exception) {
                Log.w("AiEncouragementSpeaker", "Voice selection exception: ${e.message}")
            }

            // Set audio attributes
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(audioAttributes)
            } catch (e: Exception) {
                Log.w("AiEncouragementSpeaker", "AudioAttributes exception: ${e.message}")
            }

            // Set masculine voice acoustic characteristics:
            // Pitch 0.78f - 0.82f shifts vocal resonance down into a confident, deep masculine tone
            tts?.setPitch(0.80f)
            // Speech rate 1.0f: clear, natural and articulate Gulf cadence
            tts?.setSpeechRate(1.02f)

            // Setup UtteranceProgressListener so clapping can play right after speech finishes
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    onSpeechFinishedCallback?.invoke()
                    onSpeechFinishedCallback = null
                }

                override fun onError(utteranceId: String?) {
                    onSpeechFinishedCallback?.invoke()
                    onSpeechFinishedCallback = null
                }
            })

            isInitialized = true

            // Speak any phrase requested while initializing
            pendingPhrase?.let {
                speak(it, pendingCallback)
                pendingPhrase = null
                pendingCallback = null
            }
        } else {
            isInitialized = false
        }
    }

    fun getRandomPhrase(): String {
        return ENCOURAGEMENT_PHRASES[random.nextInt(ENCOURAGEMENT_PHRASES.size)]
    }

    /**
     * Speaks the phrase and invokes [onComplete] when speech finishes
     * (or immediately if TTS is unavailable).
     */
    fun speak(phrase: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) {
            pendingPhrase = phrase
            pendingCallback = onComplete
            return
        }

        onSpeechFinishedCallback = onComplete
        try {
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            val utteranceId = "GULF_MALE_AI_${System.currentTimeMillis()}"
            val res = tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            if (res != TextToSpeech.SUCCESS) {
                onSpeechFinishedCallback?.invoke()
                onSpeechFinishedCallback = null
            }
        } catch (e: Exception) {
            Log.e("AiEncouragementSpeaker", "Error speaking phrase: ${e.message}")
            onSpeechFinishedCallback?.invoke()
            onSpeechFinishedCallback = null
        }
    }

    fun shutdown() {
        try {
            onSpeechFinishedCallback = null
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
