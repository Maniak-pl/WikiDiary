package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.maniak.wikidiary.feature.diary.composer.Capture
import pl.maniak.wikidiary.feature.diary.composer.CaptureError
import pl.maniak.wikidiary.feature.diary.composer.QuickCapture
import pl.maniak.wikidiary.feature.diary.composer.TextEdit
import pl.maniak.wikidiary.feature.diary.composer.Token
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.ui.DiaryNavigation

class QuickCaptureTest {

    private val work = Tag(1, "Work", "Projects")
    private val book = Tag(2, "Book")
    private val bookClub = Tag(3, "Book club")
    private val challenge = Tag(4, "Lubimy czytać wyzwanie")
    private val tags = listOf(work, book, bookClub, challenge)

    private fun parse(text: String) = QuickCapture.parse(text, tags)

    @Test
    fun hashtagSavesNoteUnderTagWithoutTheHashtag() {
        assertEquals(Capture.SaveNote(work, "Kupiłem mleko"), parse("Kupiłem mleko #Work"))
        assertEquals(Capture.SaveNote(work, "spotkanie o 10"), parse("#Work spotkanie o 10"))
        assertEquals(Capture.SaveNote(work, "a b"), parse("a #Work b"))
    }

    @Test
    fun hashtagMatchesMultiWordTagCaseInsensitivelyPreferringLongest() {
        assertEquals(Capture.SaveNote(challenge, "przeczytane"), parse("#lubimy czytać wyzwanie przeczytane"))
        assertEquals(Capture.SaveNote(bookClub, "spotkanie"), parse("#Book club spotkanie"))
        assertEquals(Capture.SaveNote(book, "clubbing"), parse("#Book clubbing"))
    }

    @Test
    fun mentionsAreKeptInNoteContent() {
        assertEquals(
            Capture.SaveNote(work, "Spotkanie z @Martyn Pekala"),
            parse("Spotkanie z @Martyn Pekala #Work")
        )
    }

    @Test
    fun noteWithoutKnownTagIsRejected() {
        assertEquals(Capture.Rejected(CaptureError.MISSING_TAG), parse("Kupiłem mleko"))
        assertEquals(Capture.Rejected(CaptureError.MISSING_TAG), parse("Kupiłem #Nieznany"))
        assertEquals(Capture.Rejected(CaptureError.MISSING_TAG), parse("mail a#Work"))
    }

    @Test
    fun emptyNoteIsRejected() {
        assertEquals(Capture.Rejected(CaptureError.EMPTY), parse("   "))
        assertEquals(Capture.Rejected(CaptureError.EMPTY), parse("#Work "))
    }

    @Test
    fun todoCommandAddsTask() {
        assertEquals(Capture.AddTodo("Umyć garki"), parse("/todo  Umyć garki "))
        assertEquals(Capture.Rejected(CaptureError.MISSING_ARGUMENT), parse("/todo"))
    }

    @Test
    fun expenseCommandOpensFormWithOptionalShortcutAndClickedTag() {
        assertEquals(Capture.Navigate(DiaryNavigation.Expense()), parse("/wydatek"))
        assertEquals(
            Capture.Navigate(DiaryNavigation.Expense("120 OLX – Kurtka #Work")),
            parse("/WYDATEK 120 OLX – Kurtka #Work")
        )
        assertEquals(
            Capture.Navigate(DiaryNavigation.Expense("12 Kawa", book.id)),
            QuickCapture.parseForTag("/wydatek 12 Kawa", book)
        )
    }

    @Test
    fun fixmeCommandSavesNoteMarkedForCompletion() {
        assertEquals(
            Capture.SaveNote(work, "Wizyta u lekarza", needsCompletion = true),
            parse("/fixme Wizyta u lekarza #Work")
        )
        assertEquals(Capture.Rejected(CaptureError.MISSING_TAG), parse("/fixme Wizyta u lekarza"))
        assertEquals(Capture.Rejected(CaptureError.MISSING_ARGUMENT), parse("/FIXME "))
    }

