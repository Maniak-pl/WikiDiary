package pl.maniak.wikidiary.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.GregorianCalendar
import org.koin.android.ext.android.inject
import pl.maniak.wikidiary.core.date.SelectedDateStore
import pl.maniak.wikidiary.core.ui.theme.WikiTheme
import pl.maniak.wikidiary.feature.diary.ui.CreateCategoryRoute
import pl.maniak.wikidiary.feature.diary.ui.CreateProjectRoute
import pl.maniak.wikidiary.feature.diary.ui.DiaryNavigation
import pl.maniak.wikidiary.feature.filmweb.ui.FilmwebRoute
import pl.maniak.wikidiary.feature.people.ui.PersonManagerRoute
import pl.maniak.wikidiary.feature.pictograms.ui.PictogramManagerRoute

class MainActivity : ComponentActivity() {

    private val selectedDate: SelectedDateStore by inject()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WikiTheme {
                var sheet by remember { mutableStateOf<AppSheet>(AppSheet.None) }
                val scaffoldState = rememberBottomSheetScaffoldState()
                val context = LocalContext.current
                val closeSheet = { sheet = AppSheet.None }
                val pickDate = { showDatePicker(context) }

                LaunchedEffect(sheet) {
                    with(scaffoldState.bottomSheetState) {
                        if (sheet != AppSheet.None) expand() else collapse()
                    }
                }

                LaunchedEffect(scaffoldState.bottomSheetState) {
                    snapshotFlow { scaffoldState.bottomSheetState.isCollapsed }
                        .collect { isCollapsed -> if (isCollapsed) sheet = AppSheet.None }
                }

                BackHandler(enabled = sheet != AppSheet.None, onBack = closeSheet)

                BottomSheetScaffold(
                    sheetContent = {
                        when (val current = sheet) {
                            is AppSheet.TagEditor -> CreateProjectRoute(tag = current.tag, onDone = closeSheet)
                            AppSheet.Categories -> CreateCategoryRoute()
                            AppSheet.Filmweb -> FilmwebRoute(onPickDate = pickDate, onClose = closeSheet)
                            AppSheet.Pictograms -> PictogramManagerRoute()
                            AppSheet.People -> PersonManagerRoute()
                            AppSheet.None -> {}
                        }
                    },
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        MainScreen(
                            onNavigate = { destination ->
                                when (destination) {
                                    DiaryNavigation.CreateTag -> sheet = AppSheet.TagEditor()
                                    is DiaryNavigation.EditTag -> sheet = AppSheet.TagEditor(destination.tag)
                                    DiaryNavigation.Categories -> sheet = AppSheet.Categories
                                    DiaryNavigation.Filmweb -> sheet = AppSheet.Filmweb
                                    DiaryNavigation.Pictograms -> sheet = AppSheet.Pictograms
                                    DiaryNavigation.People -> sheet = AppSheet.People
                                    DiaryNavigation.PickDate -> pickDate()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun showDatePicker(context: Context) {
        val calendar = Calendar.getInstance().apply { time = selectedDate.date.value }
        DatePickerDialog(
            context,
            { _, year, month, day -> selectedDate.select(GregorianCalendar(year, month, day).time) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
