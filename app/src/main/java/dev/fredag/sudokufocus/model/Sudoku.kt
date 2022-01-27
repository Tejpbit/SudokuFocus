package dev.fredag.sudokufocus.model

class Sudoku {
    private val submittedGrid = Grid<Int>()
    private val guessGrid = Grid<MutableSet<Int>>()

    fun submitCell(number: Int, x: Int, y: Int) {
        submittedGrid.write(Coordinate(x,y), number)
    }

    fun clearCell(number: Int, x: Int, y: Int) {
        submittedGrid.clear(Coordinate(x,y))
    }

    fun toggleGuessOnCell(number: Int, x: Int, y: Int) {
        guessGrid.modify(Coordinate(x, y)) {
            it?.let {
                if (it.contains(number)) {
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

data class Coordinate(val x: Int, val y: Int)
