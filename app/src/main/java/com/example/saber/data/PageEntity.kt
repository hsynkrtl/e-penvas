package com.example.saber.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.saber.model.PaperStyle

@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["notebookId", "pageIndex"])]
)
data class PageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notebookId: Long,
    val pageIndex: Int,
    val paperStyle: PaperStyle = PaperStyle.GRID,
    val strokesJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis()
)
