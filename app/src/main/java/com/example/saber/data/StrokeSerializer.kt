package com.example.saber.data

import com.example.saber.model.ShapeType
import com.example.saber.model.Stroke
import com.example.saber.model.StrokePoint
import com.example.saber.model.ToolType
import org.json.JSONArray
import org.json.JSONObject

object StrokeSerializer {
    fun toJson(strokes: List<Stroke>): String {
        val rootArray = JSONArray()
        for (stroke in strokes) {
            val strokeObj = JSONObject()
            strokeObj.put("id", stroke.id)
            strokeObj.put("color", stroke.color)
            strokeObj.put("width", stroke.width.toDouble())
            strokeObj.put("tool", stroke.toolType.name)
            strokeObj.put("shape", stroke.shapeType.name)

            val pointsArray = JSONArray()
            for (p in stroke.points) {
                val pObj = JSONObject()
                pObj.put("x", p.x.toDouble())
                pObj.put("y", p.y.toDouble())
                pObj.put("p", p.pressure.toDouble())
                pointsArray.put(pObj)
            }
            strokeObj.put("points", pointsArray)
            rootArray.put(strokeObj)
        }
        return rootArray.toString()
    }

    fun fromJson(json: String?): List<Stroke> {
        if (json.isNullOrBlank()) return emptyList()
        val strokes = mutableListOf<Stroke>()
        try {
            val rootArray = JSONArray(json)
            for (i in 0 until rootArray.length()) {
                val strokeObj = rootArray.getJSONObject(i)
                val id = strokeObj.optString("id", "")
                val color = strokeObj.optLong("color", 0xFF18181BL)
                val width = strokeObj.optDouble("width", 4.0).toFloat()
                val toolType = try {
                    ToolType.valueOf(strokeObj.optString("tool", ToolType.PEN.name))
                } catch (e: Exception) {
                    ToolType.PEN
                }
                val shapeType = try {
                    ShapeType.valueOf(strokeObj.optString("shape", ShapeType.NONE.name))
                } catch (e: Exception) {
                    ShapeType.NONE
                }

                val points = mutableListOf<StrokePoint>()
                val pointsArray = strokeObj.optJSONArray("points")
                if (pointsArray != null) {
                    for (j in 0 until pointsArray.length()) {
                        val pObj = pointsArray.getJSONObject(j)
                        val x = pObj.optDouble("x", 0.0).toFloat()
                        val y = pObj.optDouble("y", 0.0).toFloat()
                        val p = pObj.optDouble("p", 1.0).toFloat()
                        points.add(StrokePoint(x, y, p))
                    }
                }

                strokes.add(
                    Stroke(
                        id = if (id.isNotEmpty()) id else java.util.UUID.randomUUID().toString(),
                        points = points,
                        color = color,
                        width = width,
                        toolType = toolType,
                        shapeType = shapeType
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strokes
    }
}
