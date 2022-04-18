package dev.fredag.sudokufocus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.fredag.sudokufocus.model.SudokuSource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Campaign(navController: NavHostController, sudokuRepoViewModel: SudokuRepoViewModel) {

    val campaignSudokus by sudokuRepoViewModel.campaignSudokos.collectAsState()

    Column {
        ScreenHeader("Campaign")
        LazyVerticalGrid(
            cells = GridCells.Adaptive(64.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(color = MaterialTheme.colors.background),
        ) {
            items(campaignSudokus.size) { index ->
                Thing(index + 1) {
                    sudokuRepoViewModel.setCurrentGame(campaignSudokus[index])
                    navController.navigate(Routes.gamePlay)
                }
            }
        }
    }
}

@Composable
fun Thing(index: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .height(64.dp)
            .padding(5.dp)
            .border(
                1.dp,
                MaterialTheme.colors.onBackground,
                RectangleShape
            )
            .background(MaterialTheme.colors.background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$index",
            modifier = Modifier,
        )
    }
}

val sudokus = listOf(
    SudokuSource.Unparsed(".25..163914..5.2..8.372..51..74...9..91...56.2....678.93..12...5.2384.1.4.8.....5;725841639149653278863729451687435192391278564254196783936512847572384916418967325")
)

/*
The puzzles are strings, 162 characters long,
81 characters with numbers and dashes or dots
where the blanks are going to be, then another
81 with the solution. Then columns for each
of the stats, like how many singles, doubles, etc.
*/