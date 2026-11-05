package com.example.saber.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebooks ORDER BY updatedAt DESC")
    fun getAllNotebooks(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE folder = :folder ORDER BY updatedAt DESC")
    fun getNotebooksByFolder(folder: String): Flow<List<NotebookEntity>>

    @Query("SELECT DISTINCT folder FROM notebooks ORDER BY folder ASC")
    fun getAllFolders(): Flow<List<String>>

    @Query("SELECT * FROM notebooks WHERE id = :id LIMIT 1")
    suspend fun getNotebookById(id: Long): NotebookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebook(notebook: NotebookEntity): Long

    @Update
    suspend fun updateNotebook(notebook: NotebookEntity)

    @Delete
    suspend fun deleteNotebook(notebook: NotebookEntity)

    @Query("DELETE FROM notebooks WHERE id = :id")
    suspend fun deleteNotebookById(id: Long)
}
