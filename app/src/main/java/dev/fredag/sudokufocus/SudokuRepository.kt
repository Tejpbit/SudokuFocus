package dev.fredag.sudokufocus

import dev.fredag.sudokufocus.model.SudokuSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


interface SudokuRepository {
    val campaignSudokus: StateFlow<List<SudokuSource.ValidUnparsed>>
}

@Singleton
class LocalSudokuRepository @Inject constructor() : SudokuRepository {

    override val campaignSudokus: StateFlow<List<SudokuSource.ValidUnparsed>> =  MutableStateFlow(
            listOf(
                SudokuSource.Unparsed(".25..163914..5.2..8.372..51..74...9..91...56.2....678.93..12...5.2384.1.4.8.....5;725841639149653278863729451687435192391278564254196783936512847572384916418967325")
                    .validate()!!,
                SudokuSource.Unparsed(".25..163914..5.2..8.372..51..74...9..91...56.2....678.93..12...5.2384.1.4.8.....5;725841639149653278863729451687435192391278564254196783936512847572384916418967325")
                    .validate()!!
            )
        ).asStateFlow()
}

