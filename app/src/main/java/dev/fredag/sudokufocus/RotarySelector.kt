package dev.fredag.sudokufocus

import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun RotarySelector(list: List<String>, sectionClicked: (seciton: String) -> Unit) {
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
            Joystick(
                this@BoxWithConstraints.maxWidth,
                this@BoxWithConstraints.maxHeight,
                sectionClicked,
                list
            )
        }
    }
}

val paint = android.graphics.Paint().apply {
    textAlign = android.graphics.Paint.Align.CENTER
    textSize = 64f
    color = 0xffff0000.toInt()
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Joystick(
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
    val center = Offset(width / 2, height / 2)

    var pos by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    var activeZone: Int? by remember {
        mutableStateOf(null)
    }

    val animateFloat = remember { Animatable(0f) }
    LaunchedEffect(animateFloat) {
        animateFloat.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutLinearInEasing)
        )
    }

    val radiansPerZone = (2 * PI / zones.size).toFloat()

    val arcDiameter = with(LocalDensity.current) {
        minOf(parentWidth.toPx(), parentHeight.toPx()) / 1.2f
    }

    val thumbRadius = arcDiameter / 4
    val maxThumbTranslation = ((arcDiameter / 2) - (thumbRadius))

    println("activezone $activeZone")
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(height.dp)
        .pointerInteropFilter {
            when (it.action) {

                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val pointer = CartesianCoordinate(it.x, it.y)
                    val centerOffsetPointer = pointer.remove(center)
                    val centerOffsetPointerPolar = centerOffsetPointer.toPolar()

                    val thumbPos = if (centerOffsetPointerPolar.r > maxThumbTranslation) {
                        centerOffsetPointerPolar.newWithRadius(maxThumbTranslation)
                    } else {
                        centerOffsetPointerPolar
                    }

                    val (x, y) = thumbPos.toCartesian()
                    pos = Offset(x, y)
                    //thumbPositionMoved(x / maxThumbTranslation, y / maxThumbTranslation)

                    var (thumbRad, thumbAngle) = CartesianCoordinate(pos.x, pos.y).toPolar()
                    println("thumbangle $thumbAngle, modulus ${((thumbAngle) / radiansPerZone).toInt()}")
                    thumbAngle =
                        if (thumbAngle < 0) (thumbAngle + Math.PI * 2).toFloat() else thumbAngle
                    activeZone = if (thumbRad > 0) (thumbAngle / radiansPerZone).toInt() else null
                }
                MotionEvent.ACTION_UP -> {
                    pos = Offset(0f, 0f)
                    activeZone?.let { a -> sectionClicked(zones[a]) }
                    activeZone = null

                }
            }
            true
        }

    ) {
        if (activeZone != null) {


            for ((index, zone) in zones.withIndex()) {
                val i = index.toFloat()
                val lineAngle = i * radiansPerZone
                val polarCoord = PolarCoordinate(arcDiameter / 2, lineAngle)
                val coord = polarCoord.toCartesian()

                val textPos =
                    polarCoord.newWithRadius(polarCoord.r * 0.7f)
                        .newWithAngleOffset(radiansPerZone / 2)
                        .toCartesian2().move(center.x, center.y)
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        zone,
                        textPos.x,
                        textPos.y, paint
                    )
                }
                drawLine(
                    Color.Red,
                    with(center) { Offset(x, y) },
                    with(center) { Offset(x + coord.first, y + coord.second) }
                )
                activeZone?.let {
                    if (it == index) {
                        println("i: $i, degreesPerZone: $radiansPerZone, mult: (${i * lineAngle}), polar $polarCoord, coord $coord")
//                    drawLine(
//                        Color.Green,
//                        with(center) { Offset(x, y) },
//                        with(center) { Offset(x + coord.first, y + coord.second) }
//                    )

                        val t1 = PolarCoordinate(arcDiameter / 2, lineAngle).toCartesian()
                        val t2 =
                            PolarCoordinate(
                                arcDiameter / 2,
                                lineAngle + radiansPerZone
                            ).toCartesian()
                        val trianglePath = Path().apply {
                            // Moves to top center position
                            moveTo(center.x, center.y)
                            // Add line to bottom right corner
                            lineTo(center.x + t1.first, center.y + t1.second)
                            // Add line to bottom left corner
                            lineTo(center.x + t2.first, center.y + t2.second)
                        }
                        drawPath(
                            trianglePath,
                            Color.Red,
                        )
                        drawArc(
                            Color.Red,
                            startAngle = (lineAngle / (Math.PI) * 180).toFloat(),
                            sweepAngle = (radiansPerZone / (Math.PI) * 180).toFloat(),
                            useCenter = false,
                            topLeft = with(center) {
                                Offset(x - arcDiameter / 2, y - arcDiameter / 2)
                            },
                            size = Size(
                                arcDiameter,
                                arcDiameter,
                            ),
                        )
                    }
                }
            }

            drawArc(
                color = Color.Red,
                startAngle = 0f,
                sweepAngle = 360f * animateFloat.value,
                useCenter = false,
                topLeft = with(center) {
                    Offset(x - arcDiameter / 2, y - arcDiameter / 2)
                },
                size = Size(
                    arcDiameter,
                    arcDiameter,
                ),
                style = Stroke(2.0f)
            )

//        drawCircle(
//            color = Color.Red,
//            radius = thumbRadius,
//            center = pos + center
//        )
        }
    }
}

data class CartesianCoordinate(val x: Float, val y: Float) {
    fun remove(other: Offset) = CartesianCoordinate(x - other.x, y - other.y)
    fun move(x: Float, y: Float) = CartesianCoordinate(this.x + x, this.y + y)
    fun toPolar() = when {
        x < 0 && y < 0 -> PolarCoordinate(sqrt(x * x + y * y), atan(y / x) + PI.toFloat())
        x < 0 -> PolarCoordinate(sqrt(x * x + y * y), atan(y / x) + PI.toFloat())
        y < 0 -> PolarCoordinate(sqrt(x * x + y * y), atan(y / x))
        else -> PolarCoordinate(sqrt(x * x + y * y), atan(y / x))
    }

}

data class PolarCoordinate(val r: Float, val angle: Float) {
    fun toCartesian() = Pair(r * cos(angle), r * sin(angle))
    fun toCartesian2() = CartesianCoordinate(r * cos(angle), r * sin(angle))
    fun newWithRadius(newRadius: Float) = PolarCoordinate(newRadius, angle)
    fun newWithAngleOffset(angleOffset: Float) = PolarCoordinate(r, angle + angleOffset)
}