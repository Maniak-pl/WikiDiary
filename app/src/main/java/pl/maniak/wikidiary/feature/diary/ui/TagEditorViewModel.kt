package pl.maniak.wikidiary.feature.diary.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Date
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.maniak.wikidiary.feature.diary.domain.Category
import pl.maniak.wikidiary.feature.diary.domain.CategoryRepository
import pl.maniak.wikidiary.feature.diary.domain.Tag
import pl.maniak.wikidiary.feature.diary.domain.TagRepository

data class TagEditorUiState(
    val categories: List<Category> = emptyList(),
    val tags: List<Tag> = emptyList(),
)

sealed interface TagEditorIntent {
    data class SaveTag(
        val id: Long,
        val name: String,
        val category: String,
        val color: Color,
    ) : TagEditorIntent

    data class DeleteTag(val id: Long) : TagEditorIntent
    data class SaveCategory(val id: Long = 0, val name: String) : TagEditorIntent
    data class DeleteCategory(val id: Long) : TagEditorIntent
}

class TagEditorViewModel(
    private val tagRepository: TagRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val uiState: StateFlow<TagEditorUiState> = combine(
        categoryRepository.observeCategories(),
        tagRepository.observeTags(),
        ::TagEditorUiState
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TagEditorUiState())

    fun onIntent(intent: TagEditorIntent) {
        viewModelScope.launch {
            when (intent) {
                is TagEditorIntent.SaveTag -> tagRepository.saveTag(
                    Tag(
                        id = intent.id,
                        name = intent.name,
                        category = intent.category,
                        color = intent.color,
                        date = Date()
                    )
                )
                is TagEditorIntent.DeleteTag -> tagRepository.deleteTag(intent.id)
                is TagEditorIntent.SaveCategory ->
                    categoryRepository.saveCategory(Category(id = intent.id, name = intent.name))
                is TagEditorIntent.DeleteCategory -> categoryRepository.deleteCategory(intent.id)
            }
        }
    }
}
