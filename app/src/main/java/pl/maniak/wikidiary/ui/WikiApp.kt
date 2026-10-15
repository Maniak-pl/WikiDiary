package pl.maniak.wikidiary.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.util.Date
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.core.date.fromPickerMillis
import pl.maniak.wikidiary.core.date.toPickerMillis
import pl.maniak.wikidiary.feature.diary.ui.CreateCategoryRoute
import pl.maniak.wikidiary.feature.diary.ui.CreateProjectRoute
import pl.maniak.wikidiary.feature.diary.ui.DiaryNavigation
import pl.maniak.wikidiary.feature.filmweb.ui.FilmwebRoute
import pl.maniak.wikidiary.feature.books.ui.BooksRoute
import pl.maniak.wikidiary.feature.expenses.ui.ExpenseRoute
import pl.maniak.wikidiary.feature.people.ui.PersonManagerRoute
import pl.maniak.wikidiary.feature.pictograms.ui.PictogramManagerRoute
import pl.maniak.wikidiary.ui.navigation.AppRoute
import pl.maniak.wikidiary.ui.navigation.TopLevelDestination
import pl.maniak.wikidiary.ui.navigation.WikiNavHost
import pl.maniak.wikidiary.ui.navigation.navigateTo
import pl.maniak.wikidiary.ui.navigation.topLevelDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiApp(selectedDate: SelectedDateStore = koinInject()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val current = destination.topLevelDestination()
    val onSettings = destination?.hasRoute(AppRoute.Settings::class) == true
    var sheet by rememberSaveable(stateSaver = AppSheet.Saver) { mutableStateOf<AppSheet>(AppSheet.None) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (onSettings) "Ustawienia" else (current ?: TopLevelDestination.DIARY).title) },
                navigationIcon = {
                    if (onSettings) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                        }
                    }
                },
                actions = {
                    if (!onSettings) {
                        IconButton(onClick = { navController.navigate(AppRoute.Settings) { launchSingleTop = true } }) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Ustawienia")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!onSettings) NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    val selected = destination == current
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigateTo(destination) },
                        icon = {
                            Icon(
                                imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        WikiNavHost(
            navController = navController,
            onDiaryNavigate = { destination ->
                when (destination) {
                    DiaryNavigation.CreateTag -> sheet = AppSheet.TagEditor()
                    is DiaryNavigation.EditTag -> sheet = AppSheet.TagEditor(destination.tag.id)
                    DiaryNavigation.Categories -> sheet = AppSheet.Categories
                    is DiaryNavigation.Filmweb -> sheet = AppSheet.Filmweb(destination.query)
                    is DiaryNavigation.Books -> sheet = AppSheet.Books(destination.query)
                    is DiaryNavigation.Expense -> sheet = AppSheet.Expense(destination.input, destination.tagId)
                    DiaryNavigation.Pictograms -> sheet = AppSheet.Pictograms
                    DiaryNavigation.People -> sheet = AppSheet.People
                    DiaryNavigation.PickDate -> showDatePicker = true
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
        )
    }

    if (sheet != AppSheet.None) {
        AppSheetHost(
            sheet = sheet,
            onDismiss = { sheet = AppSheet.None },
            onPickDate = { showDatePicker = true }
        )
    }

    if (showDatePicker) {
        val date by selectedDate.date.collectAsState()
        DiaryDatePicker(
            initial = date,
            onDismiss = { showDatePicker = false },
            onConfirm = selectedDate::select
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSheetHost(sheet: AppSheet, onDismiss: () -> Unit, onPickDate: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when (sheet) {
            is AppSheet.TagEditor -> CreateProjectRoute(tagId = sheet.tagId, onDone = close)
            AppSheet.Categories -> CreateCategoryRoute()
            is AppSheet.Filmweb -> FilmwebRoute(initialQuery = sheet.query, onPickDate = onPickDate, onClose = close)
            is AppSheet.Books -> BooksRoute(
                initialQuery = sheet.query, onPickDate = onPickDate, onClose = close
            )
            is AppSheet.Expense -> ExpenseRoute(
                initialInput = sheet.input,
                initialTagId = sheet.tagId,
                onPickDate = onPickDate,
                onClose = close
            )
            AppSheet.Pictograms -> PictogramManagerRoute()
            AppSheet.People -> PersonManagerRoute()
            AppSheet.None -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryDatePicker(initial: Date, onDismiss: () -> Unit, onConfirm: (Date) -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.toPickerMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onConfirm(fromPickerMillis(it)) }
                    onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    ) {
        DatePicker(state = state)
    }
}
