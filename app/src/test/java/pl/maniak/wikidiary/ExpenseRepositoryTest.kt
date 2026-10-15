package pl.maniak.wikidiary

import android.database.sqlite.SQLiteException
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.diary.data.TagMapperImpl
import pl.maniak.wikidiary.feature.diary.domain.NotesExportContributor
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.expenses.data.ExpenseRepositoryImpl
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseDraft
import pl.maniak.wikidiary.feature.expenses.domain.PaymentMethod
import pl.maniak.wikidiary.feature.export.domain.WikiExporter
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import pl.maniak.wikidiary.testing.nextValueAfter

@RunWith(RobolectricTestRunner::class)
class ExpenseRepositoryTest {
    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val expenses = ExpenseRepositoryImpl(database, TagMapperImpl())
    private val tag = Tag(0, "Finances")
    private val date = Date(1_728_480_000_000)

    @After
    fun tearDown() = database.close()

    @Test
    fun savesStructuredExpenseAndExportsEachPaymentMethodExactlyOnce() = runBlocking {
        val accountId = expenses.saveExpense(ExpenseDraft(12_050, "OLX – Kurtka", tag), date)
        val cashId = expenses.saveExpense(ExpenseDraft(201, "Kawa", tag, PaymentMethod.CASH), date)
        val saved = expenses.observeExpenses().first()
        assertEquals(listOf(cashId, accountId), saved.map { it.noteId })
        assertEquals(listOf(201L, 12_050L), saved.map { it.amountInGrosze })
        assertEquals(listOf(PaymentMethod.CASH, PaymentMethod.ACCOUNT), saved.map { it.paymentMethod })
        assertTrue(saved.all { it.date == date })
        assertEquals(listOf("Finances"), repositories.tags.getTags().map { it.name })

        val export = WikiExporter(listOf(NotesExportContributor(repositories.notes.observeNotes())))
            .document().first()
        assertEquals(1, export.split("    * 💳 OLX – Kurtka – 120,50 zł\n").size - 1)
        assertEquals(1, export.split("    * 💵 Kawa – 2,01 zł\n").size - 1)
        assertFalse(export.contains("/wydatek"))
    }

    @Test
    fun deletingNoteAlsoDeletesExpenseAndUpdatesObservedLatestDate() = runBlocking {
        val oldId = expenses.saveExpense(ExpenseDraft(100, "Stary zakup", tag), date)
        val newer = Date(date.time + 86_400_000)
        val newId = expenses.saveExpense(ExpenseDraft(200, "Nowy zakup", tag), newer)
        assertEquals(newer, expenses.observeExpenses().first().first().date)

        val remaining = expenses.observeExpenses().nextValueAfter { repositories.notes.deleteNoteById(newId) }
        assertEquals(oldId, remaining.single().noteId)
        assertEquals(date, remaining.single().date)
        val empty = expenses.observeExpenses().nextValueAfter { repositories.notes.deleteAllNotes() }
        assertTrue(empty.isEmpty())
    }

    @Test
    fun reusesExistingTagAndPreservesItsCategory() = runBlocking {
        repositories.tags.saveTag(Tag(0, "Zakupy", "Finances"))
        val existing = repositories.tags.getTags().single()
        expenses.saveExpense(ExpenseDraft(100, "Zakup", existing), date)
        val note = repositories.notes.getNotes().single()
        assertEquals("Zakupy", note.tag)
        assertEquals("Finances", note.category)
        assertEquals(existing.id, repositories.tags.getTags().single().id)
    }

    @Test
    fun failedExpenseInsertRollsBackNoteAndTag() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER reject_expense BEFORE INSERT ON expense_table " +
                "BEGIN SELECT RAISE(ABORT, 'expense test failure'); END"
        )
        try {
            expenses.saveExpense(ExpenseDraft(100, "Zakup", tag), date)
            fail("Expected expense insert to fail")
        } catch (_: SQLiteException) {
            assertTrue(repositories.notes.getNotes().isEmpty())
            assertTrue(repositories.tags.getTags().isEmpty())
            assertTrue(expenses.observeExpenses().first().isEmpty())
        }
    }
}
