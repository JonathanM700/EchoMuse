package com.example.echomuse.data

import androidx.compose.ui.graphics.Color
import com.example.echomuse.model.EqualizerPreset
import com.example.echomuse.model.Playlist
import com.example.echomuse.model.Song

class MusicRepository {

    val sampleSongs: List<Song> = listOf(
        Song(
            id = "s1",
            title = "Midnight Neon Drive",
            artist = "Echo Muse",
            album = "Synthwave Memories",
            durationMs = 214000L,
            genre = "Synthwave",
            isFavorite = true,
            primaryColor = Color(0xFF8E24AA),
            secondaryColor = Color(0xFF00E5FF),
            lyrics = "[00:15] Cruising through the neon city lights...\n[00:30] Electric pulse underneath the midnight skies...\n[01:05] Echoes of synthesizer in the atmosphere...\n[01:40] Driving into tomorrow without any fear."
        ),
        Song(
            id = "s2",
            title = "Velvet Coffee & Rain",
            artist = "Lo-Fi Beats Co.",
            album = "Midnight Study Sessions",
            durationMs = 185000L,
            genre = "Lo-Fi",
            isFavorite = false,
            primaryColor = Color(0xFF3E2723),
            secondaryColor = Color(0xFFFFB74D),
            lyrics = "[00:10] Raindrops tapping gently against the window pane...\n[00:45] Soft jazz chords playing on a vinyl record...\n[01:20] Warm coffee steam drifting into the calm night."
        ),
        Song(
            id = "s3",
            title = "Starlight Horizon",
            artist = "Aura Wave",
            album = "Cosmic Journey",
            durationMs = 248000L,
            genre = "Electronic",
            isFavorite = true,
            primaryColor = Color(0xFF1A237E),
            secondaryColor = Color(0xFF7C4DFF),
            lyrics = "[00:20] Floating through stellar clouds...\n[01:00] Horizons glowing with celestial starlight...\n[01:50] Lost in the vast ocean of sound."
        ),
        Song(
            id = "s4",
            title = "Acoustic Sunsets",
            artist = "Oliver Rivers",
            album = "Golden Hour Whispers",
            durationMs = 192000L,
            genre = "Acoustic",
            isFavorite = false,
            primaryColor = Color(0xFFE65100),
            secondaryColor = Color(0xFFFFD54F),
            lyrics = "[00:12] Soft strumming on a wooden guitar...\n[00:50] Watching the sun dip below the ocean horizon...\n[01:30] Peace found in simple acoustic melodies."
        ),
        Song(
            id = "s5",
            title = "Cyber Pulse",
            artist = "Hyperion 9000",
            album = "Grid Runner 2099",
            durationMs = 226000L,
            genre = "Synthwave",
            isFavorite = true,
            primaryColor = Color(0xFFD500F9),
            secondaryColor = Color(0xFF00B0FF),
            lyrics = "[00:18] Neon grids flashing in high resolution...\n[01:02] Cybernetic rhythms powering the grid...\n[01:45] Resonance accelerating through quantum channels."
        ),
        Song(
            id = "s6",
            title = "Chill Lofi Resonance",
            artist = "Nostalgia Sound",
            album = "Rainy Afternoon",
            durationMs = 168000L,
            genre = "Lo-Fi",
            isFavorite = false,
            primaryColor = Color(0xFF004D40),
            secondaryColor = Color(0xFF80CBC4),
            lyrics = "[00:08] Soft tape hiss and cozy lo-fi beats...\n[00:40] Perfect soundtrack for quiet contemplation..."
        ),
        Song(
            id = "s7",
            title = "Electric Symphony",
            artist = "Echo Muse",
            album = "Synthwave Memories",
            durationMs = 260000L,
            genre = "Electronic",
            isFavorite = false,
            primaryColor = Color(0xFF311B92),
            secondaryColor = Color(0xFFEA80FC),
            lyrics = "[00:22] Synthesizer strings rising into a sonic crescendo...\n[01:15] Harmony between digital and analog soundwaves."
        )
    )

    val samplePlaylists: List<Playlist> = listOf(
        Playlist(
            id = "p1",
            name = "Late Night Focus & Study",
            description = "Relaxing Lo-Fi and ambient chill tracks for productivity.",
            songs = listOf(sampleSongs[1], sampleSongs[5], sampleSongs[3]),
            primaryColor = Color(0xFF3E2723),
            secondaryColor = Color(0xFFFFB74D)
        ),
        Playlist(
            id = "p2",
            name = "Synthwave Highway",
            description = "High energy neon synth beats for late night drives.",
            songs = listOf(sampleSongs[0], sampleSongs[4], sampleSongs[6]),
            primaryColor = Color(0xFF8E24AA),
            secondaryColor = Color(0xFF00E5FF)
        ),
        Playlist(
            id = "p3",
            name = "Cosmic Chillout",
            description = "Deep electronic and atmospheric space sounds.",
            songs = listOf(sampleSongs[2], sampleSongs[6], sampleSongs[0]),
            primaryColor = Color(0xFF1A237E),
            secondaryColor = Color(0xFF7C4DFF)
        )
    )

    val equalizerPresets: List<EqualizerPreset> = listOf(
        EqualizerPreset("eq_flat", "Flat", 0f, 0f, 0f, 0f, 0f, 0.0f),
        EqualizerPreset("eq_bass", "Bass Boost", 6f, 4f, 1f, 0f, 1f, 0.8f),
        EqualizerPreset("eq_pop", "Pop", 2f, 4f, 5f, 3f, 1f, 0.3f),
        EqualizerPreset("eq_rock", "Rock", 5f, 3f, -1f, 3f, 5f, 0.5f),
        EqualizerPreset("eq_jazz", "Jazz", 3f, 2f, -2f, 2f, 4f, 0.2f),
        EqualizerPreset("eq_classical", "Classical", 4f, 3f, 2f, 3f, 4f, 0.1f)
    )
}
