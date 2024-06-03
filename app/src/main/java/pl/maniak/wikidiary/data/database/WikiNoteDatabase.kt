package pl.maniak.wikidiary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [WikiNoteEntity::class, TagEntity::class, CategoryEntity::class, RoutineEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WikiNoteDatabase : RoomDatabase() {

    abstract fun wikiNoteDao(): WikiNoteDao
    abstract fun tagDao(): TagDao
    abstract fun categoryDao(): CategoryDao
    abstract fun routineDao(): RoutineDao

    companion object {
        private var INSTANCE: WikiNoteDatabase? = null

        fun getDatabase(context: Context): WikiNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context.applicationContext,
                    WikiNoteDatabase::class.java,
                    "wiki_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}