package pl.maniak.wikidiary.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/** Type-safe routes: the bottom navigation tabs plus screens opened on top of them. */
sealed interface AppRoute {
    @Serializable data object Diary : AppRoute
    @Serializable data object Tasks : AppRoute
    @Serializable data object Routines : AppRoute
    @Serializable data object Notes : AppRoute
    @Serializable data object Export : AppRoute
    @Serializable data object Settings : AppRoute
}

enum class TopLevelDestination(
    val route: AppRoute,
    val label: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DIARY(AppRoute.Diary, "Dziennik", "Dziennik", Icons.Filled.Edit, Icons.Outlined.Edit),
    TASKS(AppRoute.Tasks, "Zadania", "Zadania ToDo", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    ROUTINES(AppRoute.Routines, "Rutyny", "Rutyny dzienne", Icons.Filled.Refresh, Icons.Outlined.Refresh),
    NOTES(AppRoute.Notes, "Notatki", "Notatki", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
    EXPORT(AppRoute.Export, "Eksport", "Eksport do Wiki", Icons.Filled.Share, Icons.Outlined.Share),
}
