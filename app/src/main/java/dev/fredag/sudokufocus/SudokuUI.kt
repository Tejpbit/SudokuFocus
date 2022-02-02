package dev.fredag.sudokufocus

import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.navigation.NavController
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
import dev.fredag.sudokufocus.previewproviders.SudokuCanvasParameters
import dev.fredag.sudokufocus.previewproviders.SudokuCanvasParametersProvider
import java.io.Serializable
import kotlin.math.min

val defaultRotaryWithCenterSelector = SelectorType.RotaryWithCenter(
    listOf(
        "1",
        "2",
        "3",
        "6",
        "9",
        "8",
        "7",
        "4"
    ), "5"
)

val defaultGridSelector = SelectorType.Grid(
    listOf(
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
    )
)

@Composable
fun SudokuUI(
    navController: NavController,
    sudoku: Sudoku,
    settingsViewModel: SettingsViewModel,
    updateSudoku: (sudoku: Sudoku) -> Unit
) {

    val activeSelectorType = settingsViewModel.selectorType
    val showSelectorUi = settingsViewModel.showSelectorUi

    var showHints by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PeekButton(
                    onTouchDown = { showHints = true },
                    onTouchRelease = { showHints = false }
                )
                SettingsButton(navController)
            }
        }) {

    }

    BoxWithConstraints(
        Modifier
            .fillMaxHeight()
            .width(400.dp)
    ) {
        Box(
            Modifier
                .size(
                    width = maxWidth,
                    height = maxHeight,
                )
        ) {
            Column() {
                Row() {
                    if (sudoku.isSolved()) {
                        Text(text = "Solved!")
                    }
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    SudokuCanvas(
                        SudokuCanvasParameters(
                            if (showHints) sudoku.autoFillGuesses() else sudoku,
                            this@BoxWithConstraints.maxWidth,
                            this@BoxWithConstraints.maxHeight,
                            updateSudoku,
                            activeSelectorType,
                            showSelectorUi
                        )
                    )
                }

            }
        }
    }
}


val numberPickerPaint = Paint().apply {
    textAlign = Paint.Align.CENTER
    textSize = 64f
    color = 0xff00ff00.toInt()
}


val gridSelectorSize = 500f

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun SudokuCanvas(
    @PreviewParameter(SudokuCanvasParametersProvider::class) sudokuCanvasParameters: SudokuCanvasParameters,
) {
    val (
        sudoku,
        parentWidth,
        parentHeight,
        updateSudoku,
        selectorType,
        showSelectorUI
    ) = sudokuCanvasParameters

    val primaryColor = MaterialTheme.colors.primary
    val secondaryColor = MaterialTheme.colors.secondary
    val guessTextColor = MaterialTheme.colors.onBackground

    val height = with(LocalDensity.current) {
        parentHeight.toPx()
    }

    val width = with(LocalDensity.current) {
        parentWidth.toPx()
    }

    val activatedCellCalculator = when (selectorType) {
        is SelectorType.Grid -> {
            GridActivatedCellCalulator(gridSelectorSize, selectorType.zones)
        }
        is SelectorType.Rotary -> RotaryActivatedCellCelculator(selectorType.zones)
        is SelectorType.RotaryWithCenter -> RotaryWithCenterActivatedCellCalculator(
            500f, width / 9 / 2,
            selectorType.zones,
            selectorType.center
        )
    }

    val selectorTypeWithLogic: SelectorTypeWithLogic = when (selectorType) {
        is SelectorType.Grid ->
            SelectorTypeWithLogic.Grid(
                selectorType.zones,
                GridActivatedCellCalulator(gridSelectorSize, selectorType.zones)
            )
        is SelectorType.Rotary -> SelectorTypeWithLogic.Rotary(
            selectorType.zones,
            RotaryActivatedCellCelculator(selectorType.zones)
        )
        is SelectorType.RotaryWithCenter -> SelectorTypeWithLogic.RotaryWithCenter(
            selectorType.zones,
            selectorType.center,
            RotaryWithCenterActivatedCellCalculator(
                500f, width / 9 / 2,
                selectorType.zones,
                selectorType.center
            )

        )
    }

    var touchDownPos: Offset? by remember {
        mutableStateOf(null)
    }

    var pos: Offset? by remember {
        mutableStateOf(null)
    }

    val arcDiameter = with(LocalDensity.current) {
        minOf(parentWidth.toPx(), parentHeight.toPx()) / 1.2f
    }

    var activatedCoord: Coordinate? by remember {
        mutableStateOf(null)
    }
    val squareSize = width / 9

    var selectorPos: Offset? by remember {
        mutableStateOf(null)
    }

    val font = LocalContext.current.resources.getFont(R.font.amarante_regular)
    Canvas(modifier = Modifier
        .fillMaxWidth()
//        .width((width/10).dp)
        .height(parentWidth)
        .pointerInteropFilter {
            if (it.action == MotionEvent.ACTION_DOWN) {
                activatedCoord =
                    Coordinate((it.x / squareSize).toInt(), (it.y / squareSize).toInt())
                selectorPos = activatedCoord?.let { activatedCoord ->
                    Offset(
                        activatedCoord.x * squareSize + squareSize / 2,
                        activatedCoord.y * squareSize + squareSize / 2
                    )
                }
                touchDownPos = Offset(it.x, it.y)
            }

            when (it.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    pos = Offset(it.x, it.y)

                    var (thumbRad, thumbAngle) = PolarCoordinate.fromTwoCartesianCoordinates(
                        CartesianCoordinate(touchDownPos?.x ?: 0f, touchDownPos?.y ?: 0f),
                        CartesianCoordinate(pos?.x ?: 0f, pos?.y ?: 0f)
                    )
                    thumbAngle =
                        if (thumbAngle < 0) (thumbAngle + Math.PI * 2).toFloat() else thumbAngle
                }
                MotionEvent.ACTION_UP -> {
                    selectorPos?.let { selectorPos ->
                        pos?.let { pos ->
                            activatedCoord?.let { activatedCoord ->
                                updateSudoku(
                                    activatedCellCalculator.calculateNextSudoku(
                                        sudoku,
                                        selectorPos,
                                        pos,
                                        activatedCoord
                                    )
                                )
                            }
                        }
                    }
                    selectorPos = null
                    pos = null
                }
            }
            true
        }

    ) {


        drawSudokuField(
            sudoku,
            Size(width, height),
            primaryColor,
            secondaryColor,
            guessTextColor,
            font
        )
        if (showSelectorUI) {
            selectorPos?.let { selectorPos ->
                when (selectorTypeWithLogic) {
                    is SelectorTypeWithLogic.Grid -> {
                        drawGridSelector(
                            size = gridSelectorSize,
                            selectorPos,
                            pos ?: selectorPos,
                            selectorTypeWithLogic.logic
                        )
                    }
                    is SelectorTypeWithLogic.Rotary -> drawRotarySelector(
                        arcDiameter,
                        selectorTypeWithLogic.zones,
                        selectorPos,
                        pos ?: selectorPos
                    )
                    is SelectorTypeWithLogic.RotaryWithCenter -> drawRotaryWithCenterSelector(
                        selectorTypeWithLogic.zones,
                        selectorTypeWithLogic.center,
                        selectorPos,
                        pos ?: selectorPos,
                        selectorTypeWithLogic.logic
                    )
                }
            }
        }
    }
}

