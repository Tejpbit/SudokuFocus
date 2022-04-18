package dev.fredag.sudokufocus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fredag.sudokufocus.datastore.SudokuEntity
import dev.fredag.sudokufocus.model.Sudoku
import dev.fredag.sudokufocus.model.SudokuSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SudokuRepoViewModel @Inject constructor(
    private val sudokuRepository: SudokuRepository
) : ViewModel() {

    val activeSudokuSource = MutableStateFlow<SudokuSource?>(null)

    val activeSudokuEntity: Flow<SudokuEntity?> = flow {
        activeSudokuSource.collect() {
            it?.let {
                sudokuRepository.getLastPlayedForSource(it).collect { lastPlayedForSource ->
                    emit(lastPlayedForSource)
                }
            }
        }
    }

    val savedSudokusForCurrentActiveSudoku = flow {
        viewModelScope.launch(Dispatchers.IO) {
            activeSudokuSource.collect {
                it?.let {
                    sudokuRepository.getSavedSudoku(it).collect { sudokusWithSameSource ->
                        emit(sudokusWithSameSource)
                    }
                }
            }
        }
    }


    val campaignSudokos = sudokuRepository.campaignSudokus

    fun saveSudoku(sudokuEntity: SudokuEntity): SudokuEntity {
        viewModelScope.launch(Dispatchers.IO) {
            sudokuRepository.saveSudoku(sudokuEntity)
        }
        return sudokuEntity
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSudoku(sudokuSource: SudokuSource): Flow<List<SudokuEntity>> {
        return sudokuRepository.getSavedSudoku(sudokuSource)
    }

    fun hasOngoingSudokus(): Flow<Boolean> {
        return sudokuRepository.getOngoingSudokus().map { it.isNotEmpty() }
    }

    fun getOngoingSudokus(): Flow<List<SudokuEntity>> {
        return sudokuRepository.getOngoingSudokus()
    }

    fun setCurrentGame(source: SudokuSource) {
        viewModelScope.launch(Dispatchers.IO) {
            if (sudokuRepository.savedGameDoesNotExistsForSource(source)) {
                sudokuRepository.saveSudoku(
                    SudokuEntity(
                        Sudoku.from(source),
                        source = source,
                        tag = ""
                    )
                )
            }
            activeSudokuSource.value = source
        }

    }
}