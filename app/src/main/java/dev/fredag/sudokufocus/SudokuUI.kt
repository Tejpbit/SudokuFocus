package dev.fredag.sudokufocus

import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
import dev.fredag.sudokufocus.previewproviders.SudokuCanvasParameters
import dev.fredag.sudokufocus.previewproviders.SudokuCanvasParametersProvider
import kotlin.math.PI

@Composable
fun SudokuUI(sudoku: Sudoku, list: List<String>, sectionClicked: (seciton: String) -> Unit) {
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
            SudokuCanvas(
                SudokuCanvasParameters(
                    sudoku,
                    this@BoxWithConstraints.maxWidth,
                    this@BoxWithConstraints.maxHeight,
                    sectionClicked,
                    list,
                    SelectorType.Grid
                )

            )
        }
    }
}

val numberPickerPaint = android.graphics.Paint().apply {
    textAlign = android.graphics.Paint.Align.CENTER
    textSize = 64f
    color = 0xff00ff00.toInt()
}

val submittedValuePaint = android.graphics.Paint().apply {
    textAlign = android.graphics.Paint.Align.CENTER
    textSize = 64f
    color = 0xffff0000.toInt()
}

val guessValuePaint = android.graphics.Paint().apply {
    textAlign = android.graphics.Paint.Align.CENTER
    textSize = 24f
    color = 0xffffffff.toInt()
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
        sectionClicked,
        zones,
        selectorType,
    ) = sudokuCanvasParameters

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

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(height.dp)
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
                    activeZone = if (thumbRad > 0) (thumbAngle / radiansPerZone).toInt() else null
                }
                MotionEvent.ACTION_UP -> {
                    touchDownPos?.let { touchDownPos ->
                        pos?.let { pos ->
                            activatedCellCalculator
                                .calculateActivatedCell(touchDownPos, pos)
                                ?.let { activatedCell ->
                                    activatedCoord?.let { activatedCoord ->
                                        when (activatedCell) {
                                            "x" -> {
                                                sudoku.clearCell(activatedCoord)
                                            }
                                            else -> {
                                                sudoku.submitCell(
                                                    activatedCell.toInt(),
                                                    activatedCoord
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


        drawSudokuField(sudoku, Size(width, height))
        activeZone?.let {
            when (selectorType) {
                SelectorType.Grid -> {
                    drawGridSelector(size = gridSelectorSize, touchDownPos, pos)
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