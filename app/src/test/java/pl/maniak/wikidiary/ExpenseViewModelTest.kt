package pl.maniak.wikidiary

import android.database.sqlite.SQLiteException
import java.util.Date
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.expenses.domain.Expense
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseDraft
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseRepository
import pl.maniak.wikidiary.feature.expenses.domain.PaymentMethod
import pl.maniak.wikidiary.feature.expenses.ui.ExpenseIntent
import pl.maniak.wikidiary.feature.expenses.ui.ExpenseViewModel
import pl.maniak.wikidiary.testing.InMemoryConfig
import pl.maniak.wikidiary.testing.MainDispatcherRule
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase

@RunWith(RobolectricTestRunner::class)
class ExpenseViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()
    private val database = inMemoryDatabase()
    private val repositories = TestRepositories(database)
    private val config = InMemoryConfig()
    private val date = SelectedDateStore(Date(1_728_480_000_000))
    private val repository = RecordingExpenses()
    private val viewModel by lazy { ExpenseViewModel(repository, repositories.tags, config, date) }

    @After
    fun tearDown() = database.close()

    @Test
    fun defaultsToAccountAndResetsPaymentOnNextOpening() = runBlocking {
        viewModel.open("", null)
        assertEquals(PaymentMethod.ACCOUNT, viewModel.uiState.value.paymentMethod)
        assertEquals("Finances", viewModel.uiState.value.tag.name)
        viewModel.onIntent(ExpenseIntent.PaymentSelected(PaymentMethod.CASH))
        viewModel.open("", null)
        assertEquals(PaymentMethod.ACCOUNT, viewModel.uiState.value.paymentMethod)
    }

    @Test
    fun recreatingSameSheetPreservesEditedFieldsAndPayment() = runBlocking {
        viewModel.open("12 Kawa", null, "first-sheet")
        viewModel.onIntent(ExpenseIntent.Amount("15,50"))
        viewModel.onIntent(ExpenseIntent.PaymentSelected(PaymentMethod.CASH))
        viewModel.open("12 Kawa", null, "first-sheet")
        assertEquals("15,50", viewModel.uiState.value.amount)
        assertEquals(PaymentMethod.CASH, viewModel.uiState.value.paymentMethod)
        viewModel.open("", null, "second-sheet")
        assertEquals("", viewModel.uiState.value.amount)
        assertEquals(PaymentMethod.ACCOUNT, viewModel.uiState.value.paymentMethod)
    }

    @Test
    fun shortcutTagParsingDoesNotChangeDescriptionText() = runBlocking {
        repositories.tags.saveTag(Tag(0, "Zakupy"))
        viewModel.open("120 Kurtka /fixme #Zakupy", null)
        assertEquals("Kurtka /fixme", viewModel.uiState.value.description)
    }

    @Test
    fun shortcutPrefillsAmountDescriptionAndKnownTag() = runBlocking {
        repositories.tags.saveTag(Tag(0, "Zakupy", "Finances"))
        val tag = repositories.tags.getTags().single()
        viewModel.open("120,50 OLX – Kurtka #Zakupy", null)
        val state = viewModel.uiState.value
        assertEquals("120,50", state.amount)
        assertEquals("OLX – Kurtka", state.description)
        assertEquals(tag, state.tag)
        viewModel.open("12 Kawa", tag.id)
        assertEquals(tag, viewModel.uiState.value.tag)
    }

    @Test
    fun rejectsBadAmountAndBlankDescriptionWithoutSaving() = runBlocking {
        viewModel.open("", null)
        viewModel.onIntent(ExpenseIntent.Amount("1,234"))
        viewModel.onIntent(ExpenseIntent.Save)
        assertNotNull(viewModel.uiState.value.error)
        viewModel.onIntent(ExpenseIntent.Amount("12,50"))
        viewModel.onIntent(ExpenseIntent.Save)
        assertEquals("Wpisz opis wydatku.", viewModel.uiState.value.error)
        assertTrue(repository.saved.isEmpty())
        assertEquals(0L, config.getLastUpdated())
    }

    @Test
    fun savesCashOnSelectedDayAndBlocksRepeatedSubmission() = runBlocking {
        viewModel.open("12,50 Kawa", null)
        viewModel.onIntent(ExpenseIntent.PaymentSelected(PaymentMethod.CASH))
        val selected = Date(date.date.value.time - 86_400_000)
        date.select(selected)
        repository.gate = CompletableDeferred()
        viewModel.onIntent(ExpenseIntent.Save)
        viewModel.onIntent(ExpenseIntent.Save)
        assertTrue(viewModel.uiState.value.isSaving)
        assertEquals(1, repository.saved.size)
        repository.gate?.complete(Unit)
        withTimeout(5_000) { viewModel.uiState.first { it.saved } }
        val (draft, savedDate) = repository.saved.single()
        assertEquals(1_250L, draft.amountInGrosze)
        assertEquals(PaymentMethod.CASH, draft.paymentMethod)
        assertEquals(selected, savedDate)
        assertTrue(config.getLastUpdated() > 0)
    }

    @Test
    fun saveFailureKeepsFormAndAllowsRetry() = runBlocking {
        viewModel.open("12,50 Kawa", null)
        repository.failSave = true
        viewModel.onIntent(ExpenseIntent.Save)
        assertEquals("Błąd zapisu", viewModel.uiState.value.error)
        assertEquals("Kawa", viewModel.uiState.value.description)
        assertFalse(viewModel.uiState.value.saved)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(0L, config.getLastUpdated())
        repository.failSave = false
        viewModel.onIntent(ExpenseIntent.Save)
        assertTrue(viewModel.uiState.value.saved)
    }

    private class RecordingExpenses : ExpenseRepository {
        val saved = mutableListOf<Pair<ExpenseDraft, Date>>()
        var gate: CompletableDeferred<Unit>? = null
        var failSave = false
        override fun observeExpenses() = MutableStateFlow<List<Expense>>(emptyList())
        override suspend fun saveExpense(draft: ExpenseDraft, date: Date): Long {
            if (failSave) throw SQLiteException("Błąd zapisu")
            saved += draft to date
            gate?.await()
            return saved.size.toLong()
        }
    }
}
