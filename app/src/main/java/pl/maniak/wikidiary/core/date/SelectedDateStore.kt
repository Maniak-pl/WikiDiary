package pl.maniak.wikidiary.core.date

import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Diary day chosen by the user; shared by every feature that writes notes. */
class SelectedDateStore(initial: Date = Date()) {
    private val _date = MutableStateFlow(initial)
    val date: StateFlow<Date> = _date.asStateFlow()

    fun select(date: Date) {
        _date.value = date
    }
}
