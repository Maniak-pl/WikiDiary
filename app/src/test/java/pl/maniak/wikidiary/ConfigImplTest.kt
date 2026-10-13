package pl.maniak.wikidiary

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import pl.maniak.wikidiary.core.config.ConfigImpl

@RunWith(RobolectricTestRunner::class)
class ConfigImplTest {

    private val preferences = RuntimeEnvironment.getApplication()
        .getSharedPreferences("config-test", Context.MODE_PRIVATE)

    @Test
    fun themeFollowsSystemUntilUserChoosesOne() {
        assertNull(ConfigImpl(preferences).darkTheme.value)
    }

    @Test
    fun chosenThemeIsPublishedAndRemembered() {
        val config = ConfigImpl(preferences)

        config.setDarkTheme(true)
        assertEquals(true, config.darkTheme.value)
        assertEquals(true, ConfigImpl(preferences).darkTheme.value)

        config.setDarkTheme(false)
        assertEquals(false, ConfigImpl(preferences).darkTheme.value)
    }

    @Test
    fun wikiPageUrlIsTrimmedPublishedAndRemembered() {
        val config = ConfigImpl(preferences)

        config.setWikiPageUrl("  https://wiki.example.com/doku.php?id=diary ")

        assertEquals("https://wiki.example.com/doku.php?id=diary", config.wikiPageUrl.value)
        assertEquals("https://wiki.example.com/doku.php?id=diary", ConfigImpl(preferences).wikiPageUrl.value)
    }
}
