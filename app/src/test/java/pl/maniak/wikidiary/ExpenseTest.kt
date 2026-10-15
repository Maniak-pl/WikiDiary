package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseAmount
import pl.maniak.wikidiary.feature.expenses.domain.ExpenseDraft
import pl.maniak.wikidiary.feature.expenses.domain.PaymentMethod

class ExpenseTest {
    @Test
    fun parsesZlotyAsExactGroszeWithoutFloatingPointRounding() {
        mapOf(
            "120" to 12_000L,
            " 120,50 " to 12_050L,
            "120.5" to 12_050L,
            "0,01" to 1L,
            "92233720368547758,07" to Long.MAX_VALUE,
        ).forEach { (input, expected) -> assertEquals(input, expected, ExpenseAmount.parse(input)) }
    }

    @Test
    fun rejectsInvalidZeroNegativeTooPreciseAndOverflowingAmounts() {
        listOf("", "0", "0,00", "-1", "+1", "1,234", "1.2.3", "1e3", "NaN", "1 200", "12 zł",
            "92233720368547758,08").forEach {
            assertNull(it, ExpenseAmount.parse(it))
        }
    }

    @Test
    fun defaultsToAccountAndFormatsExactPolishAmountWithPaymentIcon() {
        val draft = ExpenseDraft(12_050, " OLX – Kurtka ", Tag(0, "Finances"))
        assertEquals(PaymentMethod.ACCOUNT, draft.paymentMethod)
        assertEquals("💳 OLX – Kurtka – 120,50 zł", draft.noteContent())
        assertEquals("💵 OLX – Kurtka – 120,50 zł", draft.copy(paymentMethod = PaymentMethod.CASH).noteContent())
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotCreateExpenseWithoutDescription() {
        ExpenseDraft(100, " ", Tag(0, "Finances"))
    }
}
