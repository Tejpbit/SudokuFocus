package dev.fredag.sudokufocus

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SudokuRepoViewModel @Inject constructor(
    sudokuRepository: SudokuRepository
) : ViewModel() {

    val campaignSudokos = sudokuRepository.campaignSudokus

}