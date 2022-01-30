package dev.fredag.sudokufocus

import androidx.compose.ui.geometry.Offset
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
import org.junit.Assert.assertEquals

import org.junit.Test

class RotaryWithCenterActivatedCellCalculatorTest {

    private val rotaryWithCenterActivatedCellCalculator =
        RotaryWithCenterActivatedCellCelculator(
            100f,
            20f,
            listOf(
                "1",
                "2",
                "3",
                "6",
                "9",
                "8",
                "7",
                "4"
            ), "5"
        )
    private var sudoku = Sudoku.generateFromSeed(123)
    private val activatedCoord = Coordinate(4, 0)

    @Test
    fun `it should set the cell to correct number if thumb moves to the right location`() {
        val d = hypotenuse(21f, 21f)

        val offsetToNumber = mapOf(
            Offset(0f, 0f) to 5,


            Offset(-d, -d) to 1,
            Offset(0f, -21f) to 2,
            Offset(d, -d) to 3,
            Offset(21f, 0f) to 6,
            Offset(d, d) to 9,
            Offset(0f, 21f) to 8,
            Offset(-d, d) to 7,
            Offset(-21f, 0f) to 4,

        )
        for ((offset, number) in offsetToNumber) {
            val nextSudoku = rotaryWithCenterActivatedCellCalculator.calculateNextSudoku(
                sudoku,
                Offset(0f, 0f),
                offset,
                activatedCoord
            )
            assertEquals(number, nextSudoku.getSubmittedValueAt(activatedCoord))
        }
    }
}