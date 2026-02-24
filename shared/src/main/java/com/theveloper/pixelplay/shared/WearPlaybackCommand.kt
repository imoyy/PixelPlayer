package com.theveloper.pixelplay.shared

import kotlinx.serialization.Serializable

/**
 * Command sent from the watch to the phone to control playback.
 * Serialized to JSON and sent via MessageClient.
 */
@Serializable
data class WearPlaybackCommand(
    val action: String,
    /** Optional song ID for PLAY_ITEM action */
    val songId: String? = null,
    /** Optional target state for idempotent toggle actions (favorite/shuffle). */
    val targetEnabled: Boolean? = null,
) {
    companion object {
        const val PLAY = "play"
        const val PAUSE = "pause"
        const val TOGGLE_PLAY_PAUSE = "toggle_play_pause"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val TOGGLE_FAVORITE = "toggle_favorite"
        const val TOGGLE_SHUFFLE = "toggle_shuffle"
        const val CYCLE_REPEAT = "cycle_repeat"
        /** Play a specific song by ID */
        const val PLAY_ITEM = "play_item"
    }
}
