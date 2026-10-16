package com.example.echomuse.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.sin

class AudioEngine {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var currentFrequency = 440f
    private var currentVolume = 1.0f
    private var playbackSpeed = 1.0f

    fun startSynthTone(frequency: Float, speed: Float = 1.0f) {
        stopSynthTone()
        currentFrequency = frequency
        playbackSpeed = speed
        isPlaying = true

        thread {
            try {
                val sampleRate = 44100
                val buffSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                val samples = ShortArray(buffSize)
                var angle = 0.0

                while (isPlaying) {
                    val effectiveFreq = currentFrequency * playbackSpeed
                    val angularFrequency = 2.0 * Math.PI * effectiveFreq / sampleRate

                    for (i in samples.indices) {
                        // Ambient synth tone with soft harmonic warmth
                        val sine1 = sin(angle)
                        val sine2 = 0.3 * sin(angle * 1.5)
                        val combined = (sine1 + sine2) * 0.4 * currentVolume
                        samples[i] = (combined * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        angle += angularFrequency
                        if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                    }

                    audioTrack?.write(samples, 0, samples.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
    }

    fun setSpeed(speed: Float) {
        playbackSpeed = speed
    }

    fun stopSynthTone() {
        isPlaying = false
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
