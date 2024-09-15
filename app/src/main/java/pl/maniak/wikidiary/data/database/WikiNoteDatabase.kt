package pl.maniak.wikidiary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                )
                    .addCallback(DatabaseCallback(context)) // Add callback
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(context)
                }
            }
        }

        private fun populateInitialData(context: Context) {
            val database = getDatabase(context)
            val tagDao = database.tagDao()
            val categoryDao = database.categoryDao()
            val routineDao = database.routineDao()

            val black = 0xFF000000.toInt()
            val defaultTags = listOf(
                TagEntity(tag = "Book", color = black),
                TagEntity(tag = "Meeting", color = black),
                TagEntity(tag = "ToDo", color = black),
                TagEntity(tag = "Today", color = black),
                TagEntity(tag = "Work", color = black),
                TagEntity(
                    tag = "Lubimy czytać wyzwanie",
                    category = "Books",
                    color = 0xFF4CAF50.toInt()
                ),
            )
            defaultTags.forEach { tagDao.insert(it) }

            val defaultCategories = listOf(
                CategoryEntity(name = "Books"),
                CategoryEntity(name = "Community"),
                CategoryEntity(name = "Education"),
                CategoryEntity(name = "Finances"),
                CategoryEntity(name = "Health"),
                CategoryEntity(name = "Hobby"),
                CategoryEntity(name = "RealEstate"),
                CategoryEntity(name = "Transport"),
                CategoryEntity(name = "Travels"),
                CategoryEntity(name = "WikiDiary"),
                CategoryEntity(name = "Work"),
            )
            defaultCategories.forEach { categoryDao.insert(it) }

            val defaultRoutines = listOf(
                RoutineEntity(name = "\uD83D\uDCDE Telefon do przyjaciela"),
                RoutineEntity(name = "❤\uFE0F Love"),
                RoutineEntity(name = "\uD83C\uDDEC\uD83C\uDDE7 Angielski"),
                RoutineEntity(name = "\uD83C\uDFA7 Audiobook"),
                RoutineEntity(name = "\uD83C\uDFAC Film"),
                RoutineEntity(name = "\uD83C\uDFCB\uFE0F Aktywność"),
                RoutineEntity(name = "\uD83D\uDC63 10000 kroków"),
                RoutineEntity(name = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66 Rodzina"),
                RoutineEntity(name = "\uD83D\uDC8A Kuracja"),
                RoutineEntity(name = "\uD83D\uDCBB Kurs"),
                RoutineEntity(name = "\uD83D\uDCD6 Book"),
                RoutineEntity(name = "\uD83D\uDE34 Sen"),
                RoutineEntity(name = "\uD83E\uDD1D Meeting"),
                RoutineEntity(name = "\uD83E\uDD38\uFE0F Ćwiczenia"),
                RoutineEntity(name = "\uD83E\uDDD8\uD83C\uDFFB Joga"),
                RoutineEntity(name = "\uD83D\uDDFA\uFE0F Nowe miejsce"),
                RoutineEntity(name = "\uD83E\uDDD8\u200D♂\uFE0F Relax"),
            )
            defaultRoutines.forEach { routineDao.insert(it) }
        }
    }
}
