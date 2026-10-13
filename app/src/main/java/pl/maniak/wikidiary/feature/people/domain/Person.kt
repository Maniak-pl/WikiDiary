package pl.maniak.wikidiary.feature.people.domain

data class Person(
    val id: Long = 0,
    val name: String,
    val gender: PersonGender = PersonGender.NEUTRAL
)

enum class PersonGender(
    val storageValue: String,
    val icon: String
) {
    FEMALE("female", "🙍🏻‍♀️"),
    MALE("male", "🙍🏻‍♂️"),
    NEUTRAL("neutral", "🙍");

    companion object {
        fun fromStorage(value: String): PersonGender {
            return entries.firstOrNull { it.storageValue == value } ?: NEUTRAL
        }
    }
}
