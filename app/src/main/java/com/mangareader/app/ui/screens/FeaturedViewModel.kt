package com.mangareader.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.repo.ComicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FeaturedViewModel @Inject constructor(repository: ComicRepository) : ViewModel() {
    val comics: StateFlow<List<ComicEntity>?> = repository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
