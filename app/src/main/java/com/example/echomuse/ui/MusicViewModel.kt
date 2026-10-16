package com.example.echomuse.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echomuse.data.AudioEngine
import com.example.echomuse.data.MusicRepository
import com.example.echomuse.model.EqualizerPreset
import com.example.echomuse.model.Playlist
import com.example.echomuse.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NavigationTab {
    HOME,
    LIBRARY,
    PLAYLISTS,
    FAVORITES
}

enum class VisualizerTheme(
    val themeName: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    NEON_SYNTH("Neon Synth", Color(0xFF00E5FF), Color(0xFF9D4EDD)),
    CYBERPUNK("Cyberpunk", Color(0xFFFF007F), Color(0xFF00F0FF)),
    GOLDEN_SUNSET("Golden Sunset", Color(0xFFFF9100), Color(0xFFFFD600)),
    EMERALD_FOREST("Emerald Forest", Color(0xFF00E676), Color(0xFF1DE9B6))
}

data class MusicUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val playbackProgressMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val stereoBalance: Float = 0.0f, // -1.0 Left to +1.0 Right
    val visualizerTheme: VisualizerTheme = VisualizerTheme.NEON_SYNTH,
    val isShuffleEnabled: Boolean = false,
    val isRepeatEnabled: Boolean = false,
    val searchQuery: String = "",
    val selectedGenre: String = "All",
    val activeTab: NavigationTab = NavigationTab.HOME,
    val allSongs: List<Song> = emptyList(),
    val userPlaylists: List<Playlist> = emptyList(),
    val favoriteSongIds: Set<String> = emptySet(),
    val downloadedSongIds: Set<String> = emptySet(),
    val playbackQueue: List<Song> = emptyList(),
    val isNowPlayingExpanded: Boolean = false,
    val isEqualizerOpen: Boolean = false,
    val isQueueOpen: Boolean = false,
    val isSleepTimerDialogOpen: Boolean = false,
    val sleepTimerMinutesSelected: Int? = null,
    val sleepTimerSecondsRemaining: Int = 0,
    val currentEqualizerPreset: EqualizerPreset = EqualizerPreset("eq_flat", "Flat", 0f, 0f, 0f, 0f, 0f, 0f),
    val equalizerPresets: List<EqualizerPreset> = emptyList()
) {
    val filteredSongs: List<Song>
        get() = allSongs.filter { song ->
            val matchesGenre = selectedGenre == "All" || song.genre.equals(selectedGenre, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artist.contains(searchQuery, ignoreCase = true) ||
                    song.album.contains(searchQuery, ignoreCase = true)
            matchesGenre && matchesQuery
        }

    val favoriteSongs: List<Song>
        get() = allSongs.filter { favoriteSongIds.contains(it.id) }

    val sleepTimerFormatted: String
        get() {
            if (sleepTimerSecondsRemaining <= 0) return ""
            val mins = sleepTimerSecondsRemaining / 60
            val secs = sleepTimerSecondsRemaining % 60
            return String.format("%d:%02d", mins, secs)
        }
}

class MusicViewModel(
    private val repository: MusicRepository = MusicRepository(),
    private val audioEngine: AudioEngine = AudioEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        val initialSongs = repository.sampleSongs
        val initialPlaylists = repository.samplePlaylists
        val initialFavorites = initialSongs.filter { it.isFavorite }.map { it.id }.toSet()
        val initialDownloads = initialSongs.filter { it.isDownloaded }.map { it.id }.toSet()
        val defaultSong = initialSongs.firstOrNull()

        _uiState.update {
            it.copy(
                allSongs = initialSongs,
                userPlaylists = initialPlaylists,
                favoriteSongIds = initialFavorites,
                downloadedSongIds = initialDownloads,
                playbackQueue = initialSongs,
                currentSong = defaultSong,
                equalizerPresets = repository.equalizerPresets,
                currentEqualizerPreset = repository.equalizerPresets.firstOrNull()
                    ?: EqualizerPreset("eq_flat", "Flat", 0f, 0f, 0f, 0f, 0f, 0f)
            )
        }
    }

    fun playTrack(song: Song) {
        if (_uiState.value.currentSong?.id == song.id) {
            togglePlayPause()
            return
        }
        _uiState.update {
            it.copy(
                currentSong = song,
                playbackProgressMs = 0L,
                isPlaying = true
            )
        }
        audioEngine.startSynthTone(song.audioFrequency, _uiState.value.playbackSpeed)
        startPlaybackTimer()
    }

    fun togglePlayPause() {
        val currentlyPlaying = _uiState.value.isPlaying
        val currentSong = _uiState.value.currentSong
        _uiState.update { it.copy(isPlaying = !currentlyPlaying) }

        if (!currentlyPlaying) {
            if (currentSong != null) {
                audioEngine.startSynthTone(currentSong.audioFrequency, _uiState.value.playbackSpeed)
            }
            startPlaybackTimer()
        } else {
            audioEngine.stopSynthTone()
            playbackJob?.cancel()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
        audioEngine.setSpeed(speed)
    }

    fun setStereoBalance(balance: Float) {
        _uiState.update { it.copy(stereoBalance = balance) }
        audioEngine.setStereoBalance(balance)
    }

    fun setVisualizerTheme(theme: VisualizerTheme) {
        _uiState.update { it.copy(visualizerTheme = theme) }
    }

    fun seekTo(progressMs: Long) {
        val maxDuration = _uiState.value.currentSong?.durationMs ?: 0L
        val clampedProgress = progressMs.coerceIn(0L, maxDuration)
        _uiState.update { it.copy(playbackProgressMs = clampedProgress) }
    }

    fun skipNext() {
        val state = _uiState.value
        val queue = if (state.playbackQueue.isNotEmpty()) state.playbackQueue else state.allSongs
        if (queue.isEmpty()) return

        val currentIndex = queue.indexOfFirst { it.id == state.currentSong?.id }
        val nextIndex = if (state.isShuffleEnabled) {
            (queue.indices).random()
        } else if (currentIndex != -1 && currentIndex < queue.size - 1) {
            currentIndex + 1
        } else {
            0
        }
        playTrack(queue[nextIndex])
    }

    fun skipPrevious() {
        val state = _uiState.value
        val queue = if (state.playbackQueue.isNotEmpty()) state.playbackQueue else state.allSongs
        if (queue.isEmpty()) return

        val currentIndex = queue.indexOfFirst { it.id == state.currentSong?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else queue.size - 1
        playTrack(queue[prevIndex])
    }

    fun toggleShuffle() {
        _uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
    }

    fun toggleRepeat() {
        _uiState.update { it.copy(isRepeatEnabled = !it.isRepeatEnabled) }
    }

    fun toggleFavorite(songId: String) {
        _uiState.update { state ->
            val updatedFavorites = state.favoriteSongIds.toMutableSet()
            if (updatedFavorites.contains(songId)) {
                updatedFavorites.remove(songId)
            } else {
                updatedFavorites.add(songId)
            }
            val updatedSongs = state.allSongs.map { song ->
                if (song.id == songId) song.copy(isFavorite = updatedFavorites.contains(songId)) else song
            }
            val updatedCurrentSong = if (state.currentSong?.id == songId) {
                state.currentSong.copy(isFavorite = updatedFavorites.contains(songId))
            } else {
                state.currentSong
            }
            state.copy(
                favoriteSongIds = updatedFavorites,
                allSongs = updatedSongs,
                currentSong = updatedCurrentSong
            )
        }
    }

    fun toggleDownload(songId: String) {
        _uiState.update { state ->
            val updatedDownloads = state.downloadedSongIds.toMutableSet()
            if (updatedDownloads.contains(songId)) {
                updatedDownloads.remove(songId)
            } else {
                updatedDownloads.add(songId)
            }
            val updatedSongs = state.allSongs.map { song ->
                if (song.id == songId) song.copy(isDownloaded = updatedDownloads.contains(songId)) else song
            }
            val updatedCurrentSong = if (state.currentSong?.id == songId) {
                state.currentSong.copy(isDownloaded = updatedDownloads.contains(songId))
            } else {
                state.currentSong
            }
            state.copy(
                downloadedSongIds = updatedDownloads,
                allSongs = updatedSongs,
                currentSong = updatedCurrentSong
            )
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60
        _uiState.update {
            it.copy(
                sleepTimerMinutesSelected = minutes,
                sleepTimerSecondsRemaining = totalSeconds,
                isSleepTimerDialogOpen = false
            )
        }

        sleepTimerJob = viewModelScope.launch {
            var secondsLeft = totalSeconds
            while (secondsLeft > 0 && _uiState.value.sleepTimerMinutesSelected != null) {
                delay(1000L)
                secondsLeft--
                _uiState.update { it.copy(sleepTimerSecondsRemaining = secondsLeft) }

                // Fade out volume in the last 10 seconds
                if (secondsLeft in 1..10) {
                    val fadeVol = secondsLeft / 10.0f
                    audioEngine.setVolume(fadeVol)
                }
            }

            // Sleep timer reached 0 -> Stop playback
            if (secondsLeft <= 0) {
                cancelSleepTimer()
                audioEngine.stopSynthTone()
                _uiState.update { it.copy(isPlaying = false) }
                playbackJob?.cancel()
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        audioEngine.setVolume(1.0f)
        _uiState.update {
            it.copy(
                sleepTimerMinutesSelected = null,
                sleepTimerSecondsRemaining = 0,
                isSleepTimerDialogOpen = false
            )
        }
    }

    fun removeFromQueue(index: Int) {
        _uiState.update { state ->
            val mutableQueue = state.playbackQueue.toMutableList()
            if (index in mutableQueue.indices) {
                mutableQueue.removeAt(index)
            }
            state.copy(playbackQueue = mutableQueue)
        }
    }

    fun clearQueue() {
        _uiState.update { it.copy(playbackQueue = emptyList()) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedGenre(genre: String) {
        _uiState.update { it.copy(selectedGenre = genre) }
    }

    fun setActiveTab(tab: NavigationTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isNowPlayingExpanded = expanded) }
    }

    fun setEqualizerOpen(open: Boolean) {
        _uiState.update { it.copy(isEqualizerOpen = open) }
    }

    fun setQueueOpen(open: Boolean) {
        _uiState.update { it.copy(isQueueOpen = open) }
    }

    fun setSleepTimerDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isSleepTimerDialogOpen = open) }
    }

    fun selectEqualizerPreset(preset: EqualizerPreset) {
        _uiState.update { it.copy(currentEqualizerPreset = preset) }
    }

    fun createPlaylist(name: String, description: String) {
        if (name.isBlank()) return
        val newPlaylist = Playlist(
            id = "user_p_${System.currentTimeMillis()}",
            name = name,
            description = description,
            songs = _uiState.value.allSongs.take(3)
        )
        _uiState.update { state ->
            state.copy(userPlaylists = state.userPlaylists + newPlaylist)
        }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                val speed = _uiState.value.playbackSpeed.coerceAtLeast(0.1f)
                val delayTime = (1000L / speed).toLong()
                delay(delayTime)

                val current = _uiState.value
                val maxDuration = current.currentSong?.durationMs ?: 0L
                val nextProgress = current.playbackProgressMs + 1000L

                if (nextProgress >= maxDuration && maxDuration > 0L) {
                    if (current.isRepeatEnabled) {
                        _uiState.update { it.copy(playbackProgressMs = 0L) }
                    } else {
                        skipNext()
                    }
                } else {
                    _uiState.update { it.copy(playbackProgressMs = nextProgress) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopSynthTone()
        playbackJob?.cancel()
        sleepTimerJob?.cancel()
    }
}
