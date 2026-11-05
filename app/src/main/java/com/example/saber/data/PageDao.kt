package com.example.saber.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE notebookId = :notebookId ORDER BY pageIndex ASC")
    fun getPagesForNotebook(notebookId: Long): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE notebookId = :notebookId AND pageIndex = :pageIndex LIMIT 1")
    suspend fun getPageByIndex(notebookId: Long, pageIndex: Int): PageEntity?

    @Query("SELECT COUNT(*) FROM pages WHERE notebookId = :notebookId")
    suspend fun getPageCount(notebookId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity): Long

    @Update
    suspend fun updatePage(page: PageEntity)

    @Query("UPDATE pages SET strokesJson = :strokesJson, updatedAt = :updatedAt WHERE id = :pageId")
    suspend fun updatePageStrokes(pageId: Long, strokesJson: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM pages WHERE id = :pageId")
    suspend fun deletePageById(pageId: Long)

    @Query("DELETE FROM pages WHERE notebookId = :notebookId AND pageIndex = :pageIndex")
    suspend fun deletePageByIndex(notebookId: Long, pageIndex: Int)
}
