package pl.maniak.wikidiary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase

@Database(entities = [WikiNoteEntity::class], version = 1, exportSchema = false)
abstract class WikiNoteDatabase : RoomDatabase() {

    abstract fun wikiNoteDao(): WikiNoteDao

    companion object {
        private var INSTANCE: WikiNoteDatabase? = null

        fun getDatabase(context: Context): WikiNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context.applicationContext,
                    WikiNoteDatabase::class.java,
                    "wiki_note_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}