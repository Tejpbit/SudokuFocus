package dev.fredag.sudokufocus.model

import org.junit.Assert.*

import org.junit.Test

class SudokuTest {

    @Test
    fun toggleGuessOnCell() {
        var s = Sudoku(Grid(), setOf(), Grid())

        s = s.toggleGuessOnCell(5, Coordinate(5, 5))
        s = s.toggleGuessOnCell(6, Coordinate(5, 5))

        assertTrue(s.getGuessedValuesAt(Coordinate(5, 5))?.contains(5) == true)
        assertTrue(s.getGuessedValuesAt(Coordinate(5, 5))?.contains(6) == true)
    }

    @Test
    fun isSolvedWorks() {
        val s = Sudoku.generateSolvedFromSeed(SudokuSeed.day())
        assertTrue(s.isSolved())
    }
}