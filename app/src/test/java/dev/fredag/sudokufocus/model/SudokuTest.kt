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
//        val s = Sudoku.generateSolvedFromSeed(SudokuSeed.day())
//        assertTrue(s.isSolved())
    }

    @Test
    fun canParseFromString() {
        val sudokuSource = SudokuSource.Unparsed(".25..163914..5.2..8.372..51..74...9..91...56.2....678.93..12...5.2384.1.4.8.....5;725841639149653278863729451687435192391278564254196783936512847572384916418967325").validate()!!
        val sudoku = sudokuSource.parse()
        assertEquals(2, sudoku.getSubmittedValueAt(Coordinate(1, 0)))
    }
}