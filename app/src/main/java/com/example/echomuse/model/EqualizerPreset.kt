package com.example.echomuse.model

data class EqualizerPreset(
    val id: String,
    val name: String,
    val band60Hz: Float,    // -10dB to +10dB
    val band230Hz: Float,
    val band910Hz: Float,
    val band3600Hz: Float,
    val band14000Hz: Float,
    val bassBoost: Float    // 0.0 to 1.0
)
