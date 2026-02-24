package com.theveloper.pixelplay.data.service.wear

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.common.util.concurrent.ListenableFuture
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.content.ContextCompat
import com.theveloper.pixelplay.data.service.MusicService
import com.theveloper.pixelplay.shared.WearDataPaths
import com.theveloper.pixelplay.shared.WearPlaybackCommand
import com.theveloper.pixelplay.shared.WearVolumeCommand
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * WearableListenerService that receives commands from the Wear OS watch app.
 * Handles playback commands (play, pause, next, prev, etc.) and volume commands.
 *
 * Commands are received via the Wear Data Layer MessageClient and forwarded
 * to the MusicService via MediaController.
 */
@AndroidEntryPoint
class WearCommandReceiver : WearableListenerService() {

    private val json = Json { ignoreUnknownKeys = true }
    private var mediaController: MediaController? = null
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    companion object {
        private const val TAG = "WearCommandReceiver"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Timber.tag(TAG).d("Received message on path: ${messageEvent.path}")

        when (messageEvent.path) {
            WearDataPaths.PLAYBACK_COMMAND -> handlePlaybackCommand(messageEvent)
            WearDataPaths.VOLUME_COMMAND -> handleVolumeCommand(messageEvent)
            else -> Timber.tag(TAG).w("Unknown message path: ${messageEvent.path}")
        }
    }

    private fun handlePlaybackCommand(messageEvent: MessageEvent) {
        val commandJson = String(messageEvent.data, Charsets.UTF_8)
        val command = try {
            json.decodeFromString<WearPlaybackCommand>(commandJson)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to parse playback command")
            return
        }

        Timber.tag(TAG).d("Playback command: ${command.action}")

        getOrBuildMediaController { controller ->
            when (command.action) {
                WearPlaybackCommand.PLAY -> controller.play()
                WearPlaybackCommand.PAUSE -> controller.pause()
                WearPlaybackCommand.TOGGLE_PLAY_PAUSE -> {
                    if (controller.isPlaying) controller.pause() else controller.play()
                }
                WearPlaybackCommand.NEXT -> controller.seekToNext()
                WearPlaybackCommand.PREVIOUS -> controller.seekToPrevious()
                WearPlaybackCommand.TOGGLE_SHUFFLE -> {
                    controller.shuffleModeEnabled = !controller.shuffleModeEnabled
                }
                WearPlaybackCommand.CYCLE_REPEAT -> {
                    val newMode = when (controller.repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_OFF ->
                            androidx.media3.common.Player.REPEAT_MODE_ONE
                        androidx.media3.common.Player.REPEAT_MODE_ONE ->
                            androidx.media3.common.Player.REPEAT_MODE_ALL
                        else -> androidx.media3.common.Player.REPEAT_MODE_OFF
                    }
                    controller.repeatMode = newMode
                }
                WearPlaybackCommand.TOGGLE_FAVORITE -> {
                    // Favorite toggling is handled via custom session command
                    val sessionCommand = androidx.media3.session.SessionCommand(
                        "com.theveloper.pixelplay.LIKE",
                        android.os.Bundle.EMPTY
                    )
                    controller.sendCustomCommand(sessionCommand, android.os.Bundle.EMPTY)
                }
                else -> Timber.tag(TAG).w("Unknown playback action: ${command.action}")
            }
        }
    }

    private fun handleVolumeCommand(messageEvent: MessageEvent) {
        val commandJson = String(messageEvent.data, Charsets.UTF_8)
        val command = try {
            json.decodeFromString<WearVolumeCommand>(commandJson)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to parse volume command")
            return
        }

        Timber.tag(TAG).d("Volume command: direction=${command.direction}, value=${command.value}")

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val absoluteValue = command.value
        if (absoluteValue != null) {
            // Set absolute volume (scaled from 0-100 to device range)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (absoluteValue * maxVolume / 100).coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        } else {
            when (command.direction) {
                WearVolumeCommand.UP -> audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    0
                )
                WearVolumeCommand.DOWN -> audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    0
                )
            }
        }
    }

    /**
     * Get existing MediaController or build a new one, then execute the action.
     */
    private fun getOrBuildMediaController(action: (MediaController) -> Unit) {
        runOnMainThread {
            val existing = mediaController
            if (existing != null && existing.isConnected) {
                action(existing)
                return@runOnMainThread
            }

            val inFlight = mediaControllerFuture
            if (inFlight != null && !inFlight.isDone) {
                inFlight.addListener(
                    {
                        try {
                            action(inFlight.get())
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Failed to reuse pending MediaController")
                        }
                    },
                    ContextCompat.getMainExecutor(this)
                )
                return@runOnMainThread
            }

            val sessionToken = SessionToken(
                this,
                ComponentName(this, MusicService::class.java)
            )
            val future = MediaController.Builder(this, sessionToken)
                .setApplicationLooper(Looper.getMainLooper())
                .buildAsync()
            mediaControllerFuture = future
            future.addListener(
                {
                    try {
                        val controller = future.get()
                        mediaController = controller
                        action(controller)
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Failed to build MediaController")
                    }
                },
                ContextCompat.getMainExecutor(this)
            )
        }
    }

    private fun runOnMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    private fun releaseController() {
        val controller = mediaController
        mediaController = null
        mediaControllerFuture = null
        if (controller != null) {
            try {
                controller.release()
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Failed to release MediaController")
            }
        }
    }

    override fun onDestroy() {
        runOnMainThread { releaseController() }
        super.onDestroy()
    }
}
