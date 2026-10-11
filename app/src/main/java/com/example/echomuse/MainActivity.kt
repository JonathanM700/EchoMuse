package com.example.echomuse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.echomuse.ui.MusicUiState
import com.example.echomuse.ui.MusicViewModel
import com.example.echomuse.ui.NavigationTab
import com.example.echomuse.ui.components.AudioWaveformVisualizer
import com.example.echomuse.ui.components.MiniPlayerBar
import com.example.echomuse.ui.screens.EqualizerDialog
import com.example.echomuse.ui.screens.FavoritesScreen
import com.example.echomuse.ui.screens.HomeScreen
import com.example.echomuse.ui.screens.LibraryScreen
import com.example.echomuse.ui.screens.NowPlayingSheet
import com.example.echomuse.ui.screens.PlaylistsScreen
import com.example.echomuse.ui.theme.EchoMuseTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EchoMuseTheme {
                val uiState by viewModel.uiState.collectAsState()
                EchoMuseApp(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EchoMuseApp(
    uiState: MusicUiState,
    viewModel: MusicViewModel
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AudioWaveformVisualizer(isPlaying = uiState.isPlaying)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EchoMuse",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEqualizerOpen(true) }) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Equalizer",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column {
                // Persistent Mini Player Overlay
                AnimatedVisibility(
                    visible = uiState.currentSong != null && !uiState.isNowPlayingExpanded,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    MiniPlayerBar(
                        song = uiState.currentSong,
                        isPlaying = uiState.isPlaying,
                        progressMs = uiState.playbackProgressMs,
                        onBarClick = { viewModel.setNowPlayingExpanded(true) },
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onSkipNextClick = { viewModel.skipNext() }
                    )
                }

                // Bottom Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.activeTab == NavigationTab.HOME,
                        onClick = { viewModel.setActiveTab(NavigationTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == NavigationTab.LIBRARY,
                        onClick = { viewModel.setActiveTab(NavigationTab.LIBRARY) },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == NavigationTab.PLAYLISTS,
                        onClick = { viewModel.setActiveTab(NavigationTab.PLAYLISTS) },
                        icon = { Icon(Icons.Default.QueueMusic, contentDescription = "Playlists") },
                        label = { Text("Playlists") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == NavigationTab.FAVORITES,
                        onClick = { viewModel.setActiveTab(NavigationTab.FAVORITES) },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                NavigationTab.HOME -> HomeScreen(
                    uiState = uiState,
                    onSongClick = { viewModel.playTrack(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onGenreSelect = { viewModel.setSelectedGenre(it) }
                )

                NavigationTab.LIBRARY -> LibraryScreen(
                    uiState = uiState,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onSongClick = { viewModel.playTrack(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) }
                )

                NavigationTab.PLAYLISTS -> PlaylistsScreen(
                    uiState = uiState,
                    onCreatePlaylist = { name, desc -> viewModel.createPlaylist(name, desc) },
                    onSongClick = { viewModel.playTrack(it) }
                )

                NavigationTab.FAVORITES -> FavoritesScreen(
                    uiState = uiState,
                    onSongClick = { viewModel.playTrack(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) }
                )
            }
        }
    }

    // Fullscreen Now Playing Modal Sheet
    if (uiState.isNowPlayingExpanded) {
        NowPlayingSheet(
            uiState = uiState,
            onDismiss = { viewModel.setNowPlayingExpanded(false) },
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onSkipNextClick = { viewModel.skipNext() },
            onSkipPreviousClick = { viewModel.skipPrevious() },
            onSeek = { viewModel.seekTo(it) },
            onFavoriteToggle = { viewModel.toggleFavorite(it) },
            onShuffleToggle = { viewModel.toggleShuffle() },
            onRepeatToggle = { viewModel.toggleRepeat() },
            onOpenEqualizer = { viewModel.setEqualizerOpen(true) }
        )
    }

    // Equalizer Dialog
    if (uiState.isEqualizerOpen) {
        EqualizerDialog(
            uiState = uiState,
            onDismiss = { viewModel.setEqualizerOpen(false) },
            onPresetSelect = { viewModel.selectEqualizerPreset(it) }
        )
    }
}
