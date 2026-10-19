package com.mangareader.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.model.progressOf
import com.mangareader.app.data.repo.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionUi(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val count: Int,
    /** Até 4 capas para o mosaico. */
    val covers: List<String>,
    /** Média do progresso dos títulos da coleção (0..1). */
    val progress: Float,
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repository: CollectionRepository,
) : ViewModel() {

    val collections: StateFlow<List<CollectionUi>?> = combine(
        repository.observeSummaries(),
        repository.observeMembers(),
    ) { summaries, members ->
        val byCollection = members.groupBy { it.collectionId }
        summaries.map { summary ->
            val own = byCollection[summary.id].orEmpty()
            CollectionUi(
                id = summary.id,
                name = summary.name,
                createdAt = summary.createdAt,
                count = summary.comicCount,
                covers = own.mapNotNull { it.coverPath }.take(4),
                progress = if (own.isEmpty()) {
                    0f
                } else {
                    own.map { progressOf(it.pageCount, it.currentPage, it.lastReadAt) }.average().toFloat()
                },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun create(name: String) {
        viewModelScope.launch { repository.create(name) }
    }
}
