package dev.fredag.sudokufocus.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Serializable
data class Sudoku(
    private val submittedGrid: Grid<Int>,
    private val lockedCoordinates: Set<Coordinate>,
    private val guessGrid: Grid<Set<Int>>
) {
    fun getSubmittedValueAt(coord: Coordinate): Int? {
        return submittedGrid.getAt(coord)
    }

    fun getGuessedValuesAt(coord: Coordinate): Set<Int>? {
        return guessGrid.getAt(coord)
    }

    fun isLocked(coord: Coordinate): Boolean {
        return lockedCoordinates.contains(coord)
    }

    fun submitCell(number: Int, coord: Coordinate): Sudoku {
        if (!lockedCoordinates.contains(coord)) {
            val submittedGrid = submittedGrid.write(coord, number)
            return Sudoku(submittedGrid, lockedCoordinates, guessGrid)
        }
        return this
    }

    fun clearCell(coord: Coordinate): Sudoku {
        if (!lockedCoordinates.contains(coord)) {
            val submittedGrid = submittedGrid.clear(coord)
            return Sudoku(submittedGrid, lockedCoordinates, guessGrid)
        }
        return this
    }

    fun autoFillGuesses(): Sudoku {
        var newGuessGrid = Grid<Set<Int>>()
        submittedGrid.getEmptyCoordinatesInRange(Coordinate(0, 0), Coordinate(8, 8)).forEach {
            newGuessGrid = newGuessGrid.write(
                it,
                possibleTokensForCoordinate(submittedGrid, it).toMutableSet()
            )
        }
        return Sudoku(submittedGrid, lockedCoordinates, newGuessGrid)
    }

    fun clearGuessesOnCell(coord: Coordinate): Sudoku {
        return Sudoku(submittedGrid, lockedCoordinates, guessGrid.clear(coord))
    }

    fun toggleGuessOnCell(number: Int, coord: Coordinate): Sudoku {
        if (!lockedCoordinates.contains(coord)) {
            val guessGrid = guessGrid.modify(coord) {
                it?.let {
                    val newGuesses = it.toMutableSet()
                    if (!it.contains(number)) {
                        newGuesses.add(number)
                    } else {
                        newGuesses.remove(number)
                    }
                    newGuesses
                } ?: setOf(number)
            }
            return Sudoku(submittedGrid, lockedCoordinates, guessGrid)
        }
        return this

    }

    fun contains9UniqueTokens(list: List<Int>): Boolean {
        return list.size == 9 && list.size == list.toSet().size
    }

    fun isUntouched() =
        guessGrid.isEmpty() && this.submittedGrid.getOccupiedCoordinates() == this.lockedCoordinates


    fun isSolved(): Boolean {
        for (i in 0..8) {
            val coordinatesInBlockTo = Coordinate(0, i).getCoordinatesInBlockTo(Coordinate(8, i))
            val numbersInRow = coordinatesInBlockTo
                .mapNotNull {
                    submittedGrid.getAt(it)
                }

            val coordinatesInBlockTo1 = Coordinate(i, 0).getCoordinatesInBlockTo(Coordinate(i, 8))
            val numbersInColumn = coordinatesInBlockTo1
                .mapNotNull {
                    submittedGrid.getAt(it)
                }

            val upperLeftOfSquare = Coordinate((i % 3) * 3, (i / 3) * 3)
            val coordinatesInBlockTo2 =
                upperLeftOfSquare.getCoordinatesInBlockTo(upperLeftOfSquare.move(2, 2))
            val numbersIn3by3 =
                coordinatesInBlockTo2
                    .mapNotNull { submittedGrid.getAt(it) }

            if (
                !contains9UniqueTokens(numbersInRow) ||
                !contains9UniqueTokens(numbersInColumn) ||
                !contains9UniqueTokens(
                    numbersIn3by3
                )
            ) {
                return false
            }
        }


        return true
    }

    fun isValidCoordinate(coordinate: Coordinate): Boolean {
        return coordinate.x in 0..8 && coordinate.y in 0..8
    }

    fun isSubmittable(coordinate: Coordinate): Boolean {
        return this.isValidCoordinate(coordinate) && !this.isLocked(coordinate)
    }

    companion object {

        fun generateFromSeed(seed: SudokuSource.SudokuSeed): Sudoku {
            return SudokuGenerator.generate(Random(seed.value))
        }

        fun parse(str: String): Sudoku? {
            val parts = str.split(";")
            var grid: Grid<Int> = Grid()

            if (parts.size != 2 || (parts[0].length != parts[1].length) || parts[0].length != 81) {
                return null
            }

            for (char in parts[0]) {
                for (y in 0..8) {
                    for (x in 0..8) {
                        if (char.isDigit()) {
                            grid = grid.write(Coordinate(x, y), char.digitToInt())
                        }
                    }
                }
            }

            return Sudoku(grid, grid.getOccupiedCoordinates(), Grid())
        }

        fun from(sudokuSource: SudokuSource): Sudoku {
            return when (sudokuSource) {
                is SudokuSource.Parsed -> sudokuSource.sudoku
                is SudokuSource.SudokuSeed -> generateFromSeed(sudokuSource)
                is SudokuSource.Unparsed -> TODO()
                is SudokuSource.ValidUnparsed -> sudokuSource.parse()
                is SudokuSource.Daily -> {
                    val date = sudokuSource.date
                    val dateSeed = date.year * 1000 +
                            date.month.value * 10 +
                            date.dayOfMonth
                    generateFromSeed(SudokuSource.SudokuSeed(dateSeed))
                }
            }
        }
    }
}

