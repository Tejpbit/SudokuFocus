package dev.fredag.sudokufocus

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*


fun DrawScope.drawRotaryWithCenterSelector(
    zones: List<String>,
    centerZone: String,
    touchDownPos: Offset,
    currentTouchPos: Offset,
    selectorLogic: RotaryWithCenterActivatedCellCalculator,
    primaryColor: Color,
    secondaryColor: Color
) {
    val textPaintInactive = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 64f
        color = colorToInt(primaryColor)
    }

    val textPaintActive = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 64f
        color = colorToInt(secondaryColor)
    }

    val activatedZone = selectorLogic.calculateActivatedCell(touchDownPos, currentTouchPos)
    val diameter = selectorLogic.outerRadius
    val thumbDistanceFromCenter =
        hypotenuse(abs(touchDownPos.x - currentTouchPos.x), abs(touchDownPos.y - currentTouchPos.y))
    val centerCircleDimeter = selectorLogic.centerRadius
    val radiansPerZone = (2 * PI / zones.size).toFloat()
    var (_, thumbAngle) = PolarCoordinate.fromTwoCartesianCoordinates(
        CartesianCoordinate(touchDownPos.x, touchDownPos.y),
        CartesianCoordinate(currentTouchPos.x, currentTouchPos.y)
    )
    thumbAngle =
        if (thumbAngle < 0) (thumbAngle + Math.PI * 2).toFloat() else thumbAngle
    val isInsideCenterZone = thumbDistanceFromCenter < centerCircleDimeter / 2
    val activeZone =
        if (isInsideCenterZone) centerZone.toInt() else (thumbAngle / radiansPerZone).toInt()
    // Outer Circle
    drawArc(
        color = secondaryColor,
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


    for ((zoneIndex, zone) in zones.withIndex()) {
        val i = zoneIndex.toFloat()
        val startLineAngle =
            i * radiansPerZone +
                    radiansPerZone / 2 +// offset by half a sector to not have horizontal lines
                    Math.PI.toFloat() // offset by half to have first zone be up to the left.
        val endLineAngle = startLineAngle + radiansPerZone
        val polarCoord = PolarCoordinate(diameter / 2, startLineAngle)
        val coord = polarCoord.toCartesian()


        if (activatedZone == zone) {
            println("i: $i, degreesPerZone: $radiansPerZone, mult: (${i * startLineAngle}), polar $polarCoord, coord $coord")

            val t1 = PolarCoordinate(diameter / 2, startLineAngle).toCartesian()
            val t2 =
                PolarCoordinate(
                    diameter / 2,
                    startLineAngle + radiansPerZone
                ).toCartesian()
            val trianglePath = Path().apply {
                moveTo(touchDownPos.x, touchDownPos.y)
                lineTo(touchDownPos.x + t1.first, touchDownPos.y + t1.second)
                lineTo(touchDownPos.x + t2.first, touchDownPos.y + t2.second)
            }
            drawPath(
                trianglePath,
                primaryColor,
            )
            drawArc(
                primaryColor,
                startAngle = (startLineAngle / (Math.PI) * 180).toFloat(),
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



        val textPos =
            polarCoord.newWithRadius(polarCoord.r * 0.7f)
                .newWithAngleOffset(radiansPerZone / 2)
                .toCartesian2().move(touchDownPos.x, touchDownPos.y)
        drawText(
            zone,
            textPos.x,
            textPos.y,
            if (activatedZone == zone) textPaintActive else textPaintInactive
        )
        // Lines for sectors
        // Starts after center circle and stops at outer circle
        drawLine(
            primaryColor,
            with(touchDownPos) {
                val offsetFromCenter = CartesianCoordinate(x, y).toPolar()
                    .move(PolarCoordinate(centerCircleDimeter / 2, startLineAngle)).toCartesian()
                Offset(offsetFromCenter.first, offsetFromCenter.second)
            },
            with(touchDownPos) { Offset(x + coord.first, y + coord.second) }
        )

    }

    // Draw center
    drawArc(
        color = if (activatedZone == centerZone) primaryColor else secondaryColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = with(touchDownPos) {
            Offset(x - centerCircleDimeter / 2, y - centerCircleDimeter / 2)
        },
        size = Size(
            centerCircleDimeter,
            centerCircleDimeter,
        ),
        style = Fill
    )

    drawArc(
        color = primaryColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = with(touchDownPos) {
            Offset(x - centerCircleDimeter / 2, y - centerCircleDimeter / 2)
        },
        size = Size(
            centerCircleDimeter,
            centerCircleDimeter,
        ),
        style = Stroke(3f)
    )

    // Magic number to move the text down so it renders in the center of the circle.
    val adjustYToCenter = 25
    drawText(
        centerZone,
        touchDownPos.x,
        touchDownPos.y + adjustYToCenter,
        paint = if (activatedZone == centerZone) textPaintActive else textPaintInactive
    )
}