    @Test
    fun clickedTagSavesNoteAndHonoursFixmeCommand() {
        assertEquals(Capture.SaveNote(book, "Rozdział 3"), QuickCapture.parseForTag(" Rozdział 3 ", book))
        assertEquals(
            Capture.SaveNote(book, "Rozdział 3", needsCompletion = true),
            QuickCapture.parseForTag("/fixme Rozdział 3", book)
        )
        assertEquals(
            Capture.SaveNote(book, "Rozdział 3", needsCompletion = true),
            QuickCapture.parseForTag("Rozdział 3 /fixme", book)
        )
        assertEquals(Capture.SaveNote(book, "/fixmeX"), QuickCapture.parseForTag("/fixmeX", book))
        assertEquals(Capture.Rejected(CaptureError.MISSING_ARGUMENT), QuickCapture.parseForTag("/fixme", book))
    }

    @Test
    fun fixmeWordAnywhereInNoteIsRemovedAndMarksNote() {
        val expected = Capture.SaveNote(work, "Wizyta u lekarza", needsCompletion = true)
        assertEquals(expected, parse("Wizyta u lekarza /fixme #Work"))
        assertEquals(expected, parse("Wizyta /FIXME u lekarza #Work"))
        assertEquals(expected, parse("#Work Wizyta u lekarza /fixme"))
        assertEquals(expected, parse("/fixme Wizyta u lekarza /fixme #Work"))
        assertEquals(
            Capture.SaveNote(work, "Wizyta, u lekarza", needsCompletion = true),
            parse("Wizyta /fixme, u lekarza #Work")
        )
        assertEquals(Capture.SaveNote(work, "ścieżka a/fixme i /fixmeX"), parse("ścieżka a/fixme i /fixmeX #Work"))
        assertEquals(Capture.Rejected(CaptureError.EMPTY), parse("/fixme #Work"))
        assertEquals(Capture.Rejected(CaptureError.EMPTY), parse("#Work /fixme"))
    }

    @Test
    fun filmCommandOpensFilmwebWithQuery() {
        assertEquals(Capture.Navigate(DiaryNavigation.Filmweb("Diuna")), parse("/film Diuna"))
        assertEquals(Capture.Navigate(DiaryNavigation.Filmweb("")), parse("/FILM"))
    }

    @Test
    fun tagCommandCreatesTag() {
        assertEquals(Capture.AddTag("Książki"), parse("/tag Książki"))
        assertEquals(Capture.Rejected(CaptureError.MISSING_ARGUMENT), parse("/tag "))
    }

    @Test
    fun panelCommandsNavigate() {
        assertEquals(Capture.Navigate(DiaryNavigation.Pictograms), parse("/piktogram"))
        assertEquals(Capture.Navigate(DiaryNavigation.People), parse("/osoby"))
        assertEquals(Capture.Navigate(DiaryNavigation.Categories), parse("/kategorie"))
        assertEquals(Capture.Navigate(DiaryNavigation.CreateTag), parse("/projekt"))
        assertEquals(Capture.Navigate(DiaryNavigation.PickDate), parse("/data"))
    }

    @Test
    fun unknownCommandIsRejected() {
        assertEquals(Capture.Rejected(CaptureError.UNKNOWN_COMMAND), parse("/nieznana coś"))
    }

    @Test
    fun activeTokenDetectsTriggerBeforeCursor() {
        assertEquals(Token('@', "Mar", 12), QuickCapture.activeToken("Spotkanie z @Mar", 16))
        assertEquals(Token('@', "Martyn P", 0), QuickCapture.activeToken("@Martyn P", 9))
        assertEquals(Token('#', "Lub", 2), QuickCapture.activeToken("x #Lub", 6))
        assertEquals(Token('/', "to", 0), QuickCapture.activeToken("/to", 3))
    }

    @Test
    fun activeTokenIgnoresClosedOrEmbeddedTriggers() {
        assertNull(QuickCapture.activeToken("@Martyn ", 8))
        assertNull(QuickCapture.activeToken("mail a#b", 8))
        assertNull(QuickCapture.activeToken("x /to", 5))
        assertNull(QuickCapture.activeToken("/todo zadanie", 13))
        assertNull(QuickCapture.activeToken("Bez tokenu", 10))
    }

    @Test
    fun completeReplacesTokenAndPlacesCursorAfterIt() {
        val text = "Spotkanie z @Mar dziś"
        val token = QuickCapture.activeToken(text, 16)!!

        assertEquals(
            TextEdit("Spotkanie z @Martyn Pekala  dziś", 27),
            QuickCapture.complete(text, 16, token, "@Martyn Pekala")
        )
    }
}
