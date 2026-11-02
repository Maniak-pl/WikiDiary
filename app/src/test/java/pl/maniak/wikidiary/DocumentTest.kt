package pl.maniak.wikidiary

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.pictograms.data.PictogramEntity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pl.maniak.wikidiary.feature.documents.DocumentField
import pl.maniak.wikidiary.feature.pictograms.domain.documentImageFile
import pl.maniak.wikidiary.feature.documents.DocumentDraft
import pl.maniak.wikidiary.feature.documents.DocumentStore
import pl.maniak.wikidiary.feature.diary.domain.NotesExportContributor
import pl.maniak.wikidiary.feature.export.domain.WikiExporter
import pl.maniak.wikidiary.testing.TestRepositories
import pl.maniak.wikidiary.testing.inMemoryDatabase
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class DocumentTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferences = context.getSharedPreferences("document-test", Context.MODE_PRIVATE)
    private val database = inMemoryDatabase()
    private val store = DocumentStore(preferences, database)
    private val date = SimpleDateFormat("yyyy.MM.dd", Locale.ROOT).parse("2026.09.02")!!.time
    private val draft = DocumentDraft(
        uri = "content://test/document", mimeType = "application/pdf", extension = ".pdf",
        subject = "LuxMed", description = "Gastroskopia", date = date,
        tag = "Dokumenty", documentCategory = "Health", driveId = "file123",
    )

    @After
    fun close() {
        database.close()
        preferences.edit().clear().commit()
    }

    @Test
    fun fileNameIncludesDateButLinkLabelDoesNot() {
        draft.validate()
        assertEquals("2026.09.02 - LuxMed - Gastroskopia.pdf", draft.fileName)
        assertEquals("LuxMed - Gastroskopia.pdf", draft.label)
    }

    @Test
    fun retrySavesOneNoteAndExportIncludesNestedLinkAndCategoryImage() = runBlocking {
        store.saveNote(draft)
        store.saveNote(draft)
        assertEquals(1, database.wikiNoteDao().getNoteCount())
        val export = WikiExporter(listOf(NotesExportContributor(TestRepositories(database).notes.observeNotes())))
            .document().first()
        assertTrue(export.contains("  * **Dokumenty**"))
        assertFalse(export.contains("Projekt - Dokumenty"))
        assertTrue(export.contains("    * **📋 Dokumentacja:**\n      * [[https://drive.google.com/open?id=file123|LuxMed - Gastroskopia.pdf]]"))
        assertTrue(export.contains("{{ :pictograms_-_doc_health.png?200 |}}"))
    }

    @Test
    fun pendingUploadSurvivesStoreRecreation() {
        store.setPending(draft)
        val restored = DocumentStore(preferences, database)
        assertEquals(draft, restored.pending())
    }

    @Test
    fun imageNameUsesApplicationCategoryWithoutHardcodedMappings() {
        assertEquals("pictograms_-_doc_realestate.png", documentImageFile("RealEstate"))
        assertEquals("pictograms_-_doc_wikidiary.png", documentImageFile("WikiDiary"))
        assertEquals("pictograms_-_doc_my_documents.png", documentImageFile("My Documents"))
        val realEstate = draft.copy(documentCategory = "RealEstate", imageFile = documentImageFile("RealEstate"))
        assertTrue(realEstate.note().content.contains("{{ :pictograms_-_doc_realestate.png?200 |}}"))
        assertNull(realEstate.note().category)
    }

    @Test
    fun savingDocumentAddsReusablePictogramWithoutAttachingAnotherEntry() = runBlocking {
        store.saveNote(draft)
        store.saveNote(draft.copy(driveId = "file456"))
        val pictogram = database.pictogramDao().getAll().single()
        assertEquals("pictograms_-_doc_health.png", WikiParser.pictogramFileName(pictogram.name))
        assertTrue(database.pictogramEntryDao().getAll().isEmpty())
    }

    @Test
    fun existingPictogramWithSameFileNameIsNotDuplicatedOrChanged() = runBlocking {
        val original = PictogramEntity(name = "Doc Health", icon = "custom")
        val id = database.pictogramDao().insert(original)
        store.saveNote(draft)
        assertEquals(listOf(original.copy(id = id)), database.pictogramDao().getAll())
    }

    @Test
    fun missingFieldsHaveSeparateErrorsAndDoNotRequireDriveFolder() {
        val errors = draft.copy(uri = "", subject = "", description = "", tag = "", documentCategory = null)
            .validationErrors()
        assertEquals(DocumentField.entries.toSet(), errors.keys)
        assertTrue(draft.validationErrors().isEmpty())
        draft.validate()
    }

    @Test
    fun invalidNamesAreReportedAtTheirFields() {
        val errors = draft.copy(subject = "Lux|Med", description = "Bad\nname").validationErrors()
        assertEquals(setOf(DocumentField.SUBJECT, DocumentField.DESCRIPTION), errors.keys)
        val categoryErrors = draft.copy(documentCategory = "Bad/Category", imageFile = "invalid/image.png")
            .validationErrors()
        assertEquals(setOf(DocumentField.CATEGORY), categoryErrors.keys)
    }

    @Test
    fun oldPendingUploadWithFolderCanStillBeRead() {
        val json = kotlinx.serialization.json.Json.encodeToString(DocumentDraft.serializer(), draft)
        preferences.edit().putString("pending_document", json.dropLast(1) + ""","folderId":"oldFolder"}""").commit()
        assertEquals(draft, store.pending())
    }

    @Test
    fun documentCategoryDoesNotOverrideExistingTagCategory() {
        val note = draft.copy(documentCategory = "RealEstate", noteCategory = "Work").note()
        assertEquals("Dokumenty", note.tag)
        assertEquals("Work", note.category)
    }

    @Test
    fun oldPendingUploadDoesNotTreatDocumentCategoryAsProject() {
        val json = kotlinx.serialization.json.Json.encodeToString(DocumentDraft.serializer(), draft)
        preferences.edit().putString("pending_document", json).commit()
        val restored = requireNotNull(store.pending())
        assertEquals("Health", restored.documentCategory)
        assertNull(restored.note().category)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidLinkDelimiterIsRejected() {
        draft.copy(description = "opis|link").validate()
    }
}
