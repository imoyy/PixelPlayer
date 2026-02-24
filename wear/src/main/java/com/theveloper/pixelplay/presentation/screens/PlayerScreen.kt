package com.theveloper.pixelplay.presentation.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.MusicNote
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.theveloper.pixelplay.presentation.viewmodel.WearPlayerViewModel
import com.theveloper.pixelplay.shared.WearPlayerState

@Composable
fun PlayerScreen(
    viewModel: WearPlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.playerState.collectAsState()
    val albumArt by viewModel.albumArt.collectAsState()
    val isPhoneConnected by viewModel.isPhoneConnected.collectAsState()

    PlayerContent(
        state = state,
        albumArt = albumArt,
        isPhoneConnected = isPhoneConnected,
        onTogglePlayPause = viewModel::togglePlayPause,
        onNext = viewModel::next,
        onPrevious = viewModel::previous,
        onToggleFavorite = viewModel::toggleFavorite,
        onToggleShuffle = viewModel::toggleShuffle,
        onCycleRepeat = viewModel::cycleRepeat,
    )
}

@Composable
private fun PlayerContent(
    state: WearPlayerState,
    albumArt: Bitmap?,
    isPhoneConnected: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
) {
    val columnState = rememberResponsiveColumnState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        // Album Art
        item {
            AlbumArtSection(albumArt = albumArt)
        }

        // Track Info
        item {
            TrackInfoSection(
                state = state,
                isPhoneConnected = isPhoneConnected,
            )
        }

        // Main Controls (prev, play/pause, next)
        item {
            MainControlsRow(
                isPlaying = state.isPlaying,
                isEmpty = state.isEmpty,
                enabled = isPhoneConnected,
                onTogglePlayPause = onTogglePlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
            )
        }

        // Secondary Controls (favorite, shuffle, repeat)
        item {
            SecondaryControlsRow(
                isFavorite = state.isFavorite,
                isShuffleEnabled = state.isShuffleEnabled,
                repeatMode = state.repeatMode,
                enabled = isPhoneConnected,
                onToggleFavorite = onToggleFavorite,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
            )
        }
    }
}

@Composable
private fun AlbumArtSection(albumArt: Bitmap?) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colors.surface),
        contentAlignment = Alignment.Center,
    ) {
        if (albumArt != null) {
            Image(
                bitmap = albumArt.asImageBitmap(),
                contentDescription = "Album art",
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "No album art",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun TrackInfoSection(
    state: WearPlayerState,
    isPhoneConnected: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.songTitle.ifEmpty { "Not Playing" },
            style = MaterialTheme.typography.title3,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.artistName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = state.artistName,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (!isPhoneConnected) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Phone disconnected",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.error,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MainControlsRow(
    isPlaying: Boolean,
    isEmpty: Boolean,
    enabled: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous
        Button(
            onClick = onPrevious,
            enabled = enabled,
            modifier = Modifier.size(ButtonDefaults.SmallButtonSize),
            colors = ButtonDefaults.secondaryButtonColors(),
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
            )
        }

        // Play/Pause (larger)
        Button(
            onClick = onTogglePlayPause,
            enabled = enabled && !isEmpty,
            modifier = Modifier.size(ButtonDefaults.LargeButtonSize),
            colors = ButtonDefaults.primaryButtonColors(),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(30.dp),
            )
        }

        // Next
        Button(
            onClick = onNext,
            enabled = enabled,
            modifier = Modifier.size(ButtonDefaults.SmallButtonSize),
            colors = ButtonDefaults.secondaryButtonColors(),
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
            )
        }
    }
}

@Composable
private fun SecondaryControlsRow(
    isFavorite: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    enabled: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Favorite
        CompactButton(
            onClick = onToggleFavorite,
            enabled = enabled,
            colors = ButtonDefaults.secondaryButtonColors(),
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Toggle favorite",
                tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colors.onSurface,
            )
        }

        // Shuffle
        CompactButton(
            onClick = onToggleShuffle,
            enabled = enabled,
            colors = ButtonDefaults.secondaryButtonColors(),
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Toggle shuffle",
                tint = if (isShuffleEnabled) MaterialTheme.colors.primary
                else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            )
        }

        // Repeat
        CompactButton(
            onClick = onCycleRepeat,
            enabled = enabled,
            colors = ButtonDefaults.secondaryButtonColors(),
        ) {
            Icon(
                imageVector = when (repeatMode) {
                    1 -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                },
                contentDescription = "Cycle repeat mode",
                tint = if (repeatMode != 0) MaterialTheme.colors.primary
                else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}
