package com.example.saber.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.example.saber.model.ShapeType
import com.example.saber.model.Stroke
import com.example.saber.model.StrokePoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object CanvasMath {

    /**
     * Builds a smooth Bezier path across stroke points using mid-point smoothing.
     */
    fun createSmoothPath(points: List<StrokePoint>): Path {
        val path = Path()
        if (points.isEmpty()) return path

        if (points.size == 1) {
            val p = points.first()
            path.moveTo(p.x, p.y)
            path.lineTo(p.x + 0.1f, p.y + 0.1f)
            return path
        }

        path.moveTo(points[0].x, points[0].y)

        if (points.size == 2) {
            path.lineTo(points[1].x, points[1].y)
            return path
        }

        for (i in 1 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val midX = (p1.x + p2.x) / 2f
            val midY = (p1.y + p2.y) / 2f
            path.quadraticTo(p1.x, p1.y, midX, midY)
        }

        // Connect the last point
        val last = points.last()
        path.lineTo(last.x, last.y)
        return path
    }

    /**
     * Generates a geometric shape path (Line, Rectangle, Circle, Arrow).
     */
    fun createShapePath(points: List<StrokePoint>, shapeType: ShapeType): Path {
        val path = Path()
        if (points.isEmpty()) return path

        val start = points.first()
        val end = points.last()

        when (shapeType) {
            ShapeType.LINE -> {
                path.moveTo(start.x, start.y)
                path.lineTo(end.x, end.y)
            }
            ShapeType.RECTANGLE -> {
                val left = minOf(start.x, end.x)
                val top = minOf(start.y, end.y)
                val right = maxOf(start.x, end.x)
                val bottom = maxOf(start.y, end.y)
                path.moveTo(left, top)
                path.lineTo(right, top)
                path.lineTo(right, bottom)
                path.lineTo(left, bottom)
                path.close()
            }
            ShapeType.CIRCLE -> {
                val left = minOf(start.x, end.x)
                val top = minOf(start.y, end.y)
                val right = maxOf(start.x, end.x)
                val bottom = maxOf(start.y, end.y)
                path.addOval(Rect(left, top, right, bottom))
            }
            ShapeType.ARROW -> {
                path.moveTo(start.x, start.y)
                path.lineTo(end.x, end.y)

                // Arrow head
                val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
                val headLength = 26f
                val headAngle = Math.PI / 6 // 30 degrees

                val x1 = end.x - (headLength * cos(angle - headAngle)).toFloat()
                val y1 = end.y - (headLength * sin(angle - headAngle)).toFloat()
                val x2 = end.x - (headLength * cos(angle + headAngle)).toFloat()
                val y2 = end.y - (headLength * sin(angle + headAngle)).toFloat()

                path.moveTo(end.x, end.y)
                path.lineTo(x1, y1)
                path.moveTo(end.x, end.y)
                path.lineTo(x2, y2)
            }
            ShapeType.NONE -> {
                return createSmoothPath(points)
            }
        }
        return path
    }

    /**
     * Checks if a touch point is close to any point in the stroke for the stroke eraser.
     */
    fun isPointNearStroke(touch: Offset, stroke: Stroke, hitThreshold: Float): Boolean {
        val radius = maxOf(hitThreshold, stroke.width / 2f + 12f)
        for (i in 0 until stroke.points.size - 1) {
            val p1 = stroke.points[i].toOffset()
            val p2 = stroke.points[i + 1].toOffset()
            if (distanceToSegment(touch, p1, p2) <= radius) {
                return true
            }
        }
        if (stroke.points.size == 1) {
            val p = stroke.points[0].toOffset()
            if (hypot(touch.x - p.x, touch.y - p.y) <= radius) return true
        }
        return false
    }

    private fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
        val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (l2 == 0f) return hypot(p.x - a.x, p.y - a.y)
        val t = maxOf(0f, minOf(1f, ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2))
        val projection = Offset(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y))
        return hypot(p.x - projection.x, p.y - projection.y)
    }

    /**
     * Smart Dark Mode color inversion (Saber signature feature):
     * Deep black ink turns crisp white; white turns black; vibrant colors remain crisp.
     */
    fun resolveInkColor(argb: Long, isSmartDarkMode: Boolean): Color {
        val original = Color(argb)
        if (!isSmartDarkMode) return original

        // In Smart Dark Mode:
        // If color is very dark (like standard black ink #18181B or < 0.2 luminance), invert to white
        val red = original.red
        val green = original.green
        val blue = original.blue
        val luminance = 0.2126f * red + 0.7152f * green + 0.0722f * blue

        return when {
            luminance < 0.25f -> Color.White
            luminance > 0.85f && red > 0.8f && green > 0.8f && blue > 0.8f -> Color(0xFF18181B)
            else -> original
        }
    }
}
