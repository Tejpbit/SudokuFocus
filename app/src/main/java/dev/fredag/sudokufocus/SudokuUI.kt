package dev.fredag.sudokufocus

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
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
                sudoku,
                this@BoxWithConstraints.maxWidth,
                this@BoxWithConstraints.maxHeight,
                sectionClicked,
                list
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


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SudokuCanvas(
    sudoku: Sudoku,
    parentWidth: Dp,
    parentHeight: Dp,
    sectionClicked: (section: String) -> Unit,
    zones: List<String>
) {
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
                    pos = Offset(0f, 0f)
                    activeZone?.let { a ->
                        sectionClicked(zones[a])

                        activatedCoord?.let {
                            when (zones[a]) {
                                "x" -> {
                                    sudoku.clearCell(it)
                                }
                                else -> {
                                    sudoku.submitCell(zones[a].toInt(), it)
                                }
                            }
                            activatedCoord = null
                        }

                    }
                    activeZone = null


                }
            }
            true
        }

    ) {
        activeZone?.let {
            drawRotarySelector(arcDiameter, zones, touchDownPos, pos)
        }
        drawSudokuField(sudoku, Size(width, height))
    }
}