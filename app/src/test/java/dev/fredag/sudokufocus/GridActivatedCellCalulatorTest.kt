package dev.fredag.sudokufocus

import androidx.compose.ui.geometry.Offset
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

class GridActivatedCellCalulatorTest {

    private val gridActivatedCellCalculator =
        GridActivatedCellCalulator(500f, listOf("1", "2", "3", "4", "5", "6", "7", "8", "9"))
    private var sudoku = Sudoku.generateFromSeed(123)
    private val activatedCoord = Coordinate(4, 0)

    @Test
    fun `when activating a value not in the guesses it should be added to the guesses`() {
        sudoku = gridActivatedCellCalculator.calculateNextSudoku(
            sudoku,
            Offset(540f, 60f),
            Offset(540f, 60f),
            activatedCoord
        )

        sudoku = gridActivatedCellCalculator.calculateNextSudoku(
            sudoku,
            Offset(540f, 60f),
            Offset(740f, 60f),
            activatedCoord
        )

        sudoku = gridActivatedCellCalculator.calculateNextSudoku(
            sudoku,
            Offset(540f, 60f),
            Offset(340f, 60f),
            activatedCoord
        )

        assertEquals(setOf(4, 5, 6), sudoku.getGuessedValuesAt(activatedCoord))
    }

    @Test
    fun `when activating a value while another is already submitted it should put both in guesses`() {
        sudoku = gridActivatedCellCalculator.calculateNextSudoku(
            sudoku,
            Offset(540f, 60f),
            Offset(540f, 60f),
            activatedCoord
        )

        sudoku = gridActivatedCellCalculator.calculateNextSudoku(
            sudoku,
            Offset(540f, 60f),
            Offset(740f, 60f),
            activatedCoord
        )

        assertEquals(null, sudoku.getSubmittedValueAt(activatedCoord))
        assertEquals(setOf(5, 6), sudoku.getGuessedValuesAt(activatedCoord))
    }

    @Test
    fun `it should remove submitted value when same value is activated again`() {
        sudoku = gridActivatedCellCalculator.calculateNextSudoku(
            sudoku,
            Offset(540f, 60f),
            Offset(540f, 60f),
            activatedCoord
        )

        // Not moving the press/release offset -> 5 is in the center
        assertEquals(5, sudoku.getSubmittedValueAt(activatedCoord))

        sudoku = gridActivatedCellCalculator.calculateNextSudoku(
            sudoku,
            Offset(540f, 60f),
            Offset(540f, 60f),
            activatedCoord
        )

        // Same action again should clear the cell
        assertEquals(null, sudoku.getSubmittedValueAt(activatedCoord))
    }
}