sealed class SelectorType(val name: String) : Serializable {
    class Rotary(val zones: List<String>) : SelectorType("Rotary")
    class RotaryWithCenter(val zones: List<String>, val center: String) :
        SelectorType("RotaryWithCenter")

    class Grid(val zones: List<String>) : SelectorType("Grid")
}

sealed class SelectorTypeWithLogic {
    class Rotary(val zones: List<String>, logic: RotaryActivatedCellCelculator) :
        SelectorTypeWithLogic()

    class RotaryWithCenter(
        val zones: List<String>,
        val center: String,
        val logic: RotaryWithCenterActivatedCellCalculator
    ) : SelectorTypeWithLogic()

    class Grid(val zones: List<String>, val logic: GridActivatedCellCalulator) :
        SelectorTypeWithLogic()
}

fun DrawScope.drawText(text: String, x: Float, y: Float, paint: Paint, typeFace: Typeface? = null) {
    if (typeFace != null) {
        paint.typeface = typeFace
    }
    drawIntoCanvas {
        it.nativeCanvas.drawText(
            text,
            x,
            y,
            paint
        )
    }
}

fun DrawScope.drawSudokuField(
    sudoku: Sudoku,
    size: Size,
    primaryColor: Color,
    secondaryColor: Color,
    guessTextColor: Color,
    typeFace: Typeface? = null
) {
    val guessValuePaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
        color = colorToInt(guessTextColor)
    }

    val submittedFontSize = 64f
    val submittedValuePaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = submittedFontSize
        color = colorToInt(primaryColor)
    }

    val submittedLockedValuePaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = submittedFontSize
        color = colorToInt(secondaryColor)
    }

    val fieldWidth = min(size.height, size.width)
    val cellWidth = fieldWidth / 9
    val cellSize = Size(cellWidth, cellWidth)
    val cornerRad = CornerRadius(cellSize.width * 0.1f, cellSize.width * 0.1f)

    for (coord in Coordinate(0, 0).getCoordinatesInBlockTo(Coordinate(8, 8))) {
        val topLeft = Offset(coord.x * cellSize.width, coord.y * cellSize.height)
        drawRect(
            color = primaryColor,
            topLeft = topLeft,
            size = cellSize,
            style = Stroke(2f)
        )

        sudoku.getSubmittedValueAt(coord)?.let { submitted ->
            drawText(
                submitted.toString(),
                topLeft.x + cellSize.width / 2,
                topLeft.y + cellSize.height / 2 + submittedFontSize / 2 - 10, // 10: Small adjust since a fonts height is not the height of the character
                if (sudoku.isLocked(coord)) submittedLockedValuePaint else submittedValuePaint,
                typeFace
            )
        }

        sudoku.getGuessedValuesAt(coord)?.let { guessed ->
            drawText(
                guessed.sorted().joinToString(" "),
                topLeft.x + cellSize.width / 2,
                topLeft.y + cellSize.height * 0.9f,
                guessValuePaint,
                typeFace
            )
        }
    }

    // Thicker 3x3 divider lines
    for (i in listOf(0, 3, 6, 9)) {
        drawLine(
            primaryColor,
            start = Offset(0f, cellWidth * i),
            end = Offset(cellWidth * 9, cellWidth * i),
            strokeWidth = 10f,
        )
        drawLine(
            primaryColor,
            start = Offset(cellWidth * i, 0f),
            end = Offset(cellWidth * i, cellWidth * 9),
            strokeWidth = 10f,
        )
    }
}

private fun colorToInt(color: Color) = android.graphics.Color.argb(
    color.toArgb().alpha,
    color.toArgb().red,
    color.toArgb().green,
    color.toArgb().blue
)