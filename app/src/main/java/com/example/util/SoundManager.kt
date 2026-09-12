package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural Synthesized Sound Effects for Android.
 * Generates crisp, realistic UI sounds in real-time without needing external mp3/wav files!
 * - Click Sound: Crisp wooden/mechanical tap
 * - Enter Sound: Harmonic welcoming chime
 * - Save Sound: Positive confirmation double-tone
 * - Money / Taka Joma Sound: Rich metallic cash coin clinking chime
 */
object SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 44100

    /**
     * Crisp button click tap sound
     */
    fun playClick() {
        scope.launch {
            try {
                val durationMs = 35
                val numSamples = (SAMPLE_RATE * durationMs / 1000)
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = 800.0 - (t * 600.0 / (durationMs / 1000.0))
                    val decay = exp(-i.toDouble() / (SAMPLE_RATE * 0.008))
                    val sample = sin(2 * PI * freq * t) * decay
                    buffer[i] = (sample * Short.MAX_VALUE * 0.35).toInt().toShort()
                }
                playRawPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    /**
     * Welcoming entrance swell / chime when entering the dashboard
     */
    fun playEnter() {
        scope.launch {
            try {
                val durationMs = 280
                val numSamples = (SAMPLE_RATE * durationMs / 1000)
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val decay = exp(-i.toDouble() / (SAMPLE_RATE * 0.08))
                    // Two ascending harmonic chimes
                    val tone1 = sin(2 * PI * 523.25 * t) // C5
                    val tone2 = sin(2 * PI * 659.25 * t) // E5
                    val tone3 = sin(2 * PI * 783.99 * t) // G5
                    val sample = ((tone1 + tone2 + tone3) / 3.0) * decay
                    buffer[i] = (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
                }
                playRawPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    /**
     * Success / Save confirmation chime
     */
    fun playSave() {
        scope.launch {
            try {
                val durationMs = 220
                val numSamples = (SAMPLE_RATE * durationMs / 1000)
                val buffer = ShortArray(numSamples)
                val half = numSamples / 2
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = if (i < half) 587.33 else 880.0 // D5 -> A5
                    val localIndex = if (i < half) i else (i - half)
                    val decay = exp(-localIndex.toDouble() / (SAMPLE_RATE * 0.05))
                    val sample = sin(2 * PI * freq * t) * decay
                    buffer[i] = (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
                }
                playRawPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    /**
     * Cash register / Coin drop chime for "টাকা জমা" (Taka Joma)
     * Rich metallic ringing coin resonance
     */
    fun playMoneyReceived() {
        scope.launch {
            try {
                val durationMs = 450
                val numSamples = (SAMPLE_RATE * durationMs / 1000)
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    // Coin 1 strike at t=0 (High metallic frequencies 2400Hz, 3200Hz, 4800Hz)
                    val decay1 = exp(-i.toDouble() / (SAMPLE_RATE * 0.09))
                    val coin1 = (sin(2 * PI * 2489.0 * t) * 0.5 + sin(2 * PI * 3322.0 * t) * 0.4 + sin(2 * PI * 4978.0 * t) * 0.2) * decay1

                    // Coin 2 strike slightly delayed (at 60ms) for double-clink
                    val delaySamples = (SAMPLE_RATE * 0.06).toInt()
                    val coin2 = if (i > delaySamples) {
                        val i2 = i - delaySamples
                        val t2 = i2.toDouble() / SAMPLE_RATE
                        val decay2 = exp(-i2.toDouble() / (SAMPLE_RATE * 0.12))
                        (sin(2 * PI * 2793.0 * t2) * 0.6 + sin(2 * PI * 3729.0 * t2) * 0.4) * decay2
                    } else 0.0

                    // Cash register bell ding at 120ms
                    val bellDelay = (SAMPLE_RATE * 0.12).toInt()
                    val bell = if (i > bellDelay) {
                        val i3 = i - bellDelay
                        val t3 = i3.toDouble() / SAMPLE_RATE
                        val decay3 = exp(-i3.toDouble() / (SAMPLE_RATE * 0.15))
                        (sin(2 * PI * 1760.0 * t3) * 0.4 + sin(2 * PI * 3520.0 * t3) * 0.3) * decay3
                    } else 0.0

                    val combined = (coin1 + coin2 + bell).coerceIn(-1.0, 1.0)
                    buffer[i] = (combined * Short.MAX_VALUE * 0.55).toInt().toShort()
                }
                playRawPcm(buffer)
            } catch (_: Exception) {}
        }
    }

    private fun playRawPcm(pcmData: ShortArray) {
        try {
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
                .setBufferSizeInBytes(pcmData.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcmData, 0, pcmData.size)
            audioTrack.play()
            audioTrack.setNotificationMarkerPosition(pcmData.size)
            audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onPeriodicNotification(track: AudioTrack?) {}
                override fun onMarkerReached(track: AudioTrack?) {
                    track?.release()
                }
            })
        } catch (_: Exception) {}
    }
}
