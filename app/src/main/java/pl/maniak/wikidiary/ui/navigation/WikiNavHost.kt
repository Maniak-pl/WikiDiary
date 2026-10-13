package pl.maniak.wikidiary.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.maniak.wikidiary.feature.diary.ui.DiaryNavigation
import pl.maniak.wikidiary.feature.diary.ui.DiaryRoute
import pl.maniak.wikidiary.feature.diary.ui.NotesListRoute
import pl.maniak.wikidiary.feature.export.ui.ExportRoute
import pl.maniak.wikidiary.feature.routines.ui.RoutinesRoute
import pl.maniak.wikidiary.feature.todo.ui.TodoRoute

@Composable
fun WikiNavHost(
    navController: NavHostController,
    onDiaryNavigate: (DiaryNavigation) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Diary,
        modifier = modifier,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
    ) {
        composable<AppRoute.Diary> { DiaryRoute(onNavigate = onDiaryNavigate) }
        composable<AppRoute.Tasks> { TodoRoute() }
        composable<AppRoute.Routines> { RoutinesRoute() }
        composable<AppRoute.Notes> { NotesListRoute() }
        composable<AppRoute.Export> { ExportRoute() }
    }
}

/** Switches tabs keeping one entry per tab and restoring each tab's state. */
fun NavHostController.navigateTo(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavDestination?.topLevelDestination(): TopLevelDestination =
    TopLevelDestination.entries.firstOrNull { destination ->
        this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
    } ?: TopLevelDestination.DIARY
