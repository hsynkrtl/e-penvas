package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.saber.canvas.CanvasMath
import com.example.saber.data.StrokeSerializer
import com.example.saber.model.PaperStyle
import com.example.saber.model.ShapeType
import com.example.saber.model.Stroke
import com.example.saber.model.StrokePoint
import com.example.saber.model.ToolType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Saber Notes", appName)
    }

    @Test
    fun `test stroke serialization roundtrip`() {
        val stroke = Stroke(
            id = "test-123",
            points = listOf(StrokePoint(10f, 20f), StrokePoint(30f, 40f)),
            color = 0xFF18181BL,
            width = 5.0f,
            toolType = ToolType.PEN,
            shapeType = ShapeType.NONE
        )

        val json = StrokeSerializer.toJson(listOf(stroke))
        assertNotNull(json)

        val deserialized = StrokeSerializer.fromJson(json)
        assertEquals(1, deserialized.size)
        assertEquals("test-123", deserialized[0].id)
        assertEquals(2, deserialized[0].points.size)
        assertEquals(ToolType.PEN, deserialized[0].toolType)
    }

    @Test
    fun `test smart dark mode ink color inversion`() {
        // Deep black ink should invert to White in Smart Dark Mode
        val blackInk = 0xFF18181BL
        val inverted = CanvasMath.resolveInkColor(blackInk, isSmartDarkMode = true)
        assertEquals(androidx.compose.ui.graphics.Color.White, inverted)

        // Non-inverted in Light Mode
        val standard = CanvasMath.resolveInkColor(blackInk, isSmartDarkMode = false)
        assertEquals(androidx.compose.ui.graphics.Color(blackInk), standard)
    }
}
