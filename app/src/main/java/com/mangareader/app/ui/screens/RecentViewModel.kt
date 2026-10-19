package com.mangareader.app.ui.screens

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

data class RecentUi(
    /** Já abertos alguma vez, do mais recente para o mais antigo. */
    val reading: List<ComicEntity>,
    /** Últimos importados. */
    val added: List<ComicEntity>,
)

@HiltViewModel
class RecentViewModel @Inject constructor(repository: ComicRepository) : ViewModel() {
    val state: StateFlow<RecentUi?> = repository.observeAll()
        .map { all ->
            RecentUi(
                reading = all.filter { it.lastReadAt != null }.sortedByDescending { it.lastReadAt }.take(20),
                added = all.sortedByDescending { it.addedAt }.take(10),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
