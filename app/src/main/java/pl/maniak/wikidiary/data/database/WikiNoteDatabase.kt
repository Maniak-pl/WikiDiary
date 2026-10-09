package pl.maniak.wikidiary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WikiNoteEntity::class,
        TagEntity::class,
        CategoryEntity::class,
        RoutineEntity::class,
        FilmEntity::class,
        FilmNoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WikiNoteDatabase : RoomDatabase() {

    abstract fun wikiNoteDao(): WikiNoteDao
    abstract fun tagDao(): TagDao
    abstract fun categoryDao(): CategoryDao
    abstract fun routineDao(): RoutineDao
    abstract fun filmDao(): FilmDao
    abstract fun filmNoteDao(): FilmNoteDao

    companion object {
        private var INSTANCE: WikiNoteDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `film_table` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `canonicalUrl` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `year` INTEGER NOT NULL,
                        `mediaType` TEXT NOT NULL,
                        `posterUrl` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_film_table_canonicalUrl` " +
                        "ON `film_table` (`canonicalUrl`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `film_note_table` (
                        `noteId` INTEGER NOT NULL,
                        `filmId` INTEGER NOT NULL,
                        PRIMARY KEY(`noteId`),
                        FOREIGN KEY(`noteId`) REFERENCES `wiki_note_table`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`filmId`) REFERENCES `film_table`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_film_note_table_filmId` " +
                        "ON `film_note_table` (`filmId`)"
                )
            }
        }

        fun getDatabase(context: Context): WikiNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context.applicationContext,
                    WikiNoteDatabase::class.java,
                    "wiki_database"
                )
                    .addMigrations(MIGRATION_1_2)
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
