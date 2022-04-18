package dev.fredag.sudokufocus

import dev.fredag.sudokufocus.datastore.SudokuDao
import dev.fredag.sudokufocus.datastore.SudokuEntity
import dev.fredag.sudokufocus.model.SudokuSource
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton


interface SudokuRepository {
    val campaignSudokus: StateFlow<List<SudokuSource.ValidUnparsed>>
    fun saveSudoku(sudokuEntity: SudokuEntity)
    fun getLastPlayed(): Flow<SudokuEntity?>
    fun getLastPlayedForSource(sudokuSource: SudokuSource): Flow<SudokuEntity?>
    fun getOngoingSudokus(): Flow<List<SudokuEntity>>
    fun getLatestSudoku(): Flow<List<SudokuEntity>>
    fun getSavedSudoku(sudokuSource: SudokuSource): Flow<List<SudokuEntity>>
    fun savedGameDoesNotExistsForSource(source: SudokuSource): Boolean
}

@Singleton
class LocalSudokuRepository @Inject constructor(
    private val sudokuDao: SudokuDao
) : SudokuRepository {


    override val campaignSudokus: StateFlow<List<SudokuSource.ValidUnparsed>> = MutableStateFlow(
        sudokuLibrary.mapNotNull { it.validate() }
    ).asStateFlow()

    override fun saveSudoku(sudokuEntity: SudokuEntity) {
        sudokuDao.insertWithTimestamp(sudokuEntity)
    }

    override fun getLastPlayed(): Flow<SudokuEntity?> {
        return sudokuDao.getLastModifiedSudoku()
    }

    override fun getLastPlayedForSource(sudokuSource: SudokuSource): Flow<SudokuEntity?> {
//        return sudokuDao.findLastModifiedBySource(sudokuSource)
        return sudokuDao.findBySource(sudokuSource).map {
            if (it.isEmpty()) null else it.minByOrNull { it.modifiedAt }!!
        }

    }

    override fun getSavedSudoku(sudokuSource: SudokuSource): Flow<List<SudokuEntity>> {
        return sudokuDao.findBySource(sudokuSource)
    }

    override fun savedGameDoesNotExistsForSource(source: SudokuSource): Boolean {
        return sudokuDao.numberOfGamesForSource(source) == 0
    }

    override fun getOngoingSudokus() = sudokuDao.getOngoing()

    override fun getLatestSudoku(): Flow<List<SudokuEntity>> {
        TODO("Not yet implemented")
    }


}
