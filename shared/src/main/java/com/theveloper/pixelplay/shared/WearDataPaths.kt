package com.theveloper.pixelplay.shared

/**
 * Shared constants for Wear Data Layer API paths.
 * Used by both the phone app and the Wear OS app for communication.
 */
object WearDataPaths {
    /** DataItem path for player state (phone -> watch) */
    const val PLAYER_STATE = "/player_state"

    /** Message path for playback commands (watch -> phone) */
    const val PLAYBACK_COMMAND = "/playback_command"

    /** Message path for volume commands (watch -> phone) */
    const val VOLUME_COMMAND = "/volume_command"

    /** Key for the album art Asset within a DataItem */
    const val KEY_ALBUM_ART = "album_art"

    /** Key for the JSON state payload within a DataItem */
    const val KEY_STATE_JSON = "state_json"

    /** Key for timestamp to force DataItem updates */
    const val KEY_TIMESTAMP = "timestamp"
}
