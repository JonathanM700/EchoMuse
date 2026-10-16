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
    private var stereoBalance = 0.0f // -1.0f (Full Left) to +1.0f (Full Right)

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
                    AudioFormat.CHANNEL_OUT_STEREO,
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
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
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

                    val leftGain = if (stereoBalance <= 0f) 1.0f else (1.0f - stereoBalance)
                    val rightGain = if (stereoBalance >= 0f) 1.0f else (1.0f + stereoBalance)

                    var i = 0
                    while (i < samples.size - 1) {
                        val sine1 = sin(angle)
                        val sine2 = 0.3 * sin(angle * 1.5)
                        val combined = (sine1 + sine2) * 0.4 * currentVolume

                        val leftSample = (combined * leftGain * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        val rightSample = (combined * rightGain * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                        samples[i] = leftSample
                        samples[i + 1] = rightSample

                        angle += angularFrequency
                        if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                        i += 2
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

    fun setStereoBalance(balance: Float) {
        stereoBalance = balance.coerceIn(-1.0f, 1.0f)
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
