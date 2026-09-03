package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural synthesizer for pure, authentic applause and handclapping sounds (صوت تصفيق حقيقي)
 * using Android AudioTrack without any external audio files or musical chime overlays.
 */
object ClappingSoundSynthesizer {

    private const val SAMPLE_RATE = 44100

    /**
     * Plays an enthusiastic clapping / applause sound effect (تصفيق حار للأيدي).
     */
    suspend fun playApplauseSound() = withContext(Dispatchers.Default) {
        try {
            val pcmData = generatePureClappingPcm()
            playPcm(pcmData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Generates a pure, realistic multi-person audience handclap / applause (صوت تصفيق واقعي).
     * Simulates multiple individuals clapping hands energetically with natural timing variations,
     * palm thumps, and acoustic fleshy snaps, with zero musical instruments or synthesizer chords.
     */
    private fun generatePureClappingPcm(): ShortArray {
        val totalDurationMs = 1350
        val totalSamples = (SAMPLE_RATE * (totalDurationMs / 1000.0)).toInt()
        val buffer = FloatArray(totalSamples)
        val random = Random(55)

        // Simulate 5 enthusiastic clappers in a room, each with their own clapping cadence
        val clapperDelays = intArrayOf(0, 45, 90, 140, 210)
        val clapperIntervals = intArrayOf(200, 220, 190, 210, 230) // Clapping interval in ms
        val clapperClapCounts = intArrayOf(6, 6, 6, 5, 5)

        for (c in clapperDelays.indices) {
            var currentMs = clapperDelays[c]
            val interval = clapperIntervals[c]
            val clapCount = clapperClapCounts[c]

            for (k in 0 until clapCount) {
                // Add natural human jitter to timing (+/- 18ms)
                val jitter = (random.nextFloat() - 0.5f) * 36f
                val clapTimeMs = (currentMs + jitter).toInt().coerceAtLeast(0)
                currentMs += interval

                val startSample = (SAMPLE_RATE * (clapTimeMs / 1000.0)).toInt()
                if (startSample >= totalSamples) break

                // Handclap acoustic characteristics
                val clapDurationMs = 32
                val clapSamples = (SAMPLE_RATE * (clapDurationMs / 1000.0)).toInt()
                val clapIntensity = 0.65f + random.nextFloat() * 0.35f

                // Resonant thump frequency between 140Hz - 190Hz (cupped palm cavity)
                val thumpFreq = 140.0 + random.nextDouble() * 50.0

                var filterState1 = 0f
                var filterState2 = 0f

                for (i in 0 until clapSamples) {
                    val targetIndex = startSample + i
                    if (targetIndex >= totalSamples) break

                    val t = i.toFloat() / SAMPLE_RATE
                    // Fast exponential decay envelope
                    val env = exp(-t * 95.0).toFloat()

                    // Low palm cavity resonance (thump)
                    val thump = sin(2.0 * PI * thumpFreq * t).toFloat() * 0.4f

                    // Fleshy slap snap (band-passed noise)
                    val whiteNoise = random.nextFloat() * 2f - 1f
                    filterState1 = 0.55f * filterState1 + 0.45f * whiteNoise
                    filterState2 = 0.6f * filterState2 + 0.4f * (whiteNoise - filterState1)
                    val snap = filterState2

                    // Composite realistic handclap: snap + thump
                    val sampleVal = (snap * 0.78f + thump * 0.22f) * env * clapIntensity
                    buffer[targetIndex] += sampleVal
                }
            }
        }

        // Normalize buffer and convert to 16-bit PCM
        var maxAbs = 0.001f
        for (sample in buffer) {
            val abs = kotlin.math.abs(sample)
            if (abs > maxAbs) maxAbs = abs
        }

        val outShorts = ShortArray(totalSamples)
        val scale = 30500.0f / maxAbs
        for (i in 0 until totalSamples) {
            val scaled = buffer[i] * scale
            val clamped = scaled.coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat())
            outShorts[i] = clamped.toInt().toShort()
        }

        return outShorts
    }

    private fun playPcm(pcmData: ShortArray) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, pcmData.size * 2)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.write(pcmData, 0, pcmData.size)
            audioTrack.play()
            val playDurationMs = (pcmData.size * 1000L) / SAMPLE_RATE
            Thread.sleep(playDurationMs + 40)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
