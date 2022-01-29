package dev.fredag.sudokufocus

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import dev.fredag.sudokufocus.model.Coordinate
import dev.fredag.sudokufocus.model.Sudoku
import kotlin.math.*


fun DrawScope.drawRotarySelector(
    diameter: Float,
    zones: List<String>,
    touchDownPos: Offset?,
    currentTouchPos: Offset?,
) {
    if (touchDownPos == null) {
        return
    }

    val radiansPerZone = (2 * PI / zones.size).toFloat()
    var (_, thumbAngle) = if (currentTouchPos == null) PolarCoordinate(
        1f,
        0f
    ) else PolarCoordinate.fromTwoCartesianCoordinates(
        CartesianCoordinate(touchDownPos.x, touchDownPos.y),
        CartesianCoordinate(currentTouchPos.x, currentTouchPos.y)
    )
    thumbAngle =
        if (thumbAngle < 0) (thumbAngle + Math.PI * 2).toFloat() else thumbAngle
    val activeZone = (thumbAngle / radiansPerZone).toInt()
    drawArc(
        color = Color.Gray,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = with(touchDownPos) {
            Offset(x - diameter / 2, y - diameter / 2)
        },
        size = Size(
            diameter,
            diameter,
        ),
    )


    for ((index, zone) in zones.withIndex()) {
        val i = index.toFloat()
        val lineAngle = i * radiansPerZone
        val polarCoord = PolarCoordinate(diameter / 2, lineAngle)
        val coord = polarCoord.toCartesian()

        val textPos =
            polarCoord.newWithRadius(polarCoord.r * 0.7f)
                .newWithAngleOffset(radiansPerZone / 2)
                .toCartesian2().move(touchDownPos.x, touchDownPos.y)
        drawIntoCanvas {
            it.nativeCanvas.drawText(
                zone,
                textPos.x,
                textPos.y,
                numberPickerPaint
            )
        }
        drawLine(
            Color.Green,
            with(touchDownPos) { Offset(x, y) },
            with(touchDownPos) { Offset(x + coord.first, y + coord.second) }
        )
        activeZone.let {
            if (it == index) {
                println("i: $i, degreesPerZone: $radiansPerZone, mult: (${i * lineAngle}), polar $polarCoord, coord $coord")

                val t1 = PolarCoordinate(diameter / 2, lineAngle).toCartesian()
                val t2 =
                    PolarCoordinate(
                        diameter / 2,
                        lineAngle + radiansPerZone
                    ).toCartesian()
                val trianglePath = Path().apply {
                    moveTo(touchDownPos.x, touchDownPos.y)
                    lineTo(touchDownPos.x + t1.first, touchDownPos.y + t1.second)
                    lineTo(touchDownPos.x + t2.first, touchDownPos.y + t2.second)
                }
                drawPath(
                    trianglePath,
                    Color.Green,
                )
                drawArc(
                    Color.Green,
                    startAngle = (lineAngle / (Math.PI) * 180).toFloat(),
                    sweepAngle = (radiansPerZone / (Math.PI) * 180).toFloat(),
                    useCenter = false,
                    topLeft = with(touchDownPos) {
                        Offset(x - diameter / 2, y - diameter / 2)
                    },
                    size = Size(
                        diameter,
                        diameter,
                    ),
                )
            }
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

    companion object {
        fun fromTwoCartesianCoordinates(
            from: CartesianCoordinate,
            to: CartesianCoordinate
        ): PolarCoordinate {
            return to.move(-from.x, -from.y).toPolar()
        }
    }
}
