package com.example.saber.model

import androidx.compose.ui.geometry.Offset
import java.util.UUID

enum class ToolType {
    PEN,
    HIGHLIGHTER,
    ERASER,
    SHAPE
}

enum class ShapeType(val displayName: String) {
    NONE("Keine"),
    LINE("Gerade Linie"),
    RECTANGLE("Rechteck"),
    CIRCLE("Kreis / Ellipse"),
    ARROW("Pfeil")
}

data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f
) {
    fun toOffset(): Offset = Offset(x, y)
}

data class Stroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<StrokePoint>,
    val color: Long, // ARGB 32-bit color
    val width: Float,
    val toolType: ToolType,
    val shapeType: ShapeType = ShapeType.NONE
)
