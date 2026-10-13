package pl.maniak.wikidiary.testing

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import pl.maniak.wikidiary.core.database.WikiNoteDatabase

fun inMemoryDatabase(): WikiNoteDatabase =
    Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        WikiNoteDatabase::class.java
    )
        .allowMainThreadQueries()
        .build()
