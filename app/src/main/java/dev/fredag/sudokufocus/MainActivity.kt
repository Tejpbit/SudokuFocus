package dev.fredag.sudokufocus

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dev.fredag.sudokufocus.Routes.campaign
import dev.fredag.sudokufocus.Routes.gamePlay
import dev.fredag.sudokufocus.Routes.home
import dev.fredag.sudokufocus.Routes.root
import dev.fredag.sudokufocus.Routes.savedGames
import dev.fredag.sudokufocus.Routes.settings
import dev.fredag.sudokufocus.Routes.splash_screen
import dev.fredag.sudokufocus.model.SudokuSource
import dev.fredag.sudokufocus.ui.theme.SudokuFocusTheme
import java.time.LocalDate

@HiltAndroidApp
class Application : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            SudokuFocusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Router(navController)
                }
            }
        }
    }
}

object Routes {
    const val root = "root"
    const val home = "home"
    const val splash_screen = "splash_screen"
    const val campaign = "campaign"
    const val settings = "settings"
    const val gamePlay = "game/play"
    const val gameSeed = "game/{seed}"
    const val savedGames = "game/saved"
    const val gameCampaign = "game/campaign/{index}"
}

@Composable
fun Router(navController: NavHostController) {
    val builder: NavGraphBuilder.() -> Unit = {
        composable(splash_screen) {
            SplashScreen(navController = navController)
        }

        composable(home) { Home(navController) }
        composable(campaign) {
            Campaign(
                navController, viewModelAtRoute(
                    navController,
                    route = root
                )
            )
        }
        composable(settings) {
            Settings(viewModelAtRoute(navController, root))
        }
        composable(savedGames) {
            SavedGamesScreen(navController)
        }
        composable(gamePlay) {
            SavedSudoku(
                navController,
                viewModelAtRoute(navController, root),
                viewModelAtRoute(navController, root),
            )
        }
    }
    NavHost(navController = navController, startDestination = home, route = root, builder = builder)
}

@Composable
fun SavedGamesScreen(
    navController: NavController,
    sudokuRepoViewModel: SudokuRepoViewModel = viewModelAtRoute(navController, root)
) {
    val ongoingSudokuSources by sudokuRepoViewModel.getOngoingSudokus().collectAsState(listOf())

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(text = "Saved games")
        }
        for (sudokuEntity in ongoingSudokuSources) {
            val text = when (sudokuEntity.source) {
                is SudokuSource.Parsed -> "From parsed ${sudokuEntity.source}"
                is SudokuSource.SudokuSeed -> "From Seed ${sudokuEntity.source.value}"
                is SudokuSource.Unparsed -> "From unparsed"
                is SudokuSource.ValidUnparsed -> "From valid unparsed ${sudokuEntity.uid}"
                is SudokuSource.Daily -> "Daily from ${sudokuEntity.source.date}"
            }
            Text(
                text, modifier = Modifier.clickable {
                    sudokuRepoViewModel.setCurrentGame(sudokuEntity.source)
                    navController.navigate(gamePlay)
                }
            )

        }
    }
}

@Composable
inline fun <reified T : ViewModel> viewModelAtRoute(
    navController: NavController,
    route: String
): T {
    val parentEntry = remember {
        navController.getBackStackEntry(route)
    }
    return hiltViewModel(
        parentEntry
    )
}

@Composable
fun Home(
    navController: NavController,
    sudokuRepoViewModel: SudokuRepoViewModel = viewModelAtRoute(navController, root)
) {


    Column(horizontalAlignment = CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_with_text),
            contentDescription = "Logo",
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(30.dp)
        )
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            val hasOnGoingSudokus by sudokuRepoViewModel.hasOngoingSudokus()
                .collectAsState(initial = false)
            if (hasOnGoingSudokus) {
                MenuItem(
                    text = "Resume Game"
                ) { navController.navigate(savedGames) }
            }
            MenuItem(
                text = "Quick Game"
            ) {
                sudokuRepoViewModel.setCurrentGame(SudokuSource.SudokuSeed.random())
                navController.navigate(gamePlay)
            }
            MenuItem(
                text = "Daily challenge"
            ) {
                sudokuRepoViewModel.setCurrentGame(SudokuSource.Daily(LocalDate.now()))
                navController.navigate(gamePlay)
            }
            MenuItem(
                text = "Campaign"
            ) { navController.navigate(campaign) }
            MenuItem(text = "Settings") { navController.navigate("settings") }
        }

    }
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(250.dp)
            .padding(0.dp, 5.dp)
            .border(2.dp, MaterialTheme.colors.onBackground)
            .clickable { onClick() },
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            text,
            modifier = Modifier
                .padding(0.dp, 10.dp)
                .background(MaterialTheme.colors.background),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground,
            fontSize = 18.sp,
        )
    }

}

@Composable
fun SavedSudoku(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    sudokuRepoViewModel: SudokuRepoViewModel,
) {
    val sudokuEntity by sudokuRepoViewModel.activeSudokuEntity.collectAsState(null)
    sudokuEntity?.let { activeEntity ->
        SudokuUI(
            navController,
            activeEntity.sudoku,
            settingsViewModel,
        ) { newSudoku ->
            sudokuRepoViewModel.saveSudoku(activeEntity.copy(sudoku = newSudoku))
        }
    } ?:
    CircularProgressIndicator()
}
