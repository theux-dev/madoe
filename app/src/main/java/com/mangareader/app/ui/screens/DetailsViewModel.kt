package com.mangareader.app.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.repo.ComicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface DetailsUi {
    data object Loading : DetailsUi

    /** [comic] é `null` quando o título deixou de existir (por exemplo, foi removido). */
    data class Loaded(val comic: ComicEntity?) : DetailsUi
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: ComicRepository,
) : ViewModel() {

    private val comicId: Long = checkNotNull(savedStateHandle.get<Long>("comicId"))

    val ui: StateFlow<DetailsUi> = repository.observeById(comicId)
        .map<ComicEntity?, DetailsUi> { DetailsUi.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailsUi.Loading)
}
