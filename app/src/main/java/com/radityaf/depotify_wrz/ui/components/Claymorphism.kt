package com.radityaf.depotify_wrz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.radityaf.depotify_wrz.ui.theme.DepotifyColors

/**
 * Custom Modifier for inflated Claymorphism Cards with double shadows:
 * - Outer dark drop shadow on bottom-right
 * - Outer light highlight shadow on top-left
 * - Subtle inner white highlight border
 */
fun Modifier.clayCard(
    cornerRadius: Dp = 20.dp,
    color: Color = DepotifyColors.Surface,
    shadowColor: Color = DepotifyColors.ShadowDark,
    highlightColor: Color = DepotifyColors.ShadowLight,
    shadowOffset: Offset = Offset(10f, 12f),
    highlightOffset: Offset = Offset(-8f, -8f),
    shadowBlur: Dp = 16.dp,
    highlightBlur: Dp = 12.dp,
    borderAlpha: Float = 0.6f
): Modifier = this
    .drawBehind {
        val radiusPx = cornerRadius.toPx()
        val shadowBlurPx = shadowBlur.toPx()
        val highlightBlurPx = highlightBlur.toPx()

        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint()

            // 1. Outer Dark Drop Shadow (Bottom-Right)
            paint.reset()
            paint.color = shadowColor.toArgb()
            if (shadowBlurPx > 0) {
                paint.maskFilter = android.graphics.BlurMaskFilter(
                    shadowBlurPx,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
            canvas.nativeCanvas.drawRoundRect(
                shadowOffset.x,
                shadowOffset.y,
                size.width + shadowOffset.x,
                size.height + shadowOffset.y,
                radiusPx,
                radiusPx,
                paint
            )

            // 2. Outer Light Highlight Shadow (Top-Left)
            paint.reset()
            paint.color = highlightColor.toArgb()
            if (highlightBlurPx > 0) {
                paint.maskFilter = android.graphics.BlurMaskFilter(
                    highlightBlurPx,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
            canvas.nativeCanvas.drawRoundRect(
                highlightOffset.x,
                highlightOffset.y,
                size.width + highlightOffset.x,
                size.height + highlightOffset.y,
                radiusPx,
                radiusPx,
                paint
            )
        }

        // 3. Card Surface Fill
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )

        // 4. Subtle Top/Left Border Highlight
        drawRoundRect(
            color = Color.White.copy(alpha = borderAlpha),
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Custom Modifier for Recessed / Sunken Clay Inset elements (Input Fields, Inset Pills, Search Bar):
 * - Soft inner dark shadow in top-left
 * - Light inner shadow in bottom-right
 */
fun Modifier.clayInset(
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = DepotifyColors.SurfaceInset,
    darkShadowColor: Color = Color(0x080F172A),
    lightShadowColor: Color = Color(0x90FFFFFF)
): Modifier = this
    .drawBehind {
        val radiusPx = cornerRadius.toPx()

        // 1. Fill Inset Base Surface
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )

        // 2. Top-Left Soft Inner Shadow Effect
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(darkShadowColor, Color.Transparent),
                startY = 0f,
                endY = size.height * 0.3f
            ),
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )

        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(darkShadowColor, Color.Transparent),
                startX = 0f,
                endX = size.width * 0.3f
            ),
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )

        // 3. Bottom-Right Light Inner Highlight Effect
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, lightShadowColor),
                startY = size.height * 0.7f,
                endY = size.height
            ),
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )

        // 4. Subtle Outline Stroke
        drawRoundRect(
            color = Color(0x100F172A),
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = 1.dp.toPx())
        )
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Custom Modifier for Inflated Clay Button with Gradient fill and colored soft shadow
 */
fun Modifier.clayButton(
    cornerRadius: Dp = 24.dp,
    gradient: Brush = DepotifyColors.GreenGradient,
    shadowColor: Color = Color(0x3300A884),
    enabled: Boolean = true
): Modifier = this
    .drawBehind {
        val radiusPx = cornerRadius.toPx()

        if (enabled) {
            drawIntoCanvas { canvas ->
                val paint = Paint().asFrameworkPaint()
                paint.color = shadowColor.toArgb()
                paint.maskFilter = android.graphics.BlurMaskFilter(
                    14.dp.toPx(),
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
                canvas.nativeCanvas.drawRoundRect(
                    0f,
                    6.dp.toPx(),
                    size.width,
                    size.height + 6.dp.toPx(),
                    radiusPx,
                    radiusPx,
                    paint
                )
            }
        }

        // Fill Button Gradient
        if (enabled) {
            drawRoundRect(
                brush = gradient,
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        } else {
            drawRoundRect(
                color = DepotifyColors.TextMuted.copy(alpha = 0.3f),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }

        // Top Light Highlight Edge
        if (enabled) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Ambient Clay Background with soft gradient orbs (matching web Claymorphism background)
 */
@Composable
fun ClayBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DepotifyColors.Background)
            .drawBehind {
                // Top-Left Soft Teal Ambient Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3534D399),
                            Color(0x1034D399),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.1f, size.height * 0.15f),
                        radius = size.width * 0.6f
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.15f),
                    radius = size.width * 0.6f
                )

                // Bottom-Right Soft Blue/Teal Ambient Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2538BDF8),
                            Color(0x0838BDF8),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.85f),
                        radius = size.width * 0.7f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.85f),
                    radius = size.width * 0.7f
                )
            },
        content = content
    )
}
