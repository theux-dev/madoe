package com.mangareader.app.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.db.BookmarkEntity
import com.mangareader.app.data.db.ComicEntity
import com.mangareader.app.data.model.AppSettings
import com.mangareader.app.data.model.FitMode
import com.mangareader.app.data.model.OrientationPref
import com.mangareader.app.data.model.PageNavigation
import com.mangareader.app.data.model.ReadingDirection
import com.mangareader.app.data.model.ReadingMode
import com.mangareader.app.data.prefs.SettingsRepository
import com.mangareader.app.data.repo.BookmarkRepository
import com.mangareader.app.data.repo.ComicRepository
import com.mangareader.app.data.source.PageSourceFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ReaderState {
    data object Loading : ReaderState
    data class Error(val message: String) : ReaderState
    data class Ready(val comic: ComicEntity, val loader: PageLoader, val startPage: Int) : ReaderState
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val comics: ComicRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val factory: PageSourceFactory,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val comicId: Long = checkNotNull(savedStateHandle.get<Long>("comicId"))

    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val bookmarks: StateFlow<List<BookmarkEntity>> = bookmarkRepository.observe(comicId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val pageChanges = MutableSharedFlow<Int>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var loader: PageLoader? = null

    init {
        viewModelScope.launch { open() }
        // Salva o progresso a cada troca de página; só a última importa se o usuário folhear rápido.
        viewModelScope.launch {
            pageChanges.collectLatest { comics.saveProgress(comicId, it) }
        }
    }

    private suspend fun open() {
        val comic = comics.getById(comicId)
        if (comic == null) {
            _state.value = ReaderState.Error("Este título não está mais na biblioteca.")
            return
        }
        try {
            val source = withContext(Dispatchers.IO) { factory.open(comic.format, comic.uri) }
            val pageLoader = PageLoader(source)
            loader = pageLoader
            // Retoma exatamente de onde parou; títulos nunca abertos começam na página 1.
            val start = if (comic.lastReadAt != null) comic.currentPage else 0
            val safeStart = start.coerceIn(0, (pageLoader.pageCount - 1).coerceAtLeast(0))
            _state.value = ReaderState.Ready(comic, pageLoader, safeStart)
            comics.saveProgress(comicId, safeStart)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.value = ReaderState.Error(e.message ?: "Não foi possível abrir o arquivo.")
        }
    }

    /** Para arquivos que sumiram ou ficaram ilegíveis: tira só o registro da biblioteca. */
    fun removeFromLibrary(onDone: () -> Unit) {
        viewModelScope.launch {
            comics.removeFromLibrary(comicId)
            onDone()
        }
    }

    fun onPageChanged(page: Int) {
        pageChanges.tryEmit(page)
    }

    fun addBookmark(page: Int) {
        viewModelScope.launch { bookmarkRepository.add(comicId, page) }
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch { bookmarkRepository.remove(id) }
    }

    fun setReadingDirection(v: ReadingDirection) {
        viewModelScope.launch { settingsRepository.setReadingDirection(v) }
    }

    fun setReadingMode(v: ReadingMode) {
        viewModelScope.launch { settingsRepository.setReadingMode(v) }
    }

    fun setFitMode(v: FitMode) {
        viewModelScope.launch { settingsRepository.setFitMode(v) }
    }

    fun setPageNavigation(v: PageNavigation) {
        viewModelScope.launch { settingsRepository.setPageNavigation(v) }
    }

    fun setOrientation(v: OrientationPref) {
        viewModelScope.launch { settingsRepository.setOrientation(v) }
    }

    fun setNightMode(v: Boolean) {
        viewModelScope.launch { settingsRepository.setReaderNightMode(v) }
    }

    override fun onCleared() {
        loader?.close()
        super.onCleared()
    }
}
