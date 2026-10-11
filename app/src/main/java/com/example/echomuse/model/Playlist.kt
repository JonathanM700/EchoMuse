package com.example.echomuse.model

import androidx.compose.ui.graphics.Color

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val songs: List<Song> = emptyList(),
    val primaryColor: Color = Color(0xFF651FFF),
    val secondaryColor: Color = Color(0xFF00B0FF)
) {
    val trackCount: Int
        get() = songs.size

    val totalDurationFormatted: String
        get() {
            val totalMs = songs.sumOf { it.durationMs }
            val minutes = totalMs / 1000 / 60
            return "$minutes mins"
        }
}
