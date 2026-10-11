package com.example.echomuse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class MusicUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val playbackProgressMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val isRepeatEnabled: Boolean = false,
    val searchQuery: String = "",
    val selectedGenre: String = "All",
    val activeTab: NavigationTab = NavigationTab.HOME,
    val allSongs: List<Song> = emptyList(),
    val userPlaylists: List<Playlist> = emptyList(),
    val favoriteSongIds: Set<String> = emptySet(),
    val isNowPlayingExpanded: Boolean = false,
    val isEqualizerOpen: Boolean = false,
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
}

class MusicViewModel(
    private val repository: MusicRepository = MusicRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        val initialSongs = repository.sampleSongs
        val initialPlaylists = repository.samplePlaylists
        val initialFavorites = initialSongs.filter { it.isFavorite }.map { it.id }.toSet()
        val defaultSong = initialSongs.firstOrNull()

        _uiState.update {
            it.copy(
                allSongs = initialSongs,
                userPlaylists = initialPlaylists,
                favoriteSongIds = initialFavorites,
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
        startPlaybackTimer()
    }

    fun togglePlayPause() {
        val currentlyPlaying = _uiState.value.isPlaying
        _uiState.update { it.copy(isPlaying = !currentlyPlaying) }
        if (!currentlyPlaying) {
            startPlaybackTimer()
        } else {
            playbackJob?.cancel()
        }
    }

    fun seekTo(progressMs: Long) {
        val maxDuration = _uiState.value.currentSong?.durationMs ?: 0L
        val clampedProgress = progressMs.coerceIn(0L, maxDuration)
        _uiState.update { it.copy(playbackProgressMs = clampedProgress) }
    }

    fun skipNext() {
        val state = _uiState.value
        val songList = if (state.filteredSongs.isNotEmpty()) state.filteredSongs else state.allSongs
        if (songList.isEmpty()) return

        val currentIndex = songList.indexOfFirst { it.id == state.currentSong?.id }
        val nextIndex = if (state.isShuffleEnabled) {
            (songList.indices).random()
        } else if (currentIndex != -1 && currentIndex < songList.size - 1) {
            currentIndex + 1
        } else {
            0
        }
        playTrack(songList[nextIndex])
    }

    fun skipPrevious() {
        val state = _uiState.value
        val songList = if (state.filteredSongs.isNotEmpty()) state.filteredSongs else state.allSongs
        if (songList.isEmpty()) return

        val currentIndex = songList.indexOfFirst { it.id == state.currentSong?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else songList.size - 1
        playTrack(songList[prevIndex])
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
                delay(1000L)
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
        playbackJob?.cancel()
    }
}
