package pl.maniak.wikidiary.feature.expenses.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import pl.maniak.wikidiary.feature.diary.data.WikiNoteEntity

@Entity(
    tableName = "expense_table",
    foreignKeys = [
        ForeignKey(
            entity = WikiNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey val noteId: Long,
    val amountInGrosze: Long,
    val description: String,
    val paymentMethod: String,
)

data class ExpenseRecord(
    val noteId: Long,
    val amountInGrosze: Long,
    val description: String,
    val paymentMethod: String,
    val date: Long,
)
