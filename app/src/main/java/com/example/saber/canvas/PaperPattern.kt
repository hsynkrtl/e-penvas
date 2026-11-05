package com.example.saber.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.saber.model.PaperStyle
import com.example.ui.theme.PaperDarkBg
import com.example.ui.theme.PaperDarkGrid
import com.example.ui.theme.PaperDarkLine
import com.example.ui.theme.PaperDarkMargin
import com.example.ui.theme.PaperLightBg
import com.example.ui.theme.PaperLightGrid
import com.example.ui.theme.PaperLightLine
import com.example.ui.theme.PaperLightMargin

object PaperPattern {

    fun drawPaper(
        drawScope: DrawScope,
        style: PaperStyle,
        isSmartDarkMode: Boolean,
        scale: Float = 1f,
        offset: Offset = Offset.Zero
    ) {
        with(drawScope) {
            val bg = if (isSmartDarkMode) PaperDarkBg else PaperLightBg
            val lineCol = if (isSmartDarkMode) PaperDarkLine else PaperLightLine
            val gridCol = if (isSmartDarkMode) PaperDarkGrid else PaperLightGrid
            val marginCol = if (isSmartDarkMode) PaperDarkMargin else PaperLightMargin

            // Background canvas fill
            drawRect(color = bg)

            val width = size.width
            val height = size.height

            when (style) {
                PaperStyle.GRID -> {
                    val step = 36f * scale
                    if (step > 4f) {
                        var x = (offset.x % step)
                        while (x <= width) {
                            if (x >= 0) {
                                drawLine(
                                    color = gridCol,
                                    start = Offset(x, 0f),
                                    end = Offset(x, height),
                                    strokeWidth = 1f
                                )
                            }
                            x += step
                        }

                        var y = (offset.y % step)
                        while (y <= height) {
                            if (y >= 0) {
                                drawLine(
                                    color = gridCol,
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f
                                )
                            }
                            y += step
                        }
                    }
                }

                PaperStyle.RULED -> {
                    val step = 42f * scale
                    if (step > 4f) {
                        // Horizontal ruled lines
                        var y = 60f * scale + (offset.y % step)
                        while (y <= height) {
                            if (y >= 0) {
                                drawLine(
                                    color = lineCol,
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1.2f
                                )
                            }
                            y += step
                        }

                        // Vertical red/accent margin line (Left margin like legal pad / notebook)
                        val marginX = 80f * scale + offset.x
                        if (marginX in 0f..width) {
                            drawLine(
                                color = marginCol.copy(alpha = 0.6f),
                                start = Offset(marginX, 0f),
                                end = Offset(marginX, height),
                                strokeWidth = 1.8f
                            )
                        }
                    }
                }

                PaperStyle.DOTS -> {
                    val step = 32f * scale
                    if (step > 8f) {
                        var x = (offset.x % step)
                        while (x <= width) {
                            if (x >= 0) {
                                var y = (offset.y % step)
                                while (y <= height) {
                                    if (y >= 0) {
                                        drawCircle(
                                            color = lineCol.copy(alpha = 0.85f),
                                            radius = 1.6f,
                                            center = Offset(x, y)
                                        )
                                    }
                                    y += step
                                }
                            }
                            x += step
                        }
                    }
                }

                PaperStyle.CORNELL -> {
                    // Header area
                    val headerY = 90f * scale + offset.y
                    if (headerY in 0f..height) {
                        drawLine(
                            color = lineCol,
                            start = Offset(0f, headerY),
                            end = Offset(width, headerY),
                            strokeWidth = 2.5f
                        )
                    }

                    // Left cue column (approx 30% width)
                    val cueX = (width * 0.3f)
                    drawLine(
                        color = marginCol.copy(alpha = 0.8f),
                        start = Offset(cueX, maxOf(0f, headerY)),
                        end = Offset(cueX, height - 160f * scale),
                        strokeWidth = 2f
                    )

                    // Bottom summary boundary
                    val summaryY = height - 160f * scale
                    if (summaryY in 0f..height) {
                        drawLine(
                            color = lineCol,
                            start = Offset(0f, summaryY),
                            end = Offset(width, summaryY),
                            strokeWidth = 2.5f
                        )
                    }

                    // Ruled lines in the right note-taking section
                    val step = 38f * scale
                    var y = maxOf(0f, headerY) + step
                    while (y < summaryY) {
                        drawLine(
                            color = gridCol,
                            start = Offset(cueX, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }
                }

                PaperStyle.BLANK -> {
                    // Clean canvas, no pattern lines
                }
            }
        }
    }
}
