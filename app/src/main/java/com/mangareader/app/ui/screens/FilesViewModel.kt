package com.mangareader.app.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.library.LibraryManager
import com.mangareader.app.data.library.LibraryTaskState
import com.mangareader.app.data.model.SortOrder
import com.mangareader.app.data.prefs.SettingsRepository
import com.mangareader.app.data.repo.ComicRepository
import com.mangareader.app.data.source.naturalCompare
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilesUi(
    val comics: List<ComicEntity>,
    /** `true` quando a biblioteca inteira está vazia (diferente de "busca sem resultado"). */
    val libraryEmpty: Boolean,
    val query: String,
    val sort: SortOrder,
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    repository: ComicRepository,
    private val settings: SettingsRepository,
    private val library: LibraryManager,
) : ViewModel() {

    private val query = MutableStateFlow("")

    /** `null` enquanto o banco carrega, para não piscar o estado vazio. */
    val state: StateFlow<FilesUi?> = combine(
        repository.observeAll(),
        query,
        settings.settings.map { it.sortOrder }.distinctUntilChanged(),
    ) { all, q, sort ->
        val filtered = if (q.isBlank()) all else all.filter { it.title.contains(q.trim(), ignoreCase = true) }
        FilesUi(filtered.sortedWith(comparatorFor(sort)), all.isEmpty(), q, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val taskState: StateFlow<LibraryTaskState> = library.state

    fun setQuery(value: String) {
        query.value = value
    }

    fun setSort(order: SortOrder) {
        viewModelScope.launch { settings.setSortOrder(order) }
    }

    fun importFiles(uris: List<Uri>) = library.importFiles(uris)
    fun addFolder(uri: Uri) = library.addFolder(uri)
    fun dismissResult() = library.dismissResult()

    private fun comparatorFor(sort: SortOrder): Comparator<ComicEntity> {
        val byName = Comparator<ComicEntity> { a, b -> naturalCompare(a.title, b.title) }
        return when (sort) {
            SortOrder.NAME -> byName
            SortOrder.RECENTLY_ADDED -> compareByDescending<ComicEntity> { it.addedAt }.then(byName)
            SortOrder.LAST_READ -> compareByDescending<ComicEntity> { it.lastReadAt ?: 0L }.then(byName)
        }
    }
}
