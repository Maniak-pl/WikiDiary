package pl.maniak.wikidiary.feature.expenses.data

import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.maniak.wikidiary.core.database.WikiNoteDatabase
import pl.maniak.wikidiary.feature.diary.data.TagMapper
import pl.maniak.wikidiary.feature.diary.data.WikiNoteEntity
import pl.maniak.wikidiary.feature.expenses.domain.Expense
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseDraft
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseRepository
import pl.maniak.wikidiary.feature.expenses.domain.PaymentMethod

class ExpenseRepositoryImpl(
    private val database: WikiNoteDatabase,
    private val tagMapper: TagMapper,
) : ExpenseRepository {
    override suspend fun saveExpense(draft: ExpenseDraft, date: Date): Long = withContext(Dispatchers.IO) {
        database.runInTransaction<Long> {
            val tags = database.tagDao().getAll()
            val tag = tags.firstOrNull { it.id == draft.tag.id && it.id != 0L }
                ?: tags.firstOrNull { it.tag.equals(draft.tag.name, ignoreCase = true) }
                ?: tagMapper.mapToEntity(draft.tag)
            val noteId = database.wikiNoteDao().insert(
                WikiNoteEntity(
                    tag = tag.tag,
                    content = draft.noteContent(),
                    folder = tag.category,
                    date = date.time,
                    isSend = false
                )
            )
            database.expenseDao().insert(
                ExpenseEntity(noteId, draft.amountInGrosze, draft.description.trim(), draft.paymentMethod.name)
            )
            database.tagDao().insert(tag.copy(date = Date()))
            noteId
        }
    }

    override fun observeExpenses(): Flow<List<Expense>> = database.expenseDao().observeAll().map { records ->
        records.map {
            Expense(it.noteId, it.amountInGrosze, it.description, PaymentMethod.valueOf(it.paymentMethod), Date(it.date))
        }
    }
}
