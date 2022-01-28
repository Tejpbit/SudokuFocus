package dev.fredag.sudokufocus

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.lang.IllegalArgumentException

interface ActivatedCellCalculator {
    fun calculateActivatedCell(touchDownPos: Offset, releasePos: Offset): String?
}

class GridActivatedCellCalulator(private val size: Float, private val fields: List<String>): ActivatedCellCalculator {
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
                val cellTopLeft = with(topLeft) { Offset(x + cellDimension * i, y + cellDimension * j) }
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
}

class RotartyActivatedCellCelculator() : ActivatedCellCalculator {
    override fun calculateActivatedCell(touchDownPos: Offset, releasePos: Offset): String? {
        return null
    }

}