package dev.fredag.sudokufocus.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.fredag.sudokufocus.SelectorType
import dev.fredag.sudokufocus.model.Sudoku

class SudokuCanvasParametersProvider : PreviewParameterProvider<SudokuCanvasParameters> {
    override val values: Sequence<SudokuCanvasParameters>
        get() = sequence {
            SudokuCanvasParameters(
                Sudoku(),
                100.dp,
                100.dp,
                sectionClicked = {},
                zones = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9"),
                selectorType = SelectorType.Grid
            )
        }

}


data class SudokuCanvasParameters(
    val sudoku: Sudoku,
    val parentWidth: Dp,
    val parentHeight: Dp,
    val sectionClicked: (section: String) -> Unit,
    val zones: List<String>,
    val selectorType: SelectorType
)
