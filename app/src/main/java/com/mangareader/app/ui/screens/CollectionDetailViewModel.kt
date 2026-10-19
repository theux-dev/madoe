package com.mangareader.app.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.db.CollectionSummary
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.repo.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CollectionRepository,
) : ViewModel() {

    private val collectionId: Long = checkNotNull(savedStateHandle.get<Long>("collectionId"))

    val collection: StateFlow<CollectionSummary?> = repository.observeSummaries()
        .map { list -> list.firstOrNull { it.id == collectionId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val comics: StateFlow<List<ComicEntity>?> = repository.observeComics(collectionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun rename(name: String) {
        viewModelScope.launch { repository.rename(collectionId, name) }
    }

    fun delete() {
        viewModelScope.launch { repository.delete(collectionId) }
    }

    fun removeComic(comicId: Long) {
        viewModelScope.launch { repository.removeComic(collectionId, comicId) }
    }
}
