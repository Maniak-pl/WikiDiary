package pl.maniak.wikidiary.ui

import android.app.DatePickerDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.koin.android.ext.android.inject
import pl.maniak.wikidiary.ui.model.ActionClick.DataPickerChangeDate
import pl.maniak.wikidiary.ui.model.BottomSheetUiState
import pl.maniak.wikidiary.ui.model.BottomSheetUiState.CreateCategory
import pl.maniak.wikidiary.ui.model.BottomSheetUiState.CreateProject
import pl.maniak.wikidiary.ui.screens.MainScreen
import pl.maniak.wikidiary.ui.screens.bottomsheet.CreateCategoryScreen
import pl.maniak.wikidiary.ui.screens.bottomsheet.CreateProjectScreen
import pl.maniak.wikidiary.ui.theme.WikiTheme
import java.util.Calendar
import java.util.GregorianCalendar

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by inject()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WikiTheme {
                val bottomSheetExpanded by viewModel.bottomSheetExpanded.collectAsState()
                val bottomSheetUiState by viewModel.bottomSheetUiState.collectAsState()
                val scaffoldState = rememberBottomSheetScaffoldState()

                observableState(viewModel)

                LaunchedEffect(scaffoldState.bottomSheetState) {
                    snapshotFlow { scaffoldState.bottomSheetState.isExpanded }
                        .collect { isExpanded ->
                            if (bottomSheetExpanded != isExpanded) {
                                viewModel.setBottomSheetExpanded(isExpanded)
                            }
                        }
                }

                LaunchedEffect(bottomSheetExpanded) {
                    with(scaffoldState.bottomSheetState) {
                        if (bottomSheetExpanded) expand() else collapse()
                    }
                }

                BackHandler(onBack = {
                    if (scaffoldState.bottomSheetState.isExpanded) {
                        viewModel.setBottomSheetExpanded(false)
                    } else {
                        finish()
                    }
                })

                BottomSheetScaffold(
                    sheetContent = {
                        when (val state = bottomSheetUiState) {
                            is CreateProject ->
                                CreateProjectScreen(
                                    tag = state.tag,
                                    categories = viewModel.categories.value,
                                    onClick = viewModel::onActionClick
                                )
                            is CreateCategory -> CreateCategoryScreen(
                                categories = viewModel.categories.value,
                                onClick = viewModel::onActionClick
                            )
                            BottomSheetUiState.None -> {}
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
                            list = viewModel.notes.value,
                            selectedDate = viewModel.selectedDate.value,
                            tagList = viewModel.tags.value,
                            routines = viewModel.routines.value,
                            onClick = viewModel::onActionClick
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun observableState(viewModel: MainViewModel) {
        if (viewModel.showDatePickerDialog.value) {
            val context = LocalContext.current
            val calendar = Calendar.getInstance()
            calendar.time = viewModel.selectedDate.value
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    viewModel.onActionClick(
                        DataPickerChangeDate(
                            GregorianCalendar(
                                selectedYear,
                                selectedMonth,
                                selectedDay
                            ).time
                        )
                    )
                },
                year,
                month,
                day
            ).apply {
                setOnCancelListener {
                    viewModel.onActionClick(DataPickerChangeDate(viewModel.selectedDate.value))
                }
            }.show()
        }
    }
}