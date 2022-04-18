package dev.fredag.sudokufocus

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import com.guru.fontawesomecomposelib.FaIcons
import dev.fredag.sudokufocus.datastore.SudokuEntity
import dev.fredag.sudokufocus.model.SudokuSource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedGamesScreen(
    navController: NavController,
    sudokuRepoViewModel: SudokuRepoViewModel = viewModelAtRoute(navController, Routes.root)
) {
    val context = LocalContext.current

    val ongoingSudokuSources by sudokuRepoViewModel.getOngoingSudokus().collectAsState(listOf())

    Column {
        ScreenHeader("Saved games")
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            ongoingSudokuSources.map { sudokuEntity ->
                val icon = iconFromSudokuEntity(sudokuEntity)
                val text = gameNameFromSudokuEntity(sudokuEntity)
                item(key = sudokuEntity.uid) {
                    var gameToRemove: SudokuEntity? by remember { mutableStateOf(null) }
                    val offsetX = remember { Animatable(0f) }
                    SavedGameRow(
                        modifier = Modifier
                            .animateItemPlacement()
                            .swipeToDelete(offsetX, 300f) {
                                gameToRemove = sudokuEntity
                            },
                        text = text, icon, onClick = {
                            sudokuRepoViewModel.setCurrentGame(sudokuEntity.source)
                            navController.navigate(Routes.gamePlay)
                        })

                    LaunchedEffect(key1 = gameToRemove) {
                        if (gameToRemove == null) {
                            offsetX.animateTo(0f)
                        }
                    }
                    gameToRemove?.let {
                        RemoveDialog(
                            gameName = text,
                            gameIcon = icon,
                            onRemoveClicked = {
                                sudokuRepoViewModel.deleteSudoku(it)
                                Toast.makeText(context, "Deleted game", Toast.LENGTH_SHORT).show()
                            },
                            dismiss = {
                                gameToRemove = null

                            })

                    }
                }

            }
        }
    }

}

private fun gameNameFromSudokuEntity(sudokuEntity: SudokuEntity): String {
    return when (sudokuEntity.source) {
        is SudokuSource.Parsed -> "From parsed ${sudokuEntity.source}"
        is SudokuSource.SudokuSeed -> "From Seed ${sudokuEntity.source.value}"
        is SudokuSource.Unparsed -> "From unparsed"
        is SudokuSource.ValidUnparsed -> "From valid unparsed ${sudokuEntity.uid}"
        is SudokuSource.Daily -> "Daily from ${sudokuEntity.source.date}"
    }
}

private fun iconFromSudokuEntity(sudokuEntity: SudokuEntity): FaIconType {
    return when (sudokuEntity.source) {
        is SudokuSource.Parsed -> FaIcons.Trophy
        is SudokuSource.SudokuSeed -> FaIcons.Dice
        is SudokuSource.Unparsed -> FaIcons.Dice
        is SudokuSource.ValidUnparsed -> FaIcons.Trophy
        is SudokuSource.Daily -> FaIcons.CalendarDay
    }
}

@Composable
private fun RemoveDialog(
    gameName: String,
    gameIcon: FaIconType,
    onRemoveClicked: () -> Unit,
    dismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = {
            dismiss()
        },
        title = {
            Text(text = "Remove Game")
        },
        text = {
            Column() {
                Text("Are you sure you want to remove")
                SavedGameRow(
                    modifier = Modifier
                        .border(BorderStroke(1.dp, MaterialTheme.colors.onBackground))
                        .padding(10.dp)
                        .fillMaxWidth(),
                    text = gameName,
                    icon = gameIcon
                )

            }
        },
        confirmButton = {
            Button(

                onClick = {
                    dismiss()
                    onRemoveClicked()
                }) {
                Text("Remove it")
            }
        },
        dismissButton = {
            Button(

                onClick = {
                    dismiss()
                }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SavedGameRow(
    modifier: Modifier = Modifier,
    text: String,
    icon: FaIconType,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 18.sp)
        FaIcon(faIcon = icon, tint = MaterialTheme.colors.onBackground, size = 36.dp)
    }
}

