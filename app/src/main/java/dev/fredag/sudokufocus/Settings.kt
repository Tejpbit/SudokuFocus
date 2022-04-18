package dev.fredag.sudokufocus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
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

@HiltViewModel
class SettingsViewModel @Inject constructor(
//    private val savedState: SavedStateHandle // TODO store configuration between restarts
) : ViewModel() {
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

