package dev.fredag.sudokufocus

import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
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
import androidx.compose.ui.graphics.graphicsLayer
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
fun SudokuUI(sudoku: Sudoku, list: List<String>, updateSudoku: (sudoku: Sudoku) -> Unit) {
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
                    if (sudoku.isSolved()) {
                        Text(text = "Solved!")
                    }
                }
                SudokuCanvas(
                    SudokuCanvasParameters(
                        sudoku,
                        this@BoxWithConstraints.maxWidth,
                        this@BoxWithConstraints.maxHeight,
                        updateSudoku,
                        list,
                        SelectorType.Grid
                    )
                )
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
        zones,
        selectorType,
    ) = sudokuCanvasParameters

    val primaryColor = MaterialTheme.colors.primary
    val secondaryColor = MaterialTheme.colors.secondary
    val guessTextColor = MaterialTheme.colors.onBackground

    val activatedCellCalculator = when (selectorType) {
        SelectorType.Grid -> GridActivatedCellCalulator(gridSelectorSize, zones)
        SelectorType.Rotary -> TODO()
    }

    val width = with(LocalDensity.current) {
        parentWidth.toPx()
    }
    val height = with(LocalDensity.current) {
        parentHeight.toPx()
    }

    var touchDownPos: Offset? by remember {
        mutableStateOf(null)
    }

    var pos: Offset? by remember {
        mutableStateOf(null)
    }
    var activeZone: Int? by remember {
        mutableStateOf(null)
    }

    val radiansPerZone = (2 * PI / zones.size).toFloat()

    val arcDiameter = with(LocalDensity.current) {
        minOf(parentWidth.toPx(), parentHeight.toPx()) / 1.2f
    }

    var activatedCoord: Coordinate? by remember {
        mutableStateOf(null)
    }

    var scale by remember { mutableStateOf(0.9f) }
    var scroll by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        scroll += offsetChange
    }

    Canvas(modifier = Modifier
        .background(Color.LightGray)
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = scroll.x,
            translationY = scroll.y
        )
        .transformable(state = state)
        .fillMaxWidth()
        .height(parentWidth)
        .pointerInteropFilter {

            if (it.action == MotionEvent.ACTION_DOWN) {
                val squareSize = width / 9
                activatedCoord =
                    Coordinate((it.x / squareSize).toInt(), (it.y / squareSize).toInt())
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
                    activeZone =
                        if (thumbRad > 0) (thumbAngle / radiansPerZone).toInt() else null
                }
                MotionEvent.ACTION_UP -> {
                    touchDownPos?.let { touchDownPos ->
                        pos?.let { pos ->

                            activatedCellCalculator.calculateNextSudoku(
                                sudoku, touchDownPos, pos
                            )
                            activatedCellCalculator
                                .calculateActivatedCell(touchDownPos, pos)
                                ?.let { activatedCell ->
                                    activatedCoord?.let { activatedCoord ->
                                        when (activatedCell) {
                                            "x" -> {
                                                sudoku.clearCell(activatedCoord)
                                            }
                                            else -> {
                                                updateSudoku(
                                                    sudoku.submitCell(
                                                        activatedCell.toInt(),
                                                        activatedCoord
                                                    )
                                                )
                                            }
                                        }

                                    }
                                }
                        }
                    }

                    pos = Offset(0f, 0f)
//                    activeZone?.let { a ->
//                        sectionClicked(zones[a])
//
//                        activatedCoord?.let {
//                            when (zones[a]) {
//                                "x" -> {
//                                    sudoku.clearCell(it)
//                                }
//                                else -> {
//                                    sudoku.submitCell(zones[a].toInt(), it)
//                                }
//                            }
//                            activatedCoord = null
//                        }
//
//                    }
                    activeZone = null


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
            guessTextColor
        )
        activeZone?.let {
            when (selectorType) {
                SelectorType.Grid -> {
                    drawGridSelector(size = gridSelectorSize, touchDownPos, pos, scale, scroll)
                }
                SelectorType.Rotary -> drawRotarySelector(arcDiameter, zones, touchDownPos, pos)

            }
        }
    }

}

sealed class SelectorType {
    object Rotary : SelectorType()
    object Grid : SelectorType()
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