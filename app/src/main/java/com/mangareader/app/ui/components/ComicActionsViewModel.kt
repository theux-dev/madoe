package com.mangareader.app.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.db.CollectionSummary
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.repo.CollectionRepository
import com.mangareader.app.data.repo.ComicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Ações que qualquer card de título pode disparar: favoritar, renomear, remover, coleções. */
@HiltViewModel
class ComicActionsViewModel @Inject constructor(
    private val comics: ComicRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    val collections: StateFlow<List<CollectionSummary>> = collectionRepository.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun collectionIdsFor(comicId: Long): Flow<Set<Long>> =
        collectionRepository.observeCollectionIdsFor(comicId).map { it.toSet() }

    fun toggleFavorite(comic: ComicEntity) {
        viewModelScope.launch { comics.setFavorite(comic.id, !comic.isFavorite) }
    }

    fun rename(comicId: Long, title: String) {
        viewModelScope.launch { comics.rename(comicId, title) }
    }

    fun removeFromLibrary(comicId: Long) {
        viewModelScope.launch { comics.removeFromLibrary(comicId) }
    }

    fun setInCollection(collectionId: Long, comicId: Long, inCollection: Boolean) {
        viewModelScope.launch {
            if (inCollection) {
                collectionRepository.addComic(collectionId, comicId)
            } else {
                collectionRepository.removeComic(collectionId, comicId)
            }
        }
    }

    fun createCollectionAndAdd(name: String, comicId: Long) {
        viewModelScope.launch {
            val id = collectionRepository.create(name)
            collectionRepository.addComic(id, comicId)
        }
    }
}
