package dev.fredag.sudokufocus.model

import kotlin.math.max
import kotlin.math.min

class Sudoku {
    private val submittedGrid = Grid<Int>()
    private val guessGrid = Grid<MutableSet<Int>>()

    init {
        submitCell(0, Coordinate(0, 0))
        toggleGuessOnCell(5, Coordinate(0, 1))
        toggleGuessOnCell(4, Coordinate(0, 1))
    }

    fun getSubmittedValueAt(coord: Coordinate): Int? {
        return submittedGrid.getAt(coord)
    }

    fun getGuessedValuesAt(coord: Coordinate): Set<Int>? {
        return guessGrid.getAt(coord)
    }

    fun submitCell(number: Int, coord: Coordinate) {
        submittedGrid.write(coord, number)
    }

    fun clearCell(coord: Coordinate) {
        submittedGrid.clear(coord)
    }

    fun toggleGuessOnCell(number: Int, coord: Coordinate) {
        guessGrid.modify(coord) {
            it?.let {
                if (!it.contains(number)) {
                    it.add(number)
                } else {
                    it.remove(number)
                }
                it
            } ?: mutableSetOf(number)
        }
    }
}

data class Grid<T>(private val grid: MutableMap<Coordinate, T> = mutableMapOf()) {

    fun getAt(coord: Coordinate): T? {
        return grid[coord]
    }

    fun write(coord: Coordinate, value: T) {
        grid[coord] = value
    }

    fun modify(coord: Coordinate, updateFunction: (prev: T?) -> T) {
        grid[coord] = updateFunction(grid[coord])
    }

    fun clear(coord: Coordinate) {
        grid.remove(coord)
    }
}

data class Coordinate(val x: Int, val y: Int) {
    /**
     * Block selection
     */
    fun getCoordinatesInBlockTo(other: Coordinate): List<Coordinate> {
        val minX = min(x, other.x)
        val maxX = max(x, other.x)
        val minY = min(y, other.y)
        val maxY = max(y, other.y)
        val ret = mutableListOf<Coordinate>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                ret.add(Coordinate(x, y))
            }
        }
        return ret
    }
}
