package pl.maniak.wikidiary.feature.expenses.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    fun insert(expense: ExpenseEntity)

    @Query(
        """
        SELECT expense_table.*, wiki_note_table.date
        FROM expense_table
        INNER JOIN wiki_note_table ON wiki_note_table.id = expense_table.noteId
        ORDER BY wiki_note_table.date DESC, expense_table.noteId DESC
        """
    )
    fun observeAll(): Flow<List<ExpenseRecord>>
}
