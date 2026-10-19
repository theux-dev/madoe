package com.mangareader.app.data.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class ReadingDirection { LTR, RTL }
enum class ReadingMode { PAGED, CONTINUOUS }
enum class FitMode { WIDTH, HEIGHT }
enum class PageNavigation { GESTURES_AND_TAP, GESTURES_ONLY, TAP_ONLY }
enum class OrientationPref { AUTO, PORTRAIT, LANDSCAPE }
enum class SortOrder { NAME, RECENTLY_ADDED, LAST_READ }

/** Preferências do usuário, persistidas no DataStore. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val readingDirection: ReadingDirection = ReadingDirection.LTR,
    val readingMode: ReadingMode = ReadingMode.PAGED,
    val fitMode: FitMode = FitMode.WIDTH,
    val pageNavigation: PageNavigation = PageNavigation.GESTURES_AND_TAP,
    val orientation: OrientationPref = OrientationPref.AUTO,
    val sortOrder: SortOrder = SortOrder.NAME,
    val readerNightMode: Boolean = false,
    val onboardingDone: Boolean = false,
    val autoScanOnStart: Boolean = true,
    /** URIs (árvore SAF) das pastas que o usuário adicionou à biblioteca. */
    val libraryFolders: Set<String> = emptySet(),
)
