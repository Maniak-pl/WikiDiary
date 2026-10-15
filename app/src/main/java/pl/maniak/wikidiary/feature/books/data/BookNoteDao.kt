package pl.maniak.wikidiary.feature.books.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookNoteDao {
    @Insert
    fun insert(book: BookNoteEntity)

    @Query("SELECT * FROM book_note_table")
    fun observeAll(): Flow<List<BookNoteEntity>>
}
