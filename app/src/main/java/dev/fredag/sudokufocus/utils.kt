package dev.fredag.sudokufocus

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.sqrt

fun hypotenuse(side1: Float, side2: Float): Float {
    return sqrt(side1 * side1 + side2 * side2.toDouble()).toFloat()
}

fun colorToInt(color: Color) = android.graphics.Color.argb(
    color.toArgb().alpha,
    color.toArgb().red,
    color.toArgb().green,
    color.toArgb().blue
)