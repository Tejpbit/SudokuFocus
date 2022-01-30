package dev.fredag.sudokufocus

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
import java.lang.IllegalArgumentException
import kotlin.math.PI
import kotlin.math.abs

/**
 * ActivatedCellCalculator allows different implementations for the number selector that pops up on screen when a user
 * interacts with the sudoku.
 *
 */
interface ActivatedCellCalculator {

    /**
     * Derive the activated number. Used for drawing and helps with creating the next sudoku
     */
    fun calculateActivatedCell(touchDownPos: Offset, releasePos: Offset): String?

    /**
     * @param sudoku: The current sudoku state
     * @param touchDownPos center coordinate of cell that the user started touching
     * @param releasePos coordinate where user releases the gesture
     * @param activatedCoord the coordinate that that this gesture affects in the sudoku
     */
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

    override fun calculateActivatedCell(touchDownPos: Offset, releasePos: Offset): String? {
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
            sudokuInteractionStrategy(sudoku, activatedCoord, activatedToken)
        } ?: sudoku
    }
}

class RotaryActivatedCellCelculator(val zones: List<String>) : ActivatedCellCalculator {
    override fun calculateActivatedCell(touchDownPos: Offset, releasePos: Offset): String? {
        TODO("Not yet implemented")
    }

    override fun calculateNextSudoku(
        sudoku: Sudoku,
        touchDownPos: Offset,
        releasePos: Offset,
        activatedCoord: Coordinate
    ): Sudoku {
        TODO("Not yet implemented")
    }

}

class RotaryWithCenterActivatedCellCelculator(
    val outerRadius: Float,
    val centerRadius: Float,
    val zones: List<String>,
    val center: String
) : ActivatedCellCalculator {
    override fun calculateActivatedCell(touchDownPos: Offset, releasePos: Offset): String? {
        val thumbDistanceFromCenter =
            hypotenuse(abs(touchDownPos.x - releasePos.x), abs(touchDownPos.y - releasePos.y))

        if (thumbDistanceFromCenter < centerRadius) {
            return center
        }


        val radiansPerZone = (2 * PI / zones.size).toFloat()
        var (_, thumbAngle) = PolarCoordinate.fromTwoCartesianCoordinates(
            CartesianCoordinate(touchDownPos.x, touchDownPos.y),
            CartesianCoordinate(releasePos.x, releasePos.y)
        )

        for ((zoneIndex, zone) in zones.withIndex()) {
            val i = zoneIndex.toFloat()
            val startLineAngle =
                i * radiansPerZone +
                        radiansPerZone / 2 +// offset by half a sector to not have horizontal lines
                        Math.PI.toFloat() // offset by half to have first zone be up to the left.
            val endLineAngle = startLineAngle + radiansPerZone

            if (thumbAngle < startLineAngle) {
                thumbAngle += (Math.PI*2).toFloat()
            }

            if (startLineAngle < thumbAngle && thumbAngle < endLineAngle) {
                return zone
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
            sudokuInteractionStrategy(sudoku, activatedCoord, activatedToken)
        } ?: sudoku

    }


}

private fun sudokuInteractionStrategy(
    sudoku: Sudoku,
    activatedCoord: Coordinate,
    activatedToken: String
): Sudoku {
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
}
