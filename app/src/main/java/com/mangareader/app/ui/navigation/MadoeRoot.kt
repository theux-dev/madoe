package com.mangareader.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mangareader.app.ui.onboarding.OnboardingScreen
import com.mangareader.app.ui.reader.ReaderScreen
import com.mangareader.app.ui.screens.CollectionDetailScreen
import com.mangareader.app.ui.screens.CollectionsScreen
import com.mangareader.app.ui.screens.DetailsScreen
import com.mangareader.app.ui.screens.FeaturedScreen
import com.mangareader.app.ui.screens.FilesScreen
import com.mangareader.app.ui.screens.RecentScreen
import com.mangareader.app.ui.settings.SettingsScreen

@Composable
fun MadoeRoot(showOnboarding: Boolean, onFinishOnboarding: () -> Unit) {
    val navController = rememberNavController()
    // Fixo na primeira composição: ao terminar a introdução, o destino inicial não pode mudar.
    val startDestination = remember { if (showOnboarding) Routes.ONBOARDING else TopLevelTab.RECENT.route }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelTab.entries.any { it.route == currentRoute }

    val openSettings = { navController.navigate(Routes.SETTINGS) }
    val openDetails = { id: Long -> navController.navigate(Routes.details(id)) }
    val openReader = { id: Long -> navController.navigate(Routes.reader(id)) }
    val openCollection = { id: Long -> navController.navigate(Routes.collection(id)) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Cada tela cuida dos próprios insets (barra de status no topo, navegação embaixo).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onFinish = {
                            onFinishOnboarding()
                            navController.navigate(TopLevelTab.RECENT.route) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        },
                    )
                }
                composable(TopLevelTab.RECENT.route) { RecentScreen(openSettings, openDetails, openReader) }
                composable(TopLevelTab.FILES.route) { FilesScreen(openSettings, openDetails, openReader) }
                composable(TopLevelTab.FEATURED.route) { FeaturedScreen(openSettings, openDetails, openReader) }
                composable(TopLevelTab.COLLECTIONS.route) { CollectionsScreen(openSettings, openCollection) }
                composable(
                    Routes.DETAILS,
                    arguments = listOf(navArgument("comicId") { type = NavType.LongType }),
                ) { DetailsScreen(onBack = { navController.popBackStack() }, onRead = openReader) }
                composable(
                    Routes.READER,
                    arguments = listOf(navArgument("comicId") { type = NavType.LongType }),
                ) { ReaderScreen(onBack = { navController.popBackStack() }) }
                composable(
                    Routes.COLLECTION,
                    arguments = listOf(navArgument("collectionId") { type = NavType.LongType }),
                ) {
                    CollectionDetailScreen(
                        onBack = { navController.popBackStack() },
                        onOpenDetails = openDetails,
                        onRead = openReader,
                    )
                }
                composable(Routes.SETTINGS) { SettingsScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}
