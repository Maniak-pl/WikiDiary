package pl.maniak.wikidiary.feature.expenses.domain

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import pl.maniak.wikidiary.feature.diary.domain.Tag

enum class PaymentMethod(val icon: String, val label: String) {
    ACCOUNT("\uD83D\uDCB3", "Konto"),
    CASH("\uD83D\uDCB5", "Gotówka"),
}

data class Expense(
    val noteId: Long,
    val amountInGrosze: Long,
    val description: String,
    val paymentMethod: PaymentMethod,
    val date: Date,
)

data class ExpenseDraft(
    val amountInGrosze: Long,
    val description: String,
    val tag: Tag,
    val paymentMethod: PaymentMethod = PaymentMethod.ACCOUNT,
) {
    init {
        require(amountInGrosze > 0) { "Kwota musi być większa od zera." }
        require(description.isNotBlank()) { "Wpisz opis wydatku." }
        require(tag.name.isNotBlank()) { "Wybierz tag." }
    }

    fun noteContent(): String {
        val amount = NumberFormat.getNumberInstance(Locale.forLanguageTag("pl-PL")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
            isGroupingUsed = false
        }.format(BigDecimal.valueOf(amountInGrosze, 2))
        return "${paymentMethod.icon} ${description.trim()} – $amount zł"
    }
}

object ExpenseAmount {
    private val format = Regex("[0-9]+([.,][0-9]{1,2})?")

    fun parse(text: String): Long? {
        val amount = text.trim()
        if (!format.matches(amount)) return null
        return try {
            BigDecimal(amount.replace(',', '.')).movePointRight(2).longValueExact().takeIf { it > 0 }
        } catch (_: ArithmeticException) {
            null
        }
    }
}

interface ExpenseRepository {
    suspend fun saveExpense(draft: ExpenseDraft, date: Date): Long
    fun observeExpenses(): Flow<List<Expense>>
}
