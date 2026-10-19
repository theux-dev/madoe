package com.mangareader.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangareader.app.data.library.LibraryManager
import com.mangareader.app.data.model.AppSettings
import com.mangareader.app.data.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SettingsRepository,
    library: LibraryManager,
) : ViewModel() {

    /** `null` até o DataStore carregar, para não piscar o tema errado na abertura. */
    val settings: StateFlow<AppSettings?> = repository.settings
        .map<AppSettings, AppSettings?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun finishOnboarding() {
        viewModelScope.launch { repository.setOnboardingDone(true) }
    }

    init {
        // Ao abrir o app, procura quadrinhos novos nas pastas monitoradas (se o usuário não desligou).
        viewModelScope.launch {
            val current = repository.settings.first()
            if (current.autoScanOnStart && current.libraryFolders.isNotEmpty()) {
                library.rescanAll(userInitiated = false)
            }
        }
    }
}
