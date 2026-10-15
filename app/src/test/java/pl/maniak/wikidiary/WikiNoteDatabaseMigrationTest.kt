package pl.maniak.wikidiary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.core.database.WikiNoteDatabase

@RunWith(RobolectricTestRunner::class)
class WikiNoteDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "migration-test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(name)
    }

    @Test
    fun migration5To6KeepsNotesAndAddsCompletionMarker() {
        createVersion5DatabaseWithNote(content = "Stara notatka")

        val database = Room.databaseBuilder(context, WikiNoteDatabase::class.java, name)
            .addMigrations(WikiNoteDatabase.MIGRATION_5_6, WikiNoteDatabase.MIGRATION_6_7)
            .allowMainThreadQueries()
            .build()
        val note = database.wikiNoteDao().getAll().single()
        database.close()

        assertEquals("Stara notatka", note.content)
        assertEquals(false, note.needsCompletion)
    }

    @Test
    fun migration6To7KeepsNotesAndCreatesExpenseTable() {
        Room.databaseBuilder(context, WikiNoteDatabase::class.java, name)
            .allowMainThreadQueries().build().apply { openHelper.writableDatabase }.close()
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.execSQL("DROP TABLE `expense_table`")
            db.execSQL(
                "INSERT INTO wiki_note_table (tag, content, date, isSend, needsCompletion) " +
                    "VALUES ('Today', 'Stara notatka', 0, 0, 1)"
            )
            db.version = 6
        }
        val database = Room.databaseBuilder(context, WikiNoteDatabase::class.java, name)
            .addMigrations(WikiNoteDatabase.MIGRATION_6_7)
            .allowMainThreadQueries().build()
        try {
            val note = database.wikiNoteDao().getAll().single()
            assertEquals("Stara notatka", note.content)
            assertEquals(true, note.needsCompletion)
            database.expenseDao().insert(
                pl.maniak.wikidiary.feature.expenses.data.ExpenseEntity(note.id, 12000, "Zakup", "ACCOUNT")
            )
        } finally {
            database.close()
        }
    }

    /** Builds the current schema, then rewrites `wiki_note_table` to its version 5 shape. */
    private fun createVersion5DatabaseWithNote(content: String) {
        Room.databaseBuilder(context, WikiNoteDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()
            .apply { openHelper.writableDatabase }
            .close()

        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.execSQL("PRAGMA foreign_keys = OFF")
            db.execSQL("DROP TABLE `expense_table`")
            db.execSQL("DROP TABLE `wiki_note_table`")
            db.execSQL(
                "CREATE TABLE `wiki_note_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`tag` TEXT NOT NULL, `content` TEXT NOT NULL, `folder` TEXT, " +
                    "`date` INTEGER NOT NULL, `isSend` INTEGER NOT NULL)"
            )
            db.execSQL(
                "INSERT INTO `wiki_note_table` (`tag`, `content`, `date`, `isSend`) VALUES ('Today', ?, 0, 0)",
                arrayOf(content)
            )
            db.version = 5
        }
    }
}
