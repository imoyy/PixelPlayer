package com.theveloper.pixelplay.ui.glancewidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.theveloper.pixelplay.MainActivity
import com.theveloper.pixelplay.data.model.PlayerInfo

class GridWidget2x2 : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PlayerInfoStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val playerInfo = currentState<PlayerInfo>()
            GlanceTheme {
                GridWidget2x2Content(playerInfo = playerInfo, context = context)
            }
        }
    }

    @Composable
    private fun GridWidget2x2Content(
        playerInfo: PlayerInfo,
        context: Context
    ) {
        val isPlaying = playerInfo.isPlaying
        val albumArtBitmapData = playerInfo.albumArtBitmapData
        val albumArtUri = playerInfo.albumArtUri

        val colors = playerInfo.getWidgetColors()

        val widgetPadding = 12.dp
        val widgetCornerRadius = systemWidgetCornerRadius()
        val itemCornerRadius = 16.dp
        val gridSpacing = 5.dp

        val size = LocalSize.current
        val contentWidth = size.width - (widgetPadding * 2)
        val contentHeight = size.height - (widgetPadding * 2)
        val gridSide = min(contentWidth, contentHeight)

        val dynamicIconSize = (gridSide.value * 0.14f).dp
        val dynamicPlayIconSize = (gridSide.value * 0.16f).dp
        val albumArtSize = (gridSide.value * 0.40f).dp

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(colors.surface)
                    .cornerRadius(widgetCornerRadius)
                    .padding(widgetPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = GlanceModifier.size(gridSide)
                ) {
                    // Top
                    Row(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxWidth()
                    ) {
                        AlbumArtImage(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                            bitmapData = albumArtBitmapData,
                            albumArtUri = albumArtUri,
                            size = albumArtSize, // Used for optimization and placeholder size
                            context = context,
                            cornerRadius = itemCornerRadius
                        )

                        Spacer(GlanceModifier.width(gridSpacing))

                        // Play/Pause Button
                        PlayPauseButton(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                            isPlaying = isPlaying,
                            backgroundColor = colors.playPauseBackground,
                            iconColor = colors.playPauseIcon,
                            cornerRadius = itemCornerRadius,
                            iconSize = dynamicPlayIconSize
                        )
                    }

                    Spacer(GlanceModifier.height(gridSpacing))

                    // Bottom
                    Row(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxWidth()
                    ) {
                        // Previous Button
                        PreviousButton(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                            backgroundColor = colors.prevNextBackground,
                            iconColor = colors.prevNextIcon,
                            cornerRadius = itemCornerRadius,
                            iconSize = dynamicIconSize
                        )

                        Spacer(GlanceModifier.width(gridSpacing))

                        // Next Button
                        NextButton(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                            backgroundColor = colors.prevNextBackground,
                            iconColor = colors.prevNextIcon,
                            cornerRadius = itemCornerRadius,
                            iconSize = dynamicIconSize
                        )
                    }
                }
            }
        }
    }
}
