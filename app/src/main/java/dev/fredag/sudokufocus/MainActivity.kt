package dev.fredag.sudokufocus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.fredag.sudokufocus.model.Sudoku
import dev.fredag.sudokufocus.ui.theme.SudokuFocusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuFocusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    FieldActionSelector()
                }
            }
        }
    }
}

@Composable
fun FieldActionSelector() {
    var sudoku by remember {
        mutableStateOf(Sudoku.generateDaily())
    }
    SudokuUI(
        sudoku,
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