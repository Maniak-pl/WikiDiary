package pl.maniak.wikidiary.feature.export.domain

import java.net.URI
import java.net.URISyntaxException

object WikiPageUrl {
    /** Accepts only absolute `http`/`https` addresses with a host. */
    fun isValid(value: String): Boolean = try {
        val uri = URI(value)
        (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) &&
            !uri.host.isNullOrBlank()
    } catch (_: URISyntaxException) {
        false
    }

    const val INVALID_MESSAGE = "Podaj pełny adres URL zaczynający się od http:// lub https://."
}
