package dev.fredag.sudokufocus.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.fredag.sudokufocus.SelectorType
import dev.fredag.sudokufocus.model.Sudoku
import dev.fredag.sudokufocus.model.SudokuSeed

class SudokuCanvasParametersProvider : PreviewParameterProvider<SudokuCanvasParameters> {
    override val values: Sequence<SudokuCanvasParameters>
        get() = sequence {
            SudokuCanvasParameters(
                Sudoku.generateFromSeed(SudokuSeed(123)),
                100.dp,
                100.dp,
                updateSudoku = {},
                selectorType = SelectorType.Grid(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")),
                true
            )
        }

}


data class SudokuCanvasParameters(
    val sudoku: Sudoku,
    val parentWidth: Dp,
    val parentHeight: Dp,
    val updateSudoku: (sudoku: Sudoku) -> Unit,
    val selectorType: SelectorType,
    val showSelectorUi: Boolean,
)
