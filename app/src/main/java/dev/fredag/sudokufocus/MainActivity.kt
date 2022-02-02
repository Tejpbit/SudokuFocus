package dev.fredag.sudokufocus

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.fredag.sudokufocus.model.Sudoku
import dev.fredag.sudokufocus.model.SudokuSeed
import dev.fredag.sudokufocus.ui.theme.SudokuFocusTheme
import java.time.LocalDate
import androidx.compose.foundation.border
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

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

@Composable
fun Router(navController: NavHostController) {
    val viewModel = hiltViewModel<SettingsViewModel>()
    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(navController = navController)
        }

        composable("home") { Home(navController) }
        composable("settings") {

            Settings(viewModel)
        }
        composable(
            "game/{seed}",
            arguments = listOf(navArgument("seed") { type = NavType.IntType })
        ) { backStackEntry ->
            backStackEntry.arguments?.let {
                SavedSudoku(navController, viewModel, SudokuSeed(it.getInt("seed")))
            } ?: Text(text = "Something went wrong")
        }
    }
}

@Composable
fun Home(navController: NavController) {
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
            MenuItem(
                text = "Quick Game"
            ) { navController.navigate("game/${SudokuSeed.random().value}") }
            MenuItem(
                text = "Daily challenge"
            ) { navController.navigate("game/${SudokuSeed.day(LocalDate.now()).value}") }
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
    seed: SudokuSeed
) {
    var sudoku by remember {
        mutableStateOf(Sudoku.generateFromSeed(seed))
    }
    SudokuUI(
        navController,
        sudoku,
        settingsViewModel,
    ) { newSudoku ->
        sudoku = newSudoku
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SudokuFocusTheme {
        Greeting("Android")
    }
}