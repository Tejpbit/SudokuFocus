package dev.fredag.sudokufocus.model

import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class Sudoku(private val submittedGrid: Grid<Int>) {
    private val lockedCoordinates: Set<Coordinate> = submittedGrid.map {
        it.first
    }.toSet()
    private val guessGrid = Grid<MutableSet<Int>>()

    constructor() : this(Grid(mutableMapOf()))

    init {
    }

    fun getSubmittedValueAt(coord: Coordinate): Int? {
        return submittedGrid.getAt(coord)
    }

    fun getGuessedValuesAt(coord: Coordinate): Set<Int>? {
        return guessGrid.getAt(coord)
    }

    fun isLocked(coord: Coordinate): Boolean {
        return lockedCoordinates.contains(coord)
    }

    fun submitCell(number: Int, coord: Coordinate) {
        if (!lockedCoordinates.contains(coord))
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

    companion object {
        fun generateDaily(): Sudoku {
            val now = LocalDateTime.now()
            return generateFromSeed("${now.year}${now.dayOfYear}".toInt())
        }

        fun generateFromSeed(seed: Int): Sudoku {
            return SudokuGenerator.generate(Random(seed))
        }
    }
}

data class Grid<T>(private val grid: MutableMap<Coordinate, T> = mutableMapOf()) :
    Iterable<Pair<Coordinate, T>>, Cloneable {

    public override fun clone(): Grid<T> {
        return Grid(grid.toMutableMap())
    }

    fun getOccupiedCoordinates() = grid.keys

    fun getEmptyCoordinatesInRange(from: Coordinate, to: Coordinate): Set<Coordinate> {
        val allCoords = from.getCoordinatesInBlockTo(to).toSet()
        return allCoords.minus(getOccupiedCoordinates())
    }

    fun getOccupiedCellsOnRow(row: Int): List<T> {
        return grid.keys.filter { it.y == row }.map { grid[it]!! }.toList()
    }

    fun getOccupiedCellsOnColumn(col: Int): List<T> {
        return grid.keys.filter { it.x == col }.map { grid[it]!! }.toList()
    }

    fun getOccupiedCellsOnRowAndCol(coord: Coordinate): List<T> {
        return getOccupiedCellsOnRow(coord.y) + getOccupiedCellsOnColumn(coord.x)
    }

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

    override fun iterator(): Iterator<Pair<Coordinate, T>> {
        return grid.entries.map { e -> Pair(e.key, e.value) }.iterator()
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
