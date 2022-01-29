package dev.fredag.sudokufocus.model

import java.util.*
import kotlin.random.Random

class SudokuGenerator private constructor() {
    companion object {
        fun generate(random: Random): Sudoku {
            val firstRow = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9).shuffled(random)
            var rows = listOf(0, 3, 6, 1, 4, 7, 2, 5, 8).map {
                val l = firstRow.toMutableList()
                Collections.rotate(l, it)
                l
            }

            val g = mutableMapOf<Coordinate, Int>()
            for ((i, row) in rows.withIndex()) {
                for ((j, cell) in row.withIndex()) {
                    g[Coordinate(j, i)] = cell
                }
            }

            val sudokuGenerator = SudokuGenerator()
            val grid = Grid(g)
            sudokuGenerator.removeRandomCellUntilAmbigeous(grid, random)


            return Sudoku(grid)
        }
    }

    fun removeRandomCellUntilAmbigeous(grid: Grid<Int>, random: Random): Grid<Int> {
        var prevGrid = grid.clone()
        var _grid = grid.clone()
        while (isSolvableWithoutGuessing(grid)) {
            prevGrid = _grid.clone()
            _grid = removeRandomCell(grid, random)
        }
        return prevGrid
    }

    fun removeRandomCell(grid: Grid<Int>, random: Random): Grid<Int> {
        val randomCoord = grid.getOccupiedCoordinates().random(random)
        grid.clear(randomCoord)
        return grid
    }
}


fun possibleTokensForCoordinate(grid: Grid<Int>, coordinate: Coordinate): Set<Int> {
    val allTokens = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    //var occupiedCoords = grid.getOccupiedCoordinates().toSet()
    val busyTokens = grid.getOccupiedCellsOnRowAndCol(coordinate).toSet()
    return allTokens.minus(busyTokens)
}

fun isSolvableWithoutGuessing(grid: Grid<Int>): Boolean {
    val _grid = grid.clone()
    val remainingCoords =
        _grid.getEmptyCoordinatesInRange(Coordinate(0, 0), Coordinate(8, 8)).toMutableSet()
    if (remainingCoords.isEmpty())
        return true

    //1 while remaining coords is not empty
    while (remainingCoords.isNotEmpty()) {
        //2 pair each coord with possible tokens for them.
        val possibleTokensForCoords = remainingCoords.map {
            Pair(it, possibleTokensForCoordinate(_grid, it))
        }
        //3 if any of those pairs only have one token.
        val coordsThatOnlyHaveOnePossibility =
            possibleTokensForCoords.filter { it.second.size == 1 }
                .map { Pair(it.first, it.second.take(1)[0]) }

        //3.5 There are remaining coords but none of them have a unique token to place.
        if (coordsThatOnlyHaveOnePossibility.isEmpty()) {
            return false
        }
        //4 fill it. and remove that coord from remaining coords
        for ((coord, token) in coordsThatOnlyHaveOnePossibility) {
            _grid.write(coord, token)
            remainingCoords.remove(coord)
        }
        // Go to 2
        // done if there are no remaining coords or if 3.5 happens
    }

    return true
}