package dev.fredag.sudokufocus

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
import java.lang.IllegalArgumentException

interface ActivatedCellCalculator {
    fun calculateNextSudoku(
        sudoku: Sudoku,
        touchDownPos: Offset,
        releasePos: Offset,
        activatedCoord: Coordinate
    ): Sudoku
}

class GridActivatedCellCalulator(private val size: Float, private val fields: List<String>) :
    ActivatedCellCalculator {
    init {
        if (fields.size != 9)
            throw IllegalArgumentException("fields must be length 9")
        if (size <= 0)
            throw IllegalArgumentException("size must be greater than zero")

    }

    private fun calculateActivatedCell(touchDownPos: Offset, releasePos: Offset): String? {
        val cellDimension = size / 3
        val topLeft = with(touchDownPos) { Offset(x - size / 2, y - size / 2) }

        val rectangles = mutableListOf<Pair<Offset, Size>>()
        for (j in 0..2) {
            for (i in 0..2) {
                val cellTopLeft =
                    with(topLeft) { Offset(x + cellDimension * i, y + cellDimension * j) }
                val cellSize = Size(cellDimension, cellDimension)
                rectangles.add(Pair(cellTopLeft, cellSize))
            }
        }

        for ((i, rect) in rectangles.withIndex()) {
            if (isInside(releasePos, rect.first, rect.second)) {
                return fields[i]
            }
        }
        return null
    }

    override fun calculateNextSudoku(
        sudoku: Sudoku,
        touchDownPos: Offset,
        releasePos: Offset,
        activatedCoord: Coordinate
    ): Sudoku {
        return calculateActivatedCell(touchDownPos, releasePos)?.let { activatedToken ->
            val previouslySubmittedValue = sudoku.getSubmittedValueAt(activatedCoord)
            val previouslyGuessedValue = sudoku.getGuessedValuesAt(activatedCoord)

            return when {
                (previouslySubmittedValue == null && previouslyGuessedValue == null) -> {
                    sudoku.submitCell(activatedToken.toInt(), activatedCoord)
                }
                previouslySubmittedValue == activatedToken.toInt() && previouslyGuessedValue == null -> {
                    sudoku.clearCell(activatedCoord)
                }
                previouslySubmittedValue != null && previouslyGuessedValue == null -> {
                    sudoku
                        .clearCell(activatedCoord)
                        .toggleGuessOnCell(activatedToken.toInt(), activatedCoord)
                        .toggleGuessOnCell(previouslySubmittedValue, activatedCoord)
                }
                previouslySubmittedValue == null && previouslyGuessedValue != null &&
                        previouslyGuessedValue.contains(activatedToken.toInt()) -> {
                    sudoku
                        .submitCell(activatedToken.toInt(), activatedCoord)
                        .clearGuessesOnCell(activatedCoord)
                }
                previouslySubmittedValue == null && previouslyGuessedValue != null -> {
                    sudoku
                        .toggleGuessOnCell(activatedToken.toInt(), activatedCoord)
                }
                else -> {
                    sudoku
                }
            }
        } ?: sudoku
    }
}

class RotartyActivatedCellCelculator() : ActivatedCellCalculator {
    override fun calculateNextSudoku(
        sudoku: Sudoku,
        touchDownPos: Offset,
        releasePos: Offset,
        activatedCoord: Coordinate
    ): Sudoku {
        TODO("Not yet implemented")
    }

}