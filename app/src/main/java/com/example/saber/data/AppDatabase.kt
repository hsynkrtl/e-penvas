package com.example.saber.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.saber.model.PaperStyle

class Converters {
    @TypeConverter
    fun fromPaperStyle(style: PaperStyle?): String {
        return style?.name ?: PaperStyle.GRID.name
    }

    @TypeConverter
    fun toPaperStyle(value: String?): PaperStyle {
        return try {
            if (value != null) PaperStyle.valueOf(value) else PaperStyle.GRID
        } catch (e: Exception) {
            PaperStyle.GRID
        }
    }
}

@Database(
    entities = [NotebookEntity::class, PageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun pageDao(): PageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saber_notes.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
