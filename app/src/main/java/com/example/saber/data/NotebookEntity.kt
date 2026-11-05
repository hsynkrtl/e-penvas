package com.example.saber.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.saber.model.PaperStyle

@Entity(tableName = "notebooks")
data class NotebookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val folder: String = "Hauptordner",
    val coverColor: Long = 0xFF4338CAL, // Default Indigo
    val defaultPaperStyle: PaperStyle = PaperStyle.GRID,
    val pageCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
