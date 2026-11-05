package com.example.saber.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.widget.Toast
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.example.saber.canvas.CanvasMath
import com.example.saber.model.PaperStyle
import com.example.saber.model.ShapeType
import com.example.saber.model.Stroke
import com.example.saber.model.ToolType
import com.example.ui.theme.PaperDarkBg
import com.example.ui.theme.PaperLightBg
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    fun exportAndSharePage(
        context: Context,
        title: String,
        pageIndex: Int,
        strokes: List<Stroke>,
        paperStyle: PaperStyle,
        isSmartDarkMode: Boolean,
        width: Int = 1200,
        height: Int = 1600
    ) {
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw background
            val bgPaint = Paint().apply {
                color = if (isSmartDarkMode) PaperDarkBg.toArgb() else PaperLightBg.toArgb()
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Draw paper lines subtly
            val linePaint = Paint().apply {
                color = if (isSmartDarkMode) 0xFF282D37.toInt() else 0xFFE2E8F0.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

            if (paperStyle == PaperStyle.GRID) {
                var x = 0f
                while (x <= width) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), linePaint)
                    x += 50f
                }
                var y = 0f
                while (y <= height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, linePaint)
                    y += 50f
                }
            } else if (paperStyle == PaperStyle.RULED) {
                var y = 120f
                while (y <= height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, linePaint)
                    y += 60f
                }
                val marginPaint = Paint().apply {
                    color = if (isSmartDarkMode) 0x66991B1B else 0x66F87171
                    strokeWidth = 3f
                }
                canvas.drawLine(140f, 0f, 140f, height.toFloat(), marginPaint)
            }

            // Draw strokes: highlighters first (like Saber), then pen/shapes
            val highlighters = strokes.filter { it.toolType == ToolType.HIGHLIGHTER }
            val normalStrokes = strokes.filter { it.toolType != ToolType.HIGHLIGHTER }

            fun renderStroke(stroke: Stroke) {
                val resolvedColor = CanvasMath.resolveInkColor(stroke.color, isSmartDarkMode)
                val paint = Paint().apply {
                    color = resolvedColor.toArgb()
                    isAntiAlias = true
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND

                    if (stroke.toolType == ToolType.HIGHLIGHTER) {
                        alpha = 110 // Uniform opacity without muddy accumulation
                        style = Paint.Style.STROKE
                        strokeWidth = stroke.width * 1.5f
                    } else if (stroke.shapeType == ShapeType.RECTANGLE || stroke.shapeType == ShapeType.CIRCLE) {
                        style = Paint.Style.STROKE
                        strokeWidth = stroke.width
                    } else {
                        style = Paint.Style.STROKE
                        strokeWidth = stroke.width
                    }
                }

                val composePath = if (stroke.shapeType != ShapeType.NONE) {
                    CanvasMath.createShapePath(stroke.points, stroke.shapeType)
                } else {
                    CanvasMath.createSmoothPath(stroke.points)
                }
                canvas.drawPath(composePath.asAndroidPath(), paint)
            }

            highlighters.forEach { renderStroke(it) }
            normalStrokes.forEach { renderStroke(it) }

            // Save to cache directory
            val cacheDir = File(context.cacheDir, "images").apply { mkdirs() }
            val sanitized = title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val file = File(cacheDir, "saber_${sanitized}_seite_${pageIndex + 1}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$title - Seite ${pageIndex + 1}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Notiz exportieren"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export fehlgeschlagen: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
