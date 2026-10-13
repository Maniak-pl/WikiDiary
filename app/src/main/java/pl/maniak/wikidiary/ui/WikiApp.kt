package pl.maniak.wikidiary.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import pl.maniak.wikidiary.feature.people.ui.PersonManagerRoute
import pl.maniak.wikidiary.feature.pictograms.ui.PictogramManagerRoute
import pl.maniak.wikidiary.ui.navigation.TopLevelDestination
import pl.maniak.wikidiary.ui.navigation.WikiNavHost
import pl.maniak.wikidiary.ui.navigation.navigateTo
import pl.maniak.wikidiary.ui.navigation.topLevelDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiApp(selectedDate: SelectedDateStore = koinInject()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination.topLevelDestination()
    var sheet by rememberSaveable(stateSaver = AppSheet.Saver) { mutableStateOf<AppSheet>(AppSheet.None) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(current.title) }) },
        bottomBar = {
            NavigationBar {
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
                    DiaryNavigation.Filmweb -> sheet = AppSheet.Filmweb
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
            AppSheet.Filmweb -> FilmwebRoute(onPickDate = onPickDate, onClose = close)
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
