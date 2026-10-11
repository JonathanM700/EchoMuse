package com.example.echomuse.model

import androidx.compose.ui.graphics.Color

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val genre: String,
    val isFavorite: Boolean = false,
    val primaryColor: Color = Color(0xFF7C4DFF),
    val secondaryColor: Color = Color(0xFF00E5FF),
    val lyrics: String = "Instrumental audio track - Enjoy the rhythm and sound of EchoMuse."
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val secString = if (seconds < 10) "0$seconds" else "$seconds"
            return "$minutes:$secString"
        }
}
