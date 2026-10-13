package pl.maniak.wikidiary

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.maniak.wikidiary.data.Person
import pl.maniak.wikidiary.data.PersonGender
import pl.maniak.wikidiary.utils.helpers.PersonMentionResolver

class PersonMentionResolverTest {

    @Test
    fun resolvesPeopleInMentionOrderWithoutDuplicates() {
        val dorota = Person(1, "Dorota Płachno", PersonGender.FEMALE)
        val damian = Person(2, "Damian Hanusiak", PersonGender.MALE)
        val people = listOf(damian, dorota)

        val result = PersonMentionResolver.resolve(
            "Porozmawiałem z @Dorota Płachno, a później z @Damian Hanusiak. " +
                "Ponownie: @Dorota Płachno.",
            people
        )

        assertEquals(listOf(dorota, damian), result)
    }

    @Test
    fun replacesKnownMentionWithInlineWikiLink() {
        val martyn = Person(3, "Martyn Pekala", PersonGender.MALE)

        val result = PersonMentionResolver.replaceWithWikiLinks(
            "Umwowlem sie z @Martyn Pekala na spotaknie",
            listOf(martyn)
        )

        assertEquals(
            "Umwowlem sie z **🙍🏻‍♂️ [[:Community:Martyn Pekala]]** na spotaknie",
            result
        )
    }
}
