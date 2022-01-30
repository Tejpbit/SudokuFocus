package dev.fredag.sudokufocus

import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
import dev.fredag.sudokufocus.previewproviders.SudokuCanvasParameters
import dev.fredag.sudokufocus.previewproviders.SudokuCanvasParametersProvider
import kotlin.math.PI
import kotlin.math.min

@Composable
fun SudokuUI(sudoku: Sudoku, updateSudoku: (sudoku: Sudoku) -> Unit) {
    var showSelectorUi by remember {
        mutableStateOf(true)
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
                    Button(onClick = {
                        updateSudoku(sudoku.autoFillGuesses())
                    }) {
                        Text(text = "Deduce guesses")
                    }
                    Row {
                        Text(text = "Show selector UI: ")
                        Checkbox(
                            checked = showSelectorUi,
                            onCheckedChange = { showSelectorUi = it })
                    }

                    if (sudoku.isSolved()) {
                        Text(text = "Solved!")
                    }
                }
                Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                    SudokuCanvas(
                        SudokuCanvasParameters(
                            sudoku,
                            this@BoxWithConstraints.maxWidth,
                            this@BoxWithConstraints.maxHeight,
                            updateSudoku,
                            SelectorType.RotaryWithCenter(
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
                            ),
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
        is SelectorType.RotaryWithCenter -> RotaryWithCenterActivatedCellCelculator(
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
            RotaryWithCenterActivatedCellCelculator(
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


        drawSudokuField(sudoku, Size(width, height), primaryColor, secondaryColor, guessTextColor)
        if (showSelectorUI) {
            selectorPos?.let { selectorPos ->
                when (selectorTypeWithLogic) {
                    is SelectorTypeWithLogic.Grid -> {
                        drawGridSelector(
                            size = gridSelectorSize,
                            selectorPos,
                            pos ?: selectorPos
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

sealed class SelectorType {
    class Rotary(val zones: List<String>) : SelectorType()
    class RotaryWithCenter(val zones: List<String>, val center: String) : SelectorType()
    class Grid(val zones: List<String>) : SelectorType()
}

sealed class SelectorTypeWithLogic {
    class Rotary(val zones: List<String>, logic: RotaryActivatedCellCelculator) :
        SelectorTypeWithLogic()

    class RotaryWithCenter(
        val zones: List<String>,
        val center: String,
        val logic: RotaryWithCenterActivatedCellCelculator
    ) : SelectorTypeWithLogic()

    class Grid(val zones: List<String>, logic: GridActivatedCellCalulator) : SelectorTypeWithLogic()
}

fun DrawScope.drawText(text: String, x: Float, y: Float, paint: Paint) {
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
    guessTextColor: Color
) {
    val guessValuePaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
        color = colorToInt(guessTextColor)
    }

    val submittedValuePaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 64f
        color = colorToInt(primaryColor)
    }

    val submittedLockedValuePaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 64f
        color = colorToInt(secondaryColor)
    }

    val fieldWidth = min(size.height, size.width)
    val cellWidth = fieldWidth / 9
    val cellSize = Size(cellWidth, cellWidth)
    val cornerRad = CornerRadius(cellSize.width * 0.1f, cellSize.width * 0.1f)

    for (coord in Coordinate(0, 0).getCoordinatesInBlockTo(Coordinate(8, 8))) {
        val topLeft = Offset(coord.x * cellSize.width, coord.y * cellSize.height)
        drawRoundRect(
            color = secondaryColor,
            topLeft = topLeft,
            size = cellSize,
            cornerRadius = cornerRad,
            style = Stroke(2f)
        )

        sudoku.getSubmittedValueAt(coord)?.let { submitted ->
            drawText(
                submitted.toString(),
                topLeft.x + cellSize.width / 2,
                topLeft.y + cellSize.height / 2,
                if (sudoku.isLocked(coord)) submittedValuePaint else submittedLockedValuePaint
            )
        }

        sudoku.getGuessedValuesAt(coord)?.let { guessed ->
            drawText(
                guessed.sorted().joinToString(" "),
                topLeft.x + cellSize.width / 2,
                topLeft.y + cellSize.height * 0.9f,
                guessValuePaint
            )
        }
    }

    // Thicker 3x3 divider lines
    for (i in listOf(3, 6)) {
        drawLine(
            Color.Green,
            start = Offset(0f, cellWidth * i),
            end = Offset(cellWidth * 9, cellWidth * i),
            strokeWidth = 5f,
        )
        drawLine(
            Color.Green,
            start = Offset(cellWidth * i, 0f),
            end = Offset(cellWidth * i, cellWidth * 9),
            strokeWidth = 5f,
        )
    }
}

private fun colorToInt(color: Color) = android.graphics.Color.argb(
    color.toArgb().alpha,
    color.toArgb().red,
    color.toArgb().green,
    color.toArgb().blue
)