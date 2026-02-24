package com.theveloper.pixelplay.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.theveloper.pixelplay.presentation.screens.PlayerScreen
import com.theveloper.pixelplay.presentation.screens.VolumeScreen

/**
 * Navigation host for the Wear OS app.
 * Phase 1: Player (main) and Volume screens.
 * Phase 2 will add: Browse, Playlists, Albums, Artists screens.
 */
object WearScreens {
    const val PLAYER = "player"
    const val VOLUME = "volume"
}

@Composable
fun WearNavigation() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = WearScreens.PLAYER,
    ) {
        composable(WearScreens.PLAYER) {
            PlayerScreen()
        }

        composable(WearScreens.VOLUME) {
            VolumeScreen()
        }
    }
}
