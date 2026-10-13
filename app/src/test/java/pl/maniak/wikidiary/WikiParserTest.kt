package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.maniak.wikidiary.core.wiki.WikiParser
import pl.maniak.wikidiary.feature.people.domain.Person
import pl.maniak.wikidiary.feature.people.domain.PersonGender

class WikiParserTest {

    @Test
    fun pictogramName_generatesDokuWikiFileReference() {
        assertEquals(
            "{{ :pictograms_-_love.png?200 |}}",
            WikiParser.addPictogram("love")
        )
        assertEquals(
            "{{ :pictograms_-_spacer_na_dzien.png?200 |}}",
            WikiParser.addPictogram(" Späcer na dzień ")
        )
    }

    @Test
    fun addPerson_generatesBoldCommunityLink() {
        assertEquals(
            "**🙍🏻‍♀️ [[:Community:Dorota Płachno]]**",
            WikiParser.addPerson(Person(1, "Dorota Płachno", PersonGender.FEMALE))
        )
    }
}
