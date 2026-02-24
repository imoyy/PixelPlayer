package com.theveloper.pixelplay.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import kotlin.math.min
import kotlin.math.cos
import kotlin.math.sin

/**
 * Position indicator that remains visible while the list can scroll.
 */
@Composable
fun AlwaysOnScalingPositionIndicator(
    listState: ScalingLazyListState,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground,
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val layoutInfo = listState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems <= 1) {
        return
    }
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) {
        return
    }

    val firstVisibleIndex = visibleItems.minOf { it.index }
    val lastVisibleIndex = visibleItems.maxOf { it.index }
    val visibleCount = (lastVisibleIndex - firstVisibleIndex + 1).coerceAtLeast(1)

    val sizeFraction = (visibleCount.toFloat() / totalItems.toFloat()).coerceIn(0.10f, 1f)
    val maxFirstVisibleIndex = (totalItems - visibleCount).coerceAtLeast(0)
    val atEnd = lastVisibleIndex >= (totalItems - 1)
    val positionFraction = when {
        maxFirstVisibleIndex == 0 -> 0f
        atEnd -> 1f
        else -> (firstVisibleIndex.toFloat() / maxFirstVisibleIndex.toFloat()).coerceIn(0f, 1f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isRound) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                val outerPadding = 6.dp.toPx()
                val radius = min(size.width, size.height) / 2f - outerPadding - strokeWidth / 2f
                if (radius <= 0f) return@Canvas

                val center = Offset(x = size.width / 2f, y = size.height / 2f)
                val ovalTopLeft = Offset(center.x - radius, center.y - radius)
                val ovalSize = Size(width = radius * 2f, height = radius * 2f)

                val totalSweep = 36f
                val trackStart = -totalSweep / 2f
                val thumbSweep = (totalSweep * sizeFraction).coerceIn(4f, totalSweep)
                val thumbStart = trackStart + (totalSweep - thumbSweep) * positionFraction

                drawArc(
                    color = color.copy(alpha = 0.28f),
                    startAngle = trackStart,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    topLeft = ovalTopLeft,
                    size = ovalSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                drawArc(
                    color = color,
                    startAngle = thumbStart,
                    sweepAngle = thumbSweep,
                    useCenter = false,
                    topLeft = ovalTopLeft,
                    size = ovalSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .size(width = 4.dp, height = 56.dp),
            ) {
                val radius = size.width / 2f
                drawRoundRect(
                    color = color.copy(alpha = 0.28f),
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(radius, radius),
                )

                val thumbHeight = (size.height * sizeFraction).coerceAtLeast(size.width * 2f)
                val maxOffset = (size.height - thumbHeight).coerceAtLeast(0f)
                val thumbTop = maxOffset * positionFraction
                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, thumbTop),
                    size = Size(size.width, thumbHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
            }
        }
    }
}

@Composable
fun CurvedPagerIndicator(
    pageCount: Int,
    selectedPage: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground,
) {
    if (pageCount <= 1) {
        return
    }

    val isRound = LocalConfiguration.current.isScreenRound
    if (isRound) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(8.dp),
        ) {
            val radius = min(size.width * 0.22f, 44.dp.toPx())
            val center = Offset(
                x = size.width / 2f,
                y = radius + 2.dp.toPx(),
            )
            val totalSweep = ((pageCount - 1) * 7f).coerceIn(7f, 21f)
            val step = if (pageCount > 1) {
                totalSweep / (pageCount - 1).toFloat()
            } else {
                0f
            }
            val start = 270f - totalSweep / 2f

            repeat(pageCount) { index ->
                val angleDeg = start + (index * step)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val point = Offset(
                    x = center.x + (cos(angleRad) * radius).toFloat(),
                    y = center.y + (sin(angleRad) * radius).toFloat(),
                )
                val selected = index == selectedPage
                drawCircle(
                    color = if (selected) color else color.copy(alpha = 0.34f),
                    radius = if (selected) 2.4.dp.toPx() else 1.8.dp.toPx(),
                    center = point,
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pageCount) { index ->
                val selected = index == selectedPage
                val dotColor by animateColorAsState(
                    targetValue = if (selected) color else color.copy(alpha = 0.38f),
                    animationSpec = tween(durationMillis = 160),
                    label = "pagerDotColor",
                )
                val dotSize by animateDpAsState(
                    targetValue = if (selected) 8.dp else 6.dp,
                    animationSpec = tween(durationMillis = 160),
                    label = "pagerDotSize",
                )
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .padding(0.dp)
                        .align(Alignment.CenterVertically),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = dotColor)
                    }
                }
            }
        }
    }
}
