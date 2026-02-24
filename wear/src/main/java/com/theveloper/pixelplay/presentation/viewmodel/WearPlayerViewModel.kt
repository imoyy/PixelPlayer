package com.theveloper.pixelplay.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.theveloper.pixelplay.data.WearPlaybackController
import com.theveloper.pixelplay.data.WearStateRepository
import com.theveloper.pixelplay.shared.WearPlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for the Wear player screen.
 * Observes the player state from WearStateRepository and dispatches
 * playback commands via WearPlaybackController.
 */
@HiltViewModel
class WearPlayerViewModel @Inject constructor(
    private val stateRepository: WearStateRepository,
    private val playbackController: WearPlaybackController,
) : ViewModel() {

    val playerState: StateFlow<WearPlayerState> = stateRepository.playerState
    val albumArt: StateFlow<Bitmap?> = stateRepository.albumArt
    val isPhoneConnected: StateFlow<Boolean> = stateRepository.isPhoneConnected

    fun togglePlayPause() {
        val current = playerState.value
        stateRepository.updatePlayerState(
            current.copy(isPlaying = !current.isPlaying)
        )
        playbackController.togglePlayPause()
    }

    fun next() = playbackController.next()
    fun previous() = playbackController.previous()

    fun toggleFavorite() {
        val current = playerState.value
        playbackController.toggleFavorite(targetEnabled = !current.isFavorite)
    }

    fun toggleShuffle() {
        playbackController.toggleShuffle()
    }

    fun cycleRepeat() {
        playbackController.cycleRepeat()
    }

    fun volumeUp() = playbackController.volumeUp()
    fun volumeDown() = playbackController.volumeDown()
}
