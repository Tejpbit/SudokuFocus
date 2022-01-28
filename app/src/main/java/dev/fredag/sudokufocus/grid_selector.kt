package dev.fredag.sudokufocus

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke



fun DrawScope.drawGridSelector(size: Float, touchDownPos: Offset?, currentPos: Offset?) {
    if (touchDownPos == null) {
        return
    }

    val cellDimension = size / 3
    val topLeft = with(touchDownPos) { Offset(x - size / 2, y - size / 2) }
//    drawRect(
//        color = Color.Black,
//        topLeft,
//        size = Size(size, size),
//        style = Fill,
//    )

    val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9").iterator()

    for (j in 0..2) {
        for (i in 0..2) {
            val cellTopLeft = with(topLeft) { Offset(x + cellDimension * i, y + cellDimension * j) }
            val cellSize = Size(cellDimension, cellDimension)
            drawRoundRect(
                color = Color(0,0,0,0xbb),
                cornerRadius = CornerRadius(cellDimension * 0.3f, cellDimension * 0.3f),
                size = cellSize,
                topLeft = cellTopLeft,
            )

            drawRoundRect(
                color = Color(0xff,0,0,0xbb),
                cornerRadius = CornerRadius(cellDimension * 0.3f, cellDimension * 0.3f),
                size = cellSize,
                topLeft = cellTopLeft,
                style = if (currentPos != null && isInside(currentPos, cellTopLeft,cellSize )) Stroke(2f) else Fill
            )
            val cellTextPaint = android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 64f
                color = if (currentPos != null && isInside(currentPos, cellTopLeft,cellSize )) 0xffff0000.toInt() else 0xff000000.toInt()
            }
            drawText(
                numbers.next(),//((i + 1) * (j + 1)).toString(),
                cellTopLeft.x + cellDimension / 2,
                cellTopLeft.y + cellDimension / 2 + cellDimension / 5,
                cellTextPaint
            )

        }
    }
}

fun isInside(point: Offset, topLeft: Offset, size: Size): Boolean {
    return point.x > topLeft.x && point.y > topLeft.y && point.x < (topLeft.x + size.width) && point.y < (topLeft.y + size.height)
}