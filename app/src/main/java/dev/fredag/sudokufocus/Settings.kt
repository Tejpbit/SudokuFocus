package dev.fredag.sudokufocus

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Settings(viewModel: SettingsViewModel) {

    var expanded by remember { mutableStateOf(false) }

    Column {
        Row {
            Text(
                text = "Selector type: ${viewModel.selectorType.name}",
                modifier = Modifier.clickable(onClick = { expanded = true })
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                for (selector in listOf(
                    defaultRotaryWithCenterSelector,
                    defaultGridSelector
                ))
                    DropdownMenuItem(onClick = {
                        viewModel.selectSelectorType(selector)
                        expanded = false
                    }) {
                        Text(text = selector.name)
                    }
            }
        }
        Row {
            Text(text = "Show selector UI: ")
            Checkbox(
                checked = viewModel.showSelectorUi,
                onCheckedChange = { viewModel.selectShowSelectorUi(it) })
        }
    }
}

//@OptIn(ExperimentalPagerApi::class)
//@Composable
//fun Settings(viewModel: SettingsViewModel) {
//
//    var expanded by remember { mutableStateOf(false) }
//    val pagerState = rememberPagerState()
//
//
//
//    BoxWithConstraints() {
//        val heightDp = this@BoxWithConstraints.maxHeight / 2
//        val widthDp = this@BoxWithConstraints.maxWidth / 2
//        val height = with(LocalDensity.current) {
//            heightDp.toPx()
//        }
//        val width = with(LocalDensity.current) {
//            widthDp.toPx()
//        }
//
//        LazyColumn() {
//            item {
//                Text(
//                    text = "Selector type: ${viewModel.selectorType.name}",
//                    modifier = Modifier.clickable(onClick = { expanded = true })
//                )
//            }
//            item {
//                HorizontalPager(
//                    count = selectors.size,
//                    state = pagerState,
//                    contentPadding = PaddingValues(end = 64.dp),
//                ) {
//                    Box {
//                        Canvas(
//                            Modifier
//                                .width(widthDp)
//                                .height(widthDp)
//                                .border(
//                                    3.dp,
//                                    MaterialTheme.colors.onBackground,
//                                    RoundedCornerShape(5.dp)
//                                )
//                        ) {
//                            drawRotaryWithCenterSelector(
//                                defaultRotaryWithCenterSelector.zones,
//                                defaultRotaryWithCenterSelector.center,
//                                Offset(width / 2, width / 2),
//                                Offset(width / 2, width / 2),
//                                RotaryWithCenterActivatedCellCalculator(
//                                    300f,
//                                    40f,
//                                    defaultRotaryWithCenterSelector.zones,
//                                    defaultRotaryWithCenterSelector.center
//                                )
//                            )
//                        }
//
//                    }
//
//
//                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//                        for (selector in listOf(
//                            defaultRotaryWithCenterSelector,
//                            defaultGridSelector
//                        ))
//                            DropdownMenuItem(onClick = {
//                                viewModel.selectSelectorType(selector)
//                                expanded = false
//                            }) {
//                                Text(text = selector.name)
//                            }
//                    }
//                }
//            }
//        }
//    }
//}

@HiltViewModel
class SettingsViewModel @Inject constructor(
//    private val savedState: SavedStateHandle
) : ViewModel() {

    init {
        Log.d("HELO", "new settings view model")
    }

    var showSelectorUi: Boolean by mutableStateOf(true)
        private set

    fun selectShowSelectorUi(_showSelectorUi: Boolean) {
        showSelectorUi = _showSelectorUi
    }

    var selectorType: SelectorType by mutableStateOf(
//        savedState.get<SelectorType>(SELECTOR_TYPE) ?:
        defaultRotaryWithCenterSelector
    )
        private set

    fun selectSelectorType(_selectorType: SelectorType) {
//        savedState.set(SELECTOR_TYPE, _selectorType)
        selectorType = _selectorType
    }


    companion object {
        val SELECTOR_TYPE = "SELECTOR_TYPE"
    }
}