@Serializable
sealed class SudokuSource {
    @Serializable
    class SudokuSeed(val value: Int) : SudokuSource() {
        companion object {

            fun random(): SudokuSeed {
                return SudokuSeed(Random.nextInt())
            }

            fun day(date: LocalDate = LocalDate.now()): SudokuSeed {
                return SudokuSeed("${date.year}${date.dayOfYear}".toInt())
            }
        }
    }

    @Serializable
    class Daily(val date: @Serializable(with = LocalDateSerializer::class) LocalDate) :
        SudokuSource()

    @Serializable
    class Parsed(val sudoku: Sudoku) : SudokuSource()

    @Serializable
    class Unparsed(val string: String) : SudokuSource() {
        fun validate(): ValidUnparsed? {
            val parts = string.split(";")

            if ((parts.size != 2 || (parts[0].length != parts[1].length) || parts[0].length != 81)) {
                return null
            }

            return if (parts[0].all { c -> c.isDigit() || c == '.' } &&
                parts[1].all { c -> c.isDigit() || c == '.' }) {
                ValidUnparsed(string)

            } else {
                null
            }

        }
    }

    @Serializable
    class ValidUnparsed internal constructor(val string: String) : SudokuSource() {
        fun parse(): Sudoku {
            val parts = string.split(";")
            var grid: Grid<Int> = Grid()

            val chars = parts[0].iterator()

            for (y in 0..8) {
                for (x in 0..8) {
                    val char = chars.next()
                    if (char.isDigit()) {
                        grid = grid.write(Coordinate(x, y), char.digitToInt())
                    }
                }
            }

            return Sudoku(grid, grid.getOccupiedCoordinates(), Grid())
        }
    }
}

@Serializer(forClass = LocalDate::class)
object LocalDateSerializer : KSerializer<LocalDate> {
    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}

@Serializable
data class Grid<T>(private val grid: Map<Coordinate, T> = mapOf()) :
    Iterable<Pair<Coordinate, T>>, Cloneable, Collection<Pair<Coordinate, T>> {

    public override fun clone(): Grid<T> {
        return Grid(grid)
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

    fun write(coord: Coordinate, value: T): Grid<T> {
        val new = grid.toMutableMap()
        new[coord] = value
        return Grid(new)
    }

    fun modify(coord: Coordinate, updateFunction: (prev: T?) -> T): Grid<T> {
        val new = grid.toMutableMap()
        new[coord] = updateFunction(grid[coord])
        return Grid(new)
    }

    fun clear(coord: Coordinate): Grid<T> {
        val new = grid.toMutableMap()
        new.remove(coord)
        return Grid(new)
    }

    override fun iterator(): Iterator<Pair<Coordinate, T>> {
        return grid.entries.map { e -> Pair(e.key, e.value) }.iterator()
    }

    override val size: Int
        get() = grid.keys.size

    override fun contains(element: Pair<Coordinate, T>) = grid[element.first] == element.second

    override fun containsAll(elements: Collection<Pair<Coordinate, T>>) = elements.all(::contains)
    override fun isEmpty() = size == 0
}

@Serializable
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

    fun move(x: Int, y: Int) = Coordinate(this.x + x, this.y + y)

    override fun toString(): String {
        return "($x,$y)"
    }
}
