package dev.fredag.sudokufocus.model

import org.junit.Assert.*

import org.junit.Test

class SudokuTest {

    @Test
    fun toggleGuessOnCell() {
        val s = Sudoku()

        s.toggleGuessOnCell(5, Coordinate(5, 5))
        s.toggleGuessOnCell(6, Coordinate(5, 5))

        assertTrue(s.getGuessedValuesAt(Coordinate(5, 5))?.contains(5) == true)
        assertTrue(s.getGuessedValuesAt(Coordinate(5, 5))?.contains(6) == true)
    }
}