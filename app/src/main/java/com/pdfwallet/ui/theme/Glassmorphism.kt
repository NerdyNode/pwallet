package com.pdfwallet.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

val LocalHazeState = compositionLocalOf { HazeState() }

/**
 * A reusable glassmorphism container that provides a frosted glass effect.
 * 
 * > **Note:** This component is **deprecated** in favor of standard M3 tonal surfaces.
 * > It is retained for backward compatibility but should not be used in new screens.
 * > On light themes the effect may be invisible due to white-on-white overlays.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param shape The shape of the glass container.
 * @param blurRadius The radius of the blur effect.
 * @param content The content of the glass container.
 */
@Deprecated(
    message = "Use M3 tonal Surface with tonalElevation instead. Glassmorphism has light-theme contrast issues.",
    replaceWith = ReplaceWith(
        "Surface(tonalElevation = 3.dp, shape = shape) { content() }",
        "androidx.compose.material3.Surface"
    )
)
@Composable
fun GlassmorphicContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    blurRadiusDp: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    // Adjust overlay colors for theme: dark uses white tints, light uses dark tints
    val overlayBase = if (isDark) Color.White else Color.Black
    val overlayAlphaHigh = if (isDark) 0.08f else 0.04f
    val overlayAlphaLow = if (isDark) 0.02f else 0.01f
    val borderAlpha = if (isDark) 0.2f else 0.08f

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        overlayBase.copy(alpha = overlayAlphaHigh),
                        overlayBase.copy(alpha = overlayAlphaLow)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = overlayBase.copy(alpha = borderAlpha),
                shape = shape
            )
            .hazeChild(
                state = LocalHazeState.current,
                style = HazeStyle(
                    backgroundColor = MaterialTheme.colorScheme.background,
                    blurRadius = blurRadiusDp,
                    tint = null
                )
            ),
        content = content
    )
}
