package pl.maniak.wikidiary.feature.pictograms.domain

import pl.maniak.wikidiary.core.wiki.WikiParser

fun documentPictogram(category: String): Pictogram =
    Pictogram(name = "doc_${category.trim()}", icon = "📋")

fun documentImageFile(category: String): String =
    WikiParser.pictogramFileName(documentPictogram(category).name)
