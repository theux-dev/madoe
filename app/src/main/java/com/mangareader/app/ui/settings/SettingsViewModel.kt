package com.mangareader.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.library.LibraryManager
import com.mangareader.app.data.library.LibraryTaskState
import com.mangareader.app.data.model.AppSettings
import com.mangareader.app.data.model.FitMode
import com.mangareader.app.data.model.OrientationPref
import com.mangareader.app.data.model.PageNavigation
import com.mangareader.app.data.model.ReadingDirection
import com.mangareader.app.data.model.ReadingMode
import com.mangareader.app.data.model.ThemeMode
import com.mangareader.app.data.prefs.SettingsRepository
import com.mangareader.app.data.storage.CacheManager
import com.mangareader.app.data.storage.StorageInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val library: LibraryManager,
    private val cacheManager: CacheManager,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val libraryState: StateFlow<LibraryTaskState> = library.state

    private val _storage = MutableStateFlow(StorageInfo())
    val storage: StateFlow<StorageInfo> = _storage.asStateFlow()

    /** Bytes liberados na última limpeza, para avisar o usuário; `null` antes de qualquer limpeza. */
    private val _freedBytes = MutableStateFlow<Long?>(null)
    val freedBytes: StateFlow<Long?> = _freedBytes.asStateFlow()

    init {
        refreshStorage()
    }

    fun refreshStorage() {
        viewModelScope.launch { _storage.value = cacheManager.info() }
    }

    fun clearCache() {
        viewModelScope.launch {
            _freedBytes.value = cacheManager.clearCache()
            _storage.value = cacheManager.info()
        }
    }

    fun setThemeMode(v: ThemeMode) = viewModelScope.launch { repository.setThemeMode(v) }
    fun setReadingDirection(v: ReadingDirection) = viewModelScope.launch { repository.setReadingDirection(v) }
    fun setReadingMode(v: ReadingMode) = viewModelScope.launch { repository.setReadingMode(v) }
    fun setFitMode(v: FitMode) = viewModelScope.launch { repository.setFitMode(v) }
    fun setPageNavigation(v: PageNavigation) = viewModelScope.launch { repository.setPageNavigation(v) }
    fun setOrientation(v: OrientationPref) = viewModelScope.launch { repository.setOrientation(v) }
    fun setAutoScanOnStart(v: Boolean) = viewModelScope.launch { repository.setAutoScanOnStart(v) }

    fun addFolder(uri: Uri) = library.addFolder(uri)
    fun removeFolder(uri: String) = library.removeFolder(uri)
    fun scanNow() = library.rescanAll(userInitiated = true)
}
